package com.example.bank_transfer_app.config;

import com.example.bank_transfer_app.model.Customer;
import com.example.bank_transfer_app.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final CustomerRepository repo;

    @Override
    public void run(String... args) {
        if (repo.countCustomersNative() == 0) {
            repo.save(new Customer("100001", "Pemindu Prabodhitha", "pemindu@example.com", new BigDecimal("50000.00")));
            repo.save(new Customer("100002", "Dananja Milindu", "dananja@example.com", new BigDecimal("25000.00")));
            repo.save(new Customer("100003", "Chamuditha Kasun", "chamuditha@example.com", new BigDecimal("10000.00")));
            repo.save(new Customer("999999", "Bank Fees", "fees@bank.com", BigDecimal.ZERO));
        }
    }
}
