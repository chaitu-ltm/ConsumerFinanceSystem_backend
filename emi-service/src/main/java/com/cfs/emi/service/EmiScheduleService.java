package com.cfs.emi.service;

import java.util.List;

import com.cfs.emi.dto.EmiInstallmentResponse;
import com.cfs.emi.dto.GenerateEmiScheduleRequest;
import com.cfs.emi.dto.LoanAccountResponse;

public interface EmiScheduleService {

	LoanAccountResponse generateSchedule(GenerateEmiScheduleRequest request);

	LoanAccountResponse getByApplicationId(Long applicationId);

	List<EmiInstallmentResponse> getInstallments(Long loanAccountId);
}