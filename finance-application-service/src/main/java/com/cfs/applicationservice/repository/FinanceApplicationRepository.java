package com.cfs.applicationservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cfs.applicationservice.entity.FinanceApplication;
import com.cfs.applicationservice.entity.ApplicationStatus;

public interface FinanceApplicationRepository
        extends JpaRepository<FinanceApplication, Long> {

    Optional<FinanceApplication>
            findByApplicationNumber(
            String applicationNumber
    );

    List<FinanceApplication> findByCustomerId(
            Long customerId
    );

    List<FinanceApplication> findByStatus(ApplicationStatus status);
}