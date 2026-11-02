package com.cfs.emi.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import com.cfs.emi.client.CustomerServiceClient;
import com.cfs.emi.dto.RepaymentRequest;
import com.cfs.emi.entity.EmiInstallment;
import com.cfs.emi.entity.EmiStatus;
import com.cfs.emi.entity.LoanAccount;
import com.cfs.emi.entity.LoanAccountStatus;
import com.cfs.emi.exception.AlreadyPaidRepaymentException;
import com.cfs.emi.exception.GlobalExceptionHandler;
import com.cfs.emi.repository.EmiInstallmentRepository;
import com.cfs.emi.repository.LoanAccountRepository;
import com.cfs.emi.repository.RepaymentRepository;

class RepaymentIntegrityTest {
    private final LoanAccountRepository loans = mock(LoanAccountRepository.class);
    private final EmiInstallmentRepository installments = mock(EmiInstallmentRepository.class);
    private final RepaymentRepository repayments = mock(RepaymentRepository.class);
    private final LoanAccountOwnership ownership =
            new LoanAccountOwnership(loans, mock(CustomerServiceClient.class));
    private final RepaymentServiceImpl service = new RepaymentServiceImpl(loans, installments, repayments, ownership);

    @AfterEach
    void clearSecurity() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void locksAccountBeforeLoadingInstallmentAndClosesAtExactZeroAfterFinalPayment() {
        authenticateAdmin();
        LoanAccount account = account();
        EmiInstallment installment = installment(EmiStatus.PENDING);
        when(loans.findByIdForUpdate(10L)).thenReturn(Optional.of(account));
        when(installments.findById(20L)).thenReturn(Optional.of(installment));
        when(installments.existsByLoanAccountIdAndStatusNot(10L, EmiStatus.PAID)).thenReturn(false);
        when(repayments.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.recordRepayment(request(), "admin");

        InOrder order = inOrder(loans, installments);
        order.verify(loans).findByIdForUpdate(10L);
        order.verify(installments).findById(20L);
        assertEquals(LoanAccountStatus.CLOSED, account.getStatus());
        assertEquals(new BigDecimal("0.00"), account.getOutstandingAmount());
        assertEquals(EmiStatus.PAID, installment.getStatus());
        verify(repayments).save(any());
        verify(loans).save(account);
    }

    @Test
    void alreadyPaidInstallmentReturnsConflictWithoutSecondPaymentOrAccountWrite() {
        authenticateAdmin();
        when(loans.findByIdForUpdate(10L)).thenReturn(Optional.of(account()));
        when(installments.findById(20L)).thenReturn(Optional.of(installment(EmiStatus.PAID)));

        assertThrows(AlreadyPaidRepaymentException.class, () -> service.recordRepayment(request(), "admin"));
        assertEquals(409, new GlobalExceptionHandler()
                .handleAlreadyPaid(new AlreadyPaidRepaymentException()).getStatusCode().value());
        verify(repayments, never()).save(any());
        verify(installments, never()).save(any());
        verify(loans, never()).save(any());
    }

    private static LoanAccount account() {
        LoanAccount account = new LoanAccount();
        account.setLoanAccountId(10L);
        account.setCustomerId(99L);
        account.setOutstandingAmount(new BigDecimal("33.33"));
        account.setStatus(LoanAccountStatus.ACTIVE);
        return account;
    }

    private static EmiInstallment installment(EmiStatus status) {
        EmiInstallment installment = new EmiInstallment();
        installment.setEmiId(20L);
        installment.setLoanAccountId(10L);
        installment.setInstallmentNumber(1);
        installment.setEmiAmount(new BigDecimal("33.33"));
        installment.setAmountPaid(status == EmiStatus.PAID ? new BigDecimal("33.33") : new BigDecimal("0.00"));
        installment.setStatus(status);
        return installment;
    }

    private static RepaymentRequest request() {
        RepaymentRequest request = new RepaymentRequest();
        request.setLoanAccountId(10L);
        request.setEmiId(20L);
        request.setAmount(new BigDecimal("33.33"));
        return request;
    }

    private static void authenticateAdmin() {
        Jwt jwt = Jwt.withTokenValue("admin-token").header("alg", "HS256").subject("admin").build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
    }
}
