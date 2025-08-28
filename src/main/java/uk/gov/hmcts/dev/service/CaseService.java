package uk.gov.hmcts.dev.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import uk.gov.hmcts.dev.dto.CaseRequest;
import uk.gov.hmcts.dev.dto.SearchCriteria;
import uk.gov.hmcts.dev.dto.TaskResponseData;
import uk.gov.hmcts.dev.util.SecurityUtils;
import uk.gov.hmcts.dev.util.helper.ErrorMessageHelper;
import uk.gov.hmcts.dev.mapper.CaseMapper;
import uk.gov.hmcts.dev.model.CaseStatus;
import uk.gov.hmcts.dev.repository.CaseRepository;

import java.util.UUID;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Service
@RequiredArgsConstructor
public class CaseService {
    private final CaseRepository caseRepository;
    private final ErrorMessageHelper errorMessageHelper;
    private final CaseMapper mapper;

    @Transactional
    public TaskResponseData createCase(CaseRequest request){

        if(isNull(request.status())) {
            request = new CaseRequest(
                    null,
                    request.title(),
                    request.description(),
                    CaseStatus.OPEN,
                    request.due()
            );
        }

        var task = mapper.toCase(request);

        var response = caseRepository.save(task);

        return TaskResponseData.builder()
                .task(mapper.toCaseResponse(response))
                .build();
    }

    public TaskResponseData getCase(SearchCriteria keywords){
        var pageable = PageRequest.of(keywords.page(), keywords.limit(), Sort.by(keywords.sortOrder(), keywords.sortBy()));

        var cases = caseRepository.findAll(
                CaseSearchSpecification.withCriteria(keywords),
                pageable);

        return TaskResponseData.builder()
                .tasks(mapper.pageToCasesResponse(cases))
                .totalElement(cases.getTotalElements())
                .totalPage(cases.getTotalPages())
                .build();
    }

    public TaskResponseData getCase(UUID id){
        var response = caseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(errorMessageHelper.caseNotFoundErrorMessage()));

        return TaskResponseData.builder()
                .task(mapper.toCaseResponse(response))
                .build();
    }

    @Transactional
    public TaskResponseData updateCase(CaseRequest request){
        var task = caseRepository.findById(request.id())
                .orElseThrow(() -> new EntityNotFoundException(errorMessageHelper.caseNotFoundErrorMessage()));

        if(nonNull(request.title())){
            task.setTitle(request.title());
        }

        if(nonNull(request.description())){
            task.setDescription(request.description());
        }

        if(nonNull(request.status())){
            task.setStatus(request.status());
        }

        if(nonNull(request.due())){
            task.setDue(request.due());
        }

        SecurityUtils.getPrincipal().ifPresent(jwtUserDetails ->
                task.setUpdatedBy(jwtUserDetails.getId()));

        return TaskResponseData.builder()
                .task(mapper.toCaseResponse(caseRepository.save(task)))
                .build();
    }

    @Transactional
    public void deleteCase(UUID id){
        var response = caseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(errorMessageHelper.caseNotFoundErrorMessage()));

        response.setDeleted(true);

        caseRepository.save(response);
    }
}
