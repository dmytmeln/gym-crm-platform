package com.gym.crm.core.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gym.crm.core.exception.ApiError;
import com.gym.crm.core.facade.GymFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static java.lang.String.format;

public abstract class AbstractRestControllerTest {

    protected static final String EXPECTED_ERROR_MESSAGE_TEMPLATE = "%s: %s";
    protected static final String BASE_PATH = "/api/v1";
    protected static final String TRAINEES_ENDPOINT = BASE_PATH + "/trainees";
    protected static final String TRAINERS_ENDPOINT = BASE_PATH + "/trainers";
    protected static final String TRAININGS_ENDPOINT = BASE_PATH + "/trainings";
    protected static final String AUTH_ENDPOINT = BASE_PATH + "/auth";

    @MockitoBean
    protected GymFacade facade;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected MockMvc mockMvc;

    protected String buildExpectedErrorMessage(ApiError apiError, Exception exception) {
        return format(EXPECTED_ERROR_MESSAGE_TEMPLATE, apiError.getMessage(), exception.getMessage());
    }

}
