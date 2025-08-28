package uk.gov.hmcts.dev.dto;

import lombok.Builder;
import org.springframework.data.domain.Sort;
import uk.gov.hmcts.dev.model.CaseStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record SearchCriteria(
        String title,
        String description,
        CaseStatus status,
        LocalDateTime dueFrom,
        LocalDateTime dueTo,
        int page,
        int limit,
        String sortBy,
        Sort.Direction sortOrder,
        UUID createdBy
) {
}
