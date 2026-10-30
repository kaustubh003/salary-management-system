package com.acme.salary.mapper;

import com.acme.salary.dto.PaymentDto;
import com.acme.salary.entity.Payment;

public final class PaymentMapper {

    private PaymentMapper() {
    }

    /**
     * Payments are immutable and are created only through Payment.record(...), so there is
     * no toEntity here. This maps the stored record, including the salary snapshot.
     */
    public static PaymentDto toDto(Payment payment) {
        PaymentDto dto = new PaymentDto();
        dto.setPaymentID(payment.getPaymentID());
        dto.setEmployeeCode(payment.getEmployeeCode());
        dto.setSalaryId(payment.getSalaryId());
        dto.setPeriodStart(payment.getPeriodStart());
        dto.setPeriodEnd(payment.getPeriodEnd());
        dto.setPaidDate(payment.getPaidDate());
        dto.setPaidTime(payment.getPaidTime());
        dto.setPaidStatus(payment.getPaidStatus());
        dto.setCurrency(payment.getCurrency());
        dto.setBasicSalary(payment.getBasicSalary());
        dto.setHra(payment.getHra());
        dto.setDa(payment.getDa());
        dto.setGrossSalary(payment.getGrossSalary());
        dto.setTax(payment.getTax());
        dto.setEpf(payment.getEpf());
        dto.setNetSalary(payment.getNetSalary());
        return dto;
    }
}