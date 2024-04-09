package dev.webserver.payment.entity;

import dev.webserver.enumeration.PaymentStatus;
import dev.webserver.enumeration.SarreCurrency;
import dev.webserver.user.entity.SarreBrandUser;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;

import static jakarta.persistence.CascadeType.*;
import static jakarta.persistence.FetchType.LAZY;

@Table(name = "payment_detail",
        indexes = @Index(
                name = "IX_payment_detail_email_reference_id",
                columnList = "email, reference_id"
        )
)
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class PaymentDetail implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_detail_id", nullable = false, unique = true)
    private Long paymentDetailId;

    @Column(name = "full_name", nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(name = "reference_id", nullable = false, unique = true)
    private String referenceId; // equivalent to reference id

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private SarreCurrency currency;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(name = "payment_provider", nullable = false, length = 30)
    private String paymentProvider;

    @Column(name = "payment_status", nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    @Column(name = "paid_at")
    private String paidAt;

    @Column(name = "created_at", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createAt;

    @OneToOne(mappedBy = "paymentDetail", cascade = {PERSIST, MERGE, REFRESH})
    @PrimaryKeyJoinColumn
    private Address address;

    @OneToOne(mappedBy = "paymentDetail", cascade = {PERSIST, MERGE, REFRESH})
    @PrimaryKeyJoinColumn
    private PaymentAuthorization paymentAuthorization;

    @ManyToOne
    @JoinColumn(name = "client_id", referencedColumnName = "client_id")
    private SarreBrandUser user;

    @OneToMany(fetch = LAZY, cascade = {PERSIST, MERGE, REFRESH}, mappedBy = "paymentDetail")
    private Set<OrderDetail> orderDetails;

}
