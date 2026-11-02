package com.cfs.approval.repository;


import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cfs.approval.entity.ApprovalDecision;
import com.cfs.approval.entity.ApprovalStatus;

public interface ApprovalRepository
        extends JpaRepository<ApprovalDecision, Long> {

    Optional<ApprovalDecision> findByApplicationId(Long applicationId);

    boolean existsByApplicationId(Long applicationId);

    List<ApprovalDecision> findByDecision(ApprovalStatus decision);

    List<ApprovalDecision> findByReviewedByIgnoreCase(String reviewedBy);
}