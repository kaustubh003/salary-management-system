package com.acme.salary.repository;

import com.acme.salary.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, String> {

    Page<Employee> findByActiveTrue(Pageable pageable);

    @Query("""
            SELECT e FROM Employee e
            WHERE e.active = true
              AND (:employeeCode IS NULL OR LOWER(e.employeeCode) LIKE LOWER(CONCAT('%', :employeeCode, '%')))
              AND (:department IS NULL OR e.department = :department)
              AND (:jobTitle IS NULL OR e.jobTitle = :jobTitle)
            """)
    Page<Employee> findActiveEmployees(@Param("employeeCode") String employeeCode,
                                       @Param("department") String department,
                                       @Param("jobTitle") String jobTitle,
                                       Pageable pageable);

    @Query("SELECT DISTINCT e.department FROM Employee e WHERE e.active = true ORDER BY e.department")
    List<String> findActiveDepartments();

    @Query("SELECT DISTINCT e.jobTitle FROM Employee e WHERE e.active = true ORDER BY e.jobTitle")
    List<String> findActiveJobTitles();

    @Query("SELECT e.employeeCode FROM Employee e where e.active = true")
    List<String> getActiveEmployeeCodes();

    @Query("SELECT e.phone FROM Employee e where e.active = true")
    List<String> getActivePhones();

    @Query("SELECT e.email FROM Employee e where e.active = true")
    List<String> getActiveEmails();

}
