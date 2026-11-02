package com.cfs.emi.service;

import java.math.BigDecimal;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import com.cfs.emi.client.CustomerServiceClient;
import com.cfs.emi.dto.CustomerIdentityResponse;
import com.cfs.emi.entity.LoanAccount;
import com.cfs.emi.exception.CustomerLookupUnavailableException;
import com.cfs.emi.exception.LoanAccountNotFoundException;
import com.cfs.emi.repository.LoanAccountRepository;

import feign.FeignException;

@Component
public class LoanAccountOwnership {
    private final LoanAccountRepository repository;
    private final CustomerServiceClient customerClient;

    public LoanAccountOwnership(LoanAccountRepository repository, CustomerServiceClient customerClient) {
        this.repository = repository;
        this.customerClient = customerClient;
    }

    public LoanAccount byId(Long id, boolean readOnly) {
        LoanAccount account = repository.findById(id)
                .orElseThrow(() -> new LoanAccountNotFoundException("Loan account not found: " + id));
        assertOwned(account, readOnly);
        return account;
    }

    public LoanAccount byIdForUpdate(Long id, boolean readOnly) {
        LoanAccount account = repository.findByIdForUpdate(id)
                .orElseThrow(() -> new LoanAccountNotFoundException("Loan account not found: " + id));
        assertOwned(account, readOnly);
        return account;
    }

    public LoanAccount byApplicationId(Long id) {
        LoanAccount account = repository.findByApplicationId(id)
                .orElseThrow(() -> new LoanAccountNotFoundException("Loan account not found for application ID: " + id));
        assertOwned(account, true);
        return account;
    }

    private void assertOwned(LoanAccount account, boolean readOnly) {
        if (!(SecurityContextHolder.getContext().getAuthentication() instanceof JwtAuthenticationToken jwt)
                || !jwt.isAuthenticated()) {
            throw new AccessDeniedException("Authenticated JWT is required");
        }
        boolean admin = jwt.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean officer = jwt.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_LOAN_OFFICER"));
        if (admin || (readOnly && officer)) {
            return;
        }
        if (jwt.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER"))) {
            throw new AccessDeniedException("Customer access is required");
        }
        Long userId = userId(jwt);
        CustomerIdentityResponse identity;
        try {
            identity = customerClient.getByUserId(userId);
        } catch (FeignException exception) {
            if (exception.status() == 401 || exception.status() == 403 || exception.status() == 404) {
                throw new AccessDeniedException("Customer ownership could not be verified");
            }
            throw new CustomerLookupUnavailableException(exception);
        } catch (AccessDeniedException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new CustomerLookupUnavailableException(exception);
        }
        if (identity == null || !userId.equals(identity.getUserId()) || identity.getCustomerId() == null
                || !identity.getCustomerId().equals(account.getCustomerId())) {
            throw new AccessDeniedException("You can only access your own loan account");
        }
    }

    private Long userId(JwtAuthenticationToken jwt) {
        Object claim = jwt.getToken().getClaim("userId");
        try {
            if (claim instanceof Number number) {
                return new BigDecimal(number.toString()).longValueExact();
            }
            if (claim instanceof String text) {
                return Long.valueOf(text.trim());
            }
        } catch (NumberFormatException | ArithmeticException exception) {
            throw new AccessDeniedException("JWT userId must be an integer", exception);
        }
        throw new AccessDeniedException("JWT userId is required");
    }
}
