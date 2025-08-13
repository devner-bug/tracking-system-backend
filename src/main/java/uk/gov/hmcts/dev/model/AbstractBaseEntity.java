package uk.gov.hmcts.dev.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

import static java.util.Objects.isNull;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@MappedSuperclass
@SuperBuilder
public abstract class AbstractBaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    protected UUID id;
    @Column(columnDefinition = "bool default false")
    protected boolean deleted;
    @Column(columnDefinition = "timestamp with time zone DEFAULT CURRENT_DATE NOT NULL", updatable = false)
    protected LocalDateTime createdAt;
    @Column(columnDefinition = "timestamp with time zone")
    protected LocalDateTime updatedAt;
    @Column(updatable = false)
    protected UUID createdBy;
    @Column(updatable = false)
    protected UUID updatedBy;

    @PrePersist
    protected void onCreate() {
        if(isNull(createdAt)){
            createdAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onModify(){
        if(isNull(updatedAt)){
            updatedAt = LocalDateTime.now();
        }
    }
}
