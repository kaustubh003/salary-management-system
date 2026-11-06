package com.acme.salary.service;

import com.acme.salary.dto.AvailableLeavesDto;
import com.acme.salary.enums.LeaveType;

import java.util.List;

public interface IAvailableLeavesService {

    /**
     * @param employeeCode The Employee code
     * @param leaveType Type of leave
     * @return AvailableLeavesDto containing available leaves of a particular leave type.
     */
    AvailableLeavesDto getAvailableLeaves(String employeeCode, LeaveType leaveType);

    /**
     * @param employeeCode The Employee code
     * @return List of AvailableLeavesDto containing all types of available leaves.
     */
    List<AvailableLeavesDto> getAvailableLeavesByEmployeeCode(String employeeCode);

    /**
     * @param availableLeavesDto The AvailableLeavesDto
     * @return The AvailableLeavesDto
     */
    AvailableLeavesDto addAvailableLeaves(AvailableLeavesDto availableLeavesDto);

    /**
     * @param availableLeavesDto The AvailableLeavesDto
     * @return true if updated successfully, else false.
     */
    boolean updateAvailableLeaves(AvailableLeavesDto availableLeavesDto);

    /**
     * @param employeeCode The Employee code
     * @param leaveType Type of leave
     * @return true if deleted successfully, else false.
     */
    boolean deleteAvailableLeaves(String employeeCode, LeaveType leaveType);

}
