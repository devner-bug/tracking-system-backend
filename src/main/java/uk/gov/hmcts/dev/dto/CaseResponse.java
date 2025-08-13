package uk.gov.hmcts.dev.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import uk.gov.hmcts.dev.model.CaseStatus;
import java.util.UUID;

@Builder
public record CaseResponse(
        UUID id,
        String title,
        String description,
        CaseStatus status,
        LocalDateTime due
) {
}
