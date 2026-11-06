package com.acme.salary.repository;

import com.acme.salary.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Page<Payment> findByEmployeeCode(String employeeCode, Pageable pageable);

    boolean existsByEmployeeCodeAndPeriodStartLessThanEqualAndPeriodEndGreaterThanEqual(
            String employeeCode, LocalDate onOrAfterPeriodStart, LocalDate onOrBeforePeriodEnd);
}
