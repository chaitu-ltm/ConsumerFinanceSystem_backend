package com.cfs.applicationservice.service;

import java.util.List;

import com.cfs.applicationservice.dto
        .LoanProductRequest;

import com.cfs.applicationservice.dto
        .LoanProductResponse;

public interface LoanProductService {

    LoanProductResponse createProduct(
            LoanProductRequest request
    );

    LoanProductResponse getProduct(
            Long productId
    );

    List<LoanProductResponse> getActiveProducts();
}