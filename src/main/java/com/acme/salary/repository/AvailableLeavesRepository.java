package com.acme.salary.repository;

import com.acme.salary.entity.AvailableLeaves;
import com.acme.salary.enums.LeaveType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AvailableLeavesRepository extends JpaRepository<AvailableLeaves, Long> {
    Optional<AvailableLeaves> findByEmployeeCodeAndLeaveType(String employeeCode, LeaveType leaveType);

    List<AvailableLeaves> findByEmployeeCodeOrderByEmployeeCodeAscLeaveTypeAsc(String employeeCode);

    List<AvailableLeaves> findAllByLeaveType(LeaveType leaveType);
}
