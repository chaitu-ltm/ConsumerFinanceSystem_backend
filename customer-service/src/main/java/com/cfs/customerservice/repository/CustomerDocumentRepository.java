package com.cfs.customerservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cfs.customerservice.entity.CustomerDocument;

@Repository
public interface CustomerDocumentRepository
        extends JpaRepository<CustomerDocument, Long> {

    List<CustomerDocument> findByCustomerId(
            Long customerId
    );

    boolean existsByCustomerId(
            Long customerId
    );

    boolean existsByCustomerIdAndDocumentType(
            Long customerId,
            String documentType
    );
}