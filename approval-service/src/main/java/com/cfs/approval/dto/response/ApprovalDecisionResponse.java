package com.cfs.approval.dto.response;

import java.time.LocalDateTime;
import com.cfs.approval.entity.ApprovalStatus;

public record ApprovalDecisionResponse(
        Long approvalId,
        Long applicationId,
        Long creditAssessmentId,
        ApprovalStatus decision,
        String reviewedBy,
        String remarks,
        LocalDateTime decisionDate) {
}
