package com.example.booking.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

class PromoCodeTest {

	private static PromoCode code(DiscountType type, String value) {
		return new PromoCode("CODE", type, new BigDecimal(value), LocalDate.of(2020, 1, 1), LocalDate.of(2099, 12, 31),
				true, null, null);
	}

	private static String discount(DiscountType type, String value, String price) {
		return code(type, value).discountFor(new BigDecimal(price)).toPlainString();
	}

	@Test
	void percentageDiscountIsAShareOfThePrice() {
		assertThat(discount(DiscountType.PERCENT, "10", "45.00")).isEqualTo("4.50");
	}

	@Test
	void percentageDiscountRoundsHalfUpToTwoDecimals() {
		// 15% of 33.33 = 4.9995
		assertThat(discount(DiscountType.PERCENT, "15", "33.33")).isEqualTo("5.00");
		// 10% of 0.05 = 0.005 rounds up, 10% of 0.04 = 0.004 rounds down
		assertThat(discount(DiscountType.PERCENT, "10", "0.05")).isEqualTo("0.01");
		assertThat(discount(DiscountType.PERCENT, "10", "0.04")).isEqualTo("0.00");
	}

	@Test
	void hundredPercentDiscountsTheWholePrice() {
		assertThat(discount(DiscountType.PERCENT, "100", "45.00")).isEqualTo("45.00");
	}

	@Test
	void fixedDiscountIsTheAmount() {
		assertThat(discount(DiscountType.FIXED, "5.00", "45.00")).isEqualTo("5.00");
	}

	@Test
	void fixedDiscountNeverExceedsThePrice() {
		assertThat(discount(DiscountType.FIXED, "20.00", "15.00")).isEqualTo("15.00");
	}

	@Test
	void bookingWithCodeRecordsOriginalPriceDiscountTotalAndCode() {
		ServiceOffering service = new ServiceOffering("Facial", 45, new BigDecimal("45.00"));

		Booking booking = new Booking(service, LocalDate.of(2030, 1, 15), LocalTime.of(10, 0), "Ada",
				"ada@example.com", code(DiscountType.PERCENT, "10"));

		assertThat(booking.getOriginalPrice()).isEqualTo("45.00");
		assertThat(booking.getDiscountAmount()).isEqualTo("4.50");
		assertThat(booking.getTotalPrice()).isEqualTo("40.50");
		assertThat(booking.getPromoCode()).isEqualTo("CODE");
	}

	@Test
	void bookingWithOversizedFixedDiscountHasZeroTotal() {
		ServiceOffering service = new ServiceOffering("Trim", 30, new BigDecimal("15.00"));

		Booking booking = new Booking(service, LocalDate.of(2030, 1, 15), LocalTime.of(10, 0), "Ada",
				"ada@example.com", code(DiscountType.FIXED, "20.00"));

		assertThat(booking.getDiscountAmount()).isEqualTo("15.00");
		assertThat(booking.getTotalPrice()).isEqualTo("0.00");
	}

	@Test
	void bookingWithoutCodeHasNoDiscount() {
		ServiceOffering service = new ServiceOffering("Facial", 45, new BigDecimal("45.00"));

		Booking booking = new Booking(service, LocalDate.of(2030, 1, 15), LocalTime.of(10, 0), "Ada",
				"ada@example.com");

		assertThat(booking.getOriginalPrice()).isEqualTo("45.00");
		assertThat(booking.getDiscountAmount()).isEqualTo("0.00");
		assertThat(booking.getTotalPrice()).isEqualTo("45.00");
		assertThat(booking.getPromoCode()).isNull();
	}

}
