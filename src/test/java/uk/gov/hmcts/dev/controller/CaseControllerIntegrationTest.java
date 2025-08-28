package uk.gov.hmcts.dev.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import uk.gov.hmcts.dev.dto.CaseRequest;
import uk.gov.hmcts.dev.dto.JwtUserDetails;
import uk.gov.hmcts.dev.model.Case;
import uk.gov.hmcts.dev.model.CaseStatus;
import uk.gov.hmcts.dev.repository.CaseRepository;
import uk.gov.hmcts.dev.util.SecurityUtils;
import uk.gov.hmcts.dev.util.helper.ErrorMessageHelper;

import java.time.LocalDateTime;
import java.util.Optional;
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

    @Autowired
    private ErrorMessageHelper errorMessage;

    @Autowired
    private CaseRepository caseRepository;

    private static final String BASE_URL = "/api/v1/case/";

    @Test
    @WithMockUser(username = "testuser", roles = {"STAFF"})
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
    @WithMockUser(username = "testuser", roles = {"STAFF"})
    void shouldNotCreateTaskWhenTitleExistForSameOwner() throws Exception {
        try(MockedStatic<SecurityUtils> mockedSecurityUtils = Mockito.mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getPrincipal).thenReturn(Optional.of(JwtUserDetails.builder().id(UUID.randomUUID()).build()));

            var request = new Case(
                    "Test title",
                    "Test description",
                    CaseStatus.OPEN,
                    LocalDateTime.now().plusDays(180)
            );

            caseRepository.save(request);

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.data.errors.title").value(errorMessage.duplicateTitleErrorMessage()));
        }
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"STAFF"})
    void shouldNotCreateTaskWithInvalidRequest() throws Exception {

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CaseRequest.builder().build()))
                )
                .andExpect(status().isBadRequest());

    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    public void shouldNotCreateTaskWithWrongPermission_denyAccess() throws Exception {
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
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.data.error").value(errorMessage.unauthorizedErrorMessage()));

    }

    @Test
    @WithMockUser(username = "testuser", roles = {"STAFF"})
    public void shouldReturnAllCases() throws Exception {
        caseRepository.deleteAll();
        caseRepository.saveAll(List.of(new Case(
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
                .andExpect(jsonPath("$.data.tasks", hasSize(2)));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"STAFF"})
    public void shouldReturnCaseWhenSearchByTitle() throws Exception {
        var response = caseRepository.saveAll(List.of(new Case(
                "Test search by title",
                "Test description",
                CaseStatus.IN_PROGRESS,
                LocalDateTime.now().plusDays(180)
        ), new Case(
                "Test search by next title",
                "Test next description",
                CaseStatus.IN_PROGRESS,
                LocalDateTime.now().plusDays(180)
        )));

        mockMvc.perform(get(BASE_URL).param("title", "next"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tasks", hasSize(1)))
                .andExpect(jsonPath("$.data.tasks[0].title").value(response.get(1).getTitle()));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"STAFF"})
    public void shouldReturnCaseWhenSearchByCreatedBy() throws Exception {
        var createdBy = UUID.randomUUID();
        var task1 = Case.builder().title("Test search by created_by").description("test description").status(CaseStatus.OPEN).due(LocalDateTime.now().plusDays(180)).build();
        task1.setCreatedBy(createdBy);

        caseRepository.saveAll(List.of(task1, new Case(
                "Test search by created_by 2",
                "Test next description",
                CaseStatus.IN_PROGRESS,
                LocalDateTime.now().plusDays(180)
        )));

        mockMvc.perform(get(BASE_URL).param("createdBy", createdBy.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tasks", hasSize(1)))
                .andExpect(jsonPath("$.data.tasks[0].title").value(task1.getTitle()));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"STAFF"})
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
    @WithMockUser(username = "testuser", roles = {"STAFF"})
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
    @WithMockUser(username = "testuser", roles = {"STAFF"})
    void shouldNotUpdateTaskWithInvalidRequestId() throws Exception {

        // Updating a task without passing the id should be rejected.
        // The endpoint will return a bad request with a message
        mockMvc.perform(put(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CaseRequest.builder().status(CaseStatus.COMPLETED).build()))
                )
                .andExpect(status().isBadRequest());

    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    public void shouldNotUpdateTaskWhenUnauthorisedPermission_denyAccess() throws Exception {

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

        mockMvc.perform(put(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.data.error").value(errorMessage.unauthorizedErrorMessage()));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"STAFF"})
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
    @WithMockUser(username = "testuser", roles = {"STAFF"})
    public void shouldReturnStatusNotFoundWhenTaskDoesNotExists() throws Exception {
        // Prepare
        var taskId = UUID.randomUUID();

        // Execute & Verify
        mockMvc.perform(delete(BASE_URL + "{id}", taskId))
                .andExpect(status().isNotFound());

        // Verify task is actually deleted
        assertFalse(caseRepository.existsById(taskId));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    public void shouldNotDeleteTaskWhenUnauthorisedPermission_denyAccess() throws Exception {
        // Prepare
        var taskId = UUID.randomUUID();

        // Execute & Verify
        mockMvc.perform(delete(BASE_URL + "{id}", taskId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.data.error").value(errorMessage.unauthorizedErrorMessage()));
    }
}