package com.example.booking.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.example.booking.domain.Booking;
import com.example.booking.domain.BookingStatus;
import com.example.booking.domain.PromoCode;
import com.example.booking.domain.ServiceOffering;
import com.example.booking.repository.BookingRepository;
import com.example.booking.repository.ServiceOfferingRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookingService {

	// Slots are hourly; the first starts at 09:00 and the last at 16:00.
	static final int FIRST_SLOT_HOUR = 9;
	static final int LAST_SLOT_HOUR = 16;

	private final BookingRepository bookingRepository;
	private final ServiceOfferingRepository serviceRepository;
	private final PromoCodeService promoCodeService;
	private final Clock clock;

	// For bookings without promo codes only.
	public BookingService(BookingRepository bookingRepository, ServiceOfferingRepository serviceRepository,
			Clock clock) {
		this(bookingRepository, serviceRepository, null, clock);
	}

	@Autowired
	public BookingService(BookingRepository bookingRepository, ServiceOfferingRepository serviceRepository,
			PromoCodeService promoCodeService, Clock clock) {
		this.bookingRepository = bookingRepository;
		this.serviceRepository = serviceRepository;
		this.promoCodeService = promoCodeService;
		this.clock = clock;
	}

	@Transactional
	public Booking create(Long serviceId, LocalDate date, LocalTime time, String customerName,
			String customerEmail) {
		return create(serviceId, date, time, customerName, customerEmail, null);
	}

	@Transactional
	public Booking create(Long serviceId, LocalDate date, LocalTime time, String customerName,
			String customerEmail, String promoCode) {
		String code = PromoCodeService.normalize(promoCode);

		ServiceOffering service = serviceRepository.findById(serviceId)
			.orElseThrow(() -> new NotFoundException("Service " + serviceId + " not found"));

		if (!isValidSlot(time)) {
			throw new InvalidBookingException(String.format(
					"Time slot must be on the hour between %02d:00 and %02d:00", FIRST_SLOT_HOUR, LAST_SLOT_HOUR));
		}
		if (LocalDateTime.of(date, time).isBefore(LocalDateTime.now(clock))) {
			throw new InvalidBookingException("Cannot book a time in the past");
		}
		if (bookingRepository.existsByDateAndTimeAndStatus(date, time, BookingStatus.CONFIRMED)) {
			throw slotTaken(date, time);
		}

		// Promo checks come last so a booking rejected for any other reason never uses up a code.
		PromoCode promo = (code != null) ? promoCodeService.redeem(code, service.getPrice()) : null;

		try {
			return bookingRepository
				.saveAndFlush(new Booking(service, date, time, customerName, customerEmail, promo));
		}
		catch (DataIntegrityViolationException ex) {
			// A concurrent request took the slot between the check above and the insert.
			throw slotTaken(date, time);
		}
	}

	@Transactional(readOnly = true)
	public Booking get(Long id) {
		return bookingRepository.findById(id)
			.orElseThrow(() -> new NotFoundException("Booking " + id + " not found"));
	}

	@Transactional
	public Booking cancel(Long id) {
		Booking booking = get(id);
		if (booking.getStatus() == BookingStatus.CANCELLED) {
			throw new ConflictException("Booking " + id + " is already cancelled");
		}
		booking.cancel();
		return bookingRepository.save(booking);
	}

	private static boolean isValidSlot(LocalTime time) {
		return time.getMinute() == 0 && time.getSecond() == 0 && time.getNano() == 0
				&& time.getHour() >= FIRST_SLOT_HOUR && time.getHour() <= LAST_SLOT_HOUR;
	}

	private static ConflictException slotTaken(LocalDate date, LocalTime time) {
		return new ConflictException("The " + time + " slot on " + date + " is already booked");
	}

}
