package uk.gov.hmcts.dev.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


import uk.gov.hmcts.dev.dto.*;
import uk.gov.hmcts.dev.dto.validation.ValidateCreateGroup;
import uk.gov.hmcts.dev.dto.validation.ValidateUpdateGroup;
import uk.gov.hmcts.dev.model.CaseStatus;
import uk.gov.hmcts.dev.service.CaseService;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/case")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class CaseController {
    private final CaseService caseService;

    @GetMapping("/")
    public ResponseEntity<ResponseData<TaskResponseData>> getCase(
            @RequestParam(name = "title", required = false) String title,
            @RequestParam(name = "description", required = false) String description,
            @RequestParam(name = "status", required = false) CaseStatus status,
            @RequestParam(name = "dueFrom", required = false) LocalDateTime dueFrom,
            @RequestParam(name = "dueTo", required = false) LocalDateTime dueTo,
            @RequestParam(name = "page", defaultValue = "0", required = false) int page,
            @RequestParam(name = "limit", defaultValue = "10", required = false) int limit,
            @RequestParam(name = "sortBy", defaultValue = "createdAt", required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = "DESC", required = false) Sort.Direction sortOrder
            ){
        var response = caseService.getCase(
                SearchCriteria.builder()
                        .title(title)
                        .description(description)
                        .status(status)
                        .dueFrom(dueFrom)
                        .dueTo(dueTo)
                        .page(page)
                        .limit(limit)
                        .sortBy(sortBy)
                        .sortOrder(sortOrder)
                        .build()
        );

        return ResponseHandler.generateResponse(
                "Task(s) retrieved successfully",
                HttpStatus.OK,
                response
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseData<TaskResponseData>> getCaseById(
            @PathVariable(name = "id") UUID id){
        var response = caseService.getCase(id);

        return ResponseHandler.generateResponse(
                "Task retrieved successfully",
                HttpStatus.OK,
                response
        );
    }

    @PostMapping(value = "/")

    public ResponseEntity<ResponseData<TaskResponseData>> createCase(@RequestBody @Validated(ValidateCreateGroup.class) CaseRequest request){
        var response = caseService.createCase(request);

        return ResponseHandler.generateResponse(
                "Task created successfully",
                HttpStatus.CREATED,
                response
        );
    }

    @PutMapping("/")
    public ResponseEntity<ResponseData<Object>> updateCase(@RequestBody @Validated(ValidateUpdateGroup.class) CaseRequest request){

        return ResponseHandler.generateResponse(
                "Case updated successfully",
                HttpStatus.OK,
                caseService.updateCase(request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseData<Object>> deleteCase(
            @PathVariable(name = "id") UUID id){
        caseService.deleteCase(id);

        return ResponseHandler.generateResponse(
                "Case deleted successfully",
                HttpStatus.OK,
                null
        );
    }
}
