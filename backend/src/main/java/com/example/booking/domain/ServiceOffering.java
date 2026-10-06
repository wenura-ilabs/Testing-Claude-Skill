package com.example.booking.domain;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "services")
public class ServiceOffering {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String name;

	@Column(name = "duration_minutes", nullable = false)
	private int durationMinutes;

	@Column(nullable = false)
	private BigDecimal price;

	protected ServiceOffering() {
	}

	public ServiceOffering(String name, int durationMinutes, BigDecimal price) {
		this.name = name;
		this.durationMinutes = durationMinutes;
		this.price = price;
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
