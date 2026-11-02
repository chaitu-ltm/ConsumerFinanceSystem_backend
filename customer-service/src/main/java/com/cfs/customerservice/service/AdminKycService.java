package com.cfs.customerservice.service;

import java.util.List;

import com.cfs.customerservice.dto.AdminKycResponse;
import com.cfs.customerservice.dto.KycActionResponse;
import com.cfs.customerservice.dto.KycRejectRequest;

public interface AdminKycService {

    List<AdminKycResponse> getAllKyc();

    List<AdminKycResponse> getPendingKyc();

    AdminKycResponse getKycDetails(
            Long customerId
    );

    KycActionResponse verifyKyc(
            Long customerId
    );

    KycActionResponse rejectKyc(
            Long customerId,
            KycRejectRequest request
    );
    
}