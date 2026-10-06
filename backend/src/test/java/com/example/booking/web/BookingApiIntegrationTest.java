package com.example.booking.web;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import com.example.booking.repository.BookingRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BookingApiIntegrationTest {

	private static final String TOMORROW = LocalDate.now().plusDays(1).toString();

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private BookingRepository bookingRepository;

	@BeforeEach
	void clearBookings() {
		bookingRepository.deleteAll();
	}

	@Test
	void listsSeededServices() throws Exception {
		mockMvc.perform(get("/api/services"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", hasSize(3)))
			.andExpect(jsonPath("$[0].name").value("Consultation"))
			.andExpect(jsonPath("$[0].durationMinutes").value(45))
			.andExpect(jsonPath("$[0].price").value(40.00));
	}

	@Test
	void createsBookingWithTotalPriceAndFetchesItById() throws Exception {
		long id = createBooking(TOMORROW, "10:00");

		mockMvc.perform(get("/api/bookings/{id}", id))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(id))
			.andExpect(jsonPath("$.serviceName").value("Haircut"))
			.andExpect(jsonPath("$.date").value(TOMORROW))
			.andExpect(jsonPath("$.time").value("10:00"))
			.andExpect(jsonPath("$.customerName").value("Ada Lovelace"))
			.andExpect(jsonPath("$.customerEmail").value("ada@example.com"))
			.andExpect(jsonPath("$.totalPrice").value(25.00))
			.andExpect(jsonPath("$.status").value("CONFIRMED"));
	}

	@Test
	void rejectsDoubleBookingOfSameSlot() throws Exception {
		createBooking(TOMORROW, "10:00");

		postBooking(bookingRequest(TOMORROW, "10:00"))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.message").value("The 10:00 slot on " + TOMORROW + " is already booked"));
	}

	@Test
	void rejectsBookingInThePast() throws Exception {
		postBooking(bookingRequest(LocalDate.now().minusDays(1).toString(), "10:00"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value("Cannot book a time in the past"));
	}

	@Test
	void rejectsInvalidInputWithFieldErrors() throws Exception {
		Map<String, Object> request = bookingRequest(TOMORROW, "10:00");
		request.put("customerName", " ");
		request.put("customerEmail", "not-an-email");
		request.remove("serviceId");

		postBooking(request)
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value("Validation failed"))
			.andExpect(jsonPath("$.fieldErrors.serviceId").value("Service is required"))
			.andExpect(jsonPath("$.fieldErrors.customerName").value("Customer name is required"))
			.andExpect(jsonPath("$.fieldErrors.customerEmail").value("Customer email must be a valid email address"));
	}

	@Test
	void rejectsMalformedDate() throws Exception {
		postBooking(bookingRequest("tomorrow", "10:00"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").exists());
	}

	@Test
	void rejectsUnknownService() throws Exception {
		Map<String, Object> request = bookingRequest(TOMORROW, "10:00");
		request.put("serviceId", 9999);

		postBooking(request)
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.message").value("Service 9999 not found"));
	}

	@Test
	void returnsNotFoundForUnknownBooking() throws Exception {
		mockMvc.perform(get("/api/bookings/{id}", 9999))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.message").value("Booking 9999 not found"));
	}

	@Test
	void cancelsBookingAndFreesItsSlot() throws Exception {
		long id = createBooking(TOMORROW, "10:00");

		mockMvc.perform(post("/api/bookings/{id}/cancel", id))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("CANCELLED"));
		mockMvc.perform(post("/api/bookings/{id}/cancel", id))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.message").value("Booking " + id + " is already cancelled"));

		// The slot can be booked and cancelled again.
		long secondId = createBooking(TOMORROW, "10:00");
		mockMvc.perform(post("/api/bookings/{id}/cancel", secondId)).andExpect(status().isOk());
	}

	private long createBooking(String date, String time) throws Exception {
		String body = postBooking(bookingRequest(date, time))
			.andExpect(status().isCreated())
			.andReturn().getResponse().getContentAsString();
		return objectMapper.readTree(body).get("id").asLong();
	}

	private ResultActions postBooking(Map<String, Object> request) throws Exception {
		return mockMvc.perform(post("/api/bookings").contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(request)));
	}

	private Map<String, Object> bookingRequest(String date, String time) throws Exception {
		String body = mockMvc.perform(get("/api/services")).andReturn().getResponse().getContentAsString();
		long haircutId = 0;
		for (com.fasterxml.jackson.databind.JsonNode service : objectMapper.readTree(body)) {
			if ("Haircut".equals(service.get("name").asText())) {
				haircutId = service.get("id").asLong();
			}
		}
		Map<String, Object> request = new LinkedHashMap<>();
		request.put("serviceId", haircutId);
		request.put("date", date);
		request.put("time", time);
		request.put("customerName", "Ada Lovelace");
		request.put("customerEmail", "ada@example.com");
		return request;
	}

}
