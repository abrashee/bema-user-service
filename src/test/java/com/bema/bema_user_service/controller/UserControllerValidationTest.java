package com.bema.bema_user_service.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import com.bema.bema_user_service.exception.GlobalExceptionHandler;
import com.bema.bema_user_service.service.serviceInterface.UserService;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class UserControllerValidationTest {

    private UserService userService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);

        ObjectMapper objectMapper = new ObjectMapper()
                .findAndRegisterModules()
                .configure(
                        DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                        true
                );

        LocalValidatorFactoryBean validator =
                new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(new UserController(userService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .setMessageConverters(
                        new MappingJackson2HttpMessageConverter(objectMapper)
                )
                .build();
    }

    @Test
    void rejectsNonPositiveUserId() throws Exception {
        mockMvc.perform(get("/api/users/0")
                        .principal(internalAuthentication()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Invalid request parameter"));

        verifyNoInteractions(userService);
    }

    @Test
    void rejectsInvalidEmail() throws Exception {
        mockMvc.perform(post("/api/users/internal")
                        .principal(internalAuthentication())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "identityId": "id-1",
                                  "email": "invalid-email",
                                  "name": "John Doe"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data").isEmpty());

        verifyNoInteractions(userService);
    }

    @Test
    void rejectsShortName() throws Exception {
        mockMvc.perform(post("/api/users/internal")
                        .principal(internalAuthentication())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "identityId": "id-1",
                                  "email": "john@example.com",
                                  "name": "A"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data").isEmpty());

        verifyNoInteractions(userService);
    }

    @Test
    void rejectsUnknownJsonField() throws Exception {
        mockMvc.perform(post("/api/users/internal")
                        .principal(internalAuthentication())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "identityId": "id-1",
                                  "email": "john@example.com",
                                  "name": "John Doe",
                                  "admin": true
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Malformed request body"));

        verifyNoInteractions(userService);
    }

    @Test
    void rejectsMalformedJson() throws Exception {
        mockMvc.perform(post("/api/users/internal")
                        .principal(internalAuthentication())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "identityId":
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Malformed request body"));

        verifyNoInteractions(userService);
    }

    private UsernamePasswordAuthenticationToken internalAuthentication() {
        return new UsernamePasswordAuthenticationToken(
                "identity-service",
                null,
                List.of(new SimpleGrantedAuthority("SCOPE_INTERNAL"))
        );
    }
}
