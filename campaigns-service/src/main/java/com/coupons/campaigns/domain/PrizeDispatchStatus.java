package com.coupons.campaigns.domain;

/**
 * Estado de "entrega" do prêmio (eventualmente com reenvios) no `campaigns-service`.
 *
 * - PENDING: prêmio ainda não foi confirmado como `DELIVERED` no `prizes-service`.
 * - DELIVERED: confirmação via `prizes-service`.
 * - PUBLISH_FAILED: falha ao (re)publicar no Kafka durante o retry job.
 */
public enum PrizeDispatchStatus {
    PENDING,
    DELIVERED,
    PUBLISH_FAILED
}

