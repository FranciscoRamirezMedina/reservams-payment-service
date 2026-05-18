package com.duoc.reservams.paymentservice.repository;

import com.duoc.reservams.paymentservice.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

// Repository para trabajar con la tabla payments
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // busca pagos asociados a una reserva
    List<Payment> findByReservationId(Long reservationId);

    // busca pagos por estado, ej APPROVED
    List<Payment> findByStatus(String status);

    // busca un pago aprobado para una reserva
    Optional<Payment> findByReservationIdAndStatus(Long reservationId, String status);
}