package com.example.booking.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.example.booking.domain.BookingStatus;
import com.example.booking.repository.BookingRepository;
import com.example.booking.service.BookingService;
import com.example.booking.service.InvalidBookingException;
import com.fasterxml.jackson.databind.JsonNode;
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

// Uses the services and promo codes seeded by the Flyway migrations. Haircut costs 25.00.
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PromoCodeApiIntegrationTest {

	private static final LocalDate TOMORROW = LocalDate.now().plusDays(1);

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private BookingRepository bookingRepository;

	@Autowired
	private BookingService bookingService;

	private long haircutId;

	@BeforeEach
	void setUp() throws Exception {
		bookingRepository.deleteAll();
		String body = mockMvc.perform(get("/api/services")).andReturn().getResponse().getContentAsString();
		for (JsonNode service : objectMapper.readTree(body)) {
			if ("Haircut".equals(service.get("name").asText())) {
				haircutId = service.get("id").asLong();
			}
		}
	}

	@Test
	void bookingWithoutCodeHasNoDiscount() throws Exception {
		Map<String, Object> request = bookingRequest("10:00", null);
		request.remove("promoCode");

		postBooking(request)
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.originalPrice").value(25.00))
			.andExpect(jsonPath("$.discountAmount").value(0.00))
			.andExpect(jsonPath("$.totalPrice").value(25.00))
			.andExpect(jsonPath("$.promoCode").value(nullValue()));
	}

	@Test
	void blankCodeMeansNoCode() throws Exception {
		postBooking(bookingRequest("10:00", ""))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.discountAmount").value(0.00))
			.andExpect(jsonPath("$.totalPrice").value(25.00))
			.andExpect(jsonPath("$.promoCode").value(nullValue()));
		postBooking(bookingRequest("11:00", "   "))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.promoCode").value(nullValue()));
		postBooking(bookingRequest("12:00", null))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.promoCode").value(nullValue()));
	}

	@Test
	void appliesPercentageCode() throws Exception {
		String body = postBooking(bookingRequest("10:00", "WELCOME10"))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.originalPrice").value(25.00))
			.andExpect(jsonPath("$.discountAmount").value(2.50))
			.andExpect(jsonPath("$.totalPrice").value(22.50))
			.andExpect(jsonPath("$.promoCode").value("WELCOME10"))
			.andReturn().getResponse().getContentAsString();

		// Amounts are serialised with exactly two decimal places.
		assertThat(body).contains("\"originalPrice\":25.00", "\"discountAmount\":2.50", "\"totalPrice\":22.50");
	}

	@Test
	void appliesFixedAmountCode() throws Exception {
		postBooking(bookingRequest("10:00", "FLAT5"))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.discountAmount").value(5.00))
			.andExpect(jsonPath("$.totalPrice").value(20.00));
	}

	@Test
	void fixedDiscountLargerThanPriceGivesZeroTotal() throws Exception {
		postBooking(bookingRequest("10:00", "HUGE"))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.originalPrice").value(25.00))
			.andExpect(jsonPath("$.discountAmount").value(25.00))
			.andExpect(jsonPath("$.totalPrice").value(0.00));
	}

	@Test
	void codeIsCaseAndSpaceInsensitive() throws Exception {
		postBooking(bookingRequest("10:00", "  welcome10 "))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.promoCode").value("WELCOME10"))
			.andExpect(jsonPath("$.totalPrice").value(22.50));
	}

	@Test
	void rejectsUnknownCode() throws Exception {
		expectRejected("nosuchcode", "Promo code NOSUCHCODE is not valid");
	}

	@Test
	void rejectsInactiveCode() throws Exception {
		expectRejected("PAUSED", "Promo code PAUSED is not valid");
	}

	@Test
	void rejectsExpiredCode() throws Exception {
		expectRejected("OLDCODE", "Promo code OLDCODE has expired");
	}

	@Test
	void rejectsCodeThatHasNotStarted() throws Exception {
		expectRejected("FUTURE", "Promo code FUTURE is not active yet");
	}

	@Test
	void rejectsCodeBelowItsMinimumPrice() throws Exception {
		expectRejected("BIG15", "Promo code BIG15 requires a minimum price of 100000.00");
	}

	@Test
	void rejectsCodeLongerThan32Characters() throws Exception {
		expectRejected("A".repeat(33), "Promo code must be at most 32 characters");
	}

	@Test
	void usageLimitCountsConfirmedBookingsAndCancellingFreesAUse() throws Exception {
		String body = postBooking(bookingRequest("10:00", "ONCE"))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.totalPrice").value(12.50))
			.andReturn().getResponse().getContentAsString();
		long firstId = objectMapper.readTree(body).get("id").asLong();

		postBooking(bookingRequest("11:00", "ONCE"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value("Promo code ONCE has reached its usage limit"));

		mockMvc.perform(post("/api/bookings/{id}/cancel", firstId)).andExpect(status().isOk());

		postBooking(bookingRequest("11:00", "ONCE"))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.discountAmount").value(12.50))
			.andExpect(jsonPath("$.promoCode").value("ONCE"));
	}

	@Test
	void slotTakenErrorComesFirstAndDoesNotConsumeTheCode() throws Exception {
		postBooking(bookingRequest("10:00", null)).andExpect(status().isCreated());

		postBooking(bookingRequest("10:00", "ONCE"))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.message").value("The 10:00 slot on " + TOMORROW + " is already booked"));
		// An invalid code on a taken slot still reports the slot.
		postBooking(bookingRequest("10:00", "NOSUCHCODE")).andExpect(status().isConflict());

		postBooking(bookingRequest("11:00", "ONCE")).andExpect(status().isCreated());
	}

	@Test
	void viewingABookingShowsPricesAndCode() throws Exception {
		String body = postBooking(bookingRequest("10:00", "welcome10"))
			.andReturn().getResponse().getContentAsString();
		long id = objectMapper.readTree(body).get("id").asLong();

		mockMvc.perform(get("/api/bookings/{id}", id))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.originalPrice").value(25.00))
			.andExpect(jsonPath("$.discountAmount").value(2.50))
			.andExpect(jsonPath("$.totalPrice").value(22.50))
			.andExpect(jsonPath("$.promoCode").value("WELCOME10"));
	}

	@Test
	void usageLimitHoldsUnderConcurrentBookings() throws Exception {
		int requests = 10;
		ExecutorService executor = Executors.newFixedThreadPool(requests);
		CountDownLatch start = new CountDownLatch(1);
		List<Future<Boolean>> results = new ArrayList<>();
		try {
			for (int i = 0; i < requests; i++) {
				// Ten different free slots: 09:00-13:00 on each of two days.
				LocalDate date = TOMORROW.plusDays(i / 5);
				LocalTime time = LocalTime.of(9 + (i % 5), 0);
				results.add(executor.submit(() -> {
					start.await();
					try {
						bookingService.create(haircutId, date, time, "Ada Lovelace", "ada@example.com", "ONCE");
						return true;
					}
					catch (InvalidBookingException ex) {
						assertThat(ex).hasMessage("Promo code ONCE has reached its usage limit");
						return false;
					}
				}));
			}
			start.countDown();

			int succeeded = 0;
			for (Future<Boolean> result : results) {
				if (result.get(30, TimeUnit.SECONDS)) {
					succeeded++;
				}
			}
			assertThat(succeeded).isEqualTo(1);
			assertThat(bookingRepository.countByPromoCodeAndStatus("ONCE", BookingStatus.CONFIRMED)).isEqualTo(1);
			assertThat(bookingRepository.count()).isEqualTo(1);
		}
		finally {
			executor.shutdownNow();
		}
	}

	private void expectRejected(String promoCode, String message) throws Exception {
		postBooking(bookingRequest("10:00", promoCode))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.status").value(400))
			.andExpect(jsonPath("$.message").value(message));
		assertThat(bookingRepository.count()).isZero();
	}

	private ResultActions postBooking(Map<String, Object> request) throws Exception {
		return mockMvc.perform(post("/api/bookings").contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(request)));
	}

	private Map<String, Object> bookingRequest(String time, String promoCode) {
		Map<String, Object> request = new LinkedHashMap<>();
		request.put("serviceId", haircutId);
		request.put("date", TOMORROW.toString());
		request.put("time", time);
		request.put("customerName", "Ada Lovelace");
		request.put("customerEmail", "ada@example.com");
		request.put("promoCode", promoCode);
		return request;
	}

}
