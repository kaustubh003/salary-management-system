package com.acme.salary.service;

import com.acme.salary.dto.LeaveDto;

import java.util.List;

/**
 * Leaves are immutable: they can be read, recorded and deleted, but never updated.
 */
public interface ILeaveService {

    /**
     * @param leaveId the Leave ID
     * @return Leave DTO object based on leave ID.
     */
    LeaveDto getLeave(Long leaveId);

    /**
     * @param employeeCode the Employee code
     * @return all leaves of the employee, earliest date first.
     */
    List<LeaveDto> getLeaves(String employeeCode);

    /**
     * @param employeeCode the Employee code
     * @param leaveDto     the Leave DTO (date and type)
     * @return the recorded Leave DTO, including its generated ID.
     */
    LeaveDto addLeave(String employeeCode, LeaveDto leaveDto);

}
