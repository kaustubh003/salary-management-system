package com.acme.salary.service.impl;

import com.acme.salary.dto.LeaveDto;
import com.acme.salary.entity.AvailableLeaves;
import com.acme.salary.entity.Leave;
import com.acme.salary.enums.LeaveType;
import com.acme.salary.exception.LeaveLockedException;
import com.acme.salary.exception.ResourceAlreadyExistsException;
import com.acme.salary.exception.ResourceNotFoundException;
import com.acme.salary.mapper.LeaveMapper;
import com.acme.salary.repository.AvailableLeavesRepository;
import com.acme.salary.repository.EmployeeRepository;
import com.acme.salary.repository.LeaveRepository;
import com.acme.salary.repository.PaymentRepository;
import com.acme.salary.service.ILeaveService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class LeaveServiceImpl implements ILeaveService {

    private final LeaveRepository leaveRepository;
    private final EmployeeRepository employeeRepository;
    private final PaymentRepository paymentRepository;
    private final AvailableLeavesRepository availableLeavesRepository;

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
        requireNotInPaidPeriod(employeeCode, leave.getLeaveDate());
        deductFromAvailableLeaves(employeeCode, leaveDto.getLeaveType());
        return LeaveMapper.toDto(leaveRepository.save(leave));
    }

    /**
     * Payments are immutable and have already taken leave into account, so leaves on a date
     * inside one of the employee's paid periods are frozen.
     */
    private void requireNotInPaidPeriod(String employeeCode, LocalDate date) {
        boolean covered = paymentRepository
                .existsByEmployeeCodeAndPeriodStartLessThanEqualAndPeriodEndGreaterThanEqual(employeeCode, date, date);
        if (covered) {
            throw new LeaveLockedException("A payment for employee " + employeeCode + " already covers " + date + ", so leaves on this date can no longer be added or deleted");
        }
    }

    /**
     * Deducts leave from available leaves of the leave type.
     * Adds the availableLeaves record for the leave type if it doesn't exist.
     */
    private void deductFromAvailableLeaves(String employeeCode, LeaveType leaveType) {
        Optional<AvailableLeaves> availableLeaves = availableLeavesRepository.findByEmployeeCodeAndLeaveType(employeeCode, leaveType);
        if (availableLeaves.isPresent()) {
            AvailableLeaves newAvailableLeaves = availableLeaves.get();
            newAvailableLeaves.setAvailableLeaves(newAvailableLeaves.getAvailableLeaves() - 1);
            availableLeavesRepository.save(newAvailableLeaves);
        } else {
            AvailableLeaves newAvailableLeaves = new AvailableLeaves(
                    employeeCode,
                    leaveType,
                    -1
            );
            availableLeavesRepository.save(newAvailableLeaves);
        }
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
