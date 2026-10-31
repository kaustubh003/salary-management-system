package com.acme.salary.service.impl;

import com.acme.salary.dto.SalaryDto;
import com.acme.salary.entity.Employee;
import com.acme.salary.entity.Salary;
import com.acme.salary.exception.ResourceNotFoundException;
import com.acme.salary.repository.EmployeeRepository;
import com.acme.salary.repository.SalaryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SalaryServiceImplTests {

    @Mock
    private SalaryRepository salaryRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    private SalaryServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new SalaryServiceImpl(salaryRepository, employeeRepository);
    }

    private Employee employee() {
        return new Employee("P00001", "Asha", "Rao", "Finance", "Analyst",
                "Pune", "9876543210", "asha@example.com", "India");
    }

    private Salary salary(LocalDate from, LocalDate till) {
        return new Salary(from, till, "INR",
                new BigDecimal("60000.00"), new BigDecimal("24000.00"), new BigDecimal("6000.00"),
                new BigDecimal("9000.00"), new BigDecimal("7200.00"));
    }

    private SalaryDto salaryDto() {
        SalaryDto dto = new SalaryDto();
        dto.setEffectiveFrom(LocalDate.of(2026, 1, 1));
        dto.setCurrency("INR");
        dto.setBasicSalary(new BigDecimal("60000.00"));
        dto.setHra(new BigDecimal("24000.00"));
        dto.setDa(new BigDecimal("6000.00"));
        dto.setTax(new BigDecimal("9000.00"));
        dto.setEpf(new BigDecimal("7200.00"));
        return dto;
    }

    @Test
    void getSalaryReturnsDtoWhenFound() {
        when(salaryRepository.findById(1L)).thenReturn(Optional.of(salary(LocalDate.of(2026, 1, 1), null)));

        SalaryDto dto = service.getSalary(1L);

        assertEquals("INR", dto.getCurrency());
        assertEquals(0, new BigDecimal("73800.00").compareTo(dto.getNetSalary()));
    }

    @Test
    void getSalaryThrowsWhenNotFound() {
        when(salaryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getSalary(99L));
    }

    @Test
    void getSalariesReturnsEverySalaryOfTheEmployee() {
        Employee employee = employee();
        employee.addSalary(salary(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31)));
        employee.addSalary(salary(LocalDate.of(2026, 1, 1), null));
        when(employeeRepository.findById("P00001")).thenReturn(Optional.of(employee));

        List<SalaryDto> result = service.getSalaries("P00001");

        assertEquals(2, result.size());
        assertEquals("INR", result.get(0).getCurrency());
        assertEquals(new BigDecimal("73800.00"), result.get(0).getNetSalary());
        assertEquals("INR", result.get(1).getCurrency());
        assertEquals(new BigDecimal("73800.00"), result.get(1).getNetSalary());
    }

    @Test
    void getSalariesThrowsWhenEmployeeNotFound() {
        when(employeeRepository.findById("NOPE00")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getSalaries("NOPE00"));
    }

    @Test
    void addSalarySavesSalaryUnderTheEmployee() {
        Employee employee = employee();
        when(employeeRepository.findById("P00001")).thenReturn(Optional.of(employee));
        when(salaryRepository.save(any(Salary.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SalaryDto result = service.addSalary("P00001", salaryDto());

        ArgumentCaptor<Salary> saved = ArgumentCaptor.forClass(Salary.class);
        verify(salaryRepository).save(saved.capture());
        assertSame(employee, saved.getValue().getEmployee());
        assertEquals(1, employee.getSalaries().size());
        assertEquals("INR", result.getCurrency());
    }

    @Test
    void addSalaryClosesThePreviousOpenEndedSalary() {
        Employee employee = employee();
        Salary previous = salary(LocalDate.of(2025, 1, 1), null);
        employee.addSalary(previous);
        when(employeeRepository.findById("P00001")).thenReturn(Optional.of(employee));
        when(salaryRepository.save(any(Salary.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.addSalary("P00001", salaryDto());   // starts 2026-01-01, open-ended

        assertEquals(LocalDate.of(2025, 12, 31), previous.getEffectiveTill());
        assertEquals(2, employee.getSalaries().size());
    }

    @Test
    void addSalaryLeavesAnAlreadyClosedSalaryAlone() {
        Employee employee = employee();
        Salary closed = salary(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31));
        employee.addSalary(closed);
        when(employeeRepository.findById("P00001")).thenReturn(Optional.of(employee));
        when(salaryRepository.save(any(Salary.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.addSalary("P00001", salaryDto());

        assertEquals(LocalDate.of(2025, 12, 31), closed.getEffectiveTill());
    }

    @Test
    void addSalaryWithAnEndDateDoesNotCloseTheOpenEndedSalary() {
        Employee employee = employee();
        Salary open = salary(LocalDate.of(2026, 1, 1), null);
        employee.addSalary(open);
        when(employeeRepository.findById("P00001")).thenReturn(Optional.of(employee));
        when(salaryRepository.save(any(Salary.class))).thenAnswer(invocation -> invocation.getArgument(0));
        SalaryDto historical = salaryDto();
        historical.setEffectiveFrom(LocalDate.of(2024, 1, 1));
        historical.setEffectiveTill(LocalDate.of(2024, 12, 31));

        service.addSalary("P00001", historical);

        assertNull(open.getEffectiveTill());
    }

    @Test
    void addSalaryRejectsOpenEndedSalaryStartingBeforeTheCurrentOne() {
        Employee employee = employee();
        Salary open = salary(LocalDate.of(2026, 1, 1), null);
        employee.addSalary(open);
        when(employeeRepository.findById("P00001")).thenReturn(Optional.of(employee));
        SalaryDto earlier = salaryDto();
        earlier.setEffectiveFrom(LocalDate.of(2025, 6, 1));

        assertThrows(IllegalArgumentException.class, () -> service.addSalary("P00001", earlier));

        assertNull(open.getEffectiveTill());
        assertEquals(1, employee.getSalaries().size());
        verify(salaryRepository, never()).save(any(Salary.class));
    }

    @Test
    void addSalaryThrowsWhenEmployeeNotFound() {
        when(employeeRepository.findById("NOPE00")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.addSalary("NOPE00", salaryDto()));
        verify(salaryRepository, never()).save(any(Salary.class));
    }

    @Test
    void updateSalaryChangesFieldsAndSaves() {
        Salary existing = salary(LocalDate.of(2026, 1, 1), null);
        when(salaryRepository.findById(1L)).thenReturn(Optional.of(existing));
        SalaryDto update = salaryDto();
        update.setBasicSalary(new BigDecimal("70000.00"));

        boolean result = service.updateSalary(1L, update);

        assertTrue(result);
        assertEquals(0, new BigDecimal("70000.00").compareTo(existing.getBasicSalary()));
        assertEquals(0, new BigDecimal("83800.00").compareTo(existing.getNetSalary()));
        verify(salaryRepository).save(existing);
    }

    @Test
    void updateSalaryThrowsWhenNotFound() {
        when(salaryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.updateSalary(99L, salaryDto()));
        verify(salaryRepository, never()).save(any(Salary.class));
    }

    @Test
    void deleteSalaryRemovesTheSalary() {
        Salary existing = salary(LocalDate.of(2026, 1, 1), null);
        when(salaryRepository.findById(1L)).thenReturn(Optional.of(existing));

        boolean result = service.deleteSalary(1L);

        assertTrue(result);
        verify(salaryRepository).delete(existing);
    }

    @Test
    void deleteSalaryThrowsWhenNotFound() {
        when(salaryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.deleteSalary(99L));
        verify(salaryRepository, never()).delete(any(Salary.class));
    }
}