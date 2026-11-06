package com.acme.salary.repository;

import com.acme.salary.entity.LeavesMonthlyRefill;
import com.acme.salary.enums.LeaveType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LeavesMonthlyRefillRepository extends JpaRepository<LeavesMonthlyRefill, LeaveType> {

}
