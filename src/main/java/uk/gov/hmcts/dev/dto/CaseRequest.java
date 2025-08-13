package uk.gov.hmcts.dev.dto;


import java.time.LocalDateTime;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import uk.gov.hmcts.dev.dto.validation.ValidateCreateGroup;
import uk.gov.hmcts.dev.dto.validation.ValidateUpdateGroup;
import uk.gov.hmcts.dev.model.CaseStatus;
import java.util.UUID;

@Builder
public record CaseRequest(
        @NotNull(groups = {ValidateUpdateGroup.class}, message = "Id is required")
        UUID id,
        @NotEmpty(message = "Title is required", groups = {ValidateCreateGroup.class})
        String title,
        @NotEmpty(message = "Description is required", groups = {ValidateCreateGroup.class})
        String description,
        CaseStatus status,
        @NotNull(message = "Due date is required", groups = {ValidateCreateGroup.class})
        LocalDateTime due
) {
}
