package com.acme.salary.controller;

import com.acme.salary.dto.PayslipBatchDto;
import com.acme.salary.dto.PayslipDto;
import com.acme.salary.service.IPayslipService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Payslip", description = "Generate payslip PDFs, store them under docs/{employeeCode}, and download them")
@RestController
@RequestMapping(path = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
public class PayslipController {

    private final IPayslipService payslipService;

    @Operation(summary = "Generate the payslip PDF for a payment and store it. An existing payslip for the payment is replaced")
    @PostMapping("/payments/{paymentId}/payslip")
    public ResponseEntity<PayslipDto> generatePayslip(@PathVariable Long paymentId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(payslipService.generatePayslip(paymentId));
    }

    @Operation(summary = "Generate payslips for every payment that does not have one yet")
    @PostMapping("/payslips/generate-all")
    public ResponseEntity<PayslipBatchDto> generateAllPayslips() {
        return ResponseEntity.ok(payslipService.generateAllPayslips());
    }

    @Operation(summary = "Download the stored payslip PDF for a payment. Returns 404 if it has not been generated yet")
    @GetMapping(path = "/payments/{paymentId}/payslip", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<Resource> downloadPayslip(@PathVariable Long paymentId) {
        Resource payslip = payslipService.getPayslip(paymentId);
        String disposition = ContentDisposition.attachment()
                .filename("payslip-" + paymentId + ".pdf")
                .build()
                .toString();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition)
                .body(payslip);
    }

    @Operation(summary = "List the stored payslips of an employee")
    @GetMapping("/employees/{employeeCode}/payslips")
    public ResponseEntity<List<PayslipDto>> getPayslips(@PathVariable String employeeCode) {
        return ResponseEntity.ok(payslipService.getPayslips(employeeCode));
    }
}
