package com.acme.salary.service.impl;

import com.acme.salary.dto.AvailableLeavesDto;
import com.acme.salary.entity.AvailableLeaves;
import com.acme.salary.entity.Employee;
import com.acme.salary.enums.LeaveType;
import com.acme.salary.exception.ResourceNotFoundException;
import com.acme.salary.mapper.AvailableLeavesMapper;
import com.acme.salary.repository.AvailableLeavesRepository;
import com.acme.salary.repository.EmployeeRepository;
import com.acme.salary.service.IAvailableLeavesService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class AvailableLeavesServiceImpl implements IAvailableLeavesService {

    private final AvailableLeavesRepository availableLeavesRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional(readOnly = true)
    public AvailableLeavesDto getAvailableLeaves(String employeeCode, LeaveType leaveType) {
        Employee employee = employeeRepository.findById(employeeCode).orElseThrow(
                () -> new ResourceNotFoundException("Employee", "employeeCode", employeeCode)
        );

        return AvailableLeavesMapper.toDto(availableLeavesRepository.findByEmployeeCodeAndLeaveType(employeeCode, leaveType).orElseThrow(
                () -> new ResourceNotFoundException("AvailableLeaves", "employeeCode or leaveType", employeeCode + ", " + leaveType)
        ));
    }

    @Transactional(readOnly = true)
    public List<AvailableLeavesDto> getAvailableLeavesByEmployeeCode(String employeeCode) {
        Employee employee = employeeRepository.findById(employeeCode).orElseThrow(
                () -> new ResourceNotFoundException("Employee", "employeeCode", employeeCode)
        );

        return availableLeavesRepository.findByEmployeeCodeOrderByEmployeeCodeAscLeaveTypeAsc(employeeCode).stream()
                .map(AvailableLeavesMapper::toDto)
                .toList();
    }

    @Transactional
    public AvailableLeavesDto addAvailableLeaves(AvailableLeavesDto availableLeavesDto) {
        Employee employee = employeeRepository.findById(availableLeavesDto.getEmployeeCode()).orElseThrow(
                () -> new ResourceNotFoundException("Employee", "employeeCode", availableLeavesDto.getEmployeeCode())
        );

        return AvailableLeavesMapper.toDto(availableLeavesRepository.save(AvailableLeavesMapper.toEntity(availableLeavesDto)));
    }

    @Transactional
    public boolean updateAvailableLeaves(AvailableLeavesDto availableLeavesDto) {
        AvailableLeaves availableLeaves = availableLeavesRepository.findById(availableLeavesDto.getId()).orElseThrow(
                () -> new ResourceNotFoundException("AvailableLeaves", "employeeCode", availableLeavesDto.getEmployeeCode())
        );
        AvailableLeavesMapper.updateEntity(availableLeavesDto, availableLeaves);
        availableLeavesRepository.save(availableLeaves);
        return true;
    }

    @Transactional
    public boolean deleteAvailableLeaves(String employeeCode, LeaveType leaveType) {
        AvailableLeaves availableLeaves = availableLeavesRepository.findByEmployeeCodeAndLeaveType(employeeCode, leaveType).orElseThrow(
                () -> new ResourceNotFoundException("AvailableLeaves", "employeeCode or leaveType", employeeCode + ", " + leaveType)
        );
        availableLeavesRepository.delete(availableLeaves);
        return true;
    }
}
