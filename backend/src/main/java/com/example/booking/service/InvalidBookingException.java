package com.example.booking.service;

public class InvalidBookingException extends RuntimeException {

	public InvalidBookingException(String message) {
		super(message);
	}

}
