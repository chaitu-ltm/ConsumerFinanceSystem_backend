package com.cfs.applicationservice.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cfs.applicationservice.dto
        .LoanProductRequest;

import com.cfs.applicationservice.dto
        .LoanProductResponse;

import com.cfs.applicationservice.service
        .LoanProductService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/loan-products")
public class LoanProductController {

    private final LoanProductService
            loanProductService;


    public LoanProductController(
            LoanProductService loanProductService) {

        this.loanProductService =
                loanProductService;
    }


    @PostMapping
    public ResponseEntity<LoanProductResponse>
            createProduct(

            @Valid
            @RequestBody
            LoanProductRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                    loanProductService
                        .createProduct(request)
                );
    }


    @GetMapping
    public ResponseEntity<List<LoanProductResponse>>
            getActiveProducts() {

        return ResponseEntity.ok(
                loanProductService
                    .getActiveProducts()
        );
    }


    @GetMapping("/{productId}")
    public ResponseEntity<LoanProductResponse>
            getProduct(

            @PathVariable
            Long productId) {

        return ResponseEntity.ok(
                loanProductService
                    .getProduct(productId)
        );
    }
}