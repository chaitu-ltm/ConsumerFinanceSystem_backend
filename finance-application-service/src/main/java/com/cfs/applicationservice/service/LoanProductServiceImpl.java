package com.cfs.applicationservice.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cfs.applicationservice.dto
        .LoanProductRequest;

import com.cfs.applicationservice.dto
        .LoanProductResponse;

import com.cfs.applicationservice.entity
        .LoanProduct;

import com.cfs.applicationservice.exception
        .DuplicateResourceException;

import com.cfs.applicationservice.exception
        .ResourceNotFoundException;

import com.cfs.applicationservice.repository
        .LoanProductRepository;

@Service
@Transactional
public class LoanProductServiceImpl
        implements LoanProductService {

    private final LoanProductRepository
            loanProductRepository;


    public LoanProductServiceImpl(
            LoanProductRepository loanProductRepository) {

        this.loanProductRepository =
                loanProductRepository;
    }


    @Override
    public LoanProductResponse createProduct(
            LoanProductRequest request) {

        String productCode =
                request.getProductCode()
                        .trim()
                        .toUpperCase();

        if (loanProductRepository
                .existsByProductCode(productCode)) {

            throw new DuplicateResourceException(
                    "Loan product code already exists: "
                            + productCode
            );
        }

        if (request.getMinimumAmount()
                .compareTo(
                    request.getMaximumAmount()
                ) > 0) {

            throw new IllegalArgumentException(
                    "Minimum amount cannot exceed maximum amount"
            );
        }

        if (request.getMinimumTenureMonths()
                > request.getMaximumTenureMonths()) {

            throw new IllegalArgumentException(
                    "Minimum tenure cannot exceed maximum tenure"
            );
        }

        if (request.getMinimumInterestRate()
                .compareTo(
                    request.getMaximumInterestRate()
                ) > 0) {

            throw new IllegalArgumentException(
                    "Minimum interest rate cannot exceed maximum interest rate"
            );
        }

        LoanProduct product = new LoanProduct();

        product.setProductCode(productCode);

        product.setProductName(
                request.getProductName().trim()
        );

        product.setMinimumAmount(
                request.getMinimumAmount()
        );

        product.setMaximumAmount(
                request.getMaximumAmount()
        );

        product.setMinimumTenureMonths(
                request.getMinimumTenureMonths()
        );

        product.setMaximumTenureMonths(
                request.getMaximumTenureMonths()
        );

        product.setMinimumInterestRate(
                request.getMinimumInterestRate()
        );

        product.setMaximumInterestRate(
                request.getMaximumInterestRate()
        );

        product.setActive(request.isActive());

        LoanProduct saved =
                loanProductRepository.save(product);

        return mapProduct(saved);
    }


    @Override
    @Transactional(readOnly = true)
    public LoanProductResponse getProduct(
            Long productId) {

        return mapProduct(
                findProduct(productId)
        );
    }


    @Override
    @Transactional(readOnly = true)
    public List<LoanProductResponse>
            getActiveProducts() {

        return loanProductRepository
                .findByActiveTrue()
                .stream()
                .map(this::mapProduct)
                .toList();
    }


    private LoanProduct findProduct(
            Long productId) {

        return loanProductRepository
                .findById(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Loan product not found with ID: "
                                        + productId
                        )
                );
    }


    private LoanProductResponse mapProduct(
            LoanProduct product) {

        return new LoanProductResponse(
                product.getId(),
                product.getProductCode(),
                product.getProductName(),
                product.getMinimumAmount(),
                product.getMaximumAmount(),
                product.getMinimumTenureMonths(),
                product.getMaximumTenureMonths(),
                product.getMinimumInterestRate(),
                product.getMaximumInterestRate(),
                product.isActive()
        );
    }
}