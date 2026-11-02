package com.cfs.customerservice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cfs.customerservice.entity.EmploymentDetail;

public interface EmploymentDetailRepository
        extends JpaRepository<EmploymentDetail, Long> {

    Optional<EmploymentDetail> findByCustomerId(
            Long customerId
    );

    boolean existsByCustomerId(Long customerId);
}