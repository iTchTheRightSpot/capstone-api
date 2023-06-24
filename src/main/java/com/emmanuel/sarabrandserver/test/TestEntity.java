package com.emmanuel.sarabrandserver.test;

import jakarta.persistence.*;
import lombok.*;

@Table(name = "test_entity")
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class TestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    @Column(name = "name", unique = true, nullable = false)
    private String name;

    @Column(name = "sku", unique = true, nullable = false)
    private String sku;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "child_id", referencedColumnName = "child_id", nullable = false)
    private TestChildEntity testChildEntity;

    public void setTestChildEntity(TestChildEntity testChildEntity) {
        this.testChildEntity = testChildEntity;
        this.testChildEntity.getEntities().add(this);
    }

}
