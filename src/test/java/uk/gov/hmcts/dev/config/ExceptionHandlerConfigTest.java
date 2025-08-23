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
import uk.gov.hmcts.dev.util.helper.ErrorHelper;
import uk.gov.hmcts.dev.util.helper.FieldHelper;

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
    @Autowired
    private FieldHelper fieldHelper;
    @Autowired
    private ErrorHelper errorHelper;
    private static final String BASE_URL = "/api/v1/case/";

    @Test
    void handleEntityNotFoundExceptionHandler() throws Exception {
        mockMvc.perform(get(BASE_URL + "{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("NOT_FOUND"))
                .andExpect(jsonPath("$.data.error").value(errorHelper.caseNotFoundError()));
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
                .andExpect(jsonPath("$.data.errors.title").value(errorHelper.duplicateTitleError()));
    }

    @Test
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
    void handleUnexpectedException() throws Exception {
        mockMvc.perform(
                        post(BASE_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                ) // assuming this triggers the exception
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.data.error").value(errorHelper.unexpectedError()));
    }
}