package com.cfs.customerservice.dto;

public class BankAccountResponse {

    private Long bankAccountId;

    private Long customerId;

    private String accountHolderName;

    private String bankName;

    private String accountNumber;

    private String ifscCode;

    private String accountType;

    private boolean primaryAccount;


    public BankAccountResponse() {
    }


    public BankAccountResponse(
            Long bankAccountId,
            Long customerId,
            String accountHolderName,
            String bankName,
            String accountNumber,
            String ifscCode,
            String accountType,
            boolean primaryAccount) {

        this.bankAccountId = bankAccountId;
        this.customerId = customerId;
        this.accountHolderName = accountHolderName;
        this.bankName = bankName;
        this.accountNumber = accountNumber;
        this.ifscCode = ifscCode;
        this.accountType = accountType;
        this.primaryAccount = primaryAccount;
    }


    public Long getBankAccountId() {
        return bankAccountId;
    }

    public void setBankAccountId(Long bankAccountId) {
        this.bankAccountId = bankAccountId;
    }


    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }


    public String getAccountHolderName() {
        return accountHolderName;
    }

    public void setAccountHolderName(String accountHolderName) {
        this.accountHolderName = accountHolderName;
    }


    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }


    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }


    public String getIfscCode() {
        return ifscCode;
    }

    public void setIfscCode(String ifscCode) {
        this.ifscCode = ifscCode;
    }


    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }


    public boolean isPrimaryAccount() {
        return primaryAccount;
    }

    public void setPrimaryAccount(boolean primaryAccount) {
        this.primaryAccount = primaryAccount;
    }
}
