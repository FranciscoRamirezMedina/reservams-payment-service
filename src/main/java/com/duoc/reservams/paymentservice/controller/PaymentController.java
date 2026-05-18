package com.duoc.reservams.paymentservice.controller;

import com.duoc.reservams.paymentservice.dto.PaymentRequestDTO;
import com.duoc.reservams.paymentservice.dto.PaymentResponseDTO;
import com.duoc.reservams.paymentservice.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// controlador REST para manejar pagos
@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // lista todos los pagos
    @GetMapping
    public ResponseEntity<List<PaymentResponseDTO>> findAll() {
        return ResponseEntity.ok(paymentService.findAll());
    }

    // busca un pago por ID
    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.findById(id));
    }

    // lista pagos de una reserva
    @GetMapping("/reservation/{reservationId}")
    public ResponseEntity<List<PaymentResponseDTO>> findByReservationId(@PathVariable Long reservationId) {
        return ResponseEntity.ok(paymentService.findByReservationId(reservationId));
    }

    // lista pagos por estado
    @GetMapping("/status/{status}")
    public ResponseEntity<List<PaymentResponseDTO>> findByStatus(@PathVariable String status) {
        return ResponseEntity.ok(paymentService.findByStatus(status));
    }

    // registra un pago nuevo
    @PostMapping
    public ResponseEntity<PaymentResponseDTO> create(@Valid @RequestBody PaymentRequestDTO request) {
        return ResponseEntity.ok(paymentService.create(request));
    }

    // aprueba un pago
    @PutMapping("/{id}/approve")
    public ResponseEntity<PaymentResponseDTO> approve(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.approve(id));
    }

    // rechaza un pago
    @PutMapping("/{id}/reject")
    public ResponseEntity<PaymentResponseDTO> reject(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.reject(id));
    }

    // elimina un pago, util para pruebas
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        paymentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}