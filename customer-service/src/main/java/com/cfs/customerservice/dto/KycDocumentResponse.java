package com.cfs.customerservice.dto;

import java.time.LocalDateTime;

public class KycDocumentResponse {

    private Long id;
    private String documentType;
    private String fileName;
    private String filePath;
    private String verificationStatus;
    private LocalDateTime uploadedAt;

    public KycDocumentResponse() {
    }

    public KycDocumentResponse(
            Long id,
            String documentType,
            String fileName,
            String filePath,
            String verificationStatus,
            LocalDateTime uploadedAt) {

        this.id = id;
        this.documentType = documentType;
        this.fileName = fileName;
        this.filePath = filePath;
        this.verificationStatus = verificationStatus;
        this.uploadedAt = uploadedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public void setVerificationStatus(String verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
}