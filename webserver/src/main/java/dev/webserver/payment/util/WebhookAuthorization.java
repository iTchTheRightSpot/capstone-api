package dev.webserver.payment.util;

public record WebhookAuthorization(
        String authorization_code,
        String bin,
        String last4,
        String exp_month,
        String exp_year,
        String channel,
        String card_type,
        String bank,
        String country_code,
        String brand,
        boolean reusable,
        String signature,
        String account_name,
        String receiver_bank_account_number,
        String receiver_bank
) {}