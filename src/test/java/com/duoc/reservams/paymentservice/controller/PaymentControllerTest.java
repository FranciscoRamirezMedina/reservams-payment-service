package com.duoc.reservams.paymentservice.controller;

import com.duoc.reservams.paymentservice.dto.PaymentRequestDTO;
import com.duoc.reservams.paymentservice.dto.PaymentResponseDTO;
import com.duoc.reservams.paymentservice.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// pruebas unitarias para PaymentController
@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private PaymentController paymentController;

    @Test
    void findAll_shouldReturnPayments() {
        // Given
        when(paymentService.findAll()).thenReturn(List.of(
                buildPaymentResponse(1L, "PENDING"),
                buildPaymentResponse(2L, "APPROVED")
        ));

        // When
        ResponseEntity<List<PaymentResponseDTO>> response = paymentController.findAll();

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());

        verify(paymentService, times(1)).findAll();
    }

    @Test
    void findById_shouldReturnPayment() {
        // Given
        when(paymentService.findById(1L)).thenReturn(buildPaymentResponse(1L, "PENDING"));

        // When
        ResponseEntity<PaymentResponseDTO> response = paymentController.findById(1L);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("PENDING", response.getBody().getStatus());

        verify(paymentService, times(1)).findById(1L);
    }

    @Test
    void findByReservationId_shouldReturnPayments() {
        // Given
        when(paymentService.findByReservationId(1L)).thenReturn(List.of(
                buildPaymentResponse(1L, "PENDING")
        ));

        // When
        ResponseEntity<List<PaymentResponseDTO>> response = paymentController.findByReservationId(1L);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(1L, response.getBody().get(0).getReservationId());

        verify(paymentService, times(1)).findByReservationId(1L);
    }

    @Test
    void findByStatus_shouldReturnPayments() {
        // Given
        when(paymentService.findByStatus("APPROVED")).thenReturn(List.of(
                buildPaymentResponse(1L, "APPROVED")
        ));

        // When
        ResponseEntity<List<PaymentResponseDTO>> response = paymentController.findByStatus("APPROVED");

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("APPROVED", response.getBody().get(0).getStatus());

        verify(paymentService, times(1)).findByStatus("APPROVED");
    }

    @Test
    void create_shouldReturnCreatedPayment() {
        // Given
        PaymentRequestDTO request = buildPaymentRequest();

        when(paymentService.create(request)).thenReturn(buildPaymentResponse(1L, "PENDING"));

        // When
        ResponseEntity<PaymentResponseDTO> response = paymentController.create(request);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("PENDING", response.getBody().getStatus());

        verify(paymentService, times(1)).create(request);
    }

    @Test
    void approve_shouldReturnApprovedPayment() {
        // Given
        when(paymentService.approve(1L)).thenReturn(buildPaymentResponse(1L, "APPROVED"));

        // When
        ResponseEntity<PaymentResponseDTO> response = paymentController.approve(1L);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("APPROVED", response.getBody().getStatus());

        verify(paymentService, times(1)).approve(1L);
    }

    @Test
    void reject_shouldReturnRejectedPayment() {
        // Given
        when(paymentService.reject(1L)).thenReturn(buildPaymentResponse(1L, "REJECTED"));

        // When
        ResponseEntity<PaymentResponseDTO> response = paymentController.reject(1L);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("REJECTED", response.getBody().getStatus());

        verify(paymentService, times(1)).reject(1L);
    }

    @Test
    void delete_shouldReturnNoContent() {
        // Given
        doNothing().when(paymentService).delete(1L);

        // When
        ResponseEntity<Void> response = paymentController.delete(1L);

        // Then
        assertNotNull(response);
        assertEquals(204, response.getStatusCode().value());
        assertNull(response.getBody());

        verify(paymentService, times(1)).delete(1L);
    }

    private PaymentRequestDTO buildPaymentRequest() {
        PaymentRequestDTO request = new PaymentRequestDTO();
        request.setReservationId(1L);
        request.setAmount(new BigDecimal("120000"));
        request.setPaymentMethod("CREDIT_CARD");
        return request;
    }

    private PaymentResponseDTO buildPaymentResponse(Long id, String status) {
        return new PaymentResponseDTO(
                id,
                1L,
                new BigDecimal("120000"),
                "CREDIT_CARD",
                status,
                "TX-TEST",
                status.equals("APPROVED") ? LocalDateTime.now() : null,
                LocalDateTime.now()
        );
    }
}