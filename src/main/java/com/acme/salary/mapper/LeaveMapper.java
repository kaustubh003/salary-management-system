package com.acme.salary.mapper;

import com.acme.salary.dto.LeaveDto;
import com.acme.salary.entity.Leave;

public final class LeaveMapper {

    private LeaveMapper() {
    }

    public static LeaveDto toDto(Leave leave) {
        LeaveDto dto = new LeaveDto();
        dto.setLeaveId(leave.getLeaveId());
        dto.setEmployeeCode(leave.getEmployeeCode());
        dto.setLeaveDate(leave.getLeaveDate());
        dto.setLeaveType(leave.getLeaveType());
        return dto;
    }

    /**
     * Builds a new Leave for the given employee. The entity rejects weekend dates.
     * The ID and employee code in the DTO are ignored.
     */
    public static Leave toEntity(String employeeCode, LeaveDto dto) {
        return Leave.record(employeeCode, dto.getLeaveDate(), dto.getLeaveType());
    }
}
