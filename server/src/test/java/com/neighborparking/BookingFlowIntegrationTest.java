package com.neighborparking;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neighborparking.domain.AvailabilitySlot;
import com.neighborparking.repository.AvailabilitySlotRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class BookingFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AvailabilitySlotRepository slotRepository;

    @Test
    void shouldCompleteLoginSearchAndPreventDuplicateBooking() throws Exception {
        String token = login(1L);
        mockMvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("小林"));

        AvailabilitySlot slot = slotRepository.findAll().get(0);
        Instant startAt = slot.getStartAt().plusSeconds(7200);
        Instant endAt = startAt.plusSeconds(3600);
        String request = "{\"slotId\":" + slot.getId() + ",\"vehicleId\":1,\"startAt\":\"" + startAt
                + "\",\"endAt\":\"" + endAt + "\"}";

        String createdBody = mockMvc.perform(post("/api/v1/bookings").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.spaceCode").value("B2-128"))
                .andReturn().getResponse().getContentAsString();
        Long bookingId = objectMapper.readTree(createdBody).get("id").asLong();

        mockMvc.perform(post("/api/v1/bookings/{id}/check-in", bookingId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INVALID_STATE_TRANSITION"));

        String ownerToken = login(2L);
        mockMvc.perform(delete("/api/v1/owner/spaces/{spaceId}/slots/{slotId}", slot.getSpaceId(), slot.getId())
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("SLOT_NOT_AVAILABLE"));

        mockMvc.perform(post("/api/v1/bookings").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("BOOKING_TIME_CONFLICT"));
    }

    @Test
    void shouldRejectAdminEndpointForNormalUser() throws Exception {
        String token = login(1L);
        mockMvc.perform(get("/api/v1/admin/audits").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/owner/spaces").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowOnlyOneConcurrentBooking() throws Exception {
        String token = login(1L);
        AvailabilitySlot slot = slotRepository.findAll().get(0);
        Instant startAt = slot.getStartAt().plusSeconds(6 * 3600L);
        Instant endAt = startAt.plusSeconds(3600);
        String request = "{\"slotId\":" + slot.getId() + ",\"vehicleId\":1,\"startAt\":\"" + startAt
                + "\",\"endAt\":\"" + endAt + "\"}";
        CountDownLatch startSignal = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        Callable<Integer> task = () -> {
            startSignal.await();
            return mockMvc.perform(post("/api/v1/bookings").header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON).content(request))
                    .andReturn().getResponse().getStatus();
        };
        try {
            Future<Integer> first = executor.submit(task);
            Future<Integer> second = executor.submit(task);
            startSignal.countDown();
            List<Integer> statuses = Arrays.asList(first.get(10, TimeUnit.SECONDS),
                    second.get(10, TimeUnit.SECONDS));
            statuses.sort(Integer::compareTo);
            assertEquals(Arrays.asList(201, 409), statuses);
        } finally {
            executor.shutdownNow();
        }
    }

    private String login(Long userId) throws Exception {
        String body = mockMvc.perform(post("/api/v1/auth/dev-login")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"userId\":" + userId + "}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode json = objectMapper.readTree(body);
        return json.get("accessToken").asText();
    }
}
