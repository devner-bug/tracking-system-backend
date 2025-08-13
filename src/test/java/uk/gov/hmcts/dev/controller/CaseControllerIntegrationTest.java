package uk.gov.hmcts.dev.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import uk.gov.hmcts.dev.dto.CaseRequest;
import uk.gov.hmcts.dev.model.Case;
import uk.gov.hmcts.dev.model.CaseStatus;
import uk.gov.hmcts.dev.repository.CaseRepository;
import uk.gov.hmcts.dev.service.CaseService;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CaseControllerIntegrationTest {

    @BeforeEach
    void setUp() {
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Mock
    private CaseService taskService;

    @Autowired
    private CaseRepository caseRepository;

    private static final String BASE_URL = "/api/v1/case/";

    @Test
    void shouldCreateTask() throws Exception {
        var request = new CaseRequest(
                null,
                "Test title",
                "Test description",
                null,
                LocalDateTime.now().plusDays(180)
        );

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.task.title").value(request.title()))
                .andExpect(jsonPath("$.data.task.description").value(request.description()))
                .andExpect(jsonPath("$.data.task.status").value(CaseStatus.OPEN.toString()));

    }

    @Test
    void shouldNotCreateTaskWithInvalidRequest() throws Exception {

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CaseRequest.builder().build()))
                )
                .andExpect(status().isBadRequest());

    }

    @Test
    public void shouldReturnAllCases() throws Exception {
        caseRepository.deleteAll();
        var response = caseRepository.saveAll(List.of(new Case(
                "Test title 1",
                "Test description 1",
                CaseStatus.IN_PROGRESS,
                LocalDateTime.now().plusDays(180)
        ), new Case(
                "Test next title 1",
                "Test next description 1",
                CaseStatus.IN_PROGRESS,
                LocalDateTime.now().plusDays(180)
        )));

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tasks", hasSize(2)))
                .andExpect(jsonPath("$.data.tasks[0].title").value(response.getFirst().getTitle()));
    }

    @Test
    public void shouldReturnCaseWhenSearchByTitle() throws Exception {
        var response = caseRepository.saveAll(List.of(new Case(
                "Test title 2",
                "Test description 2",
                CaseStatus.IN_PROGRESS,
                LocalDateTime.now().plusDays(180)
        ), new Case(
                "Test next title 2",
                "Test next description 2",
                CaseStatus.IN_PROGRESS,
                LocalDateTime.now().plusDays(180)
        )));

        mockMvc.perform(get(BASE_URL).param("title", "next"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tasks", hasSize(1)))
                .andExpect(jsonPath("$.data.tasks[0].title").value(response.get(1).getTitle()));
    }

    @Test
    public void shouldReturnOneById() throws Exception {

        var response = caseRepository.save(new Case(
                "Test title 3",
                "Test description 3",
                CaseStatus.IN_PROGRESS,
                LocalDateTime.now().plusDays(180)
        ));

        mockMvc.perform(get(BASE_URL + "{id}", response.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.task.id").value(response.getId().toString()))
                .andExpect(jsonPath("$.data.task.title").value(response.getTitle()))
                .andExpect(jsonPath("$.data.task.status").value(response.getStatus().toString()));
    }

    @Test
    public void shouldUpdateStatus() throws Exception {

        var response = caseRepository.save(new Case(
                "Test title 4",
                "Test description 4",
                CaseStatus.IN_PROGRESS,
                LocalDateTime.now().plusDays(180)
        ));

        var request = CaseRequest.builder()
                .id(response.getId())
                .status(CaseStatus.COMPLETED)
                .build();

        mockMvc.perform(
                put(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.task.id").value(response.getId().toString()))
                .andExpect(jsonPath("$.data.task.title").value(response.getTitle()))
                .andExpect(jsonPath("$.data.task.status").value(request.status().toString()));
    }

    @Test
    void shouldNotUpdateTaskWithInvalidRequestId() throws Exception {

        mockMvc.perform(put(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CaseRequest.builder().status(CaseStatus.COMPLETED).build()))
                )
                .andExpect(status().isBadRequest());

    }

    @Test
    public void shouldDeleteCase() throws Exception {
        // Prepare
        var response = caseRepository.saveAndFlush(new Case(
                "Test title 5",
                "Test description 5",
                CaseStatus.IN_PROGRESS,
                LocalDateTime.now().plusDays(180)
        ));

        // Execute & Verify
        mockMvc.perform(delete(BASE_URL + "{id}", response.getId()))
                .andExpect(status().isOk());

        // Verify task is actually deleted
        assertFalse(caseRepository.existsById(response.getId()));
    }

    @Test
    public void shouldReturnNoContentWhenDeleteNoExists() throws Exception {
        // Prepare
        var taskId = UUID.randomUUID();

        // Execute & Verify
        mockMvc.perform(delete(BASE_URL + "{id}", taskId))
                .andExpect(status().isNotFound());

        // Verify task is actually deleted
        assertFalse(caseRepository.existsById(taskId));
    }
}