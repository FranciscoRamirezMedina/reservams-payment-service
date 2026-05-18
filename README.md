\# ReservaMS - Payment Service



\## Descripcion



Este microservicio administra los pagos asociados a las reservas.



En esta version el pago es simulado, por lo que no se conecta a una pasarela real.



\## Responsabilidades



\- Registrar pagos.

\- Listar pagos.

\- Buscar pagos por reserva.

\- Buscar pagos por estado.

\- Aprobar pagos.

\- Rechazar pagos.

\- Generar codigo de transaccion.



\## Puerto



8087



\## Base de datos



reservams\_payment\_db



\## Endpoints principales



\- GET /api/v1/payments

\- GET /api/v1/payments/{id}

\- GET /api/v1/payments/reservation/{reservationId}

\- GET /api/v1/payments/status/{status}

\- POST /api/v1/payments

\- PUT /api/v1/payments/{id}/approve

\- PUT /api/v1/payments/{id}/reject



\## Ejecucion



1\. Crear la base de datos reservams\_payment\_db.

2\. Ejecutar el script SQL ubicado en la carpeta database.

3\. Levantar Eureka Server.

4\. Ejecutar el payment-service.

5\. Probar los endpoints desde Postman o desde el API Gateway.



