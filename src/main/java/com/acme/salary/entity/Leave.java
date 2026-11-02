package com.acme.salary.entity;

import com.acme.salary.enums.LeaveType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Objects;

/**
 * One day of leave taken by an employee. A leave cannot be changed once it is stored,
 * so every column is non-updatable and there are no setters. An employee can take
 * at most one leave per date, and never on a Saturday or Sunday.
 */
@Entity
@Table(name = "leaves",
        uniqueConstraints = @UniqueConstraint(columnNames = {"employee_code", "leave_date"}))
@Getter
public class Leave {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long leaveId;

    @Column(name = "employee_code", nullable = false, length = 6, updatable = false)
    private String employeeCode;

    @Column(name = "leave_date", nullable = false, updatable = false)
    private LocalDate leaveDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "leave_type", nullable = false, length = 10, updatable = false)
    private LeaveType leaveType;

    protected Leave() {
        // required by JPA
    }

    private Leave(String employeeCode, LocalDate leaveDate, LeaveType leaveType) {
        this.employeeCode = employeeCode;
        this.leaveDate = leaveDate;
        this.leaveType = leaveType;
    }

    /**
     * Records a leave. Saturdays and Sundays are holidays, so a leave on those days is rejected.
     */
    public static Leave record(String employeeCode, LocalDate leaveDate, LeaveType leaveType) {
        Objects.requireNonNull(employeeCode, "employeeCode is required");
        Objects.requireNonNull(leaveDate, "leaveDate is required");
        Objects.requireNonNull(leaveType, "leaveType is required");

        DayOfWeek day = leaveDate.getDayOfWeek();
        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
            throw new IllegalArgumentException(
                    "Leave cannot be taken on a Saturday or Sunday. " + leaveDate + " is a " + day);
        }
        return new Leave(employeeCode, leaveDate, leaveType);
    }
}
