package com.duoc.reservams.paymentservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

// cliente Feign para comunicarse con reservation-service
@FeignClient(name = "reservams-reservation-service")
public interface ReservationClient {

    // Llama al endpoint que confirma una reserva
    @PutMapping("/api/v1/reservations/{id}/confirm")
    void confirmReservation(@PathVariable("id") Long reservationId);
}
