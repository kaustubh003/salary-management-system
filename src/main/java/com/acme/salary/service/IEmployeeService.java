package com.acme.salary.service;

import com.acme.salary.dto.EmployeeDto;
import com.acme.salary.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IEmployeeService {

    /**
     * @param employeeCode the Employee code
     * @return Employee DTO object based on employee code.
     */
    EmployeeDto getEmployee(String employeeCode);

    /**
     * @return Page of list of Employees using pagination and sorting.
     */
    Page<Employee> getEmployees(Pageable pageable);

    /**
     * @param employeeDto the Employee DTO
     */
    void addEmployee(EmployeeDto employeeDto);

    /**
     * @param employeeDto the Employee DTO
     * @return true if updated successfully, false if update failed.
     */
    boolean updateEmployee(EmployeeDto employeeDto);

    /**
     * @param employeeCode the Employee code
     * @return  true if deleted succcessfully, false if delete failed.
     */
    boolean deactivateEmployee(String employeeCode);

}
