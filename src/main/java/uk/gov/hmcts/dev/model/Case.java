package uk.gov.hmcts.dev.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "task")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@SQLRestriction("deleted <> true")
public class Case extends AbstractBaseEntity{
    @NonNull
    private String title;
    @NonNull
    private String description;
    @NonNull
    @Enumerated(EnumType.STRING)
    private CaseStatus status;
    @NonNull
    private LocalDateTime due;


}
