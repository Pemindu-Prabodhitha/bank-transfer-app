package com.example.bank_transfer_app.repository;

import com.example.bank_transfer_app.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    // 1. JPQL + @Param
    @Query("SELECT c FROM Customer c WHERE c.accountNumber = :accountNumber")
    Optional<Customer> getByAccountNumber(@Param("accountNumber") String accountNumber);

    // 2. JPQL COUNT
    @Query("SELECT COUNT(c) FROM Customer c WHERE c.accountNumber = :accountNumber")
    long countAccounts(@Param("accountNumber") String accountNumber);

    @Query("SELECT COUNT(c) FROM Customer c WHERE LOWER(c.email) = LOWER(:email)")
    long countEmails(@Param("email") String email);

    // 3. JPQL ORDER BY
    @Query("SELECT c FROM Customer c ORDER BY c.fullName")
    List<Customer> findAllOrdered();

    // 4. JPQL LIKE (case-insensitive)
    @Query("SELECT c FROM Customer c WHERE LOWER(c.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY c.fullName")
    List<Customer> searchByName(@Param("keyword") String keyword);

    // 5. JPQL aggregate (null if no customers)
    @Query("SELECT SUM(c.balance) FROM Customer c")
    BigDecimal getTotalBalance();

    // 6. Native SQL
    @Query(value = "SELECT COUNT(*) FROM customers", nativeQuery = true)
    long countCustomersNative();

    // 7. UPDATE with condition (returns 0 if balance too low)
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Customer c SET c.balance = c.balance - :amount " +
           "WHERE c.accountNumber = :accountNumber AND c.balance >= :amount")
    int debit(@Param("accountNumber") String accountNumber, @Param("amount") BigDecimal amount);

    // 8. UPDATE
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Customer c SET c.balance = c.balance + :amount WHERE c.accountNumber = :accountNumber")
    int credit(@Param("accountNumber") String accountNumber, @Param("amount") BigDecimal amount);
}