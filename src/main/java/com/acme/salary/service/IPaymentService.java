package com.acme.salary.service;

import com.acme.salary.dto.PaymentDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Payments are immutable: they can be read and recorded, but never updated or deleted.
 */
public interface IPaymentService {

    /**
     * @param paymentId the Payment ID
     * @return Payment DTO object based on payment ID.
     */
    PaymentDto getPayment(Long paymentId);

    /**
     * @param employeeCode the Employee code
     * @param pageable     pagination and sorting
     * @return Page of the employee's payments.
     */
    Page<PaymentDto> getPayments(String employeeCode, Pageable pageable);

    /**
     * Records a payment for a salary and pay period. The amounts are copied from the
     * salary, and the paid date and time are set by the system.
     *
     * @param paymentDto the Payment DTO (salary ID and pay period)
     * @return the recorded Payment DTO.
     */
    PaymentDto addPayment(PaymentDto paymentDto);

}