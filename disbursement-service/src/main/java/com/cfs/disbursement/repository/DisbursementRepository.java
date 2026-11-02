package com.cfs.disbursement.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cfs.disbursement.entity.Disbursement;

public interface DisbursementRepository
        extends JpaRepository<Disbursement, Long> {

    boolean existsByApplicationId(Long applicationId);

    Optional<Disbursement> findByApplicationId(
            Long applicationId
    );
}