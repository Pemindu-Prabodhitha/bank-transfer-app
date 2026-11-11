package com.example.bank_transfer_app.service;

import com.example.bank_transfer_app.exception.AccountNotFoundException;
import com.example.bank_transfer_app.exception.BankException;
import com.example.bank_transfer_app.exception.InsufficientBalanceException;
import com.example.bank_transfer_app.model.BankTransaction;
import com.example.bank_transfer_app.model.Customer;
import com.example.bank_transfer_app.model.TransactionStatus;
import com.example.bank_transfer_app.repository.BankTransactionRepository;
import com.example.bank_transfer_app.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)   
public class BankService {

    private final CustomerRepository customerRepository;
    private final BankTransactionRepository transactionRepository;

    public List<BankTransaction> largeTransfers(BigDecimal min) {
    return transactionRepository.findLargeTransfers(min);
    }

    public List<Customer> getCustomers(String keyword) {
        return (keyword == null || keyword.isBlank())
                ? customerRepository.findAllOrdered()
                : customerRepository.searchByName(keyword.trim());
    }

    public Customer getCustomer(String accountNumber) {
        return customerRepository.getByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));
    }

    public BigDecimal getTotalBalance() {
        BigDecimal total = customerRepository.getTotalBalance();
        return total == null ? BigDecimal.ZERO : total;
    }

    public long countCustomers() { return customerRepository.countCustomersNative(); }
    public long count(TransactionStatus s) { return transactionRepository.countByStatus(s); }
    public List<BankTransaction> latestTransactions() { return transactionRepository.findAllLatest(); }
    public List<BankTransaction> history(String account) { return transactionRepository.findHistory(account); }

    @Transactional   
    public Customer createCustomer(String accountNumber, String fullName, String email, BigDecimal balance) {
        if (customerRepository.countAccounts(accountNumber) > 0)
            throw new BankException("Account number " + accountNumber + " already exists");
        if (email != null && !email.isBlank() && customerRepository.countEmails(email) > 0)
            throw new BankException("Email " + email + " already exists");
        return customerRepository.save(new Customer(accountNumber, fullName,
                (email == null || email.isBlank()) ? null : email, balance));
    }

    
    @Transactional(rollbackFor = Exception.class)
    public BankTransaction transfer(String from, String to, BigDecimal amount, boolean simulateFailure) {

        
        if (from.equals(to))
            throw new BankException("Sender and receiver must be different accounts");
        if (customerRepository.countAccounts(from) == 0) throw new AccountNotFoundException(from);
        if (customerRepository.countAccounts(to) == 0)   throw new AccountNotFoundException(to);

        
        if (customerRepository.debit(from, amount) == 0)
            throw new InsufficientBalanceException(from);

        
        if (simulateFailure)
            throw new BankException("Simulated system failure after the withdrawal. All changes were rolled back.");

        
        customerRepository.credit(to, amount);

        
        return transactionRepository.save(
                new BankTransaction(from, to, amount, TransactionStatus.SUCCESS, "Transfer completed"));
    }
}
