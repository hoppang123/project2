package com.company.asset.domain.audit;

public enum AuditAction {
    // rental workflow
    RENTAL_REQUEST_CREATE,
    RENTAL_REQUEST_APPROVE_STEP,
    RENTAL_REQUEST_APPROVED_FINAL,
    RENTAL_REQUEST_REJECT,
    RENTAL_REQUEST_CANCEL,
    RENTAL_RETURN_REQUEST,
    RENTAL_RETURN_CONFIRM,

    // reservation
    RESERVATION_CREATE,
    RESERVATION_CANCEL,

    // maintenance
    MAINTENANCE_CREATE,
    MAINTENANCE_CANCEL,

    // sanction
    SANCTION_CREATED,

    // reservation
    RESERVATION_CHECKED_OUT
}