package com.acme.salary.service.impl;

import com.acme.salary.dto.LeaveDto;
import com.acme.salary.entity.Leave;
import com.acme.salary.exception.ResourceAlreadyExistsException;
import com.acme.salary.exception.ResourceNotFoundException;
import com.acme.salary.mapper.LeaveMapper;
import com.acme.salary.repository.EmployeeRepository;
import com.acme.salary.repository.LeaveRepository;
import com.acme.salary.service.ILeaveService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class LeaveServiceImpl implements ILeaveService {

    private final LeaveRepository leaveRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    @Transactional(readOnly = true)
    public LeaveDto getLeave(Long leaveId) {
        return LeaveMapper.toDto(findLeave(leaveId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveDto> getLeaves(String employeeCode) {
        requireEmployee(employeeCode);
        return leaveRepository.findByEmployeeCodeOrderByLeaveDate(employeeCode).stream()
                .map(LeaveMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public LeaveDto addLeave(String employeeCode, LeaveDto leaveDto) {
        requireEmployee(employeeCode);
        // Builds the entity first, which rejects Saturdays and Sundays
        Leave leave = LeaveMapper.toEntity(employeeCode, leaveDto);
        if (leaveRepository.existsByEmployeeCodeAndLeaveDate(employeeCode, leave.getLeaveDate())) {
            throw new ResourceAlreadyExistsException("Leave", "leaveDate", leave.getLeaveDate().toString());
        }
        return LeaveMapper.toDto(leaveRepository.save(leave));
    }

    private Leave findLeave(Long leaveId) {
        return leaveRepository.findById(leaveId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave", "leaveId", String.valueOf(leaveId)));
    }

    private void requireEmployee(String employeeCode) {
        if (!employeeRepository.existsById(employeeCode)) {
            throw new ResourceNotFoundException("Employee", "employeeCode", employeeCode);
        }
    }
}
