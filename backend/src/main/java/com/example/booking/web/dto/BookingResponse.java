package com.example.booking.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import com.example.booking.domain.Booking;
import com.example.booking.domain.BookingStatus;
import com.fasterxml.jackson.annotation.JsonFormat;

public class BookingResponse {

	private final Long id;
	private final Long serviceId;
	private final String serviceName;
	private final int durationMinutes;
	private final LocalDate date;
	@JsonFormat(pattern = "HH:mm")
	private final LocalTime time;
	private final String customerName;
	private final String customerEmail;
	private final BigDecimal originalPrice;
	private final BigDecimal discountAmount;
	private final BigDecimal totalPrice;
	private final String promoCode;
	private final BookingStatus status;

	public BookingResponse(Booking booking) {
		this.id = booking.getId();
		this.serviceId = booking.getService().getId();
		this.serviceName = booking.getService().getName();
		this.durationMinutes = booking.getService().getDurationMinutes();
		this.date = booking.getDate();
		this.time = booking.getTime();
		this.customerName = booking.getCustomerName();
		this.customerEmail = booking.getCustomerEmail();
		this.originalPrice = booking.getOriginalPrice();
		this.discountAmount = booking.getDiscountAmount();
		this.totalPrice = booking.getTotalPrice();
		this.promoCode = booking.getPromoCode();
		this.status = booking.getStatus();
	}

	public Long getId() {
		return id;
	}

	public Long getServiceId() {
		return serviceId;
	}

	public String getServiceName() {
		return serviceName;
	}

	public int getDurationMinutes() {
		return durationMinutes;
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
