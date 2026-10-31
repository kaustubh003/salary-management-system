package com.acme.salary.service.impl;

import com.acme.salary.dto.EmployeeDto;
import com.acme.salary.dto.SalaryDto;
import com.acme.salary.entity.Employee;
import com.acme.salary.entity.Salary;
import com.acme.salary.exception.ResourceAlreadyExistsException;
import com.acme.salary.exception.ResourceNotFoundException;
import com.acme.salary.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTests {

    @Mock
    private EmployeeRepository employeeRepository;

    private EmployeeServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new EmployeeServiceImpl(employeeRepository);
    }

    private Employee employee() {
        return new Employee("P00001", "Asha", "Rao", "Finance", "Analyst",
                "Pune", "9876543210", "asha@example.com", "India");
    }

    private EmployeeDto employeeDto() {
        EmployeeDto dto = new EmployeeDto();
        dto.setEmployeeCode("P00001");
        dto.setFirstName("Asha");
        dto.setLastName("Rao");
        dto.setDepartment("Finance");
        dto.setJobTitle("Analyst");
        dto.setAddress("Pune");
        dto.setPhone("9876543210");
        dto.setEmail("asha@example.com");
        dto.setCountry("India");
        return dto;
    }

    @Test
    void getEmployeeReturnsDtoWithCurrentSalary() {
        Employee employee = employee();
        // Open-ended and started long ago, so it is the current salary whenever the test runs
        employee.addSalary(new Salary(LocalDate.of(2020, 1, 1), null, "INR",
                new BigDecimal("60000.00"), new BigDecimal("24000.00"), new BigDecimal("6000.00"),
                new BigDecimal("9000.00"), new BigDecimal("7200.00")));
        when(employeeRepository.findById("P00001")).thenReturn(Optional.of(employee));

        EmployeeDto dto = service.getEmployee("P00001");

        assertEquals("P00001", dto.getEmployeeCode());
        assertEquals("Asha", dto.getFirstName());
        assertNotNull(dto.getSalary());
        assertEquals("INR", dto.getSalary().getCurrency());
    }

    @Test
    void getEmployeeThrowsWhenNotFound() {
        when(employeeRepository.findById("NOPE00")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getEmployee("NOPE00"));
    }

    @Test
    void getEmployeesReturnsTheRepositoryPage() {
        Pageable pageable = PageRequest.of(0, 10);
        when(employeeRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(employee()), pageable, 1));

        Page<EmployeeDto> result = service.getEmployees(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("P00001", result.getContent().get(0).getEmployeeCode());
    }

    @Test
    void addEmployeeSavesNewEmployeeAndReturnsDto() {
        when(employeeRepository.existsById("P00001")).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EmployeeDto result = service.addEmployee(employeeDto());

        ArgumentCaptor<Employee> saved = ArgumentCaptor.forClass(Employee.class);
        verify(employeeRepository).save(saved.capture());
        assertEquals("P00001", saved.getValue().getEmployeeCode());
        assertEquals("Asha", saved.getValue().getFirstName());

        assertEquals("P00001", result.getEmployeeCode());
        assertEquals("Asha", result.getFirstName());
        assertEquals("Rao", result.getLastName());
        assertEquals("asha@example.com", result.getEmail());
        assertNull(result.getSalary());
    }

    @Test
    void addEmployeeWithSalaryReturnsDtoWithTheSalary() {
        when(employeeRepository.existsById("P00001")).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));
        EmployeeDto dto = employeeDto();
        SalaryDto salary = new SalaryDto();
        // Open-ended and started long ago, so it is the current salary whenever the test runs
        salary.setEffectiveFrom(LocalDate.of(2020, 1, 1));
        salary.setCurrency("INR");
        salary.setBasicSalary(new BigDecimal("60000.00"));
        salary.setHra(new BigDecimal("24000.00"));
        salary.setDa(new BigDecimal("6000.00"));
        salary.setTax(new BigDecimal("9000.00"));
        salary.setEpf(new BigDecimal("7200.00"));
        dto.setSalary(salary);

        EmployeeDto result = service.addEmployee(dto);

        ArgumentCaptor<Employee> saved = ArgumentCaptor.forClass(Employee.class);
        verify(employeeRepository).save(saved.capture());
        assertEquals(1, saved.getValue().getSalaries().size());

        assertNotNull(result.getSalary());
        assertEquals("INR", result.getSalary().getCurrency());
        assertEquals(0, new BigDecimal("73800.00").compareTo(result.getSalary().getNetSalary()));
    }

    @Test
    void addEmployeeThrowsWhenCodeAlreadyExists() {
        when(employeeRepository.existsById("P00001")).thenReturn(true);

        assertThrows(ResourceAlreadyExistsException.class, () -> service.addEmployee(employeeDto()));
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void updateEmployeeChangesDetailsAndSaves() {
        Employee existing = employee();
        when(employeeRepository.findById("P00001")).thenReturn(Optional.of(existing));
        EmployeeDto update = employeeDto();
        update.setFirstName("Meera");
        update.setDepartment("Engineering");
        update.setCountry("Singapore");

        boolean result = service.updateEmployee(update);

        assertTrue(result);
        assertEquals("Meera", existing.getFirstName());
        assertEquals("Engineering", existing.getDepartment());
        assertEquals("Singapore", existing.getCountry());
        assertEquals("P00001", existing.getEmployeeCode());
        verify(employeeRepository).save(existing);
    }

    @Test
    void updateEmployeeThrowsWhenNotFound() {
        when(employeeRepository.findById("P00001")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.updateEmployee(employeeDto()));
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void deactivateEmployeeMarksEmployeeInactive() {
        Employee existing = employee();
        when(employeeRepository.findById("P00001")).thenReturn(Optional.of(existing));

        boolean result = service.deactivateEmployee("P00001");

        assertTrue(result);
        assertFalse(existing.isActive());
        verify(employeeRepository).save(existing);
    }

    @Test
    void deactivateEmployeeThrowsWhenNotFound() {
        when(employeeRepository.findById("NOPE00")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.deactivateEmployee("NOPE00"));
        verify(employeeRepository, never()).save(any(Employee.class));
    }
}