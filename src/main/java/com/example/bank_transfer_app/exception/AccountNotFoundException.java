package com.example.bank_transfer_app.exception;

public class AccountNotFoundException extends BankException {
    public AccountNotFoundException(String account) {
        super("Account not found: " + account);
    }
}