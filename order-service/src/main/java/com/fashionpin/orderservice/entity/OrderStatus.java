package com.fashionpin.orderservice.entity;

public enum OrderStatus {
    PENDING_PAYMENT,
    PAID,
    CONFIRMED,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    REFUNDED
}
