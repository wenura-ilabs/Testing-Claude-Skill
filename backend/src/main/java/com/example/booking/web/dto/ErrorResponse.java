package com.example.booking.web.dto;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

public class ErrorResponse {

	private final int status;
	private final String message;
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private final Map<String, String> fieldErrors;

	public ErrorResponse(int status, String message, Map<String, String> fieldErrors) {
		this.status = status;
		this.message = message;
		this.fieldErrors = fieldErrors;
	}

	public int getStatus() {
		return status;
	}

	public String getMessage() {
		return message;
	}

	public Map<String, String> getFieldErrors() {
		return fieldErrors;
	}

}
