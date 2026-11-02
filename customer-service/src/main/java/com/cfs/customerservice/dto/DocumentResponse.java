package com.cfs.customerservice.dto;

import java.time.LocalDateTime;

public class DocumentResponse {

    private Long documentId;

    private Long customerId;

    private String documentType;

    private String fileName;

    private String filePath;

    private String verificationStatus;

    private LocalDateTime uploadedAt;

    public DocumentResponse() {
    }

    public DocumentResponse(
            Long documentId,
            Long customerId,
            String documentType,
            String fileName,
            String filePath,
            String verificationStatus,
            LocalDateTime uploadedAt) {

        this.documentId = documentId;
        this.customerId = customerId;
        this.documentType = documentType;
        this.fileName = fileName;
        this.filePath = filePath;
        this.verificationStatus = verificationStatus;
        this.uploadedAt = uploadedAt;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(
            String verificationStatus) {

        this.verificationStatus = verificationStatus;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(
            LocalDateTime uploadedAt) {

        this.uploadedAt = uploadedAt;
    }
}