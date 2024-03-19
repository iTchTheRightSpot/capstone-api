<<<<<<<< HEAD:webserver/src/main/java/dev/webserver/user/entity/SarreBrandUser.java
package dev.webserver.user.entity;

import dev.webserver.payment.entity.PaymentDetail;
========
package dev.capstone.user.entity;

import dev.capstone.payment.entity.PaymentDetail;
>>>>>>>> 38dca43c14b569b33b94a23c1bdce50584a67195:src/main/java/dev/capstone/user/entity/SarreBrandUser.java
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Set;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.EAGER;
import static jakarta.persistence.FetchType.LAZY;

@Table(name = "clientz")
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class SarreBrandUser implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "client_id", nullable = false, unique = true)
    private Long clientId;

    @Column(name = "firstname", nullable = false)
    private String firstname;

    @Column(name = "lastname", nullable = false)
    private String lastname;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @OneToMany(cascade = ALL, fetch = EAGER, mappedBy = "sarreBrandUser", orphanRemoval = true)
    private Set<ClientRole> clientRole;

    @OneToMany(cascade = ALL, fetch = LAZY, mappedBy = "user")
    private Set<PaymentDetail> paymentDetail;

}
