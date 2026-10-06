package com.example.booking.web;

import java.util.LinkedHashMap;
import java.util.Map;

import com.example.booking.service.ConflictException;
import com.example.booking.service.InvalidBookingException;
import com.example.booking.service.NotFoundException;
import com.example.booking.web.dto.ErrorResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ApiExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
		Map<String, String> fieldErrors = new LinkedHashMap<>();
		ex.getBindingResult().getFieldErrors()
			.forEach(error -> fieldErrors.putIfAbsent(error.getField(), error.getDefaultMessage()));
		return error(HttpStatus.BAD_REQUEST, "Validation failed", fieldErrors);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErrorResponse> handleUnreadable(HttpMessageNotReadableException ex) {
		return error(HttpStatus.BAD_REQUEST,
				"Malformed request body. Use date format yyyy-MM-dd and time format HH:mm.", null);
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
		return error(HttpStatus.BAD_REQUEST, "Invalid value for '" + ex.getName() + "'", null);
	}

	@ExceptionHandler(InvalidBookingException.class)
	public ResponseEntity<ErrorResponse> handleInvalid(InvalidBookingException ex) {
		return error(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
	}

	@ExceptionHandler(NotFoundException.class)
	public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex) {
		return error(HttpStatus.NOT_FOUND, ex.getMessage(), null);
	}

	@ExceptionHandler(ConflictException.class)
	public ResponseEntity<ErrorResponse> handleConflict(ConflictException ex) {
		return error(HttpStatus.CONFLICT, ex.getMessage(), null);
	}

	private static ResponseEntity<ErrorResponse> error(HttpStatus status, String message,
			Map<String, String> fieldErrors) {
		return ResponseEntity.status(status).body(new ErrorResponse(status.value(), message, fieldErrors));
	}

}
