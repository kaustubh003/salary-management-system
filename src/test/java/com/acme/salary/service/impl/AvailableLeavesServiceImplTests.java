package com.acme.salary.service.impl;

import com.acme.salary.dto.AvailableLeavesDto;
import com.acme.salary.entity.AvailableLeaves;
import com.acme.salary.enums.LeaveType;
import com.acme.salary.exception.ResourceNotFoundException;
import com.acme.salary.repository.AvailableLeavesRepository;
import com.acme.salary.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AvailableLeavesServiceImplTests {

    @Mock
    private AvailableLeavesRepository availableLeavesRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    private AvailableLeavesServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AvailableLeavesServiceImpl(availableLeavesRepository, employeeRepository);
    }

    private AvailableLeaves balance(LeaveType type, int days) {
        return new AvailableLeaves("P00001", type, days);
    }

    private AvailableLeavesDto dto(LeaveType type, int days) {
        AvailableLeavesDto dto = new AvailableLeavesDto();
        dto.setEmployeeCode("P00001");
        dto.setLeaveType(type);
        dto.setAvailableLeaves(days);
        return dto;
    }

    // ---- getAvailableLeaves (one leave type) ----

    @Test
    void getAvailableLeavesReturnsTheBalanceOfThatType() {
        when(employeeRepository.existsById("P00001")).thenReturn(true);
        when(availableLeavesRepository.findByEmployeeCodeAndLeaveType("P00001", LeaveType.CASUAL))
                .thenReturn(Optional.of(balance(LeaveType.CASUAL, 5)));

        AvailableLeavesDto result = service.getAvailableLeaves("P00001", LeaveType.CASUAL);

        assertEquals("P00001", result.getEmployeeCode());
        assertEquals(LeaveType.CASUAL, result.getLeaveType());
        assertEquals(5, result.getAvailableLeaves());
    }

    @Test
    void getAvailableLeavesThrowsWhenEmployeeNotFound() {
        when(employeeRepository.existsById("NOPE00")).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.getAvailableLeaves("NOPE00", LeaveType.CASUAL));
        verify(availableLeavesRepository, never()).findByEmployeeCodeAndLeaveType(any(String.class), any(LeaveType.class));
    }

    @Test
    void getAvailableLeavesAddsWhenEmployeeHasNoBalanceOfThatType() {
        when(employeeRepository.existsById("P00001")).thenReturn(true);
        when(availableLeavesRepository.findByEmployeeCodeAndLeaveType("P00001", LeaveType.SICK))
                .thenReturn(Optional.empty());
        when(availableLeavesRepository.save(any(AvailableLeaves.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.getAvailableLeaves("P00001", LeaveType.SICK);

        ArgumentCaptor<AvailableLeaves> saved = ArgumentCaptor.forClass(AvailableLeaves.class);
        verify(availableLeavesRepository).save(saved.capture());
        assertEquals("P00001", saved.getValue().getEmployeeCode());
        assertEquals(LeaveType.SICK, saved.getValue().getLeaveType());
        assertEquals(0, saved.getValue().getAvailableLeaves());
    }

    @Test
    void getAvailableLeavesDoesNotPersistUnpaidBalance() {
        when(employeeRepository.existsById("P00001")).thenReturn(true);
        when(availableLeavesRepository.findByEmployeeCodeAndLeaveType("P00001", LeaveType.UNPAID))
                .thenReturn(Optional.empty());

        AvailableLeavesDto result = service.getAvailableLeaves("P00001", LeaveType.UNPAID);

        assertEquals(LeaveType.UNPAID, result.getLeaveType());
        assertEquals(0, result.getAvailableLeaves());
        verify(availableLeavesRepository, never()).save(any(AvailableLeaves.class));
    }

    // ---- getAvailableLeavesByEmployeeCode (all leave types) ----

    @Test
    void getAvailableLeavesByEmployeeCodeReturnsEveryBalance() {
        when(employeeRepository.existsById("P00001")).thenReturn(true);
        when(availableLeavesRepository.findByEmployeeCodeOrderByEmployeeCodeAscLeaveTypeAsc("P00001"))
                .thenReturn(List.of(balance(LeaveType.CASUAL, 2), balance(LeaveType.EARNED, 0), balance(LeaveType.SICK, -1)));

        List<AvailableLeavesDto> result = service.getAvailableLeavesByEmployeeCode("P00001");

        assertEquals(3, result.size());
        assertEquals(LeaveType.CASUAL, result.get(0).getLeaveType());
        assertEquals(2, result.get(0).getAvailableLeaves());
        assertEquals(LeaveType.EARNED, result.get(1).getLeaveType());
        assertEquals(0, result.get(1).getAvailableLeaves());
        assertEquals(LeaveType.SICK, result.get(2).getLeaveType());
        assertEquals(-1, result.get(2).getAvailableLeaves());
    }

    @Test
    void getAvailableLeavesByEmployeeCodeReturnsWithZeroLeavesWhenThereAreNoBalances() {
        when(employeeRepository.existsById("P00001")).thenReturn(true);
        when(availableLeavesRepository.findByEmployeeCodeOrderByEmployeeCodeAscLeaveTypeAsc("P00001"))
                .thenReturn(List.of());
        when(availableLeavesRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.getAvailableLeavesByEmployeeCode("P00001");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<AvailableLeaves>> saved = (ArgumentCaptor<List<AvailableLeaves>>) (ArgumentCaptor<?>)
                ArgumentCaptor.forClass(List.class);
        verify(availableLeavesRepository).saveAll(saved.capture());
        assertEquals(3, saved.getValue().size());
        assertEquals(LeaveType.CASUAL, saved.getValue().get(0).getLeaveType());
        assertEquals(0, saved.getValue().get(0).getAvailableLeaves());
        assertEquals(LeaveType.EARNED, saved.getValue().get(1).getLeaveType());
        assertEquals(0, saved.getValue().get(1).getAvailableLeaves());
        assertEquals(LeaveType.SICK, saved.getValue().get(2).getLeaveType());
        assertEquals(0, saved.getValue().get(2).getAvailableLeaves());
    }

    @Test
    void getAvailableLeavesByEmployeeCodeThrowsWhenEmployeeNotFound() {
        when(employeeRepository.existsById("NOPE00")).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.getAvailableLeavesByEmployeeCode("NOPE00"));
        verify(availableLeavesRepository, never())
                .findByEmployeeCodeOrderByEmployeeCodeAscLeaveTypeAsc(any(String.class));
    }

    // ---- addAvailableLeaves ----

    @Test
    void addAvailableLeavesSavesTheBalanceAndReturnsIt() {
        when(employeeRepository.existsById("P00001")).thenReturn(true);
        when(availableLeavesRepository.save(any(AvailableLeaves.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.addAvailableLeaves(dto(LeaveType.EARNED, 12));

        ArgumentCaptor<AvailableLeaves> saved = ArgumentCaptor.forClass(AvailableLeaves.class);
        verify(availableLeavesRepository).save(saved.capture());
        assertEquals("P00001", saved.getValue().getEmployeeCode());
        assertEquals(LeaveType.EARNED, saved.getValue().getLeaveType());
        assertEquals(12, saved.getValue().getAvailableLeaves());
    }

    @Test
    void addAvailableLeavesThrowsWhenEmployeeNotFound() {
        when(employeeRepository.existsById("P00001")).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.addAvailableLeaves(dto(LeaveType.CASUAL, 2)));
        verify(availableLeavesRepository, never()).save(any(AvailableLeaves.class));
    }

    @Test
    void addAvailableLeavesRejectsUnpaidBalance() {
        assertThrows(IllegalArgumentException.class, () -> service.addAvailableLeaves(dto(LeaveType.UNPAID, 2)));
        verify(availableLeavesRepository, never()).save(any(AvailableLeaves.class));
    }

    // ---- updateAvailableLeaves ----

    @Test
    void updateAvailableLeavesChangesTheNumberOfLeavesAndSaves() {
        AvailableLeaves existing = balance(LeaveType.CASUAL, 2);
        AvailableLeavesDto update = dto(LeaveType.CASUAL, 7);
        update.setId(1L);
        when(employeeRepository.existsById("P00001")).thenReturn(true);
        when(availableLeavesRepository.findByEmployeeCodeAndLeaveType("P00001", LeaveType.CASUAL))
                .thenReturn(Optional.of(existing));

        boolean result = service.updateAvailableLeaves(update);

        assertTrue(result);
        assertEquals(7, existing.getAvailableLeaves());
        verify(availableLeavesRepository).save(existing);
    }

    @Test
    void updateAvailableLeavesCreatesBalanceWhenItDoesNotExist() {
        AvailableLeavesDto update = dto(LeaveType.CASUAL, 7);
        when(employeeRepository.existsById("P00001")).thenReturn(true);
        when(availableLeavesRepository.findByEmployeeCodeAndLeaveType("P00001", LeaveType.CASUAL))
                .thenReturn(Optional.empty());
        when(availableLeavesRepository.save(any(AvailableLeaves.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertTrue(service.updateAvailableLeaves(update));

        ArgumentCaptor<AvailableLeaves> saved = ArgumentCaptor.forClass(AvailableLeaves.class);
        verify(availableLeavesRepository).save(saved.capture());
        assertEquals("P00001", saved.getValue().getEmployeeCode());
        assertEquals(LeaveType.CASUAL, saved.getValue().getLeaveType());
        assertEquals(7, saved.getValue().getAvailableLeaves());
    }

    @Test
    void updateAvailableLeavesRejectsUnpaidBalance() {
        assertThrows(IllegalArgumentException.class, () -> service.updateAvailableLeaves(dto(LeaveType.UNPAID, 2)));
        verify(availableLeavesRepository, never()).save(any(AvailableLeaves.class));
    }

    @Test
    void updateAvailableLeavesThrowsWhenEmployeeDoesNotExist() {
        when(employeeRepository.existsById("P00001")).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.updateAvailableLeaves(dto(LeaveType.CASUAL, 7)));
        verify(availableLeavesRepository, never()).save(any(AvailableLeaves.class));
    }

    // ---- deleteAvailableLeaves ----

    @Test
    void deleteAvailableLeavesRemovesTheBalance() {
        AvailableLeaves existing = balance(LeaveType.EARNED, 4);
        when(availableLeavesRepository.findByEmployeeCodeAndLeaveType("P00001", LeaveType.EARNED))
                .thenReturn(Optional.of(existing));

        boolean result = service.deleteAvailableLeaves("P00001", LeaveType.EARNED);

        assertTrue(result);
        verify(availableLeavesRepository).delete(existing);
    }

    @Test
    void deleteAvailableLeavesThrowsWhenThereIsNoSuchBalance() {
        when(availableLeavesRepository.findByEmployeeCodeAndLeaveType("P00001", LeaveType.EARNED))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.deleteAvailableLeaves("P00001", LeaveType.EARNED));
        verify(availableLeavesRepository, never()).delete(any(AvailableLeaves.class));
    }
}
