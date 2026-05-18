package com.duoc.reservams.paymentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// DTO para responder datos de pagos
@Data
@AllArgsConstructor
public class PaymentResponseDTO {

    private Long id;
    private Long reservationId;
    private BigDecimal amount;
    private String paymentMethod;
    private String status;
    private String transactionCode;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
}