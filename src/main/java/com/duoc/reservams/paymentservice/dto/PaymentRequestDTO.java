package com.duoc.reservams.paymentservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

// DTO para registrar un pago
@Data
public class PaymentRequestDTO {

    @NotNull(message = "El reservationId es obligatorio")
    private Long reservationId;

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El monto debe ser mayor a 0")
    private BigDecimal amount;

    @NotBlank(message = "El metodo de pago es obligatorio")
    private String paymentMethod;
}