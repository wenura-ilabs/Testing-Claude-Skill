package com.example.booking.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "bookings")
public class Booking {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "service_id", nullable = false)
	private ServiceOffering service;

	@Column(name = "booking_date", nullable = false)
	private LocalDate date;

	@Column(name = "start_time", nullable = false)
	private LocalTime time;

	@Column(name = "customer_name", nullable = false)
	private String customerName;

	@Column(name = "customer_email", nullable = false)
	private String customerEmail;

	@Column(name = "original_price", nullable = false)
	private BigDecimal originalPrice;

	@Column(name = "discount_amount", nullable = false)
	private BigDecimal discountAmount;

	@Column(name = "total_price", nullable = false)
	private BigDecimal totalPrice;

	// Prices and code are fixed at booking time; later changes to the code do not affect them.
	@Column(name = "promo_code")
	private String promoCode;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private BookingStatus status = BookingStatus.CONFIRMED;

	// TRUE while confirmed, NULL once cancelled; backs the unique slot constraint (see V1 migration).
	@Column(name = "active_slot")
	private Boolean activeSlot = Boolean.TRUE;

	protected Booking() {
	}

	public Booking(ServiceOffering service, LocalDate date, LocalTime time, String customerName,
			String customerEmail) {
		this(service, date, time, customerName, customerEmail, null);
	}

	public Booking(ServiceOffering service, LocalDate date, LocalTime time, String customerName,
			String customerEmail, PromoCode promoCode) {
		this.service = service;
		this.date = date;
		this.time = time;
		this.customerName = customerName;
		this.customerEmail = customerEmail;
		this.originalPrice = service.getPrice();
		this.discountAmount = (promoCode != null) ? promoCode.discountFor(originalPrice)
				: BigDecimal.ZERO.setScale(2);
		this.totalPrice = originalPrice.subtract(discountAmount);
		this.promoCode = (promoCode != null) ? promoCode.getCode() : null;
	}

	public void cancel() {
		this.status = BookingStatus.CANCELLED;
		this.activeSlot = null;
	}

	public Long getId() {
		return id;
	}

	public ServiceOffering getService() {
		return service;
	}

	public LocalDate getDate() {
		return date;
	}

	public LocalTime getTime() {
		return time;
	}

	public String getCustomerName() {
		return customerName;
	}

	public String getCustomerEmail() {
		return customerEmail;
	}

	public BigDecimal getOriginalPrice() {
		return originalPrice;
	}

	public BigDecimal getDiscountAmount() {
		return discountAmount;
	}

	public String getPromoCode() {
		return promoCode;
	}

	public BigDecimal getTotalPrice() {
		return totalPrice;
	}

	public BookingStatus getStatus() {
		return status;
	}

}
