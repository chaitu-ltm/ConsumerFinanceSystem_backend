package com.cfs.emi.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import com.cfs.emi.entity.LoanAccount;

import jakarta.persistence.LockModeType;

public interface LoanAccountRepository extends JpaRepository<LoanAccount, Long> {

	Optional<LoanAccount> findByApplicationId(Long applicationId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select account from LoanAccount account where account.loanAccountId = :id")
	Optional<LoanAccount> findByIdForUpdate(@Param("id") Long id);

	boolean existsByApplicationId(Long applicationId);

	boolean existsByDisbursementId(Long disbursementId);
}