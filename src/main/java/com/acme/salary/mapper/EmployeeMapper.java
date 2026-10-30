package com.acme.salary.mapper;

import com.acme.salary.dto.EmployeeDto;
import com.acme.salary.entity.Employee;
import com.acme.salary.entity.Salary;

import java.time.LocalDate;
import java.util.Comparator;

public final class EmployeeMapper {

    private EmployeeMapper() {
    }

    /**
     * Maps an employee with only the salary in force today. Call this inside a
     * transaction, because the salary list is loaded lazily.
     */
    public static EmployeeDto toDto(Employee employee) {
        EmployeeDto dto = new EmployeeDto();
        dto.setEmployeeCode(employee.getEmployeeCode());
        dto.setFirstName(employee.getFirstName());
        dto.setLastName(employee.getLastName());
        dto.setDepartment(employee.getDepartment());
        dto.setJobTitle(employee.getJobTitle());
        dto.setAddress(employee.getAddress());
        dto.setPhone(employee.getPhone());
        dto.setEmail(employee.getEmail());
        dto.setCountry(employee.getCountry());

        Salary current = currentSalary(employee);
        dto.setSalary(current == null ? null : SalaryMapper.toDto(current));
        return dto;
    }

    /**
     * Builds a new Employee. If the DTO carries a salary, it becomes the first
     * salary record in the employee's history.
     */
    public static Employee toEntity(EmployeeDto dto) {
        Employee employee = new Employee(
                dto.getEmployeeCode(),
                dto.getFirstName(),
                dto.getLastName(),
                dto.getDepartment(),
                dto.getJobTitle(),
                dto.getAddress(),
                dto.getPhone(),
                dto.getEmail(),
                dto.getCountry());

        if (dto.getSalary() != null) {
            employee.addSalary(SalaryMapper.toEntity(dto.getSalary()));
        }
        return employee;
    }

    /**
     * Copies the editable details onto an existing Employee. The employee code cannot
     * change, and the salary in the DTO is ignored: salary changes go through the salary mapper.
     */
    public static void updateEntity(EmployeeDto dto, Employee employee) {
        employee.setFirstName(dto.getFirstName());
        employee.setLastName(dto.getLastName());
        employee.setDepartment(dto.getDepartment());
        employee.setJobTitle(dto.getJobTitle());
        employee.setAddress(dto.getAddress());
        employee.setPhone(dto.getPhone());
        employee.setEmail(dto.getEmail());
        employee.setCountry(dto.getCountry());
    }

    private static Salary currentSalary(Employee employee) {
        LocalDate today = LocalDate.now();
        return employee.getSalaries().stream()
                .filter(salary -> salary.isEffectiveOn(today))
                .max(Comparator.comparing(Salary::getEffectiveFrom))
                .orElse(null);
    }
}