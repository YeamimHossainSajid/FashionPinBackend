package com.fashionpin.common.kafka;

public final class KafkaTopics {
    public static final String USER_REGISTERED_V1 = "fashionpin.user.registered.v1";
    public static final String USER_CREATED_V1 = "fashionpin.user.created.v1";
    public static final String MEDIA_CREATED_V1 = "fashionpin.media.created.v1";
    public static final String MEDIA_READY_V1 = "fashionpin.media.ready.v1";
    public static final String MEDIA_DELETED_V1 = "fashionpin.media.deleted.v1";
    public static final String BRAND_CREATED_V1 = "fashionpin.brand.created.v1";
    public static final String BRAND_UPDATED_V1 = "fashionpin.brand.updated.v1";
    public static final String BRAND_DELETED_V1 = "fashionpin.brand.deleted.v1";
    public static final String PRODUCT_CREATED_V1 = "fashionpin.product.created.v1";
    public static final String PRODUCT_UPDATED_V1 = "fashionpin.product.updated.v1";
    public static final String PRODUCT_DELETED_V1 = "fashionpin.product.deleted.v1";
    public static final String CATALOG_UPDATED_V1 = "fashionpin.catalog.updated.v1";

    public static final String FASHION_POST_CREATED_V1 = "fashionpin.fashion.post.created.v1";
    public static final String FASHION_POST_UPDATED_V1 = "fashionpin.fashion.post.updated.v1";
    public static final String FASHION_POST_DELETED_V1 = "fashionpin.fashion.post.deleted.v1";

    public static final String OUTFIT_CREATED_V1 = "fashionpin.fashion.outfit.created.v1";
    public static final String OUTFIT_UPDATED_V1 = "fashionpin.fashion.outfit.updated.v1";
    public static final String OUTFIT_DELETED_V1 = "fashionpin.fashion.outfit.deleted.v1";

    public static final String OUTFIT_DETECTION_REQUESTED_V1 = "fashionpin.ai.outfit-detection.requested.v1";
    public static final String OUTFIT_DETECTION_COMPLETED_V1 = "fashionpin.ai.outfit-detection.completed.v1";
    public static final String OUTFIT_DETECTION_FAILED_V1 = "fashionpin.ai.outfit-detection.failed.v1";

    public static final String ORDER_CREATED_V1 = "fashionpin.order.created.v1";
    public static final String ORDER_PAID_V1 = "fashionpin.order.paid.v1";

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
