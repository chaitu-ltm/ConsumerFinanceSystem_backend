package com.cfs.emi.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cfs.emi.dto.RepaymentRequest;
import com.cfs.emi.dto.RepaymentResponse;
import com.cfs.emi.entity.EmiInstallment;
import com.cfs.emi.entity.EmiStatus;
import com.cfs.emi.entity.LoanAccount;
import com.cfs.emi.entity.LoanAccountStatus;
import com.cfs.emi.entity.Repayment;
import com.cfs.emi.entity.RepaymentStatus;
import com.cfs.emi.exception.AlreadyPaidRepaymentException;
import com.cfs.emi.exception.EmiInstallmentNotFoundException;
import com.cfs.emi.exception.InvalidRepaymentException;
import com.cfs.emi.repository.EmiInstallmentRepository;
import com.cfs.emi.repository.LoanAccountRepository;
import com.cfs.emi.repository.RepaymentRepository;

@Service
@Transactional
public class RepaymentServiceImpl implements RepaymentService {

	private final LoanAccountRepository loanAccountRepository;
	private final EmiInstallmentRepository installmentRepository;
	private final RepaymentRepository repaymentRepository;
	private final LoanAccountOwnership ownership;

	public RepaymentServiceImpl(LoanAccountRepository loanAccountRepository,
			EmiInstallmentRepository installmentRepository, RepaymentRepository repaymentRepository,
			LoanAccountOwnership ownership) {

		this.loanAccountRepository = loanAccountRepository;
		this.installmentRepository = installmentRepository;
		this.repaymentRepository = repaymentRepository;
		this.ownership = ownership;
	}

	@Override
	public RepaymentResponse recordRepayment(RepaymentRequest request, String recordedBy) {

		LoanAccount account = ownership.byIdForUpdate(request.getLoanAccountId(), false);

		EmiInstallment installment = installmentRepository.findById(request.getEmiId()).orElseThrow(
				() -> new EmiInstallmentNotFoundException("EMI installment not found: " + request.getEmiId()));

		if (!installment.getLoanAccountId().equals(account.getLoanAccountId())) {

			throw new InvalidRepaymentException("EMI installment does not belong " + "to the supplied loan account");
		}

		if (installment.getStatus() == EmiStatus.PAID) {
			throw new AlreadyPaidRepaymentException();
		}

		BigDecimal outstanding = installment.getEmiAmount().subtract(installment.getAmountPaid());

		if (request.getAmount().compareTo(outstanding) != 0) {

			throw new InvalidRepaymentException(
					"Payment amount must equal " + "the outstanding EMI amount: " + outstanding);
		}

		Repayment repayment = new Repayment();

		repayment.setLoanAccountId(account.getLoanAccountId());

		repayment.setEmiId(installment.getEmiId());

		repayment.setPaymentReference(
				"PAY-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase());

		repayment.setAmount(request.getAmount());

		repayment.setPaymentDate(LocalDateTime.now());

		repayment.setPaymentMethod(request.getPaymentMethod());

		repayment.setStatus(RepaymentStatus.SUCCESS);

		repayment.setRecordedBy(recordedBy);

		repayment.setRemarks(request.getRemarks());

		Repayment saved = repaymentRepository.save(repayment);

		installment.setAmountPaid(installment.getEmiAmount());

		installment.setStatus(EmiStatus.PAID);

		installment.setPaidAt(LocalDateTime.now());

		installmentRepository.save(installment);

		BigDecimal newOutstanding = account.getOutstandingAmount().subtract(request.getAmount())
				.setScale(2, java.math.RoundingMode.HALF_UP);

		if (newOutstanding.compareTo(BigDecimal.ZERO) < 0) {
			newOutstanding = BigDecimal.ZERO.setScale(2);
		}

		account.setOutstandingAmount(newOutstanding);

		boolean hasUnpaid = installmentRepository.existsByLoanAccountIdAndStatusNot(account.getLoanAccountId(),
				EmiStatus.PAID);

		if (!hasUnpaid) {

			account.setStatus(LoanAccountStatus.CLOSED);

			account.setOutstandingAmount(BigDecimal.ZERO.setScale(2));
		}

		loanAccountRepository.save(account);

		return toResponse(saved, installment.getInstallmentNumber());
	}

	@Override
	@Transactional(readOnly = true)
	public List<RepaymentResponse> getByApplicationId(Long applicationId) {

		LoanAccount account = ownership.byApplicationId(applicationId);

		return repaymentRepository.findByLoanAccountIdOrderByPaymentDateDesc(account.getLoanAccountId()).stream()
				.map(repayment -> {

					EmiInstallment installment = installmentRepository.findById(repayment.getEmiId()).orElse(null);

					Integer installmentNumber = installment == null ? null : installment.getInstallmentNumber();

					return toResponse(repayment, installmentNumber);
				}).toList();
	}

	@Override
	@Transactional(readOnly = true)
	public RepaymentResponse getById(Long repaymentId) {

		Repayment repayment = repaymentRepository.findById(repaymentId)
				.orElseThrow(() -> new InvalidRepaymentException("Repayment not found: " + repaymentId));

		ownership.byId(repayment.getLoanAccountId(), true);

		EmiInstallment installment = installmentRepository.findById(repayment.getEmiId()).orElse(null);

		return toResponse(repayment, installment == null ? null : installment.getInstallmentNumber());
	}

	private RepaymentResponse toResponse(Repayment repayment, Integer installmentNumber) {
		return new RepaymentResponse(
				repayment.getRepaymentId(),
				repayment.getLoanAccountId(),
				repayment.getEmiId(),
				installmentNumber,
				repayment.getPaymentReference(),
				repayment.getAmount(),
				repayment.getPaymentDate(),
				repayment.getPaymentMethod(),
				repayment.getStatus(),
				repayment.getRecordedBy(),
				repayment.getRemarks());
	}
}