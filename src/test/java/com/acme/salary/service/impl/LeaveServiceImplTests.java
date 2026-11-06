package com.acme.salary.service.impl;

import com.acme.salary.dto.LeaveDto;
import com.acme.salary.entity.AvailableLeaves;
import com.acme.salary.entity.Leave;
import com.acme.salary.enums.LeaveType;
import com.acme.salary.exception.LeaveLockedException;
import com.acme.salary.exception.ResourceAlreadyExistsException;
import com.acme.salary.exception.ResourceNotFoundException;
import com.acme.salary.repository.AvailableLeavesRepository;
import com.acme.salary.repository.EmployeeRepository;
import com.acme.salary.repository.LeaveRepository;
import com.acme.salary.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeaveServiceImplTests {

    private static final LocalDate WEDNESDAY = LocalDate.of(2026, 11, 4);
    private static final LocalDate THURSDAY = LocalDate.of(2026, 11, 5);
    private static final LocalDate SATURDAY = LocalDate.of(2026, 11, 7);

    @Mock
    private LeaveRepository leaveRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private AvailableLeavesRepository availableLeavesRepository;

    private LeaveServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new LeaveServiceImpl(leaveRepository, employeeRepository, paymentRepository, availableLeavesRepository);
    }

    private Leave leave(LocalDate date, LeaveType type) {
        return Leave.record("P00001", date, type);
    }

    private LeaveDto leaveDto(LocalDate date, LeaveType type) {
        LeaveDto dto = new LeaveDto();
        dto.setLeaveDate(date);
        dto.setLeaveType(type);
        return dto;
    }

    /**
     * Makes the payment lookup report that one of the employee's payments covers this date.
     */
    private void paymentCovers() {
        when(paymentRepository.existsByEmployeeCodeAndPeriodStartLessThanEqualAndPeriodEndGreaterThanEqual(
                "P00001", LeaveServiceImplTests.WEDNESDAY, LeaveServiceImplTests.WEDNESDAY)).thenReturn(true);
    }

    @Test
    void getLeaveReturnsDtoWhenFound() {
        when(leaveRepository.findById(1L)).thenReturn(Optional.of(leave(WEDNESDAY, LeaveType.CASUAL)));

        LeaveDto dto = service.getLeave(1L);

        assertEquals("P00001", dto.getEmployeeCode());
        assertEquals(WEDNESDAY, dto.getLeaveDate());
        assertEquals(LeaveType.CASUAL, dto.getLeaveType());
    }

    @Test
    void getLeaveThrowsWhenNotFound() {
        when(leaveRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getLeave(99L));
    }

    @Test
    void getLeavesReturnsTheEmployeesLeaves() {
        when(employeeRepository.existsById("P00001")).thenReturn(true);
        when(leaveRepository.findByEmployeeCodeOrderByLeaveDate("P00001"))
                .thenReturn(List.of(leave(WEDNESDAY, LeaveType.CASUAL), leave(THURSDAY, LeaveType.SICK)));

        List<LeaveDto> result = service.getLeaves("P00001");

        assertEquals(2, result.size());
    }

    @Test
    void getLeavesThrowsWhenEmployeeNotFound() {
        when(employeeRepository.existsById("NOPE00")).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.getLeaves("NOPE00"));
        verify(leaveRepository, never()).findByEmployeeCodeOrderByLeaveDate(any(String.class));
    }

    @Test
    void addLeaveSavesTheLeaveAndDeductsFromAvailableLeaves() {
        when(employeeRepository.existsById("P00001")).thenReturn(true);
        when(availableLeavesRepository.findByEmployeeCodeAndLeaveType("P00001", LeaveType.UNPAID)).thenReturn(Optional.of(new AvailableLeaves("P00001", LeaveType.UNPAID, 0)));
        when(availableLeavesRepository.save(any(AvailableLeaves.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(leaveRepository.save(any(Leave.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.addLeave("P00001", leaveDto(WEDNESDAY, LeaveType.UNPAID));

        ArgumentCaptor<AvailableLeaves> savedAvailableLeave = ArgumentCaptor.forClass(AvailableLeaves.class);
        ArgumentCaptor<Leave> saved = ArgumentCaptor.forClass(Leave.class);
        verify(availableLeavesRepository).save(savedAvailableLeave.capture());
        verify(leaveRepository).save(saved.capture());
        assertEquals(-1, savedAvailableLeave.getValue().getAvailableLeaves());
        assertEquals("P00001", saved.getValue().getEmployeeCode());
        assertEquals(WEDNESDAY, saved.getValue().getLeaveDate());
        assertEquals(LeaveType.UNPAID, saved.getValue().getLeaveType());

    }

    @Test
    void addLeaveThrowsWhenEmployeeNotFound() {
        when(employeeRepository.existsById("NOPE00")).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> service.addLeave("NOPE00", leaveDto(WEDNESDAY, LeaveType.CASUAL)));
        verify(leaveRepository, never()).save(any(Leave.class));
    }

    @Test
    void addLeaveRejectsASaturday() {
        when(employeeRepository.existsById("P00001")).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> service.addLeave("P00001", leaveDto(SATURDAY, LeaveType.CASUAL)));
        verify(leaveRepository, never()).save(any(Leave.class));
    }

    @Test
    void addLeaveRejectsASecondLeaveOnTheSameDate() {
        when(employeeRepository.existsById("P00001")).thenReturn(true);
        when(leaveRepository.existsByEmployeeCodeAndLeaveDate("P00001", WEDNESDAY)).thenReturn(true);

        assertThrows(ResourceAlreadyExistsException.class,
                () -> service.addLeave("P00001", leaveDto(WEDNESDAY, LeaveType.CASUAL)));
        verify(leaveRepository, never()).save(any(Leave.class));
    }

    @Test
    void addLeaveRejectsADateInsideAPaidPeriod() {
        when(employeeRepository.existsById("P00001")).thenReturn(true);
        paymentCovers();

        assertThrows(LeaveLockedException.class,
                () -> service.addLeave("P00001", leaveDto(WEDNESDAY, LeaveType.UNPAID)));
        verify(leaveRepository, never()).save(any(Leave.class));
    }
}