package com.acme.salary.service.impl;

import com.acme.salary.dto.PayslipBatchDto;
import com.acme.salary.dto.PayslipDto;
import com.acme.salary.entity.Employee;
import com.acme.salary.entity.Payment;
import com.acme.salary.entity.Salary;
import com.acme.salary.exception.ResourceNotFoundException;
import com.acme.salary.payslip.PayslipPdfGenerator;
import com.acme.salary.repository.EmployeeRepository;
import com.acme.salary.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * The PDF generator is mocked, so these tests are about where payslips are stored and found,
 * not about what is printed in them. Files go to a temporary folder that JUnit deletes afterwards.
 */
@ExtendWith(MockitoExtension.class)
class PayslipServiceImplTests {

    private static final byte[] PDF = "%PDF-1.7 test payslip".getBytes(StandardCharsets.UTF_8);

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private PayslipPdfGenerator pdfGenerator;

    @TempDir
    Path payslipDirectory;

    private PayslipServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PayslipServiceImpl(paymentRepository, employeeRepository, pdfGenerator,
                payslipDirectory.toString());
    }

    private Employee employee(String employeeCode) {
        return new Employee(employeeCode, "Asha", "Rao", "Finance", "Analyst",
                "Pune", "9876543210", "asha@example.com", "India");
    }

    private Payment payment(long paymentId, String employeeCode) {
        Employee employee = employee(employeeCode);
        Salary salary = new Salary(LocalDate.of(2026, 1, 1), null, "INR",
                new BigDecimal("60000.00"), new BigDecimal("24000.00"), new BigDecimal("6000.00"),
                new BigDecimal("9000.00"), new BigDecimal("7200.00"));
        employee.addSalary(salary);
        Payment payment = Payment.record(salary, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31),
                LocalDate.of(2026, 1, 31), LocalTime.of(10, 30));
        ReflectionTestUtils.setField(payment, "paymentID", paymentId);
        return payment;
    }

    private Path payslipFile(String employeeCode, long paymentId) {
        return payslipDirectory.resolve(employeeCode).resolve("payslip-" + paymentId + ".pdf");
    }

    private void storedPayslip(String employeeCode, long paymentId, String content) throws Exception {
        Path file = payslipFile(employeeCode, paymentId);
        Files.createDirectories(file.getParent());
        Files.write(file, content.getBytes(StandardCharsets.UTF_8));
    }

    // ---- generatePayslip ----

    @Test
    void generatePayslipStoresThePdfInTheEmployeesFolder() throws Exception {
        Payment payment = payment(5L, "P00001");
        Employee employee = employee("P00001");
        when(paymentRepository.findById(5L)).thenReturn(Optional.of(payment));
        when(employeeRepository.findById("P00001")).thenReturn(Optional.of(employee));
        when(pdfGenerator.generate(employee, payment)).thenReturn(PDF);

        PayslipDto result = service.generatePayslip(5L);

        assertArrayEquals(PDF, Files.readAllBytes(payslipFile("P00001", 5L)));
        assertEquals(5L, result.getPaymentId());
        assertEquals("P00001", result.getEmployeeCode());
        assertEquals("payslip-5.pdf", result.getFileName());
        assertNotNull(result.getGeneratedAt());
    }

    @Test
    void generatePayslipReplacesAnExistingPayslip() throws Exception {
        Payment payment = payment(5L, "P00001");
        Employee employee = employee("P00001");
        storedPayslip("P00001", 5L, "old payslip");
        when(paymentRepository.findById(5L)).thenReturn(Optional.of(payment));
        when(employeeRepository.findById("P00001")).thenReturn(Optional.of(employee));
        when(pdfGenerator.generate(employee, payment)).thenReturn(PDF);

        service.generatePayslip(5L);

        assertArrayEquals(PDF, Files.readAllBytes(payslipFile("P00001", 5L)));
    }

    @Test
    void generatePayslipThrowsWhenPaymentNotFound() {
        when(paymentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.generatePayslip(99L));
        verify(pdfGenerator, never()).generate(any(Employee.class), any(Payment.class));
    }

    @Test
    void generatePayslipThrowsWhenEmployeeNotFound() {
        when(paymentRepository.findById(5L)).thenReturn(Optional.of(payment(5L, "P00001")));
        when(employeeRepository.findById("P00001")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.generatePayslip(5L));
        verify(pdfGenerator, never()).generate(any(Employee.class), any(Payment.class));
        assertFalse(Files.exists(payslipFile("P00001", 5L)));
    }

    // ---- generateAllPayslips ----

    @Test
    void generateAllPayslipsCreatesMissingOnesAndSkipsExistingOnes() throws Exception {
        Payment first = payment(1L, "P00001");
        Payment second = payment(2L, "P00001");
        Payment alreadyDone = payment(3L, "P00002");
        storedPayslip("P00002", 3L, "already generated");
        when(paymentRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(first, second, alreadyDone)));
        when(employeeRepository.findById("P00001")).thenReturn(Optional.of(employee("P00001")));
        when(pdfGenerator.generate(any(Employee.class), any(Payment.class))).thenReturn(PDF);

        PayslipBatchDto result = service.generateAllPayslips();

        assertEquals(2, result.getGenerated());
        assertEquals(1, result.getSkipped());
        assertArrayEquals(PDF, Files.readAllBytes(payslipFile("P00001", 1L)));
        assertArrayEquals(PDF, Files.readAllBytes(payslipFile("P00001", 2L)));
        assertEquals("already generated",
                new String(Files.readAllBytes(payslipFile("P00002", 3L)), StandardCharsets.UTF_8));
        verify(pdfGenerator, times(2)).generate(any(Employee.class), any(Payment.class));
    }

    @Test
    void generateAllPayslipsLooksUpEachEmployeeOnlyOnceAndNotForSkippedPayments() throws Exception {
        storedPayslip("P00002", 3L, "already generated");
        when(paymentRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(payment(1L, "P00001"), payment(2L, "P00001"), payment(3L, "P00002"))));
        when(employeeRepository.findById("P00001")).thenReturn(Optional.of(employee("P00001")));
        when(pdfGenerator.generate(any(Employee.class), any(Payment.class))).thenReturn(PDF);

        service.generateAllPayslips();

        verify(employeeRepository, times(1)).findById("P00001");
        verify(employeeRepository, never()).findById("P00002");
    }

    @Test
    void generateAllPayslipsWalksEveryPageOfPayments() {
        PageImpl<Payment> firstPage = new PageImpl<>(List.of(payment(1L, "P00001")), PageRequest.of(0, 1), 2);
        PageImpl<Payment> secondPage = new PageImpl<>(List.of(payment(2L, "P00001")), PageRequest.of(1, 1), 2);
        when(paymentRepository.findAll(any(Pageable.class))).thenReturn(firstPage, secondPage);
        when(employeeRepository.findById("P00001")).thenReturn(Optional.of(employee("P00001")));
        when(pdfGenerator.generate(any(Employee.class), any(Payment.class))).thenReturn(PDF);

        PayslipBatchDto result = service.generateAllPayslips();

        assertEquals(2, result.getGenerated());
        assertEquals(0, result.getSkipped());
        assertTrue(Files.exists(payslipFile("P00001", 1L)));
        assertTrue(Files.exists(payslipFile("P00001", 2L)));
    }

    @Test
    void generateAllPayslipsWithNoPaymentsDoesNothing() {
        when(paymentRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<Payment>(List.of()));

        PayslipBatchDto result = service.generateAllPayslips();

        assertEquals(0, result.getGenerated());
        assertEquals(0, result.getSkipped());
        verify(pdfGenerator, never()).generate(any(Employee.class), any(Payment.class));
    }

    // ---- getPayslip ----

    @Test
    void getPayslipReturnsTheStoredFile() throws Exception {
        when(paymentRepository.findById(5L)).thenReturn(Optional.of(payment(5L, "P00001")));
        Path file = payslipFile("P00001", 5L);
        Files.createDirectories(file.getParent());
        Files.write(file, PDF);

        Resource result = service.getPayslip(5L);

        assertTrue(result.exists());
        assertEquals("payslip-5.pdf", result.getFilename());
        assertArrayEquals(PDF, result.getInputStream().readAllBytes());
    }

    @Test
    void getPayslipThrowsWhenItHasNotBeenGeneratedYet() {
        when(paymentRepository.findById(5L)).thenReturn(Optional.of(payment(5L, "P00001")));

        assertThrows(ResourceNotFoundException.class, () -> service.getPayslip(5L));
    }

    @Test
    void getPayslipThrowsWhenPaymentNotFound() {
        when(paymentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getPayslip(99L));
    }

    // ---- getPayslips ----

    @Test
    void getPayslipsListsTheStoredPayslipsInPaymentOrder() throws Exception {
        when(employeeRepository.existsById("P00001")).thenReturn(true);
        storedPayslip("P00001", 10L, "ten");
        storedPayslip("P00001", 2L, "two");
        // Files that are not payslips are ignored
        Files.write(payslipDirectory.resolve("P00001").resolve("notes.txt"), "notes".getBytes(StandardCharsets.UTF_8));
        Files.write(payslipDirectory.resolve("P00001").resolve("payslip-abc.pdf"), "x".getBytes(StandardCharsets.UTF_8));

        List<PayslipDto> result = service.getPayslips("P00001");

        assertEquals(2, result.size());
        assertEquals(2L, result.get(0).getPaymentId());
        assertEquals("payslip-2.pdf", result.get(0).getFileName());
        assertEquals(10L, result.get(1).getPaymentId());
        assertEquals("P00001", result.get(1).getEmployeeCode());
        assertNotNull(result.get(1).getGeneratedAt());
    }

    @Test
    void getPayslipsDoesNotIncludeAnotherEmployeesPayslips() throws Exception {
        when(employeeRepository.existsById("P00001")).thenReturn(true);
        storedPayslip("P00002", 7L, "someone else");

        assertTrue(service.getPayslips("P00001").isEmpty());
    }

    @Test
    void getPayslipsReturnsAnEmptyListWhenNothingWasGenerated() {
        when(employeeRepository.existsById("P00001")).thenReturn(true);

        assertTrue(service.getPayslips("P00001").isEmpty());
    }

    @Test
    void getPayslipsThrowsWhenEmployeeNotFound() {
        when(employeeRepository.existsById("NOPE00")).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.getPayslips("NOPE00"));
    }

    @Test
    void getPayslipsRejectsAnEmployeeCodeThatWouldLeaveThePayslipFolder() {
        when(employeeRepository.existsById("../outside")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> service.getPayslips("../outside"));
    }
}
