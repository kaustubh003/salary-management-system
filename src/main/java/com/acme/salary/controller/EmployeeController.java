package com.acme.salary.controller;

import com.acme.salary.dto.EmployeeDto;
import com.acme.salary.service.IEmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

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
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size,
            @RequestParam(defaultValue = "employeeCode,asc") String sort,
            @RequestParam(required = false) String employeeCode,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String jobTitle) {
        Pageable pageable = createPageable(page, size, sort);
        return ResponseEntity.ok(employeeService.getEmployees(pageable, employeeCode, department, jobTitle));
    }

    @GetMapping("/filters")
    public ResponseEntity<Map<String, Object>> getEmployeeFilters() {
        return ResponseEntity.ok(Map.of(
                "departments", employeeService.getActiveDepartments(),
                "jobTitles", employeeService.getActiveJobTitles()));
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

    @Operation(
            summary = "Import employees from an Excel workbook",
            description = "Upload an .xlsx or .xls workbook. The first worksheet's first row must contain these exact headers in order: employeeCode, firstName, lastName, department, jobTitle, address, phone, email, country.")
    @PostMapping(path = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Integer>> importEmployees(@RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(Map.of("importedCount", employeeService.importEmployees(file)));
    }

    private static Pageable createPageable(int page, int requestedSize, String requestedSort) {
        if (page < 0 || requestedSize < 1) {
            throw new IllegalArgumentException("Page must be non-negative and size must be at least 1.");
        }

        String[] sortParts = requestedSort.split(",");
        String property = sortParts[0].trim();
        if (!Set.of("employeeCode", "firstName", "lastName").contains(property)) {
            throw new IllegalArgumentException("Employees can only be sorted by employeeCode, firstName, or lastName.");
        }
        Sort.Direction direction = sortParts.length > 1
                ? Sort.Direction.fromString(sortParts[1].trim())
                : Sort.Direction.ASC;
        if (sortParts.length > 2) {
            throw new IllegalArgumentException("Only one employee sort field may be specified.");
        }

        return PageRequest.of(page, Math.min(requestedSize, 100), Sort.by(direction, property));
    }
}