package com.nokia.esim.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.nokia.esim.controller.HealthCheckController;

@SpringBootTest
public class HealthCheckServiceImplTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HealthCheckService healthCheckService;

    @InjectMocks
    private HealthCheckController controller;

    @BeforeEach
    public void setUp() throws JSONException {
        // Mock successful health status response
        when(healthCheckService.getHealthStatus()).thenReturn(new JSONObject().put("status", "OK"));
    }

    @Test
    public void testGetHealthStatus_returnsOk() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/healthCheck"))
               .andExpect(MockMvcResultMatchers.status().isOk())
               .andExpect(MockMvcResultMatchers.content().json("{\"status\":\"OK\"}"));
    }

    // Implement the remaining test cases with appropriate mocking and assertions

}
