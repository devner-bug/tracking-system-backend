package uk.gov.hmcts.dev.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.dev.model.Case;
import uk.gov.hmcts.dev.model.CaseStatus;
import uk.gov.hmcts.dev.repository.CaseRepository;

import java.time.LocalDateTime;
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
    private static final String BASE_URL = "/api/v1/case/";

    @Test
    void handleEntityNotFoundExceptionHandler() throws Exception {
        mockMvc.perform(get(BASE_URL + "{id}", UUID.randomUUID())) // assuming this triggers the exception
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("NOT_FOUND"))
                .andExpect(jsonPath("$.data.error").value("Case not found"));
    }

    @Test
    void handleDuplicateExceptionHandler() throws Exception {
        var request = new Case(
                "Test title",
                "Test description",
                CaseStatus.IN_PROGRESS,
                LocalDateTime.now().plusDays(180)
        );
        caseRepository.save(request);
        mockMvc.perform(
                post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                ) // assuming this triggers the exception
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value("CONFLICT"))
                .andExpect(jsonPath("$.data.errors.title").value("Title already exists"));
    }

    @Test
    void handleArgumentNotValidExceptionHandler() throws Exception {
        mockMvc.perform(
                        post(BASE_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(new Case()))
                ) // assuming this triggers the exception
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.data.errors.title").value("Title is required"))
                .andExpect(jsonPath("$.data.errors.description").value("Description is required"))
                .andExpect(jsonPath("$.data.errors.due").value("Due date is required"));
    }

    @Test
    void handleArgumentNotValidExceptionHandler_forPut() throws Exception{
        mockMvc.perform(
                        put(BASE_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(new Case()))
                ) // assuming this triggers the exception
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.data.errors.id").value("Id is required"));
    }

    @Test
    void handleUnexpectedException() throws Exception {
        mockMvc.perform(
                        post(BASE_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                ) // assuming this triggers the exception
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.data.error").value("An unexpected error occurred"));
    }
}