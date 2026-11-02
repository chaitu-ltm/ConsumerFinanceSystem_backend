package com.cfs.emi.service;

import java.util.List;

import com.cfs.emi.dto.RepaymentRequest;
import com.cfs.emi.dto.RepaymentResponse;

public interface RepaymentService {

	RepaymentResponse recordRepayment(RepaymentRequest request, String recordedBy);

	List<RepaymentResponse> getByApplicationId(Long applicationId);

	RepaymentResponse getById(Long repaymentId);
}