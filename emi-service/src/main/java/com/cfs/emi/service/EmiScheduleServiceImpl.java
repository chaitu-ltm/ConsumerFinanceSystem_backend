package com.cfs.emi.service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;

import com.cfs.emi.client.DisbursementClient;
import com.cfs.emi.dto.DisbursementResponse;
import com.cfs.emi.dto.EmiInstallmentResponse;
import com.cfs.emi.dto.GenerateEmiScheduleRequest;
import com.cfs.emi.dto.LoanAccountResponse;
import com.cfs.emi.entity.EmiInstallment;
import com.cfs.emi.entity.EmiStatus;
import com.cfs.emi.entity.LoanAccount;
import com.cfs.emi.entity.LoanAccountStatus;
import com.cfs.emi.exception.DisbursementLookupUnavailableException;
import com.cfs.emi.exception.DisbursementNotFoundException;
import com.cfs.emi.exception.DisbursementVerificationException;
import com.cfs.emi.exception.DuplicateEmiScheduleException;
import com.cfs.emi.exception.InvalidEmiScheduleRequestException;
import com.cfs.emi.repository.EmiInstallmentRepository;
import com.cfs.emi.repository.LoanAccountRepository;

import feign.FeignException;

@Service
@Transactional
public class EmiScheduleServiceImpl implements EmiScheduleService {

	private static final MathContext CALCULATION_CONTEXT = MathContext.DECIMAL128;
	private static final BigDecimal MONEY_ZERO = new BigDecimal("0.00");
	private static final BigDecimal TWELVE_HUNDRED = new BigDecimal("1200");
	private final LoanAccountRepository loanAccountRepository;
	private final EmiInstallmentRepository installmentRepository;
	private final LoanAccountOwnership ownership;
	private final DisbursementClient disbursementClient;

	public EmiScheduleServiceImpl(LoanAccountRepository loanAccountRepository,
			EmiInstallmentRepository installmentRepository, LoanAccountOwnership ownership,
			DisbursementClient disbursementClient) {

		this.loanAccountRepository = loanAccountRepository;
		this.installmentRepository = installmentRepository;
		this.ownership = ownership;
		this.disbursementClient = disbursementClient;
	}

	@Override
	public LoanAccountResponse generateSchedule(GenerateEmiScheduleRequest request) {

		if (loanAccountRepository.existsByApplicationId(request.getApplicationId())) {
			throw new DuplicateEmiScheduleException(
					"EMI schedule already exists for application ID: " + request.getApplicationId());
		}

		if (loanAccountRepository.existsByDisbursementId(request.getDisbursementId())) {
			throw new DuplicateEmiScheduleException(
					"EMI schedule already exists for disbursement ID: " + request.getDisbursementId());
		}

		BigDecimal principal = normalizePrincipal(request.getPrincipalAmount());
		BigDecimal annualRate = normalizeRate(request.getAnnualInterestRate());
		verifyDisbursement(request, principal);
		BigDecimal monthlyEmi = calculateEmi(principal, annualRate, request.getTenureMonths());

		LoanAccount loanAccount = new LoanAccount();

		loanAccount.setApplicationId(request.getApplicationId());

		loanAccount.setCustomerId(request.getCustomerId());

		loanAccount.setDisbursementId(request.getDisbursementId());

		loanAccount.setPrincipalAmount(principal);

		loanAccount.setAnnualInterestRate(annualRate);

		loanAccount.setTenureMonths(request.getTenureMonths());

		loanAccount.setMonthlyEmiAmount(monthlyEmi);
		loanAccount.setTotalPayableAmount(MONEY_ZERO);
		loanAccount.setOutstandingAmount(MONEY_ZERO);

		loanAccount.setStartDate(request.getFirstDueDate().minusMonths(1));

		loanAccount.setMaturityDate(request.getFirstDueDate().plusMonths(request.getTenureMonths() - 1L));

		loanAccount.setStatus(LoanAccountStatus.ACTIVE);

		LoanAccount savedLoan = loanAccountRepository.save(loanAccount);
		BigDecimal totalPayable = generateInstallments(savedLoan, request.getFirstDueDate());
		savedLoan.setTotalPayableAmount(totalPayable);
		savedLoan.setOutstandingAmount(totalPayable);
		loanAccountRepository.save(savedLoan);

		return toLoanAccountResponse(savedLoan);
	}

	@Override
	@Transactional(readOnly = true)
	public List<EmiInstallmentResponse> getInstallments(Long loanAccountId) {

		ownership.byId(loanAccountId, true);

		return installmentRepository.findByLoanAccountIdOrderByInstallmentNumberAsc(loanAccountId).stream()
				.map(this::toInstallmentResponse).toList();
	}

	@Override
	@Transactional(readOnly = true)
	public LoanAccountResponse getByApplicationId(Long applicationId) {

		LoanAccount account = ownership.byApplicationId(applicationId);

		return toLoanAccountResponse(account);
	}

	private BigDecimal normalizePrincipal(BigDecimal principal) {
		if (principal == null || principal.signum() <= 0 || principal.stripTrailingZeros().scale() > 2
				|| principal.compareTo(new BigDecimal("9999999999999.99")) > 0) {
			throw new InvalidEmiScheduleRequestException(
					"Principal must be positive, fit the stored money precision, and have at most 2 decimal places");
		}
		return principal.setScale(2, RoundingMode.UNNECESSARY);
	}

	private BigDecimal normalizeRate(BigDecimal annualInterestRate) {
		if (annualInterestRate == null || annualInterestRate.signum() < 0
				|| annualInterestRate.stripTrailingZeros().scale() > 4
				|| annualInterestRate.compareTo(new BigDecimal("999.9999")) > 0) {
			throw new InvalidEmiScheduleRequestException(
					"Annual interest rate must fit the stored rate precision and have at most 4 decimal places");
		}
		return annualInterestRate.setScale(4, RoundingMode.UNNECESSARY);
	}

	private void verifyDisbursement(GenerateEmiScheduleRequest request, BigDecimal principal) {
		DisbursementResponse disbursement;
		try {
			disbursement = disbursementClient.getById(request.getDisbursementId());
		} catch (FeignException exception) {
			if (exception.status() == 404) {
				throw new DisbursementNotFoundException();
			}
			throw new DisbursementLookupUnavailableException(exception);
		} catch (AccessDeniedException exception) {
			throw exception;
		} catch (RuntimeException exception) {
			throw new DisbursementLookupUnavailableException(exception);
		}

		if (disbursement == null) {
			throw new DisbursementNotFoundException();
		}
		if (!request.getDisbursementId().equals(disbursement.getDisbursementId())
				|| !request.getApplicationId().equals(disbursement.getApplicationId())
				|| !"DISBURSED".equals(disbursement.getStatus())
				|| disbursement.getAmount() == null
				|| disbursement.getAmount().compareTo(principal) != 0) {
			throw new DisbursementVerificationException();
		}
	}

	private BigDecimal calculateEmi(BigDecimal principal, BigDecimal annualInterestRate, int tenureMonths) {
		BigDecimal monthlyRate = annualInterestRate.divide(TWELVE_HUNDRED, CALCULATION_CONTEXT);
		if (monthlyRate.signum() == 0) {
			return principal.divide(BigDecimal.valueOf(tenureMonths), 2, RoundingMode.HALF_UP);
		}
		BigDecimal factor = BigDecimal.ONE.add(monthlyRate, CALCULATION_CONTEXT)
				.pow(tenureMonths, CALCULATION_CONTEXT);
		BigDecimal numerator = principal.multiply(monthlyRate, CALCULATION_CONTEXT)
				.multiply(factor, CALCULATION_CONTEXT);
		BigDecimal exactEmi = numerator.divide(factor.subtract(BigDecimal.ONE, CALCULATION_CONTEXT),
				CALCULATION_CONTEXT);
		return exactEmi.setScale(2, RoundingMode.HALF_UP);
	}

	private BigDecimal generateInstallments(LoanAccount loanAccount, LocalDate firstDueDate) {

		BigDecimal balance = loanAccount.getPrincipalAmount().setScale(2, RoundingMode.UNNECESSARY);

		BigDecimal monthlyRate = loanAccount.getAnnualInterestRate().divide(TWELVE_HUNDRED, CALCULATION_CONTEXT);
		BigDecimal totalPayable = MONEY_ZERO;

		LocalDate dueDate = firstDueDate;

		for (int installmentNumber = 1; installmentNumber <= loanAccount.getTenureMonths(); installmentNumber++) {

			BigDecimal interest = balance.signum() == 0 ? MONEY_ZERO
					: balance.multiply(monthlyRate, CALCULATION_CONTEXT).setScale(2, RoundingMode.HALF_UP);
			BigDecimal principalComponent = loanAccount.getMonthlyEmiAmount().subtract(interest)
					.setScale(2, RoundingMode.HALF_UP);
			if (principalComponent.signum() < 0) {
				principalComponent = MONEY_ZERO;
			}
			if (principalComponent.compareTo(balance) > 0) {
				principalComponent = balance;
			}

			if (installmentNumber == loanAccount.getTenureMonths()) {
				principalComponent = balance;
			}

			BigDecimal installmentAmount = principalComponent.add(interest).setScale(2, RoundingMode.HALF_UP);

			EmiInstallment installment = new EmiInstallment();

			installment.setLoanAccountId(loanAccount.getLoanAccountId());

			installment.setInstallmentNumber(installmentNumber);

			installment.setDueDate(dueDate);

			installment.setOpeningPrincipal(balance);

			installment.setPrincipalComponent(principalComponent);

			installment.setInterestComponent(interest);

			installment.setEmiAmount(installmentAmount);

			installment.setAmountPaid(BigDecimal.ZERO.setScale(2));

			installment.setStatus(installmentAmount.signum() == 0 ? EmiStatus.PAID : EmiStatus.PENDING);

			installmentRepository.save(installment);
			totalPayable = totalPayable.add(installmentAmount).setScale(2, RoundingMode.HALF_UP);

			balance = balance.subtract(principalComponent).setScale(2, RoundingMode.UNNECESSARY);

			dueDate = dueDate.plusMonths(1);
		}
		return totalPayable;
	}

	private LoanAccountResponse toLoanAccountResponse(LoanAccount loanAccount) {

		LoanAccountResponse response = new LoanAccountResponse();

		response.setLoanAccountId(loanAccount.getLoanAccountId());

		response.setApplicationId(loanAccount.getApplicationId());

		response.setCustomerId(loanAccount.getCustomerId());

		response.setDisbursementId(loanAccount.getDisbursementId());

		response.setPrincipalAmount(loanAccount.getPrincipalAmount());

		response.setAnnualInterestRate(loanAccount.getAnnualInterestRate());

		response.setTenureMonths(loanAccount.getTenureMonths());

		response.setMonthlyEmiAmount(loanAccount.getMonthlyEmiAmount());

		response.setTotalPayableAmount(loanAccount.getTotalPayableAmount());

		response.setOutstandingAmount(loanAccount.getOutstandingAmount());

		response.setStartDate(loanAccount.getStartDate());

		response.setMaturityDate(loanAccount.getMaturityDate());

		response.setStatus(loanAccount.getStatus());

		return response;
	}

	private EmiInstallmentResponse toInstallmentResponse(EmiInstallment installment) {

		EmiInstallmentResponse response = new EmiInstallmentResponse();

		response.setEmiId(installment.getEmiId());

		response.setLoanAccountId(installment.getLoanAccountId());

		response.setInstallmentNumber(installment.getInstallmentNumber());

		response.setDueDate(installment.getDueDate());

		response.setOpeningPrincipal(installment.getOpeningPrincipal());

		response.setPrincipalComponent(installment.getPrincipalComponent());

		response.setInterestComponent(installment.getInterestComponent());

		response.setEmiAmount(installment.getEmiAmount());

		response.setAmountPaid(installment.getAmountPaid());

		response.setStatus(installment.getStatus());

		response.setPaidAt(installment.getPaidAt());

		return response;
	}

}