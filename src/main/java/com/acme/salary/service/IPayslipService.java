package com.acme.salary.service;

import com.acme.salary.dto.PayslipBatchDto;
import com.acme.salary.dto.PayslipDto;
import org.springframework.core.io.Resource;

import java.util.List;

public interface IPayslipService {

    /**
     * Generates the payslip PDF for a payment and stores it under docs/{employeeCode}.
     * An existing payslip for the same payment is replaced.
     *
     * @param paymentId the Payment ID
     * @return details of the stored payslip.
     */
    PayslipDto generatePayslip(Long paymentId);

    /**
     * Generates payslips for every payment that does not have one yet.
     *
     * @return how many payslips were generated and how many payments were skipped.
     */
    PayslipBatchDto generateAllPayslips();

    /**
     * @param paymentId the Payment ID
     * @return the stored payslip PDF.
     */
    Resource getPayslip(Long paymentId);

    /**
     * @param employeeCode the Employee code
     * @return details of the employee's stored payslips, ordered by payment ID.
     */
    List<PayslipDto> getPayslips(String employeeCode);

}
