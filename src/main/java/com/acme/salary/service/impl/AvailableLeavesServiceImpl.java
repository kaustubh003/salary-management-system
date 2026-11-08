package com.acme.salary.service.impl;

import com.acme.salary.dto.AvailableLeavesDto;
import com.acme.salary.entity.AvailableLeaves;
import com.acme.salary.enums.LeaveType;
import com.acme.salary.exception.ResourceAlreadyExistsException;
import com.acme.salary.exception.ResourceNotFoundException;
import com.acme.salary.mapper.AvailableLeavesMapper;
import com.acme.salary.repository.AvailableLeavesRepository;
import com.acme.salary.repository.EmployeeRepository;
import com.acme.salary.service.IAvailableLeavesService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class AvailableLeavesServiceImpl implements IAvailableLeavesService {

    private final AvailableLeavesRepository availableLeavesRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional
    public AvailableLeavesDto getAvailableLeaves(String employeeCode, LeaveType leaveType) {
        if (!employeeRepository.existsById(employeeCode)) {
            throw new ResourceNotFoundException("Employee", "employeeCode", employeeCode);
        }

        AvailableLeaves availableLeaves = availableLeavesRepository.findByEmployeeCodeAndLeaveType(employeeCode, leaveType)
                .orElseGet(() -> {
                    AvailableLeaves emptyBalance = new AvailableLeaves(employeeCode, leaveType, 0);
                    return leaveType == LeaveType.UNPAID
                            ? emptyBalance
                            : availableLeavesRepository.save(emptyBalance);
                });
        return AvailableLeavesMapper.toDto(availableLeaves);
    }

    @Transactional
    public List<AvailableLeavesDto> getAvailableLeavesByEmployeeCode(String employeeCode) {
        if (!employeeRepository.existsById(employeeCode)) {
            throw new ResourceNotFoundException("Employee", "employeeCode", employeeCode);
        }

        List<AvailableLeaves> availableLeavesList = availableLeavesRepository
                .findByEmployeeCodeOrderByEmployeeCodeAscLeaveTypeAsc(employeeCode);

        // Collect existing leave types into a Set for quick O(1) lookups
        Set<LeaveType> existingLeaveTypes = availableLeavesList.stream()
                .map(AvailableLeaves::getLeaveType)
                .collect(Collectors.toSet());

        List<AvailableLeaves> newlyCreatedLeaves = new ArrayList<>();

        // Iterate through all enum values to find and create missing ones (except UNPAID leaves)
        for (LeaveType leaveType : LeaveType.values()) {
            if (!existingLeaveTypes.contains(leaveType)
                    && !leaveType.equals(LeaveType.UNPAID)) {
                AvailableLeaves availableLeaves = new AvailableLeaves(
                        employeeCode,
                        leaveType,
                        0
                );
                newlyCreatedLeaves.add(availableLeaves);
            }
        }

        if (!newlyCreatedLeaves.isEmpty()) {
            availableLeavesRepository.saveAll(newlyCreatedLeaves);
            newlyCreatedLeaves.addAll(availableLeavesList);
            return newlyCreatedLeaves.stream()
                    .sorted(Comparator.comparing(AvailableLeaves::getEmployeeCode).thenComparing(AvailableLeaves::getLeaveType))
                    .map(AvailableLeavesMapper::toDto)
                    .collect(Collectors.toList());
        }

        return availableLeavesList.stream()
                .sorted(Comparator.comparing(AvailableLeaves::getEmployeeCode).thenComparing(AvailableLeaves::getLeaveType))
                .map(AvailableLeavesMapper::toDto)
                .collect(Collectors.toList());

    }

    @Transactional
    public AvailableLeavesDto addAvailableLeaves(AvailableLeavesDto availableLeavesDto) {
        requireNotUnpaid(availableLeavesDto.getLeaveType());
        if (!employeeRepository.existsById(availableLeavesDto.getEmployeeCode())) {
            throw new ResourceNotFoundException("Employee", "employeeCode", availableLeavesDto.getEmployeeCode());
        }
        // A balance may already exist, for example because looking it up created it. Use update to change it
        if (availableLeavesRepository.findByEmployeeCodeAndLeaveType(
                availableLeavesDto.getEmployeeCode(), availableLeavesDto.getLeaveType()).isPresent()) {
            throw new ResourceAlreadyExistsException("AvailableLeaves", "leaveType",
                    availableLeavesDto.getLeaveType().name());
        }

        return AvailableLeavesMapper.toDto(availableLeavesRepository.save(AvailableLeavesMapper.toEntity(availableLeavesDto)));
    }

    @Transactional
    public boolean updateAvailableLeaves(AvailableLeavesDto availableLeavesDto) {
        requireNotUnpaid(availableLeavesDto.getLeaveType());
        if (!employeeRepository.existsById(availableLeavesDto.getEmployeeCode())) {
            throw new ResourceNotFoundException("Employee", "employeeCode", availableLeavesDto.getEmployeeCode());
        }

        AvailableLeaves availableLeaves = availableLeavesRepository
                .findByEmployeeCodeAndLeaveType(availableLeavesDto.getEmployeeCode(), availableLeavesDto.getLeaveType())
                .orElseGet(() -> AvailableLeavesMapper.toEntity(availableLeavesDto));
        AvailableLeavesMapper.updateEntity(availableLeavesDto, availableLeaves);
        availableLeavesRepository.save(availableLeaves);
        return true;
    }

    private void requireNotUnpaid(LeaveType leaveType) {
        if (leaveType == LeaveType.UNPAID) {
            throw new IllegalArgumentException("UNPAID leave balances cannot be created or updated.");
        }
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
