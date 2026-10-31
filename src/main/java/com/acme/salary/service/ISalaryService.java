package com.acme.salary.service;

import com.acme.salary.dto.SalaryDto;

import java.util.List;

public interface ISalaryService {

    /**
     * @param salaryId the Salary ID
     * @return Salary DTO object based on salary ID.
     */
    SalaryDto getSalary(Long salaryId);

    /**
     * @param employeeCode the Employee code
     * @return all salary records of the employee, oldest first.
     */
    List<SalaryDto> getSalaries(String employeeCode);

    /**
     * @param employeeCode the Employee code
     * @param salaryDto    the Salary DTO
     * @return the created Salary DTO, including its generated ID.
     */
    SalaryDto addSalary(String employeeCode, SalaryDto salaryDto);

    /**
     * @param salaryId  the Salary ID
     * @param salaryDto the Salary DTO
     * @return true if updated successfully, false if update failed.
     */
    boolean updateSalary(Long salaryId, SalaryDto salaryDto);

    /**
     * @param salaryId the Salary ID
     * @return true if deleted successfully, false if delete failed.
     */
    boolean deleteSalary(Long salaryId);

}