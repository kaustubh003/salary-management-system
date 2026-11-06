package com.acme.salary.entity;

import com.acme.salary.enums.LeaveType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Entity
@Getter
    public class LeavesMonthlyRefill {

    @Setter
    @Id
    @Enumerated(EnumType.STRING)
    private LeaveType leaveType;

    @Setter
    @Column(nullable = false)
    private int refillValue;

    protected LeavesMonthlyRefill() {
        // required by JPA
    }

    public LeavesMonthlyRefill(LeaveType leaveType, int refillValue) {
        this.leaveType = Objects.requireNonNull(leaveType, "leaveType is required");
        this.refillValue = Objects.requireNonNull(refillValue, "refillValue is required");
    }

}
