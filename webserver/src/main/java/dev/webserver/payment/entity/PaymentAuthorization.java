package dev.webserver.payment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Table(name = "payment_authorization")
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class PaymentAuthorization implements Serializable {

    @Id
    @Column(name = "authorization_id", nullable = false, unique = true)
    private Long authorizationId;

    @Column(name = "authorization_code", nullable = false, length = 50)
    private String authorizationCode;

    @Column(nullable = false, length = 50)
    private String bin;

    @Column(name = "card_last_4_digits", length = 5, nullable = false)
    private String last4;

    @Column(name = "exp_month", length = 2, nullable = false)
    private String expirationMonth;

    @Column(name = "exp_year", length = 6, nullable = false)
    private String expirationYear;

    @Column(nullable = false, length = 10)
    private String channel;

    @Column(name = "card_type", length = 20, nullable = false)
    private String cardType;

    @Column(nullable = false, length = 100)
    private String bank;

    @Column(name = "country_code", length = 10, nullable = false)
    private String countryCode;

    @Column(nullable = false, length = 20)
    private String brand;

    @Column(name = "is_reusable", nullable = false)
    private boolean isReusable;

    @Column(nullable = false, length = 50)
    private String signature;

    @OneToOne
    @MapsId
    @JoinColumn(name = "authorization_id")
    private PaymentDetail paymentDetail;

    public Long authorizationId() {
        return authorizationId;
    }

    public String authorizationCode() {
        return authorizationCode;
    }

    public String bin() {
        return bin;
    }

    public String last4() {
        return last4;
    }

    public String expirationMonth() {
        return expirationMonth;
    }

    public String expirationYear() {
        return expirationYear;
    }

    public String channel() {
        return channel;
    }

    public String cardType() {
        return cardType;
    }

    public String bank() {
        return bank;
    }

    public String countryCode() {
        return countryCode;
    }

    public String brand() {
        return brand;
    }

    public boolean isReusable() {
        return isReusable;
    }

    public String signature() {
        return signature;
    }

    public PaymentDetail paymentDetail() {
        return paymentDetail;
    }

}
