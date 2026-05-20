package com.duoc.reservams.paymentservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class ReservamsPaymentServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReservamsPaymentServiceApplication.class, args);
	}

}
