package com.example.booking.web.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonFormat;

public class CreateBookingRequest {

	@NotNull(message = "Service is required")
	private Long serviceId;

	@NotNull(message = "Date is required")
	private LocalDate date;

	@NotNull(message = "Time slot is required")
	@JsonFormat(pattern = "HH:mm")
	private LocalTime time;

	@NotBlank(message = "Customer name is required")
	@Size(max = 100, message = "Customer name must be at most 100 characters")
	private String customerName;

	@NotBlank(message = "Customer email is required")
	@Email(message = "Customer email must be a valid email address")
	@Size(max = 255, message = "Customer email must be at most 255 characters")
	private String customerEmail;

	// Optional. Normalised and length-checked by PromoCodeService, after trimming.
	private String promoCode;

	public Long getServiceId() {
		return serviceId;
	}

	public void setServiceId(Long serviceId) {
		this.serviceId = serviceId;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public LocalTime getTime() {
		return time;
	}

	public void setTime(LocalTime time) {
		this.time = time;
	}

	public String getCustomerName() {
		return customerName;
	}

	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}

	public String getCustomerEmail() {
		return customerEmail;
	}

	public void setCustomerEmail(String customerEmail) {
		this.customerEmail = customerEmail;
	}

	public String getPromoCode() {
		return promoCode;
	}

	public void setPromoCode(String promoCode) {
		this.promoCode = promoCode;
	}

}
