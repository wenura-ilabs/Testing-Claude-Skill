package com.example.booking.web;

import javax.validation.Valid;

import com.example.booking.domain.Booking;
import com.example.booking.service.BookingService;
import com.example.booking.web.dto.BookingResponse;
import com.example.booking.web.dto.CreateBookingRequest;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

	private final BookingService bookingService;

	public BookingController(BookingService bookingService) {
		this.bookingService = bookingService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public BookingResponse create(@Valid @RequestBody CreateBookingRequest request) {
		Booking booking = bookingService.create(request.getServiceId(), request.getDate(), request.getTime(),
				request.getCustomerName().trim(), request.getCustomerEmail().trim());
		return new BookingResponse(booking);
	}

	@GetMapping("/{id}")
	public BookingResponse get(@PathVariable Long id) {
		return new BookingResponse(bookingService.get(id));
	}

	@PostMapping("/{id}/cancel")
	public BookingResponse cancel(@PathVariable Long id) {
		return new BookingResponse(bookingService.cancel(id));
	}

}
