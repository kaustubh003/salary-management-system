package com.acme.salary.service.impl;

import com.acme.salary.dto.EmployeeDto;
import com.acme.salary.entity.Employee;
import com.acme.salary.exception.ResourceAlreadyExistsException;
import com.acme.salary.exception.ResourceNotFoundException;
import com.acme.salary.mapper.EmployeeMapper;
import com.acme.salary.repository.EmployeeRepository;
import com.acme.salary.service.IEmployeeService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class EmployeeServiceImpl implements IEmployeeService {

    private EmployeeRepository employeeRepository;

    @Transactional
    public EmployeeDto getEmployee(String employeeCode) {
        Employee employee = employeeRepository.findById(employeeCode).orElseThrow(
                () -> new ResourceNotFoundException("Employee", "employeeCode", employeeCode)
        );
        return EmployeeMapper.toDto(employee);
    }

    public Page<EmployeeDto> getEmployees(Pageable pageable) {
        return employeeRepository.findAll(pageable).map(EmployeeMapper::toDto);
    }

    @Transactional
    public EmployeeDto addEmployee(EmployeeDto employeeDto) {
        if (employeeRepository.existsById(employeeDto.getEmployeeCode())) {
            throw new ResourceAlreadyExistsException("Employee", "employeeCode", employeeDto.getEmployeeCode());
        }
        return EmployeeMapper.toDto(employeeRepository.save(EmployeeMapper.toEntity(employeeDto)));
    }

    @Transactional
    public boolean updateEmployee(EmployeeDto employeeDto) {
        Employee employee = employeeRepository.findById(employeeDto.getEmployeeCode())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "employeeCode", employeeDto.getEmployeeCode()));
        EmployeeMapper.updateEntity(employeeDto, employee);
        employeeRepository.save(employee);
        return true;
    }

    @Transactional
    public boolean deactivateEmployee(String employeeCode) {
        Employee employee = employeeRepository.findById(employeeCode)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "employeeCode", employeeCode));
        employee.deactivate();
        employeeRepository.save(employee);
        return true;
    }
}