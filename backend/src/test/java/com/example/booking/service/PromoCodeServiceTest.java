package com.example.booking.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

import com.example.booking.domain.BookingStatus;
import com.example.booking.domain.DiscountType;
import com.example.booking.domain.PromoCode;
import com.example.booking.repository.BookingRepository;
import com.example.booking.repository.PromoCodeRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PromoCodeServiceTest {

	private static final ZoneId ZONE = ZoneId.of("UTC");
	private static final LocalDate TODAY = LocalDate.of(2030, 1, 15);
	private static final Clock CLOCK = Clock.fixed(TODAY.atTime(10, 30).atZone(ZONE).toInstant(), ZONE);
	private static final BigDecimal PRICE = new BigDecimal("45.00");

	@Mock
	private PromoCodeRepository promoCodeRepository;

	@Mock
	private BookingRepository bookingRepository;

	private PromoCodeService promoCodeService;

	@BeforeEach
	void setUp() {
		promoCodeService = new PromoCodeService(promoCodeRepository, bookingRepository, CLOCK);
	}

	private PromoCode given(LocalDate validFrom, LocalDate validUntil, boolean active, Integer usageLimit,
			String minPrice) {
		PromoCode promo = new PromoCode("CODE", DiscountType.PERCENT, BigDecimal.TEN, validFrom, validUntil, active,
				usageLimit, (minPrice != null) ? new BigDecimal(minPrice) : null);
		when(promoCodeRepository.findById("CODE")).thenReturn(Optional.of(promo));
		return promo;
	}

	private PromoCode givenValidBetween(LocalDate validFrom, LocalDate validUntil) {
		return given(validFrom, validUntil, true, null, null);
	}

	@Test
	void normalizeTrimsAndUpperCases() {
		assertThat(PromoCodeService.normalize("  welcome10 ")).isEqualTo("WELCOME10");
	}

	@Test
	void normalizeTreatsNullAndBlankAsNoCode() {
		assertThat(PromoCodeService.normalize(null)).isNull();
		assertThat(PromoCodeService.normalize("")).isNull();
		assertThat(PromoCodeService.normalize("   ")).isNull();
	}

	@Test
	void normalizeChecksLengthAfterTrimming() {
		String thirtyTwo = "A".repeat(32);

		assertThat(PromoCodeService.normalize("   " + thirtyTwo + "   ")).isEqualTo(thirtyTwo);
		assertThatThrownBy(() -> PromoCodeService.normalize(thirtyTwo + "A"))
			.isInstanceOf(InvalidBookingException.class)
			.hasMessage("Promo code must be at most 32 characters");
	}

	@Test
	void redeemRejectsUnknownCode() {
		when(promoCodeRepository.findById("NOSUCHCODE")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> promoCodeService.redeem("NOSUCHCODE", PRICE))
			.isInstanceOf(InvalidBookingException.class)
			.hasMessage("Promo code NOSUCHCODE is not valid");
	}

	@Test
	void redeemTreatsInactiveCodeAsUnknown() {
		given(TODAY.minusDays(1), TODAY.plusDays(1), false, null, null);

		assertThatThrownBy(() -> promoCodeService.redeem("CODE", PRICE))
			.isInstanceOf(InvalidBookingException.class)
			.hasMessage("Promo code CODE is not valid");
	}

	@Test
	void redeemAcceptsCodeEndingToday() {
		PromoCode promo = givenValidBetween(TODAY.minusDays(10), TODAY);

		assertThat(promoCodeService.redeem("CODE", PRICE)).isSameAs(promo);
	}

	@Test
	void redeemRejectsCodeThatEndedYesterday() {
		givenValidBetween(TODAY.minusDays(10), TODAY.minusDays(1));

		assertThatThrownBy(() -> promoCodeService.redeem("CODE", PRICE))
			.isInstanceOf(InvalidBookingException.class)
			.hasMessage("Promo code CODE has expired");
	}

	@Test
	void redeemAcceptsCodeStartingToday() {
		PromoCode promo = givenValidBetween(TODAY, TODAY.plusDays(10));

		assertThat(promoCodeService.redeem("CODE", PRICE)).isSameAs(promo);
	}

	@Test
	void redeemRejectsCodeStartingTomorrow() {
		givenValidBetween(TODAY.plusDays(1), TODAY.plusDays(10));

		assertThatThrownBy(() -> promoCodeService.redeem("CODE", PRICE))
			.isInstanceOf(InvalidBookingException.class)
			.hasMessage("Promo code CODE is not active yet");
	}

	@Test
	void redeemAcceptsPriceEqualToMinimum() {
		PromoCode promo = given(TODAY, TODAY, true, null, "100.00");

		assertThat(promoCodeService.redeem("CODE", new BigDecimal("100.00"))).isSameAs(promo);
	}

	@Test
	void redeemRejectsPriceOneCentBelowMinimum() {
		given(TODAY, TODAY, true, null, "100.00");

		assertThatThrownBy(() -> promoCodeService.redeem("CODE", new BigDecimal("99.99")))
			.isInstanceOf(InvalidBookingException.class)
			.hasMessage("Promo code CODE requires a minimum price of 100.00");
	}

	@Test
	void redeemLocksLimitedCodeAndAcceptsItBelowTheLimit() {
		PromoCode promo = given(TODAY, TODAY, true, 2, null);
		when(bookingRepository.countByPromoCodeAndStatus("CODE", BookingStatus.CONFIRMED)).thenReturn(1L);

		assertThat(promoCodeService.redeem("CODE", PRICE)).isSameAs(promo);
		verify(promoCodeRepository).findByCodeForUpdate("CODE");
	}

	@Test
	void redeemRejectsCodeAtItsUsageLimit() {
		given(TODAY, TODAY, true, 2, null);
		when(bookingRepository.countByPromoCodeAndStatus("CODE", BookingStatus.CONFIRMED)).thenReturn(2L);

		assertThatThrownBy(() -> promoCodeService.redeem("CODE", PRICE))
			.isInstanceOf(InvalidBookingException.class)
			.hasMessage("Promo code CODE has reached its usage limit");
	}

}
