package com.acme.salary.service.impl;

import com.acme.salary.dto.PaymentDto;
import com.acme.salary.entity.Payment;
import com.acme.salary.entity.Salary;
import com.acme.salary.exception.ResourceNotFoundException;
import com.acme.salary.mapper.PaymentMapper;
import com.acme.salary.repository.EmployeeRepository;
import com.acme.salary.repository.PaymentRepository;
import com.acme.salary.repository.SalaryRepository;
import com.acme.salary.service.IPaymentService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@AllArgsConstructor
public class PaymentServiceImpl implements IPaymentService {

    private final PaymentRepository paymentRepository;
    private final SalaryRepository salaryRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    @Transactional(readOnly = true)
    public PaymentDto getPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "paymentId", String.valueOf(paymentId)));
        return PaymentMapper.toDto(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentDto> getPayments(String employeeCode, Pageable pageable) {
        if (!employeeRepository.existsById(employeeCode)) {
            throw new ResourceNotFoundException("Employee", "employeeCode", employeeCode);
        }
        return paymentRepository.findByEmployeeCode(employeeCode, pageable)
                .map(PaymentMapper::toDto);
    }

    @Override
    @Transactional
    public PaymentDto addPayment(PaymentDto paymentDto) {
        Salary salary = salaryRepository.findById(paymentDto.getSalaryId())
                .orElseThrow(() -> new ResourceNotFoundException("Salary", "salaryId",
                        String.valueOf(paymentDto.getSalaryId())));

        // Read the clock once so the date and time can never fall on different sides of midnight
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

        Payment payment = Payment.record(salary, paymentDto.getPeriodStart(), paymentDto.getPeriodEnd(),
                now.toLocalDate(), now.toLocalTime());
        return PaymentMapper.toDto(paymentRepository.save(payment));
    }
}