package com.acme.salary.controller;

import com.acme.salary.dto.EmployeeDto;
import com.acme.salary.service.IEmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Employee", description = "Create, read, update and deactivate employees")
@RestController
@RequestMapping(path = "/api/employees", produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
public class EmployeeController {

    private final IEmployeeService employeeService;

    @Operation(summary = "Get an employee by code, with the salary currently in force")
    @GetMapping("/{employeeCode}")
    public ResponseEntity<EmployeeDto> getEmployee(@PathVariable String employeeCode) {
        return ResponseEntity.ok(employeeService.getEmployee(employeeCode));
    }

    @Operation(summary = "List employees, paginated. Use page, size and sort query parameters")
    @GetMapping
    public ResponseEntity<Page<EmployeeDto>> getEmployees(
            @PageableDefault(size = 20, sort = "employeeCode") Pageable pageable) {
        return ResponseEntity.ok(employeeService.getEmployees(pageable));
    }

    @Operation(summary = "Create an employee, optionally with a first salary")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EmployeeDto> addEmployee(@Valid @RequestBody EmployeeDto employeeDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.addEmployee(employeeDto));
    }

    @Operation(summary = "Update an employee's details. The employee code identifies the record and cannot change")
    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> updateEmployee(@Valid @RequestBody EmployeeDto employeeDto) {
        boolean updated = employeeService.updateEmployee(employeeDto);
        if (updated) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    @Operation(summary = "Deactivate an employee. The record and payment history are kept")
    @PatchMapping("/{employeeCode}/deactivate")
    public ResponseEntity<Void> deactivateEmployee(@PathVariable String employeeCode) {
        boolean deactivated = employeeService.deactivateEmployee(employeeCode);
        if (deactivated) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}