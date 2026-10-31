package com.acme.salary.service.impl;

import com.acme.salary.dto.PaymentDto;
import com.acme.salary.entity.Employee;
import com.acme.salary.entity.Payment;
import com.acme.salary.entity.Salary;
import com.acme.salary.enums.PaidStatus;
import com.acme.salary.exception.ResourceNotFoundException;
import com.acme.salary.repository.EmployeeRepository;
import com.acme.salary.repository.PaymentRepository;
import com.acme.salary.repository.SalaryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTests {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private SalaryRepository salaryRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    private PaymentServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PaymentServiceImpl(paymentRepository, salaryRepository, employeeRepository);
    }

    private Salary salaryOfEmployee() {
        Employee employee = new Employee("P00001", "Asha", "Rao", "Finance", "Analyst",
                "Pune", "9876543210", "asha@example.com", "India");
        Salary salary = new Salary(LocalDate.of(2026, 1, 1), null, "INR",
                new BigDecimal("60000.00"), new BigDecimal("24000.00"), new BigDecimal("6000.00"),
                new BigDecimal("9000.00"), new BigDecimal("7200.00"));
        employee.addSalary(salary);
        return salary;
    }

    private Payment recordedPayment() {
        return Payment.record(salaryOfEmployee(), LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31),
                LocalDate.of(2026, 1, 31), LocalTime.of(10, 30));
    }

    private PaymentDto request(LocalDate start, LocalDate end) {
        PaymentDto dto = new PaymentDto();
        dto.setSalaryId(1L);
        dto.setPeriodStart(start);
        dto.setPeriodEnd(end);
        return dto;
    }

    @Test
    void getPaymentReturnsDtoWhenFound() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(recordedPayment()));

        PaymentDto dto = service.getPayment(1L);

        assertEquals("P00001", dto.getEmployeeCode());
        assertEquals(PaidStatus.PAID, dto.getPaidStatus());
    }

    @Test
    void getPaymentThrowsWhenNotFound() {
        when(paymentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getPayment(99L));
    }

    @Test
    void getPaymentsReturnsPageForTheEmployee() {
        Pageable pageable = PageRequest.of(0, 10);
        when(employeeRepository.existsById("P00001")).thenReturn(true);
        when(paymentRepository.findByEmployeeCode("P00001", pageable))
                .thenReturn(new PageImpl<>(List.of(recordedPayment()), pageable, 1));

        Page<PaymentDto> result = service.getPayments("P00001", pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("P00001", result.getContent().get(0).getEmployeeCode());
    }

    @Test
    void getPaymentsThrowsWhenEmployeeNotFound() {
        Pageable pageable = PageRequest.of(0, 10);
        when(employeeRepository.existsById("NOPE00")).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.getPayments("NOPE00", pageable));
        verify(paymentRepository, never()).findByEmployeeCode(any(String.class), any(Pageable.class));
    }

    @Test
    void addPaymentRecordsPaymentFromTheSalary() {
        when(salaryRepository.findById(1L)).thenReturn(Optional.of(salaryOfEmployee()));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentDto result = service.addPayment(request(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31)));

        assertEquals("P00001", result.getEmployeeCode());
        assertEquals(PaidStatus.PAID, result.getPaidStatus());
        assertEquals(0, new BigDecimal("73800.00").compareTo(result.getNetSalary()));
        assertNotNull(result.getPaidDate());
        assertNotNull(result.getPaidTime());
    }

    @Test
    void addPaymentThrowsWhenSalaryNotFound() {
        when(salaryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.addPayment(request(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31))));
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void addPaymentRejectsPeriodOutsideTheSalaryRange() {
        when(salaryRepository.findById(1L)).thenReturn(Optional.of(salaryOfEmployee()));

        assertThrows(IllegalArgumentException.class,
                () -> service.addPayment(request(LocalDate.of(2025, 12, 1), LocalDate.of(2025, 12, 31))));
        verify(paymentRepository, never()).save(any(Payment.class));
    }
}