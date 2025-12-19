package com.github.tylerspaeth.common.enums;

/**
 * Statuses that an Order can be in.
 */
public enum OrderStatusEnum {
    PENDING_SUBMIT,
    SUBMITTED,
    PENDING,
    PENDING_CANCEL,
    CANCELLED,
    FILLED,
    INACTIVE,
    OTHER
}
