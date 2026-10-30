package com.acme.salary.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentDtoTests {

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

    private PaymentDto validRequest() {
        PaymentDto dto = new PaymentDto();
        dto.setSalaryId(1L);
        dto.setPeriodStart(LocalDate.of(2026, 1, 1));
        dto.setPeriodEnd(LocalDate.of(2026, 1, 31));
        return dto;
    }

    private Set<String> invalidFields(PaymentDto dto) {
        return validator.validate(dto).stream().map(v -> v.getPropertyPath().toString()).collect(Collectors.toSet());
    }

    private void assertRejected(String field, Consumer<PaymentDto> change) {
        PaymentDto dto = validRequest();
        change.accept(dto);
        assertTrue(invalidFields(dto).contains(field), field + " should be rejected");
    }

    @Test
    void requestNeedsOnlySalaryAndPeriod() {
        // paymentID, employeeCode, paidDate, paidTime, paidStatus and the snapshot are set by the backend
        assertTrue(invalidFields(validRequest()).isEmpty());
    }

    @Test
    void salaryIdIsRequired() {
        assertRejected("salaryId", d -> d.setSalaryId(null));
    }

    @Test
    void periodStartIsRequired() {
        assertRejected("periodStart", d -> d.setPeriodStart(null));
    }

    @Test
    void periodEndIsRequired() {
        assertRejected("periodEnd", d -> d.setPeriodEnd(null));
    }
}