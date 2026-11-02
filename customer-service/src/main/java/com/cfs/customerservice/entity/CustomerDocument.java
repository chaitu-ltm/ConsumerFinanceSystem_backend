package com.cfs.customerservice.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "customer_documents")
@Getter
@Setter
@NoArgsConstructor
public class CustomerDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "document_type", nullable = false)
    private String documentType;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "verification_status", nullable = false)
    private String verificationStatus;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

}