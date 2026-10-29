package com.acme.salary.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class EmployeeTests {

    private Employee employee() {
        return new Employee("EMP01", "Asha", "Rao", "Finance", "Analyst",
                "Pune", "9876543210", "asha@example.com", "India");
    }

    @Test
    void newEmployeeIsActive() {
        assertTrue(employee().isActive());
    }

    @Test
    void deactivateAndActivateToggleStatus() {
        Employee e = employee();
        e.deactivate();
        assertFalse(e.isActive());
        e.activate();
        assertTrue(e.isActive());
    }

    @Test
    void addSalaryLinksSalaryToEmployee() {
        Employee e = employee();
        Salary s = new Salary(LocalDate.of(2026, 1, 1), null, "INR",
                BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        e.addSalary(s);

        assertEquals(1, e.getSalaries().size());
        assertTrue(s.getEmployee() == e);
    }

    @Test
    void salaryListIsReadOnly() {
        Employee e = employee();
        assertThrows(UnsupportedOperationException.class,
                () -> e.getSalaries().add(new Salary(LocalDate.of(2026, 1, 1), null, "INR",
                        BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO)));
    }
}