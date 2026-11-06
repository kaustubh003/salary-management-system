package com.acme.salary.mapper;

import com.acme.salary.dto.AvailableLeavesDto;
import com.acme.salary.entity.AvailableLeaves;

public final class AvailableLeavesMapper {

    private AvailableLeavesMapper() {
    }

    public static AvailableLeavesDto toDto(AvailableLeaves availableLeaves) {
        AvailableLeavesDto dto = new AvailableLeavesDto();
        dto.setId(availableLeaves.getId());
        dto.setEmployeeCode(availableLeaves.getEmployeeCode());
        dto.setLeaveType(availableLeaves.getLeaveType());
        dto.setAvailableLeaves(availableLeaves.getAvailableLeaves());
        return dto;
    }

    public static AvailableLeaves toEntity(AvailableLeavesDto dto) {
        return new AvailableLeaves(
                dto.getEmployeeCode(),
                dto.getLeaveType(),
                dto.getAvailableLeaves()
        );
    }

    public static void updateEntity(AvailableLeavesDto dto, AvailableLeaves availableLeaves) {
        availableLeaves.setAvailableLeaves(dto.getAvailableLeaves());
    }
}
