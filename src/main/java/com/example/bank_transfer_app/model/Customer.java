package com.example.bank_transfer_app.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "customers")
@Getter @Setter @NoArgsConstructor
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_number", nullable = false, unique = true, length = 20)
    private String accountNumber;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(unique = true)
    private String email;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    public Customer(String accountNumber, String fullName, String email, BigDecimal balance) {
        this.accountNumber = accountNumber;
        this.fullName = fullName;
        this.email = email;
        this.balance = balance;
    }
}