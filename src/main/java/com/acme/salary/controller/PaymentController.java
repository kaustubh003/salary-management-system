package com.acme.salary.controller;

import com.acme.salary.dto.PaymentDto;
import com.acme.salary.service.IPaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Payment", description = "Record and read payments. Payments are immutable, so there is no update or delete")
@RestController
@RequestMapping(path = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
public class PaymentController {

    private final IPaymentService paymentService;

    @Operation(summary = "Get a payment by ID")
    @GetMapping("/payments/{paymentId}")
    public ResponseEntity<PaymentDto> getPayment(@PathVariable Long paymentId) {
        return ResponseEntity.ok(paymentService.getPayment(paymentId));
    }

    @Operation(summary = "List an employee's payments, newest pay period first. Use page, size and sort query parameters")
    @GetMapping("/employees/{employeeCode}/payments")
    public ResponseEntity<Page<PaymentDto>> getPayments(
            @PathVariable String employeeCode,
            @PageableDefault(size = 20, sort = "periodStart", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(paymentService.getPayments(employeeCode, pageable));
    }

    @Operation(summary = "Record a payment for a salary and pay period. The amounts are copied from the salary, and the paid date and time are set by the system")
    @PostMapping(path = "/payments", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PaymentDto> addPayment(@Valid @RequestBody PaymentDto paymentDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.addPayment(paymentDto));
    }
}