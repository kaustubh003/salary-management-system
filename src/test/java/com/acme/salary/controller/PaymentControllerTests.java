package com.acme.salary.controller;

import com.acme.salary.dto.PaymentDto;
import com.acme.salary.service.IPaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PaymentControllerTests {

    @Test
    void getPaymentsCapsPageSizeAtOneHundredAndPreservesPageAndSort() {
        IPaymentService paymentService = mock(IPaymentService.class);
        PaymentController controller = new PaymentController(paymentService);
        Pageable requested = PageRequest.of(2, 250, Sort.by(Sort.Direction.DESC, "periodStart"));
        when(paymentService.getPayments(eq("P00001"), org.mockito.ArgumentMatchers.any(Pageable.class)))
                .thenAnswer(invocation -> Page.empty(invocation.getArgument(1)));

        ResponseEntity<Page<PaymentDto>> response = controller.getPayments("P00001", requested);

        assertEquals(200, response.getStatusCode().value());
        verify(paymentService).getPayments(eq("P00001"), org.mockito.ArgumentMatchers.argThat(pageable ->
                pageable.getPageNumber() == 2
                        && pageable.getPageSize() == 100
                        && pageable.getSort().equals(requested.getSort())));
    }
}
