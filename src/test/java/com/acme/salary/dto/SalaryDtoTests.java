package com.acme.salary.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SalaryDtoTests {

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

    private SalaryDto validDto() {
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

    private Set<String> invalidFields(SalaryDto dto) {
        return validator.validate(dto).stream()
                .map(v -> v.getPropertyPath().toString())
                .collect(Collectors.toSet());
    }

    private void assertRejected(String field, Consumer<SalaryDto> change) {
        SalaryDto dto = validDto();
        change.accept(dto);
        assertTrue(invalidFields(dto).contains(field), field + " should be rejected");
    }

    @Test
    void validDtoHasNoViolations() {
        // id, grossSalary, netSalary and effectiveTill are all left empty here
        assertTrue(invalidFields(validDto()).isEmpty());
    }

    @Test
    void effectiveTillIsOptional() {
        SalaryDto dto = validDto();
        dto.setEffectiveTill(null);
        assertTrue(invalidFields(dto).isEmpty());
    }

    @Test
    void effectiveFromIsRequired() {
        assertRejected("effectiveFrom", d -> d.setEffectiveFrom(null));
    }

    @Test
    void currencyIsRequired() {
        assertRejected("currency", d -> d.setCurrency(null));
        assertRejected("currency", d -> d.setCurrency(""));
    }

    @Test
    void currencyMustBeThreeUppercaseLetters() {
        assertRejected("currency", d -> d.setCurrency("inr"));
        assertRejected("currency", d -> d.setCurrency("IN"));
        assertRejected("currency", d -> d.setCurrency("INRR"));
        assertRejected("currency", d -> d.setCurrency("IN1"));
    }

    @Test
    void everyAmountIsRequired() {
        assertRejected("basicSalary", d -> d.setBasicSalary(null));
        assertRejected("hra", d -> d.setHra(null));
        assertRejected("da", d -> d.setDa(null));
        assertRejected("tax", d -> d.setTax(null));
        assertRejected("epf", d -> d.setEpf(null));
    }

    @Test
    void negativeAmountsAreRejected() {
        BigDecimal negative = new BigDecimal("-0.01");
        assertRejected("basicSalary", d -> d.setBasicSalary(negative));
        assertRejected("hra", d -> d.setHra(negative));
        assertRejected("da", d -> d.setDa(negative));
        assertRejected("tax", d -> d.setTax(negative));
        assertRejected("epf", d -> d.setEpf(negative));
    }

    @Test
    void zeroAmountsAreAccepted() {
        SalaryDto dto = validDto();
        dto.setHra(BigDecimal.ZERO);
        dto.setDa(BigDecimal.ZERO);
        dto.setTax(BigDecimal.ZERO);
        dto.setEpf(BigDecimal.ZERO);
        assertTrue(invalidFields(dto).isEmpty());
    }

    @Test
    void amountsAreLimitedToThirteenDigitsAndTwoDecimals() {
        SalaryDto max = validDto();
        max.setBasicSalary(new BigDecimal("9999999999999.99"));
        assertTrue(invalidFields(max).isEmpty());

        assertRejected("basicSalary", d -> d.setBasicSalary(new BigDecimal("10000000000000.00")));
        assertRejected("basicSalary", d -> d.setBasicSalary(new BigDecimal("100.123")));
    }
}