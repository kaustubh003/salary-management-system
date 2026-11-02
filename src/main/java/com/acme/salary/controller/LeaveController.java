package com.acme.salary.controller;

import com.acme.salary.dto.LeaveDto;
import com.acme.salary.service.ILeaveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Leave", description = "Record, read and delete leaves. Leaves cannot be updated, and cannot fall on a Saturday or Sunday")
@RestController
@RequestMapping(path = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
public class LeaveController {

    private final ILeaveService leaveService;

    @Operation(summary = "Get a leave by ID")
    @GetMapping("/leaves/{leaveId}")
    public ResponseEntity<LeaveDto> getLeave(@PathVariable Long leaveId) {
        return ResponseEntity.ok(leaveService.getLeave(leaveId));
    }

    @Operation(summary = "List all leaves of an employee, earliest date first")
    @GetMapping("/employees/{employeeCode}/leaves")
    public ResponseEntity<List<LeaveDto>> getLeaves(@PathVariable String employeeCode) {
        return ResponseEntity.ok(leaveService.getLeaves(employeeCode));
    }

    @Operation(summary = "Record a leave for an employee. Weekend dates are rejected, and so is a second leave on the same date")
    @PostMapping(path = "/employees/{employeeCode}/leaves", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LeaveDto> addLeave(@PathVariable String employeeCode, @Valid @RequestBody LeaveDto leaveDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(leaveService.addLeave(employeeCode, leaveDto));
    }

}
