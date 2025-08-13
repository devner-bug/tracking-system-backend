package uk.gov.hmcts.dev.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.dev.dto.CaseRequest;
import uk.gov.hmcts.dev.dto.CaseResponse;
import uk.gov.hmcts.dev.dto.SearchCriteria;
import jakarta.persistence.EntityNotFoundException;
import uk.gov.hmcts.dev.exception.DuplicateException;
import uk.gov.hmcts.dev.mapper.CaseMapper;
import uk.gov.hmcts.dev.model.Case;
import uk.gov.hmcts.dev.model.CaseStatus;
import uk.gov.hmcts.dev.repository.CaseRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static java.util.Objects.nonNull;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CaseServiceTest {

    @Mock
    private CaseRepository caseRepository;

    @Mock
    private CaseMapper caseMapper;

    @InjectMocks
    private CaseService caseService;

    private Case task1, task2, task3;

    @BeforeEach
    void setup(){
        task1 = CaseTestFactory.createCase(UUID.randomUUID(), "Case 1", "A new description 1", CaseStatus.OPEN);
        task2 = CaseTestFactory.createCase(UUID.randomUUID(), "Case 2", "A new description 2", CaseStatus.OPEN);
        task3 = CaseTestFactory.createCase(UUID.randomUUID(), "Case 3", "A new description 3", CaseStatus.OPEN);
    }

    @Test
    void createCase_shouldSaveCase() {
        var dto = new CaseRequest(null, task1.getTitle(), task1.getDescription(), task1.getStatus(), task1.getDue());
        var outputCase = new CaseResponse(task1.getId(), task1.getTitle(), task1.getDescription(), task1.getStatus(), task1.getDue());

//      // Given
        when(caseRepository.existsByTitle(dto.title())).thenReturn(false);
        when(caseRepository.save(any())).thenReturn(any());
        when(caseMapper.toCaseResponse(task1)).thenReturn(outputCase);

        // When
        var result = caseService.createCase(dto);

        // Then
        assertTrue(nonNull(result.getTask()));
        assertEquals("Case 1", result.getTask().title());
        assertEquals("A new description 1", result.getTask().description());
        assertEquals(CaseStatus.OPEN, result.getTask().status());
    }

    @Test
    void createExistingCase_shouldThrowExceptionWhenFound(){
        var dto = new CaseRequest(null, task1.getTitle(), task1.getDescription(), task1.getStatus(), task1.getDue());

//      // Given
        when(caseRepository.existsByTitle(dto.title())).thenReturn(true);

        // When/Then
        assertThrows(DuplicateException.class, () -> {
            caseService.createCase(dto);
        });
    }

    @Test
    void getCaseById_shouldReturnCase() {
        var outputCase = new CaseResponse(task1.getId(), task1.getTitle(), task1.getDescription(), task1.getStatus(), task1.getDue());
        // Given
        when(caseRepository.findById(task1.getId())).thenReturn(Optional.of(task1));
        when(caseMapper.toCaseResponse(task1)).thenReturn(outputCase);

        // When
        var result = caseService.getCase(task1.getId());

        // Then
        assertTrue(nonNull(result.getTask()));
        assertEquals(task1.getId(), result.getTask().id());
        assertEquals("Case 1", result.getTask().title());
    }

    @Test
    void getCaseById_shouldThrowExceptionWhenNotFound() {
        // Given
        when(caseRepository.findById(task1.getId())).thenReturn(Optional.empty());

        // When/Then
        assertThrows(EntityNotFoundException.class, () -> {
            caseService.getCase(task1.getId());
        });
    }

    @Test
    void getAllCases_shouldReturnAllCases() {
        // Given
        Page<Case> mockPage = new PageImpl<>(List.of(task1, task2, task3));
        var outputCase = new CaseResponse(task1.getId(), task1.getTitle(), task1.getDescription(), task1.getStatus(), task1.getDue());
        var outputCase2 = new CaseResponse(task2.getId(), task2.getTitle(), task2.getDescription(), task2.getStatus(), task2.getDue());
        var outputCase3 = new CaseResponse(task3.getId(), task3.getTitle(), task3.getDescription(), task3.getStatus(), task3.getDue());

        when(caseRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(mockPage);
        when(caseMapper.pageToCasesResponse(mockPage)).thenReturn(List.of(outputCase, outputCase2, outputCase3));

        // When
        var result = caseService.getCase(SearchCriteria.builder().page(0).limit(10).sortBy("createdAt").sortOrder(Sort.Direction.DESC).build());

        // Then
        assertEquals(3, result.getTasks().size());
        assertEquals("Case 1", result.getTasks().get(0).title());
        assertEquals("Case 2", result.getTasks().get(1).title());
        assertEquals("Case 3", result.getTasks().get(2).title());
    }

    @Test
    void updateCaseStatus_shouldUpdateStatus() {
        // Given
        var request = new CaseRequest(task1.getId(), task1.getTitle(), null, CaseStatus.COMPLETED, task1.getDue());
        var outputCase = new CaseResponse(task1.getId(), task1.getTitle(), task1.getDescription(), request.status(), task1.getDue());
        var updatedCase = CaseTestFactory.createCase(task1.getId(), task1.getTitle(), task1.getDescription(), CaseStatus.COMPLETED);
        updatedCase.setUpdatedAt(LocalDateTime.now());

        when(caseRepository.findById(task1.getId())).thenReturn(Optional.of(task1));
        when(caseRepository.save(any())).thenReturn(any());
        when(caseMapper.toCaseResponse(updatedCase)).thenReturn(outputCase);

        // When
        var result = caseService.updateCase(request);

        // Then
        assertTrue(nonNull(result.getTask()));
        assertEquals(CaseStatus.COMPLETED, result.getTask().status());
        verify(caseRepository).save(task1);
    }

    @Test
    void updateCaseStatus_shouldThrowExceptionWhenNotFound(){
        //Given
        var request = new CaseRequest(task1.getId(), task1.getTitle(), null, CaseStatus.COMPLETED, task1.getDue());

        when(caseRepository.findById(task1.getId())).thenReturn(Optional.empty());

        //When/Then
        assertThrows(EntityNotFoundException.class, () -> {
            caseService.updateCase(request);
        });
    }

    @Test
    void deleteCase_shouldDeleteCase() {
        // Given

        when(caseRepository.findById(task1.getId())).thenReturn(Optional.ofNullable(task1));

        // When
        caseService.deleteCase(task1.getId());

        // Then
        verify(caseRepository).save(task1);
    }

    @Test
    void deleteCase_shouldThrowExceptionWhenNotFound(){
        //Given
        when(caseRepository.findById(task1.getId())).thenReturn(Optional.empty());

        //When/Then
        assertThrows(EntityNotFoundException.class, () -> {
            caseService.deleteCase(task1.getId());
        });
    }
}

class CaseTestFactory{
    public static Case createCase(UUID taskId, String title, String description, CaseStatus status){
        var task = new Case(title, description, status, LocalDateTime.of(2025, 12, 6, 6, 6));
        task.setId(taskId);
        task.setCreatedAt(LocalDateTime.now());

        return task;
    }
}