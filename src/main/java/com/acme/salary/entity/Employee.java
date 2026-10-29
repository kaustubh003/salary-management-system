package com.acme.salary.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Employee master record. Salary history is kept as Salary rows, ordered by
 * effective date. Payments reference the employee by ID and are not mapped here.
 */
@Entity
public class Employee {

    @Id
    @Getter
    @Column(nullable = false, unique = true, length = 5, updatable = false)
    private String employeeCode;

    @Getter
    @Setter
    @Column(nullable = false, length = 50)
    private String firstName;

    @Getter
    @Setter
    @Column(nullable = false, length = 50)
    private String lastName;

    @Getter
    @Setter
    @Column(nullable = false, length = 20)
    private String department;

    @Getter
    @Setter
    @Column(nullable = false, length = 50)
    private String jobTitle;

    @Getter
    @Setter
    @Column(nullable = false, length = 256)
    private String address;

    @Getter
    @Setter
    @Column(nullable = false, unique = true, length = 10)
    private String phone;

    @Getter
    @Setter
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Getter
    @Setter
    @Column(nullable = false, length = 100)
    private String country;

    @Getter
    @Column(nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = false)
    @OrderBy("effectiveFrom ASC")
    private List<Salary> salaries = new ArrayList<>();


    protected Employee() {
        // required by JPA
    }

    public Employee(String employeeCode, String firstName, String lastName, String department, String jobTitle, String address, String phone, String email, String country) {
        this.employeeCode = employeeCode;
        this.firstName = firstName;
        this.lastName = lastName;
        this.department = department;
        this.jobTitle = jobTitle;
        this.address = address;
        this.phone = phone;
        this.email = email;
        this.country = country;
    }

    /**
     * Adds a salary record to this employee's history.
     */
    public void addSalary(Salary salary) {
        salary.setEmployee(this);
        salaries.add(salary);
    }

    /**
     * Soft delete. Employees are deactivated rather than removed so payment history stays intact.
     */
    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }

    /** Returns a read-only snapshot. Use addSalary to change salary history. */
    public List<Salary> getSalaries() {
        return List.copyOf(salaries);
    }

}