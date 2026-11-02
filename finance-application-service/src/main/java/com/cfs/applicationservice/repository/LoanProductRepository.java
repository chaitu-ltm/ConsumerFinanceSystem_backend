package com.cfs.applicationservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cfs.applicationservice.entity.LoanProduct;

public interface LoanProductRepository
        extends JpaRepository<LoanProduct, Long> {

    Optional<LoanProduct> findByProductCode(
            String productCode
    );

    boolean existsByProductCode(
            String productCode
    );

    List<LoanProduct> findByActiveTrue();
}