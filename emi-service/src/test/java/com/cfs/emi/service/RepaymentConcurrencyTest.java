package com.cfs.emi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.cfs.emi.dto.RepaymentRequest;
import com.cfs.emi.entity.EmiInstallment;
import com.cfs.emi.entity.EmiStatus;
import com.cfs.emi.entity.LoanAccount;
import com.cfs.emi.entity.LoanAccountStatus;
import com.cfs.emi.exception.AlreadyPaidRepaymentException;
import com.cfs.emi.repository.EmiInstallmentRepository;
import com.cfs.emi.repository.LoanAccountRepository;
import com.cfs.emi.repository.RepaymentRepository;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

@SpringBootTest(
        properties = {
                "spring.datasource.url=jdbc:h2:mem:emi-repayment-lock;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000",
                "spring.datasource.username=sa",
                "spring.datasource.password=",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "spring.cloud.discovery.enabled=false",
                "eureka.client.enabled=false",
                "security.jwt.secret-base64=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA="
        })
class RepaymentConcurrencyTest {
    @Autowired private RepaymentService service;
    @Autowired private LoanAccountRepository loans;
    @Autowired private EmiInstallmentRepository installments;
    @Autowired private RepaymentRepository repayments;
    @Autowired private PlatformTransactionManager transactionManager;

    @Test
    void concurrentFullPaymentsSerializeOnRealJpaAccountLockAndOnlyOneWins() throws Exception {
        long accountId = insertSchedule();
        long emiId = installments.findByLoanAccountIdOrderByInstallmentNumberAsc(accountId).getFirst().getEmiId();
        ExecutorService workers = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        try {
            Future<String> first = workers.submit(() -> payTogether(accountId, emiId, ready, start));
            Future<String> second = workers.submit(() -> payTogether(accountId, emiId, ready, start));
            if (!ready.await(5, TimeUnit.SECONDS)) {
                throw new AssertionError("Payment workers did not become ready");
            }
            start.countDown();

            List<String> outcomes = List.of(first.get(15, TimeUnit.SECONDS), second.get(15, TimeUnit.SECONDS));
            assertEquals(1, outcomes.stream().filter("PAID"::equals).count());
            assertEquals(1, outcomes.stream().filter("ALREADY_PAID"::equals).count());

            TransactionTemplate transaction = new TransactionTemplate(transactionManager);
            transaction.executeWithoutResult(status -> {
                LoanAccount account = loans.findById(accountId).orElseThrow();
                EmiInstallment installment = installments.findByLoanAccountIdOrderByInstallmentNumberAsc(accountId)
                        .getFirst();
                assertEquals(1, repayments.findByLoanAccountIdOrderByPaymentDateDesc(accountId).size());
                assertEquals(EmiStatus.PAID, installment.getStatus());
                assertEquals(LoanAccountStatus.CLOSED, account.getStatus());
                assertEquals(new BigDecimal("0.00"), account.getOutstandingAmount());
            });
        } finally {
            start.countDown();
            workers.shutdownNow();
        }
    }

    private long insertSchedule() {
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);
        return transaction.execute(status -> {
            LoanAccount account = new LoanAccount();
            account.setApplicationId(System.nanoTime());
            account.setCustomerId(700L);
            account.setDisbursementId(System.nanoTime());
            account.setPrincipalAmount(new BigDecimal("10.00"));
            account.setAnnualInterestRate(new BigDecimal("0.0000"));
            account.setTenureMonths(1);
            account.setMonthlyEmiAmount(new BigDecimal("10.00"));
            account.setTotalPayableAmount(new BigDecimal("10.00"));
            account.setOutstandingAmount(new BigDecimal("10.00"));
            account.setStartDate(LocalDate.of(2026, 10, 1));
            account.setMaturityDate(LocalDate.of(2026, 11, 1));
            account.setStatus(LoanAccountStatus.ACTIVE);
            LoanAccount saved = loans.saveAndFlush(account);

            EmiInstallment installment = new EmiInstallment();
            installment.setLoanAccountId(saved.getLoanAccountId());
            installment.setInstallmentNumber(1);
            installment.setDueDate(LocalDate.of(2026, 11, 1));
            installment.setOpeningPrincipal(new BigDecimal("10.00"));
            installment.setPrincipalComponent(new BigDecimal("10.00"));
            installment.setInterestComponent(new BigDecimal("0.00"));
            installment.setEmiAmount(new BigDecimal("10.00"));
            installment.setAmountPaid(new BigDecimal("0.00"));
            installment.setStatus(EmiStatus.PENDING);
            installments.saveAndFlush(installment);
            return saved.getLoanAccountId();
        });
    }

    private String payTogether(long accountId, long emiId, CountDownLatch ready, CountDownLatch start)
            throws Exception {
        ready.countDown();
        if (!start.await(5, TimeUnit.SECONDS)) {
            throw new AssertionError("Payment start signal timed out");
        }
        Jwt jwt = Jwt.withTokenValue("admin-token").header("alg", "HS256").subject("admin").build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
        try {
            RepaymentRequest request = new RepaymentRequest();
            request.setLoanAccountId(accountId);
            request.setEmiId(emiId);
            request.setAmount(new BigDecimal("10.00"));
            service.recordRepayment(request, "admin");
            return "PAID";
        } catch (AlreadyPaidRepaymentException exception) {
            return "ALREADY_PAID";
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
