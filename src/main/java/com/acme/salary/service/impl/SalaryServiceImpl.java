package com.acme.salary.service.impl;

import com.acme.salary.dto.SalaryDto;
import com.acme.salary.entity.Employee;
import com.acme.salary.entity.Salary;
import com.acme.salary.exception.ResourceNotFoundException;
import com.acme.salary.mapper.SalaryMapper;
import com.acme.salary.repository.EmployeeRepository;
import com.acme.salary.repository.SalaryRepository;
import com.acme.salary.service.ISalaryService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@AllArgsConstructor
public class SalaryServiceImpl implements ISalaryService {

    private final SalaryRepository salaryRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    @Transactional(readOnly = true)
    public SalaryDto getSalary(Long salaryId) {
        return SalaryMapper.toDto(findSalary(salaryId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalaryDto> getSalaries(String employeeCode) {
        Employee employee = findEmployee(employeeCode);
        return employee.getSalaries().stream()
                .map(SalaryMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public SalaryDto addSalary(String employeeCode, SalaryDto salaryDto) {
        Employee employee = findEmployee(employeeCode);
        Salary salary = SalaryMapper.toEntity(salaryDto);
        if (salary.getEffectiveTill() == null) {
            closeCurrentOpenSalary(employee, salary.getEffectiveFrom());
        }
        employee.addSalary(salary);
        return SalaryMapper.toDto(salaryRepository.save(salary));
    }

    /**
     * An employee has at most one open-ended salary. When a new open-ended salary is added,
     * the previous one ends the day before the new one starts.
     */
    private void closeCurrentOpenSalary(Employee employee, LocalDate newStart) {
        for (Salary existing : employee.getSalaries()) {
            if (existing.getEffectiveTill() != null) {
                continue;
            }
            if (!newStart.isAfter(existing.getEffectiveFrom())) {
                throw new IllegalArgumentException(
                        "A new open-ended salary must start after the current one, which starts on "
                                + existing.getEffectiveFrom());
            }
            existing.setEffectiveTill(newStart.minusDays(1));
        }
    }

    @Override
    @Transactional
    public boolean updateSalary(Long salaryId, SalaryDto salaryDto) {
        Salary salary = findSalary(salaryId);
        SalaryMapper.updateEntity(salaryDto, salary);
        salaryRepository.save(salary);
        return true;
    }

    @Override
    @Transactional
    public boolean deleteSalary(Long salaryId) {
        Salary salary = findSalary(salaryId);
        salaryRepository.delete(salary);
        return true;
    }

    private Salary findSalary(Long salaryId) {
        return salaryRepository.findById(salaryId)
                .orElseThrow(() -> new ResourceNotFoundException("Salary", "salaryId", String.valueOf(salaryId)));
    }

    private Employee findEmployee(String employeeCode) {
        return employeeRepository.findById(employeeCode)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "employeeCode", employeeCode));
    }
}