package dev.webserver.payment;

import java.io.Serializable;

public record WebhookMetaData(
        String principal,
        String email,
        String name,
        String phone,
        String address,
        String city,
        String state,
        String postcode,
        String country,
        String deliveryInfo,
        String referrer
) implements Serializable { }