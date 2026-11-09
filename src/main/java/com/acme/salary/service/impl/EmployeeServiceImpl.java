package com.acme.salary.service.impl;

import com.acme.salary.dto.EmployeeDto;
import com.acme.salary.entity.Employee;
import com.acme.salary.exception.ResourceAlreadyExistsException;
import com.acme.salary.exception.ResourceNotFoundException;
import com.acme.salary.mapper.EmployeeMapper;
import com.acme.salary.repository.EmployeeRepository;
import com.acme.salary.service.IEmployeeService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.AllArgsConstructor;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class EmployeeServiceImpl implements IEmployeeService {

    private static final List<String> IMPORT_COLUMNS = List.of(
            "employeeCode", "firstName", "lastName", "department", "jobTitle",
            "address", "phone", "email", "country");

    private final EmployeeRepository employeeRepository;
    private final Validator validator;

    @Transactional
    public EmployeeDto getEmployee(String employeeCode) {
        Employee employee = employeeRepository.findById(employeeCode).orElseThrow(
                () -> new ResourceNotFoundException("Employee", "employeeCode", employeeCode)
        );
        return EmployeeMapper.toDto(employee);
    }

    @Transactional
    public Page<EmployeeDto> getEmployees(Pageable pageable) {
        return employeeRepository.findByActiveTrue(limitPageSize(pageable)).map(EmployeeMapper::toDto);
    }

    @Transactional
    public Page<EmployeeDto> getEmployees(Pageable pageable, String employeeCode, String department, String jobTitle) {
        return employeeRepository.findActiveEmployees(
                blankToNull(employeeCode), blankToNull(department), blankToNull(jobTitle), limitPageSize(pageable))
                .map(EmployeeMapper::toDto);
    }

    @Transactional(readOnly = true)
    public List<String> getActiveDepartments() {
        return employeeRepository.findActiveDepartments();
    }

    @Transactional(readOnly = true)
    public List<String> getActiveJobTitles() {
        return employeeRepository.findActiveJobTitles();
    }

    @Transactional
    public int importEmployees(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Select a non-empty Excel file to import.");
        }
        String filename = file.getOriginalFilename();
        String lowerFilename = filename == null ? "" : filename.toLowerCase(Locale.ROOT);
        if (!(lowerFilename.endsWith(".xlsx") || lowerFilename.endsWith(".xls"))) {
            throw new IllegalArgumentException("The import file must be an .xlsx or .xls Excel workbook.");
        }

        List<EmployeeDto> employees;
        try (InputStream input = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(input)) {
            if (workbook.getNumberOfSheets() == 0) {
                throw new IllegalArgumentException("The Excel workbook does not contain a worksheet.");
            }
            employees = readEmployees(workbook.getSheetAt(0));
        } catch (IOException | EncryptedDocumentException exception) {
            throw new IllegalArgumentException("Unable to read the Excel workbook.", exception);
        }

        if (employees.isEmpty()) {
            throw new IllegalArgumentException("The Excel workbook contains no employee records.");
        }

        // Check for duplicate or existing employee codes, phones or emails
        Set<String> employeeCodes = new HashSet<>();
        Set<String> existingEmployeeCodes = new HashSet<>(employeeRepository.getActiveEmployeeCodes());
        Set<String> phones = new HashSet<>();
        Set<String> existingPhones = new HashSet<>(employeeRepository.getActivePhones());
        Set<String> emails = new HashSet<>();
        Set<String> existingEmails =  new HashSet<>(employeeRepository.getActiveEmails());
        for (EmployeeDto employee : employees) {
            if (!employeeCodes.add(employee.getEmployeeCode())) {
                throw new IllegalArgumentException("Duplicate employeeCode " + employee.getEmployeeCode() + " in the workbook.");
            }
            if (existingEmployeeCodes.contains(employee.getEmployeeCode())) {
                throw new ResourceAlreadyExistsException("Employee", "employeeCode", employee.getEmployeeCode());
            }
            if (!phones.add(employee.getPhone())) {
                throw new IllegalArgumentException("Duplicate phone number " + employee.getPhone() + " in the workbook.");
            }
            if (existingPhones.contains(employee.getPhone())) {
                throw new ResourceAlreadyExistsException("Phone", "phone", employee.getPhone());
            }
            if (!emails.add(employee.getEmail())) {
                throw new IllegalArgumentException("Duplicate email " + employee.getEmail() + " in the workbook.");
            }
            if (existingEmails.contains(employee.getEmail())) {
                throw new ResourceAlreadyExistsException("Email", "email", employee.getEmail());
            }
        }

        employeeRepository.saveAll(employees.stream().map(EmployeeMapper::toEntity).toList());
        return employees.size();
    }

    private List<EmployeeDto> readEmployees(Sheet sheet) {
        Row header = sheet.getRow(0);
        if (header == null) {
            throw new IllegalArgumentException("The first worksheet row must contain the required column headers.");
        }

        DataFormatter formatter = new DataFormatter();
        FormulaEvaluator evaluator = sheet.getWorkbook().getCreationHelper().createFormulaEvaluator();
        List<String> headers = new ArrayList<>();
        for (int column = 0; column < IMPORT_COLUMNS.size(); column++) {
            headers.add(cellValue(header.getCell(column), formatter, evaluator));
        }
        if (!IMPORT_COLUMNS.equals(headers) || header.getLastCellNum() != IMPORT_COLUMNS.size()) {
            throw new IllegalArgumentException("Excel columns must be in this exact order: " + String.join(", ", IMPORT_COLUMNS));
        }

        List<EmployeeDto> employees = new ArrayList<>();
        for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null || isBlank(row, formatter, evaluator)) {
                continue;
            }

            EmployeeDto employee = new EmployeeDto();
            employee.setEmployeeCode(cellValue(row.getCell(0), formatter, evaluator));
            employee.setFirstName(cellValue(row.getCell(1), formatter, evaluator));
            employee.setLastName(cellValue(row.getCell(2), formatter, evaluator));
            employee.setDepartment(cellValue(row.getCell(3), formatter, evaluator));
            employee.setJobTitle(cellValue(row.getCell(4), formatter, evaluator));
            employee.setAddress(cellValue(row.getCell(5), formatter, evaluator));
            employee.setPhone(cellValue(row.getCell(6), formatter, evaluator));
            employee.setEmail(cellValue(row.getCell(7), formatter, evaluator));
            employee.setCountry(cellValue(row.getCell(8), formatter, evaluator));
            Set<ConstraintViolation<EmployeeDto>> violations = validator.validate(employee);
            if (!violations.isEmpty()) {
                String errors = violations.stream()
                        .map(violation -> violation.getPropertyPath() + " " + violation.getMessage())
                        .sorted()
                        .collect(Collectors.joining("; "));
                throw new IllegalArgumentException("Row " + (rowIndex + 1) + ": " + errors);
            }
            employees.add(employee);
        }
        return employees;
    }

    private static String cellValue(Cell cell, DataFormatter formatter, FormulaEvaluator evaluator) {
        return cell == null ? "" : formatter.formatCellValue(cell, evaluator).trim();
    }

    private static boolean isBlank(Row row, DataFormatter formatter, FormulaEvaluator evaluator) {
        for (int column = 0; column < IMPORT_COLUMNS.size(); column++) {
            if (!cellValue(row.getCell(column), formatter, evaluator).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private static Pageable limitPageSize(Pageable pageable) {
        if (pageable.isUnpaged()) {
            return PageRequest.of(0, 100, Sort.by(Sort.Direction.ASC, "employeeCode"));
        }
        return PageRequest.of(pageable.getPageNumber(), Math.min(pageable.getPageSize(), 100), pageable.getSort());
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    @Transactional
    public EmployeeDto addEmployee(EmployeeDto employeeDto) {
        if (employeeRepository.existsById(employeeDto.getEmployeeCode())) {
            throw new ResourceAlreadyExistsException("Employee", "employeeCode", employeeDto.getEmployeeCode());
        }
        return EmployeeMapper.toDto(employeeRepository.save(EmployeeMapper.toEntity(employeeDto)));
    }

    @Transactional
    public boolean updateEmployee(EmployeeDto employeeDto) {
        Employee employee = employeeRepository.findById(employeeDto.getEmployeeCode())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "employeeCode", employeeDto.getEmployeeCode()));
        EmployeeMapper.updateEntity(employeeDto, employee);
        employeeRepository.save(employee);
        return true;
    }

    @Transactional
    public boolean deactivateEmployee(String employeeCode) {
        Employee employee = employeeRepository.findById(employeeCode)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "employeeCode", employeeCode));
        employee.deactivate();
        employeeRepository.save(employee);
        return true;
    }
}