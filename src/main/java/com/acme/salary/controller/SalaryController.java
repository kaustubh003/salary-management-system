package com.acme.salary.controller;

import com.acme.salary.dto.SalaryDto;
import com.acme.salary.service.ISalaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Salary", description = "Create, read, update and delete salary records")
@RestController
@RequestMapping(path = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
public class SalaryController {

    private final ISalaryService salaryService;

    @Operation(summary = "Get a salary record by ID")
    @GetMapping("/salaries/{salaryId}")
    public ResponseEntity<SalaryDto> getSalary(@PathVariable Long salaryId) {
        return ResponseEntity.ok(salaryService.getSalary(salaryId));
    }

    @Operation(summary = "List all salary records of an employee, oldest first")
    @GetMapping("/employees/{employeeCode}/salaries")
    public ResponseEntity<List<SalaryDto>> getSalaries(@PathVariable String employeeCode) {
        return ResponseEntity.ok(salaryService.getSalaries(employeeCode));
    }

    @Operation(summary = "Add a salary record to an employee. A new open-ended salary ends the previous one the day before it starts")    @PostMapping(path = "/employees/{employeeCode}/salaries", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SalaryDto> addSalary(@PathVariable String employeeCode,
                                               @Valid @RequestBody SalaryDto salaryDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(salaryService.addSalary(employeeCode, salaryDto));
    }

    @Operation(summary = "Replace the details of a salary record. Net is recalculated, and past payments are unaffected")
    @PutMapping(path = "/salaries/{salaryId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> updateSalary(@PathVariable Long salaryId,
                                             @Valid @RequestBody SalaryDto salaryDto) {
        boolean updated = salaryService.updateSalary(salaryId, salaryDto);
        if (updated) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    @Operation(summary = "Delete a salary record. Payments already made keep their own copy of the amounts")
    @DeleteMapping("/salaries/{salaryId}")
    public ResponseEntity<Void> deleteSalary(@PathVariable Long salaryId) {
        boolean deleted = salaryService.deleteSalary(salaryId);
        if (deleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}