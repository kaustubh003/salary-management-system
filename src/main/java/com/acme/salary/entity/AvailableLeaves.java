package com.acme.salary.entity;

import com.acme.salary.enums.LeaveType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Entity
@Table(name = "available_leaves",
        uniqueConstraints = @UniqueConstraint(columnNames = {"employee_code", "leave_type"}))
@Getter
public class AvailableLeaves {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Column(name = "employee_code", nullable = false, updatable = false)
    private String employeeCode;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "leave_type", nullable = false, updatable = false)
    private LeaveType leaveType;

    @Setter
    @Column(nullable = false)
    private int availableLeaves;

    protected AvailableLeaves() {
        // required by JPA
    }

    public AvailableLeaves(String employeeCode, LeaveType leaveType, int availableLeaves) {
        this.employeeCode = Objects.requireNonNull(employeeCode, "employeeCode is required");
        this.leaveType = Objects.requireNonNull(leaveType, "leaveType is required");
        this.availableLeaves = availableLeaves;
    }

}
