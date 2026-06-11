package com.duoc.reservams.paymentservice.service;

import com.duoc.reservams.paymentservice.dto.PaymentRequestDTO;
import com.duoc.reservams.paymentservice.dto.PaymentResponseDTO;
import com.duoc.reservams.paymentservice.model.Payment;
import com.duoc.reservams.paymentservice.client.ReservationClient;
import com.duoc.reservams.paymentservice.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

// aqui va la lógica de negocio de pagos
@Service
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;

    private final ReservationClient reservationClient;

    public PaymentService(PaymentRepository paymentRepository,
                          ReservationClient reservationClient) {
        this.paymentRepository = paymentRepository;
        this.reservationClient = reservationClient;
    }

    public List<PaymentResponseDTO> findAll() {
        logger.info("Listando pagos");

        return paymentRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public PaymentResponseDTO findById(Long id) {
        Payment payment = findPaymentOrThrow(id);

        return toResponseDTO(payment);
    }

    public List<PaymentResponseDTO> findByReservationId(Long reservationId) {
        logger.info("Listando pagos de la reserva ID {}", reservationId);

        return paymentRepository.findByReservationId(reservationId)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public List<PaymentResponseDTO> findByStatus(String status) {
        logger.info("Listando pagos con estado {}", status);

        return paymentRepository.findByStatus(status)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public PaymentResponseDTO create(PaymentRequestDTO request) {
        logger.info("Iniciando creacion de pago para reserva ID {}", request.getReservationId());

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

        logger.info("Pago creado correctamente con ID {} para reserva ID {}",
                savedPayment.getId(),
                savedPayment.getReservationId());

        return toResponseDTO(savedPayment);
    }

    public PaymentResponseDTO approve(Long id) {
        logger.info("Iniciando aprobacion de pago ID {}", id);

        Payment payment = findPaymentOrThrow(id);

        if (payment.getStatus().equals("APPROVED")) {
            logger.warn("No se puede aprobar pago ID {} porque ya se encuentra aprobado", id);
            throw new RuntimeException("El pago ya se encuentra aprobado");
        }

        if (payment.getStatus().equals("REJECTED")) {
            logger.warn("No se puede aprobar pago ID {} porque se encuentra rechazado", id);
            throw new RuntimeException("No se puede aprobar un pago rechazado");
        }

        payment.setStatus("APPROVED");
        payment.setPaidAt(LocalDateTime.now());

        Payment approvedPayment = paymentRepository.save(payment);

        logger.info("Pago ID {} aprobado correctamente. Confirmando reserva ID {} mediante OpenFeign",
                approvedPayment.getId(),
                approvedPayment.getReservationId());

        try {
            // Cuando el pago se aprueba, se confirma la reserva asociada
            reservationClient.confirmReservation(payment.getReservationId());

            logger.info("Reserva ID {} confirmada correctamente desde payment-service",
                    payment.getReservationId());

        } catch (Exception ex) {
            logger.error("Pago ID {} aprobado, pero no se pudo confirmar la reserva ID {}. Detalle: {}",
                    approvedPayment.getId(),
                    payment.getReservationId(),
                    ex.getMessage());

            throw new RuntimeException("Pago aprobado, pero no se pudo confirmar la reserva: " + ex.getMessage());
        }

        return toResponseDTO(approvedPayment);
    }

    public PaymentResponseDTO reject(Long id) {
        logger.info("Iniciando rechazo de pago ID {}", id);

        Payment payment = findPaymentOrThrow(id);

        if (payment.getStatus().equals("APPROVED")) {
            logger.warn("No se puede rechazar pago ID {} porque ya se encuentra aprobado", id);
            throw new RuntimeException("No se puede rechazar un pago ya aprobado");
        }

        payment.setStatus("REJECTED");

        Payment rejectedPayment = paymentRepository.save(payment);

        logger.info("Pago ID {} rechazado correctamente", rejectedPayment.getId());

        return toResponseDTO(rejectedPayment);
    }

    public void delete(Long id) {
        logger.info("Iniciando eliminacion de pago ID {}", id);

        if (!paymentRepository.existsById(id)) {
            logger.warn("No se encontro pago con ID {} para eliminar", id);
            throw new RuntimeException("Pago no encontrado");
        }

        paymentRepository.deleteById(id);

        logger.info("Pago ID {} eliminado correctamente", id);
    }

    private Payment findPaymentOrThrow(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Pago no encontrado con ID {}", id);
                    return new RuntimeException("Pago no encontrado");
                });
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