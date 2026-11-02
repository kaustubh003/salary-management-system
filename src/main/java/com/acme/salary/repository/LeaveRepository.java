package com.acme.salary.repository;

import com.acme.salary.entity.Leave;
import com.acme.salary.enums.LeaveType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveRepository extends JpaRepository<Leave, Long> {

    List<Leave> findByEmployeeCodeOrderByLeaveDate(String employeeCode);

    boolean existsByEmployeeCodeAndLeaveDate(String employeeCode, LocalDate leaveDate);

    long countByEmployeeCodeAndLeaveTypeAndLeaveDateBetween(String employeeCode, LeaveType leaveType,
                                                            LocalDate from, LocalDate to);
}
