package com.example.booking.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "promo_codes")
public class PromoCode {

	private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

	@Id
	private String code;

	@Enumerated(EnumType.STRING)
	@Column(name = "discount_type", nullable = false)
	private DiscountType discountType;

	@Column(name = "discount_value", nullable = false)
	private BigDecimal discountValue;

	@Column(name = "valid_from", nullable = false)
	private LocalDate validFrom;

	@Column(name = "valid_until", nullable = false)
	private LocalDate validUntil;

	@Column(nullable = false)
	private boolean active;

	@Column(name = "usage_limit")
	private Integer usageLimit;

	@Column(name = "min_price")
	private BigDecimal minPrice;

	protected PromoCode() {
	}

	public PromoCode(String code, DiscountType discountType, BigDecimal discountValue, LocalDate validFrom,
			LocalDate validUntil, boolean active, Integer usageLimit, BigDecimal minPrice) {
		this.code = code;
		this.discountType = discountType;
		this.discountValue = discountValue;
		this.validFrom = validFrom;
		this.validUntil = validUntil;
		this.active = active;
		this.usageLimit = usageLimit;
		this.minPrice = minPrice;
	}

	/**
	 * The discount this code gives on the given price: rounded half-up to 2 decimal
	 * places and never more than the price itself.
	 */
	public BigDecimal discountFor(BigDecimal price) {
		BigDecimal discount = (discountType == DiscountType.PERCENT) ? price.multiply(discountValue).divide(HUNDRED)
				: discountValue;
		return discount.min(price).setScale(2, RoundingMode.HALF_UP);
	}

	public String getCode() {
		return code;
	}

	public LocalDate getValidFrom() {
		return validFrom;
	}

	public LocalDate getValidUntil() {
		return validUntil;
	}

	public boolean isActive() {
		return active;
	}

	public Integer getUsageLimit() {
		return usageLimit;
	}

	public BigDecimal getMinPrice() {
		return minPrice;
	}

}
