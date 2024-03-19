package dev.webserver.tax;

import jakarta.persistence.*;
import lombok.Setter;

@Table(name = "tax_setting")
@Entity
@Setter
public class Tax {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tax_id", updatable = false, unique = true, nullable = false)
    private Long taxId;

    @Column(nullable = false, unique = true, length = 5)
    private String name;

    @Column(nullable = false)
    private double rate;

    public Tax() {}

    public Tax(Long taxId, String name, double rate) {
        this.taxId = taxId;
        this.name = name;
        this.rate = rate;
    }

    public Long taxId() {
        return taxId;
    }

    public String name() {
        return name;
    }

    public double rate() {
        return rate;
    }

}
