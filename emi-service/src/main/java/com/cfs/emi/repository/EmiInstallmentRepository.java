package com.cfs.emi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cfs.emi.entity.EmiInstallment;
import com.cfs.emi.entity.EmiStatus;

public interface EmiInstallmentRepository extends JpaRepository<EmiInstallment, Long> {

	List<EmiInstallment> findByLoanAccountIdOrderByInstallmentNumberAsc(Long loanAccountId);

	List<EmiInstallment> findByStatus(EmiStatus status);

	boolean existsByLoanAccountIdAndStatusNot(Long loanAccountId, EmiStatus status);
}
