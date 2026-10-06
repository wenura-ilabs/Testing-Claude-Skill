package com.example.booking.repository;

import java.time.LocalDate;
import java.time.LocalTime;

import com.example.booking.domain.Booking;
import com.example.booking.domain.BookingStatus;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {

	boolean existsByDateAndTimeAndStatus(LocalDate date, LocalTime time, BookingStatus status);

}
