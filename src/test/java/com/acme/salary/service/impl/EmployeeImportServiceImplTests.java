package com.acme.salary.service.impl;

import com.acme.salary.entity.Employee;
import com.acme.salary.exception.ResourceAlreadyExistsException;
import com.acme.salary.repository.EmployeeRepository;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeImportServiceImplTests {

    private static final List<String> HEADERS = List.of(
            "employeeCode", "firstName", "lastName", "department", "jobTitle",
            "address", "phone", "email", "country");

    @Mock
    private EmployeeRepository employeeRepository;

    private EmployeeServiceImpl service;

    @BeforeEach
    void setUp() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        service = new EmployeeServiceImpl(employeeRepository, validator);
    }

    @Test
    void importsValidEmployeesAndReturnsImportedCount() throws IOException {
        MockMultipartFile file = workbook(HEADERS,
                List.of("P00001", "Asha", "Rao", "Finance", "Analyst", "Pune", "9876543210", "asha@example.com", "India"),
                List.of("P00002", "Ravi", "Shah", "Engineering", "Developer", "Mumbai", "9876543211", "ravi@example.com", "India"));
        when(employeeRepository.getActiveEmployeeCodes()).thenReturn(List.of());
        when(employeeRepository.getActivePhones()).thenReturn(List.of());
        when(employeeRepository.getActiveEmails()).thenReturn(List.of());
        List<Employee> saved = new ArrayList<>();
        doAnswer(invocation -> {
            Iterable<?> entities = invocation.getArgument(0);
            entities.forEach(entity -> {
                if (entity instanceof Employee employee) {
                    saved.add(employee);
                }
            });
            return entities;
        }).when(employeeRepository).saveAll(any());

        int imported = service.importEmployees(file);

        assertEquals(2, imported);
        verify(employeeRepository).saveAll(any());
        assertEquals(2, saved.size());
        assertEquals("P00001", saved.get(0).getEmployeeCode());
        assertEquals("Asha", saved.get(0).getFirstName());
        assertEquals("P00002", saved.get(1).getEmployeeCode());
        assertTrue(saved.stream().allMatch(employee -> employee.getSalaries().isEmpty()));
    }

    @Test
    void rejectsWorkbookWithIncorrectHeadersBeforeSaving() throws IOException {
        List<String> wrongHeaders = List.of(
                "firstName", "employeeCode", "lastName", "department", "jobTitle",
                "address", "phone", "email", "country");

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> service.importEmployees(workbook(wrongHeaders,
                        List.of("Asha", "P00001", "Rao", "Finance", "Analyst", "Pune", "9876543210", "asha@example.com", "India"))));

        assertTrue(error.getMessage().contains("exact order"));
        verify(employeeRepository, never()).saveAll(any());
    }

    @Test
    void rejectsInvalidRowsWithoutPartiallyImportingValidRows() throws IOException {
        MockMultipartFile file = workbook(HEADERS,
                List.of("P00001", "Asha", "Rao", "Finance", "Analyst", "Pune", "9876543210", "asha@example.com", "India"),
                List.of("P00002", "Ravi", "Shah", "Engineering", "Developer", "Mumbai", "123", "not-an-email", "India"));

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> service.importEmployees(file));

        assertTrue(error.getMessage().contains("Row 3"));
        verify(employeeRepository, never()).saveAll(any());
        verify(employeeRepository, never()).existsById(anyString());
    }

    @Test
    void rejectsEmployeeCodeThatAlreadyExists() throws IOException {
        MockMultipartFile file = workbook(HEADERS,
                List.of("P00001", "Asha", "Rao", "Finance", "Analyst", "Pune", "9876543210", "asha@example.com", "India"));
        when(employeeRepository.getActiveEmployeeCodes()).thenReturn(List.of("P00001"));
        when(employeeRepository.getActivePhones()).thenReturn(List.of());
        when(employeeRepository.getActiveEmails()).thenReturn(List.of());

        assertThrows(ResourceAlreadyExistsException.class, () -> service.importEmployees(file));

        verify(employeeRepository, never()).saveAll(any());
    }

    @Test
    void rejectsDuplicateEmployeeCodesWithinTheWorkbook() throws IOException {
        MockMultipartFile file = workbook(HEADERS,
                List.of("P00001", "Asha", "Rao", "Finance", "Analyst", "Pune", "9876543210", "asha@example.com", "India"),
                List.of("P00001", "Ravi", "Shah", "Engineering", "Developer", "Mumbai", "9876543211", "ravi@example.com", "India"));
        when(employeeRepository.getActiveEmployeeCodes()).thenReturn(List.of());
        when(employeeRepository.getActivePhones()).thenReturn(List.of());
        when(employeeRepository.getActiveEmails()).thenReturn(List.of());

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> service.importEmployees(file));

        assertTrue(error.getMessage().contains("Duplicate employeeCode"));
        verify(employeeRepository, never()).saveAll(any());
    }

    @Test
    void rejectsPhoneThatAlreadyExists() throws IOException {
        MockMultipartFile file = workbook(HEADERS,
                List.of("P00001", "Asha", "Rao", "Finance", "Analyst", "Pune", "9876543210", "asha@example.com", "India"));
        when(employeeRepository.getActiveEmployeeCodes()).thenReturn(List.of());
        when(employeeRepository.getActivePhones()).thenReturn(List.of("9876543210"));
        when(employeeRepository.getActiveEmails()).thenReturn(List.of());

        assertThrows(ResourceAlreadyExistsException.class, () -> service.importEmployees(file));

        verify(employeeRepository, never()).saveAll(any());
    }

    @Test
    void rejectsDuplicatePhonesWithinTheWorkbook() throws IOException {
        MockMultipartFile file = workbook(HEADERS,
                List.of("P00001", "Asha", "Rao", "Finance", "Analyst", "Pune", "9876543210", "asha@example.com", "India"),
                List.of("P00002", "Ravi", "Shah", "Engineering", "Developer", "Mumbai", "9876543210", "ravi@example.com", "India"));
        when(employeeRepository.getActiveEmployeeCodes()).thenReturn(List.of());
        when(employeeRepository.getActivePhones()).thenReturn(List.of());
        when(employeeRepository.getActiveEmails()).thenReturn(List.of());

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> service.importEmployees(file));

        assertTrue(error.getMessage().contains("Duplicate phone number"));
        verify(employeeRepository, never()).saveAll(any());
    }

    @Test
    void rejectsEmailsThatAlreadyExists() throws IOException {
        MockMultipartFile file = workbook(HEADERS,
                List.of("P00001", "Asha", "Rao", "Finance", "Analyst", "Pune", "9876543210", "asha@example.com", "India"));
        when(employeeRepository.getActiveEmployeeCodes()).thenReturn(List.of());
        when(employeeRepository.getActivePhones()).thenReturn(List.of());
        when(employeeRepository.getActiveEmails()).thenReturn(List.of("asha@example.com"));

        assertThrows(ResourceAlreadyExistsException.class, () -> service.importEmployees(file));

        verify(employeeRepository, never()).saveAll(any());
    }

    @Test
    void rejectsDuplicateEmailsWithinTheWorkbook() throws IOException {
        MockMultipartFile file = workbook(HEADERS,
                List.of("P00001", "Asha", "Rao", "Finance", "Analyst", "Pune", "9876543210", "same@example.com", "India"),
                List.of("P00002", "Ravi", "Shah", "Engineering", "Developer", "Mumbai", "9876543211", "same@example.com", "India"));
        when(employeeRepository.getActiveEmployeeCodes()).thenReturn(List.of());
        when(employeeRepository.getActivePhones()).thenReturn(List.of());
        when(employeeRepository.getActiveEmails()).thenReturn(List.of());

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> service.importEmployees(file));

        assertTrue(error.getMessage().contains("Duplicate email"));
        verify(employeeRepository, never()).saveAll(any());
    }

    @SafeVarargs
    private static MockMultipartFile workbook(List<String> headers, List<String>... rows) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            var sheet = workbook.createSheet("Employees");
            writeRow(sheet.createRow(0), headers);
            for (int index = 0; index < rows.length; index++) {
                writeRow(sheet.createRow(index + 1), rows[index]);
            }
            workbook.write(output);
            return new MockMultipartFile("file", "employees.xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", output.toByteArray());
        }
    }

    private static void writeRow(Row row, List<String> values) {
        for (int column = 0; column < values.size(); column++) {
            row.createCell(column).setCellValue(values.get(column));
        }
    }

}
