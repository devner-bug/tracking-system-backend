package uk.gov.hmcts.dev.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.dev.config.properties.ApplicationProperties;
import uk.gov.hmcts.dev.model.Case;
import uk.gov.hmcts.dev.model.CaseStatus;
import uk.gov.hmcts.dev.repository.CaseRepository;
import uk.gov.hmcts.dev.util.helper.ErrorMessageHelper;
import uk.gov.hmcts.dev.util.helper.FieldHelper;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ExceptionHandlerConfigTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private CaseRepository caseRepository;
    @Autowired
    private FieldHelper fieldHelper;
    @Autowired
    private ApplicationProperties appProps;
    @Autowired
    private ErrorMessageHelper errorMessageHelper;

    private static final String BASE_URL = "/api/v1/case/";

    @Test
    @WithMockUser(username = "testuser", roles = {"STAFF"})
    void handleEntityNotFoundExceptionHandler() throws Exception {
        mockMvc.perform(get(BASE_URL + "{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("NOT_FOUND"))
                .andExpect(jsonPath("$.data.error").value(errorMessageHelper.caseNotFoundErrorMessage()));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"STAFF"})
    void handleArgumentNotValidExceptionHandler() throws Exception {
        mockMvc.perform(
                        post(BASE_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(new Case()))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.data.errors.title").value(fieldHelper.titleRequired()))
                .andExpect(jsonPath("$.data.errors.description").value(fieldHelper.descriptionRequired()))
                .andExpect(jsonPath("$.data.errors.due").value(fieldHelper.dueDateRequired()));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"STAFF"})
    void handleArgumentNotValidExceptionHandler_forPut() throws Exception{
        mockMvc.perform(
                        put(BASE_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(new Case()))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.data.errors.id").value(fieldHelper.idRequired()));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"STAFF"})
    void handleUnexpectedException() throws Exception {
        mockMvc.perform(
                        post(BASE_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.data.error").value(errorMessageHelper.unexpectedErrorMessage()));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"STAFF"})
    void handleExpiredToken() throws Exception {
        var expiredToken = Jwts.builder()
                .subject("staff")
                .expiration(Date.from(Instant.now().minusSeconds(60))) // expired 1 minute ago
                .signWith(Keys.hmacShaKeyFor(appProps.getSecurityKey().getBytes()))
                .compact();

        mockMvc.perform(get(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + expiredToken)
                )
                .andExpect(status().isUnauthorized());
    }
}