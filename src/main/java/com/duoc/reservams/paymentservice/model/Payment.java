package com.duoc.reservams.paymentservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// esta clase representa un pago dentro del sistema
@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    // ID principal del pago
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ID logico de la reserva que viene desde reservation-service
    @Column(name = "reservation_id", nullable = false)
    private Long reservationId;

    // monto pagado por el cliente
    @Column(nullable = false)
    private BigDecimal amount;

    // Metodo de pago, CREDIT_CARD, DEBIT_CARD o TRANSFER
    @Column(name = "payment_method", nullable = false, length = 30)
    private String paymentMethod;

    // Estado del pago: PENDING, APPROVED o REJECTED
    @Column(nullable = false, length = 30)
    private String status;

    // codigo simple para identificar la transaccion
    @Column(name = "transaction_code", length = 100)
    private String transactionCode;

    // fecha en que el pago fue aprobado
    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    // fecha en que se creo el registro del pago
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}