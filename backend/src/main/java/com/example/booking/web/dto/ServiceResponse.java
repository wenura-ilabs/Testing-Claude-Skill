package com.example.booking.web.dto;

import java.math.BigDecimal;

import com.example.booking.domain.ServiceOffering;

public class ServiceResponse {

	private final Long id;
	private final String name;
	private final int durationMinutes;
	private final BigDecimal price;

	public ServiceResponse(ServiceOffering service) {
		this.id = service.getId();
		this.name = service.getName();
		this.durationMinutes = service.getDurationMinutes();
		this.price = service.getPrice();
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public int getDurationMinutes() {
		return durationMinutes;
	}

	public BigDecimal getPrice() {
		return price;
	}

}
