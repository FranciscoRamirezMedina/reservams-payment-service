package com.duoc.reservams.paymentservice.service;

import com.duoc.reservams.paymentservice.client.ReservationClient;
import com.duoc.reservams.paymentservice.dto.PaymentRequestDTO;
import com.duoc.reservams.paymentservice.dto.PaymentResponseDTO;
import com.duoc.reservams.paymentservice.model.Payment;
import com.duoc.reservams.paymentservice.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// pruebas unitarias para PaymentService
@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private ReservationClient reservationClient;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void findAll_shouldReturnPayments() {
        // Given
        when(paymentRepository.findAll()).thenReturn(List.of(
                buildPayment(1L, "PENDING"),
                buildPayment(2L, "APPROVED")
        ));

        // When
        List<PaymentResponseDTO> response = paymentService.findAll();

        // Then
        assertNotNull(response);
        assertEquals(2, response.size());

        verify(paymentRepository, times(1)).findAll();
        verifyNoInteractions(reservationClient);
    }

    @Test
    void findById_shouldReturnPayment_whenPaymentExists() {
        // Given
        Payment payment = buildPayment(1L, "PENDING");

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        // When
        PaymentResponseDTO response = paymentService.findById(1L);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(1L, response.getReservationId());
        assertEquals("PENDING", response.getStatus());

        verify(paymentRepository, times(1)).findById(1L);
        verifyNoInteractions(reservationClient);
    }

    @Test
    void findById_shouldThrowException_whenPaymentNotFound() {
        // Given
        when(paymentRepository.findById(99L)).thenReturn(Optional.empty());

        // When
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> paymentService.findById(99L)
        );

        // Then
        assertEquals("Pago no encontrado", exception.getMessage());

        verify(paymentRepository, times(1)).findById(99L);
        verifyNoInteractions(reservationClient);
    }

    @Test
    void findByReservationId_shouldReturnPayments() {
        // Given
        when(paymentRepository.findByReservationId(1L)).thenReturn(List.of(
                buildPayment(1L, "PENDING")
        ));

        // When
        List<PaymentResponseDTO> response = paymentService.findByReservationId(1L);

        // Then
        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals(1L, response.get(0).getReservationId());

        verify(paymentRepository, times(1)).findByReservationId(1L);
        verifyNoInteractions(reservationClient);
    }

    @Test
    void findByStatus_shouldReturnPayments() {
        // Given
        when(paymentRepository.findByStatus("APPROVED")).thenReturn(List.of(
                buildPayment(1L, "APPROVED")
        ));

        // When
        List<PaymentResponseDTO> response = paymentService.findByStatus("APPROVED");

        // Then
        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals("APPROVED", response.get(0).getStatus());

        verify(paymentRepository, times(1)).findByStatus("APPROVED");
        verifyNoInteractions(reservationClient);
    }

    @Test
    void create_shouldCreatePaymentWithPendingStatus() {
        // Given
        PaymentRequestDTO request = buildPaymentRequest();

        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            payment.setId(1L);
            return payment;
        });

        // When
        PaymentResponseDTO response = paymentService.create(request);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(1L, response.getReservationId());
        assertEquals(new BigDecimal("120000"), response.getAmount());
        assertEquals("CREDIT_CARD", response.getPaymentMethod());
        assertEquals("PENDING", response.getStatus());
        assertNotNull(response.getTransactionCode());
        assertTrue(response.getTransactionCode().startsWith("TX-"));
        assertNotNull(response.getCreatedAt());

        verify(paymentRepository, times(1)).save(any(Payment.class));
        verifyNoInteractions(reservationClient);
    }

    @Test
    void approve_shouldApprovePaymentAndConfirmReservation() {
        // Given
        Payment payment = buildPayment(1L, "PENDING");

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment savedPayment = invocation.getArgument(0);
            return savedPayment;
        });

        // When
        PaymentResponseDTO response = paymentService.approve(1L);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(1L, response.getReservationId());
        assertEquals("APPROVED", response.getStatus());
        assertNotNull(response.getPaidAt());

        verify(paymentRepository, times(1)).findById(1L);
        verify(paymentRepository, times(1)).save(any(Payment.class));
        verify(reservationClient, times(1)).confirmReservation(1L);
    }

    @Test
    void approve_shouldThrowException_whenPaymentAlreadyApproved() {
        // Given
        Payment payment = buildPayment(1L, "APPROVED");

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        // When
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> paymentService.approve(1L)
        );

        // Then
        assertEquals("El pago ya se encuentra aprobado", exception.getMessage());

        verify(paymentRepository, times(1)).findById(1L);
        verify(paymentRepository, never()).save(any(Payment.class));
        verifyNoInteractions(reservationClient);
    }

    @Test
    void approve_shouldThrowException_whenPaymentIsRejected() {
        // Given
        Payment payment = buildPayment(1L, "REJECTED");

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        // When
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> paymentService.approve(1L)
        );

        // Then
        assertEquals("No se puede aprobar un pago rechazado", exception.getMessage());

        verify(paymentRepository, times(1)).findById(1L);
        verify(paymentRepository, never()).save(any(Payment.class));
        verifyNoInteractions(reservationClient);
    }

    @Test
    void approve_shouldThrowException_whenPaymentNotFound() {
        // Given
        when(paymentRepository.findById(99L)).thenReturn(Optional.empty());

        // When
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> paymentService.approve(99L)
        );

        // Then
        assertEquals("Pago no encontrado", exception.getMessage());

        verify(paymentRepository, times(1)).findById(99L);
        verify(paymentRepository, never()).save(any(Payment.class));
        verifyNoInteractions(reservationClient);
    }

    @Test
    void approve_shouldThrowException_whenReservationClientFails() {
        // Given
        Payment payment = buildPayment(1L, "PENDING");

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment savedPayment = invocation.getArgument(0);
            return savedPayment;
        });

        doThrow(new RuntimeException("Error al confirmar reserva"))
                .when(reservationClient)
                .confirmReservation(1L);

        // When
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> paymentService.approve(1L)
        );

        // Then
        assertTrue(exception.getMessage().contains("Pago aprobado, pero no se pudo confirmar la reserva"));
        assertTrue(exception.getMessage().contains("Error al confirmar reserva"));

        verify(paymentRepository, times(1)).findById(1L);
        verify(paymentRepository, times(1)).save(any(Payment.class));
        verify(reservationClient, times(1)).confirmReservation(1L);
    }

    @Test
    void reject_shouldRejectPayment() {
        // Given
        Payment payment = buildPayment(1L, "PENDING");

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment savedPayment = invocation.getArgument(0);
            return savedPayment;
        });

        // When
        PaymentResponseDTO response = paymentService.reject(1L);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("REJECTED", response.getStatus());

        verify(paymentRepository, times(1)).findById(1L);
        verify(paymentRepository, times(1)).save(any(Payment.class));
        verifyNoInteractions(reservationClient);
    }

    @Test
    void reject_shouldThrowException_whenPaymentIsApproved() {
        // Given
        Payment payment = buildPayment(1L, "APPROVED");

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        // When
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> paymentService.reject(1L)
        );

        // Then
        assertEquals("No se puede rechazar un pago ya aprobado", exception.getMessage());

        verify(paymentRepository, times(1)).findById(1L);
        verify(paymentRepository, never()).save(any(Payment.class));
        verifyNoInteractions(reservationClient);
    }

    @Test
    void reject_shouldThrowException_whenPaymentNotFound() {
        // Given
        when(paymentRepository.findById(99L)).thenReturn(Optional.empty());

        // When
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> paymentService.reject(99L)
        );

        // Then
        assertEquals("Pago no encontrado", exception.getMessage());

        verify(paymentRepository, times(1)).findById(99L);
        verify(paymentRepository, never()).save(any(Payment.class));
        verifyNoInteractions(reservationClient);
    }

    @Test
    void delete_shouldDeletePayment_whenPaymentExists() {
        // Given
        when(paymentRepository.existsById(1L)).thenReturn(true);

        // When
        paymentService.delete(1L);

        // Then
        verify(paymentRepository, times(1)).existsById(1L);
        verify(paymentRepository, times(1)).deleteById(1L);
        verifyNoInteractions(reservationClient);
    }

    @Test
    void delete_shouldThrowException_whenPaymentNotFound() {
        // Given
        when(paymentRepository.existsById(99L)).thenReturn(false);

        // When
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> paymentService.delete(99L)
        );

        // Then
        assertEquals("Pago no encontrado", exception.getMessage());

        verify(paymentRepository, times(1)).existsById(99L);
        verify(paymentRepository, never()).deleteById(anyLong());
        verifyNoInteractions(reservationClient);
    }

    private PaymentRequestDTO buildPaymentRequest() {
        PaymentRequestDTO request = new PaymentRequestDTO();
        request.setReservationId(1L);
        request.setAmount(new BigDecimal("120000"));
        request.setPaymentMethod("CREDIT_CARD");
        return request;
    }

    private Payment buildPayment(Long id, String status) {
        Payment payment = new Payment();
        payment.setId(id);
        payment.setReservationId(1L);
        payment.setAmount(new BigDecimal("120000"));
        payment.setPaymentMethod("CREDIT_CARD");
        payment.setStatus(status);
        payment.setTransactionCode("TX-TEST");
        payment.setCreatedAt(LocalDateTime.now());
        return payment;
    }
}