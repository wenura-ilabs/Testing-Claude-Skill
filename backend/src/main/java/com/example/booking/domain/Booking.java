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

	@Column(name = "total_price", nullable = false)
	private BigDecimal totalPrice;

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
		this.service = service;
		this.date = date;
		this.time = time;
		this.customerName = customerName;
		this.customerEmail = customerEmail;
		this.totalPrice = service.getPrice();
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

	public BigDecimal getTotalPrice() {
		return totalPrice;
	}

	public BookingStatus getStatus() {
		return status;
	}

}
