package uk.gov.hmcts.dev.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


import uk.gov.hmcts.dev.dto.*;
import uk.gov.hmcts.dev.model.CaseStatus;
import uk.gov.hmcts.dev.service.CaseService;
import uk.gov.hmcts.dev.util.helper.SuccessMessageHelper;
import uk.gov.hmcts.dev.util.validation.group.ValidateCreateGroup;
import uk.gov.hmcts.dev.util.validation.group.ValidateUpdateGroup;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v2/case")
@RequiredArgsConstructor
public class CaseController {
    private final CaseService caseService;
    private final SuccessMessageHelper successMessage;

    @GetMapping("/")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<ResponseData<TaskResponseData>> getCase(
            @RequestParam(name = "title", required = false) String title,
            @RequestParam(name = "description", required = false) String description,
            @RequestParam(name = "status", required = false) CaseStatus status,
            @RequestParam(name = "dueFrom", required = false) LocalDateTime dueFrom,
            @RequestParam(name = "dueTo", required = false) LocalDateTime dueTo,
            @RequestParam(name = "page", defaultValue = "0", required = false) int page,
            @RequestParam(name = "limit", defaultValue = "10", required = false) int limit,
            @RequestParam(name = "sortBy", defaultValue = "createdAt", required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = "DESC", required = false) Sort.Direction sortOrder,
            @RequestParam(name = "createdBy", required = false) UUID createdBy
            ){
        var response = caseService.getCase(
                SearchCriteria.builder()
                        .title(title)
                        .description(description)
                        .status(status)
                        .dueFrom(dueFrom)
                        .dueTo(dueTo)
                        .createdBy(createdBy)
                        .page(page)
                        .limit(limit)
                        .sortBy(sortBy)
                        .sortOrder(sortOrder)
                        .build()
        );

        return ResponseHandler.generateResponse(
                successMessage.getTaskSuccessMessage(),
                HttpStatus.OK,
                response
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("@permissionChecker.isOwnersCase(#id) && hasRole('STAFF')")
    public ResponseEntity<ResponseData<TaskResponseData>> getCaseById(
            @PathVariable(name = "id") UUID id){
        var response = caseService.getCase(id);

        return ResponseHandler.generateResponse(
                successMessage.getTaskSuccessMessage(),
                HttpStatus.OK,
                response
        );
    }

    @PostMapping(value = "/")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<ResponseData<TaskResponseData>> createCase(@RequestBody @Validated(ValidateCreateGroup.class) CaseRequest request){
        var response = caseService.createCase(request);

        return ResponseHandler.generateResponse(
                successMessage.createTaskSuccessMessage(),
                HttpStatus.CREATED,
                response
        );
    }

    @PutMapping("/")
    @PreAuthorize("@permissionChecker.isOwnersCase(#request.id) && hasRole('STAFF')")
    public ResponseEntity<ResponseData<Object>> updateCase(@RequestBody @Validated(ValidateUpdateGroup.class) CaseRequest request){

        return ResponseHandler.generateResponse(
                successMessage.updateTaskSuccessMessage(),
                HttpStatus.OK,
                caseService.updateCase(request)
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@permissionChecker.isOwnersCase(#id) && hasRole('STAFF')")
    public ResponseEntity<ResponseData<Object>> deleteCase(
            @PathVariable(name = "id") UUID id){
        caseService.deleteCase(id);

        return ResponseHandler.generateResponse(
                successMessage.deleteTaskSuccessMessage(),
                HttpStatus.OK,
                null
        );
    }
}
