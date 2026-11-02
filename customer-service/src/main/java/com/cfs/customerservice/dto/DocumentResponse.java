package com.cfs.customerservice.dto;

import java.time.LocalDateTime;

public record DocumentResponse(
        Long documentId,
        Long customerId,
        String documentType,
        String fileName,
        String filePath,
        String verificationStatus,
        LocalDateTime uploadedAt) {

    public DocumentResponse() {
        this(null, null, null, null, null, null, null);
    }
}
