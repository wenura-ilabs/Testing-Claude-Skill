package com.example.booking.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Locale;

import com.example.booking.domain.BookingStatus;
import com.example.booking.domain.PromoCode;
import com.example.booking.repository.BookingRepository;
import com.example.booking.repository.PromoCodeRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PromoCodeService {

	static final int MAX_CODE_LENGTH = 32;

	private final PromoCodeRepository promoCodeRepository;
	private final BookingRepository bookingRepository;
	private final Clock clock;

	public PromoCodeService(PromoCodeRepository promoCodeRepository, BookingRepository bookingRepository,
			Clock clock) {
		this.promoCodeRepository = promoCodeRepository;
		this.bookingRepository = bookingRepository;
		this.clock = clock;
	}

	/**
	 * Trims and upper-cases a code as entered by a customer. Returns null when no code
	 * was entered (null or blank).
	 */
	public static String normalize(String rawCode) {
		if (rawCode == null || rawCode.isBlank()) {
			return null;
		}
		String code = rawCode.strip().toUpperCase(Locale.ROOT);
		if (code.length() > MAX_CODE_LENGTH) {
			throw new InvalidBookingException("Promo code must be at most " + MAX_CODE_LENGTH + " characters");
		}
		return code;
	}

	/**
	 * Checks that a normalised code can be used today for a service at the given price and
	 * returns it. Must run inside the transaction that saves the booking: for a code with
	 * a usage limit the code's row stays locked until that transaction ends, so concurrent
	 * bookings cannot exceed the limit.
	 */
	@Transactional(propagation = Propagation.MANDATORY)
	public PromoCode redeem(String code, BigDecimal price) {
		PromoCode promo = promoCodeRepository.findById(code).filter(PromoCode::isActive)
			.orElseThrow(() -> rejected(code, "is not valid"));

		LocalDate today = LocalDate.now(clock);
		if (today.isAfter(promo.getValidUntil())) {
			throw rejected(code, "has expired");
		}
		if (today.isBefore(promo.getValidFrom())) {
			throw rejected(code, "is not active yet");
		}
		if (promo.getMinPrice() != null && price.compareTo(promo.getMinPrice()) < 0) {
			throw rejected(code, "requires a minimum price of "
					+ promo.getMinPrice().setScale(2, RoundingMode.HALF_UP).toPlainString());
		}
		if (promo.getUsageLimit() != null) {
			promoCodeRepository.findByCodeForUpdate(code);
			if (bookingRepository.countByPromoCodeAndStatus(code, BookingStatus.CONFIRMED) >= promo.getUsageLimit()) {
				throw rejected(code, "has reached its usage limit");
			}
		}
		return promo;
	}

	private static InvalidBookingException rejected(String code, String reason) {
		return new InvalidBookingException("Promo code " + code + " " + reason);
	}

}
