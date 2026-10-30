package com.acme.salary.dto;

import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class EmployeeDtoTests {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    private EmployeeDto validDto() {
        EmployeeDto dto = new EmployeeDto();
        dto.setEmployeeCode("EMP001");
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

    private Set<String> invalidFields(EmployeeDto dto) {
        return validator.validate(dto).stream()
                .map(v -> v.getPropertyPath().toString())
                .collect(Collectors.toSet());
    }

    private void assertRejected(String field, Consumer<EmployeeDto> change) {
        EmployeeDto dto = validDto();
        change.accept(dto);
        assertTrue(invalidFields(dto).contains(field), field + " should be rejected");
    }

    @Test
    void validDtoHasNoViolations() {
        assertTrue(invalidFields(validDto()).isEmpty());
    }

    @Test
    void employeeCodeMustBeExactlySixCharacters() {
        assertRejected("employeeCode", d -> d.setEmployeeCode("EMP01"));
        assertRejected("employeeCode", d -> d.setEmployeeCode("EMP0001"));
        assertRejected("employeeCode", d -> d.setEmployeeCode(""));
    }

    @Test
    void requiredTextFieldsRejectEmptyValues() {
        assertRejected("firstName", d -> d.setFirstName(""));
        assertRejected("firstName", d -> d.setFirstName(null));
        assertRejected("lastName", d -> d.setLastName(""));
        assertRejected("department", d -> d.setDepartment(""));
        assertRejected("jobTitle", d -> d.setJobTitle(""));
        assertRejected("address", d -> d.setAddress(""));
        assertRejected("country", d -> d.setCountry(""));
    }

    @Test
    void phoneMustBeTenDigits() {
        assertRejected("phone", d -> d.setPhone("12345"));
        assertRejected("phone", d -> d.setPhone("98765432101"));
        assertRejected("phone", d -> d.setPhone("98765abcde"));
    }

    @Test
    void emailMustBeInValidFormat() {
        assertRejected("email", d -> d.setEmail("not-an-email"));
        assertRejected("email", d -> d.setEmail(""));
    }

    @Test
    void nestedSalaryIsValidated() {
        // Passes only once the salary field in EmployeeDto is annotated with @Valid
        SalaryDto salary = new SalaryDto();
        salary.setCurrency("inr");

        assertRejected("salary.currency", d -> d.setSalary(salary));
    }
}