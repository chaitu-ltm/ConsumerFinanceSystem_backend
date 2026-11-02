package com.cfs.customerservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cfs.customerservice.entity.BankAccount;

public interface BankAccountRepository
        extends JpaRepository<BankAccount, Long> {

    List<BankAccount> findByCustomerId(Long customerId);

    boolean existsByCustomerId(Long customerId);
}