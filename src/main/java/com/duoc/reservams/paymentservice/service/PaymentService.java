package com.duoc.reservams.paymentservice.service;

import com.duoc.reservams.paymentservice.dto.PaymentRequestDTO;
import com.duoc.reservams.paymentservice.dto.PaymentResponseDTO;
import com.duoc.reservams.paymentservice.model.Payment;
import com.duoc.reservams.paymentservice.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

// aqui va la lógica de negocio de pagos
@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public List<PaymentResponseDTO> findAll() {
        return paymentRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public PaymentResponseDTO findById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pago no encontrado"));

        return toResponseDTO(payment);
    }

    public List<PaymentResponseDTO> findByReservationId(Long reservationId) {
        return paymentRepository.findByReservationId(reservationId)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public List<PaymentResponseDTO> findByStatus(String status) {
        return paymentRepository.findByStatus(status)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public PaymentResponseDTO create(PaymentRequestDTO request) {
        Payment payment = new Payment();

        payment.setReservationId(request.getReservationId());
        payment.setAmount(request.getAmount());
        payment.setPaymentMethod(request.getPaymentMethod());

        // ttodo pago nuevo parte pendiente
        payment.setStatus("PENDING");

        // creamos un codigo simple para simular una transaccion
        payment.setTransactionCode("TX-" + UUID.randomUUID());

        payment.setCreatedAt(LocalDateTime.now());

        Payment savedPayment = paymentRepository.save(payment);

        return toResponseDTO(savedPayment);
    }

    public PaymentResponseDTO approve(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pago no encontrado"));

        if (payment.getStatus().equals("APPROVED")) {
            throw new RuntimeException("El pago ya se encuentra aprobado");
        }

        if (payment.getStatus().equals("REJECTED")) {
            throw new RuntimeException("No se puede aprobar un pago rechazado");
        }

        payment.setStatus("APPROVED");
        payment.setPaidAt(LocalDateTime.now());

        Payment approvedPayment = paymentRepository.save(payment);

        return toResponseDTO(approvedPayment);
    }

    public PaymentResponseDTO reject(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pago no encontrado"));

        if (payment.getStatus().equals("APPROVED")) {
            throw new RuntimeException("No se puede rechazar un pago ya aprobado");
        }

        payment.setStatus("REJECTED");

        Payment rejectedPayment = paymentRepository.save(payment);

        return toResponseDTO(rejectedPayment);
    }

    public void delete(Long id) {
        if (!paymentRepository.existsById(id)) {
            throw new RuntimeException("Pago no encontrado");
        }

        paymentRepository.deleteById(id);
    }

    // convierte la entidad Payment a DTO de respuesta
    private PaymentResponseDTO toResponseDTO(Payment payment) {
        return new PaymentResponseDTO(
                payment.getId(),
                payment.getReservationId(),
                payment.getAmount(),
                payment.getPaymentMethod(),
                payment.getStatus(),
                payment.getTransactionCode(),
                payment.getPaidAt(),
                payment.getCreatedAt()
        );
    }
}