package com.acme.salary.service;

import com.acme.salary.dto.EmployeeDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IEmployeeService {

    /**
     * @param employeeCode the Employee code
     * @return Employee DTO object based on employee code.
     */
    EmployeeDto getEmployee(String employeeCode);

    /**
     * @return Page of list of Employees using pagination and sorting.
     */
    Page<EmployeeDto> getEmployees(Pageable pageable);

    Page<EmployeeDto> getEmployees(Pageable pageable, String employeeCode, String department, String jobTitle);

    List<String> getActiveDepartments();

    List<String> getActiveJobTitles();

    int importEmployees(MultipartFile file);

    /**
     * @param employeeDto Employee DTO
     * @return Employee DTO
     */
    EmployeeDto addEmployee(EmployeeDto employeeDto);

    /**
     * @param employeeDto the Employee DTO
     * @return true if updated successfully, false if update failed.
     */
    boolean updateEmployee(EmployeeDto employeeDto);

    /**
     * @param employeeCode the Employee code
     * @return true if deleted succcessfully, false if delete failed.
     */
    boolean deactivateEmployee(String employeeCode);

}
