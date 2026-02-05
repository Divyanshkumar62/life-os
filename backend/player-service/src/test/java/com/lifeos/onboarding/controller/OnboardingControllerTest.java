package com.lifeos.onboarding.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifeos.onboarding.dto.OnboardingResponse;
import com.lifeos.onboarding.service.OnboardingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OnboardingController.class)
public class OnboardingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OnboardingService onboardingService;

    @Test
    void startOnboarding_ShouldReturnOk() throws Exception {
        given(onboardingService.startOnboarding(any()))
                .willReturn(OnboardingResponse.builder()
                        .playerId(UUID.randomUUID())
                        .message("Success")
                        .build());

        mockMvc.perform(post("/api/onboarding/start")
                        .param("username", "testuser")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
