package com.cfs.customerservice.repository;


import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;

import com.cfs.customerservice.entity.Customer;

@Repository
public interface CustomerRepository
        extends JpaRepository<Customer, Long> {

    Optional<Customer> findByUserId(
            Long userId
    );

    Optional<Customer> findByCustomerNumber(
            String customerNumber
    );

    boolean existsByUserId(
            Long userId
    );

    boolean existsByMobileNumber(
            String mobileNumber
    );

    boolean existsByPanNumber(
            String panNumber
    );

    List<Customer> findByKycStatus(
            String kycStatus
    );

    // SELECT ... FOR UPDATE: serializes KYC workflow transitions per customer.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Customer c where c.id = :id")
    Optional<Customer> findByIdForUpdate(
            @Param("id") Long id
    );
}