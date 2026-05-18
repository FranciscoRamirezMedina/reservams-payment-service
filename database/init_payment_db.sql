CREATE DATABASE IF NOT EXISTS reservams_payment_db;

USE reservams_payment_db;

CREATE TABLE payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reservation_id BIGINT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    payment_method VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    transaction_code VARCHAR(100),
    paid_at DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);