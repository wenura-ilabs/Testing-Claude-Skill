package com.example.booking.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Optional;

import com.example.booking.domain.Booking;
import com.example.booking.domain.BookingStatus;
import com.example.booking.domain.ServiceOffering;
import com.example.booking.repository.BookingRepository;
import com.example.booking.repository.ServiceOfferingRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

	private static final ZoneId ZONE = ZoneId.of("UTC");
	private static final LocalDate TODAY = LocalDate.of(2030, 1, 15);
	// "Now" is 2030-01-15 10:30.
	private static final Clock CLOCK = Clock.fixed(LocalDateTime.of(TODAY, LocalTime.of(10, 30)).atZone(ZONE).toInstant(),
			ZONE);

	@Mock
	private BookingRepository bookingRepository;

	@Mock
	private ServiceOfferingRepository serviceRepository;

	private BookingService bookingService;

	private final ServiceOffering haircut = new ServiceOffering("Haircut", 30, new BigDecimal("25.00"));

	@BeforeEach
	void setUp() {
		bookingService = new BookingService(bookingRepository, serviceRepository, CLOCK);
	}

	@Test
	void createSavesBookingWithServicePriceAsTotal() {
		when(serviceRepository.findById(1L)).thenReturn(Optional.of(haircut));
		when(bookingRepository.saveAndFlush(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

		Booking booking = bookingService.create(1L, TODAY, LocalTime.of(11, 0), "Ada", "ada@example.com");

		assertThat(booking.getTotalPrice()).isEqualByComparingTo("25.00");
		assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
		assertThat(booking.getCustomerName()).isEqualTo("Ada");
	}

	@Test
	void createRejectsUnknownService() {
		when(serviceRepository.findById(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> bookingService.create(99L, TODAY, LocalTime.of(11, 0), "Ada", "ada@example.com"))
			.isInstanceOf(NotFoundException.class)
			.hasMessage("Service 99 not found");
	}

	@Test
	void createRejectsPastDate() {
		when(serviceRepository.findById(1L)).thenReturn(Optional.of(haircut));

		assertThatThrownBy(
				() -> bookingService.create(1L, TODAY.minusDays(1), LocalTime.of(11, 0), "Ada", "ada@example.com"))
			.isInstanceOf(InvalidBookingException.class)
			.hasMessage("Cannot book a time in the past");
		verify(bookingRepository, never()).saveAndFlush(any());
	}

	@Test
	void createRejectsEarlierSlotToday() {
		when(serviceRepository.findById(1L)).thenReturn(Optional.of(haircut));

		assertThatThrownBy(() -> bookingService.create(1L, TODAY, LocalTime.of(10, 0), "Ada", "ada@example.com"))
			.isInstanceOf(InvalidBookingException.class)
			.hasMessage("Cannot book a time in the past");
	}

	@Test
	void createRejectsTimeThatIsNotASlot() {
		when(serviceRepository.findById(1L)).thenReturn(Optional.of(haircut));

		assertThatThrownBy(() -> bookingService.create(1L, TODAY, LocalTime.of(11, 15), "Ada", "ada@example.com"))
			.isInstanceOf(InvalidBookingException.class)
			.hasMessageContaining("Time slot must be on the hour");
		assertThatThrownBy(() -> bookingService.create(1L, TODAY, LocalTime.of(20, 0), "Ada", "ada@example.com"))
			.isInstanceOf(InvalidBookingException.class);
	}

	@Test
	void createRejectsSlotThatIsAlreadyBooked() {
		LocalTime time = LocalTime.of(11, 0);
		when(serviceRepository.findById(1L)).thenReturn(Optional.of(haircut));
		when(bookingRepository.existsByDateAndTimeAndStatus(TODAY, time, BookingStatus.CONFIRMED)).thenReturn(true);

		assertThatThrownBy(() -> bookingService.create(1L, TODAY, time, "Ada", "ada@example.com"))
			.isInstanceOf(ConflictException.class)
			.hasMessage("The 11:00 slot on 2030-01-15 is already booked");
		verify(bookingRepository, never()).saveAndFlush(any());
	}

	@Test
	void createReportsConflictWhenConcurrentRequestTakesSlot() {
		when(serviceRepository.findById(1L)).thenReturn(Optional.of(haircut));
		when(bookingRepository.saveAndFlush(any(Booking.class)))
			.thenThrow(new DataIntegrityViolationException("uq_bookings_active_slot"));

		assertThatThrownBy(() -> bookingService.create(1L, TODAY, LocalTime.of(11, 0), "Ada", "ada@example.com"))
			.isInstanceOf(ConflictException.class);
	}

	@Test
	void getRejectsUnknownBooking() {
		when(bookingRepository.findById(7L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> bookingService.get(7L)).isInstanceOf(NotFoundException.class)
			.hasMessage("Booking 7 not found");
	}

	@Test
	void cancelMarksBookingCancelled() {
		Booking booking = new Booking(haircut, TODAY, LocalTime.of(11, 0), "Ada", "ada@example.com");
		when(bookingRepository.findById(7L)).thenReturn(Optional.of(booking));
		when(bookingRepository.save(booking)).thenReturn(booking);

		assertThat(bookingService.cancel(7L).getStatus()).isEqualTo(BookingStatus.CANCELLED);
	}

	@Test
	void cancelRejectsAlreadyCancelledBooking() {
		Booking booking = new Booking(haircut, TODAY, LocalTime.of(11, 0), "Ada", "ada@example.com");
		booking.cancel();
		when(bookingRepository.findById(7L)).thenReturn(Optional.of(booking));

		assertThatThrownBy(() -> bookingService.cancel(7L)).isInstanceOf(ConflictException.class)
			.hasMessage("Booking 7 is already cancelled");
	}

}
