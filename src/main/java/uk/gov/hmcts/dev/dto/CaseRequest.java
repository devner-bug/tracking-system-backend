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
        @NotNull(groups = {ValidateUpdateGroup.class}, message = "{id.required}")
        UUID id,
        @NotEmpty(message = "{title.required}", groups = {ValidateCreateGroup.class})
        String title,
        @NotEmpty(message = "{description.required}", groups = {ValidateCreateGroup.class})
        String description,
        CaseStatus status,
        @NotNull(message = "{due.date.required}", groups = {ValidateCreateGroup.class})
        LocalDateTime due
) {
}
