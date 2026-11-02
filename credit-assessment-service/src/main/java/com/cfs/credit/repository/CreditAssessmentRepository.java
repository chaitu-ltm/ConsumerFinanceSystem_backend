package com.cfs.credit.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cfs.credit.entity.AssessmentStatus;
import com.cfs.credit.entity.CreditAssessment;

@Repository
public interface CreditAssessmentRepository extends JpaRepository<CreditAssessment, Long> {

    /*
     * Find credit assessment using finance application ID.
     */
    Optional<CreditAssessment> findByApplicationId(
            Long applicationId
    );

    /*
     * Check whether an assessment already exists
     * for a finance application.
     */
    boolean existsByApplicationId(
            Long applicationId
    );

    /*
     * Get assessments based on status:
     *
     * ELIGIBLE
     * NOT_ELIGIBLE
     */
    List<CreditAssessment> findByStatus(
            AssessmentStatus status
    );

    /*
     * Get assessments performed by a particular
     * credit analyst.
     */
    List<CreditAssessment> findByAssessedByIgnoreCase(
            String assessedBy
    );
}