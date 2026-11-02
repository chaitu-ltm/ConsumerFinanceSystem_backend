package com.cfs.emi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cfs.emi.entity.Repayment;
import com.cfs.emi.entity.RepaymentStatus;

public interface RepaymentRepository extends JpaRepository<Repayment, Long> {

	List<Repayment> findByLoanAccountIdOrderByPaymentDateDesc(Long loanAccountId);

	boolean existsByEmiIdAndStatus(Long emiId, RepaymentStatus status);
}
