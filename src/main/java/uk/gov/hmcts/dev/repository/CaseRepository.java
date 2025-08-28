package uk.gov.hmcts.dev.repository;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import uk.gov.hmcts.dev.model.Case;
import java.util.UUID;

@Repository
public interface CaseRepository extends JpaRepository<Case, UUID>, JpaSpecificationExecutor<Case> {
    @Query(value = "select case when count(c.*)> 0 then true else false end from task c where lower(c.title) = lower(:title) and deleted <> true", nativeQuery = true)
    boolean existsByTitle(@NotEmpty(message = "Title is required") String title);
    @Query(value = "select case when count(c.*)> 0 then true else false end from task c where lower(c.title) = lower(:title) and c.created_by = :createdBy and deleted <> true", nativeQuery = true)
    boolean existsByTitleAndCreatedBy(@NotEmpty(message = "Title is required") String title, @NotNull UUID createdBy);

    boolean existsByIdAndCreatedBy(UUID caseId, UUID createBy);
}
