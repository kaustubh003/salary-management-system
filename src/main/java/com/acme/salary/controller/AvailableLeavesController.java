package com.acme.salary.controller;

import com.acme.salary.dto.AvailableLeavesDto;
import com.acme.salary.enums.LeaveType;
import com.acme.salary.service.IAvailableLeavesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "AvailableLeaves", description = "Create, read, update and delete available leaves")
@RestController
@RequestMapping(path = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
public class AvailableLeavesController {

    private final IAvailableLeavesService availableLeavesService;

    @Operation(summary = "Get available leaves for an employee for a leave type")
    @GetMapping(value = "/{employeeCode}/availableLeaves", params = "leaveType")
    public ResponseEntity<AvailableLeavesDto> getAvailableLeaves(@PathVariable String employeeCode,
                                                                 @RequestParam("leaveType") LeaveType leaveType) {
        return ResponseEntity.ok(availableLeavesService.getAvailableLeaves(employeeCode, leaveType));
    }

    @Operation(summary = "List all available leaves of an employee")
    @GetMapping(value = "/{employeeCode}/availableLeaves", params = "!leaveType")
    public ResponseEntity<List<AvailableLeavesDto>> getAllAvailableLeaves(@PathVariable String employeeCode) {
        return ResponseEntity.ok(availableLeavesService.getAvailableLeavesByEmployeeCode(employeeCode));
    }

    @Operation(summary = "Add an available leave for an employee")
    @PostMapping(path = "/availableLeaves/addAvailableLeaves", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AvailableLeavesDto> addAvailableLeaves(@Valid @RequestBody AvailableLeavesDto availableLeavesDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(availableLeavesService.addAvailableLeaves(availableLeavesDto));
    }

    @Operation(summary = "Create or update the employee's available leaves of a leave type")
    @PutMapping(path = "/availableLeaves/updateAvailableLeaves", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> updateAvailableLeaves(@Valid @RequestBody AvailableLeavesDto availableLeavesDto) {
        boolean updated = availableLeavesService.updateAvailableLeaves(availableLeavesDto);
        if (updated) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    @Operation(summary = "Delete available leaves of a leave type for an employee")
    @DeleteMapping("/{employeeCode}/deleteAvailableLeaves")
    public ResponseEntity<Void> deleteAvailableLeaves(@PathVariable String employeeCode,
                                                      @RequestParam LeaveType leaveType) {
        boolean deleted = availableLeavesService.deleteAvailableLeaves(employeeCode, leaveType);
        if (deleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}