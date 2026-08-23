package com.fashionpin.common.kafka;

public final class KafkaTopics {
    public static final String USER_REGISTERED_V1 = "fashionpin.user.registered.v1";
    public static final String USER_CREATED_V1 = "fashionpin.user.created.v1";
    public static final String USER_EVENTS = "fashionpin.user.events";
    public static final String ORDER_EVENTS = "fashionpin.order.events";
    public static final String PRODUCT_EVENTS = "fashionpin.product.events";
    public static final String NOTIFICATION_EVENTS = "fashionpin.notification.events";
    public static final String ANALYTICS_EVENTS = "fashionpin.analytics.events";
    public static final String MEDIA_EVENTS = "fashionpin.media.events";
    public static final String PAYMENT_EVENTS = "fashionpin.payment.events";
    public static final String INVENTORY_EVENTS = "fashionpin.inventory.events";

    private KafkaTopics() {
    }
}

