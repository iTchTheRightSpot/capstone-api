package com.emmanuel.sarabrandserver.test;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Table(name = "child_entity")
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class TestChildEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "child_id", nullable = false, unique = true)
    private Long id;

    @Column(name = "name")
    private String name;

    @OneToMany(mappedBy = "testChildEntity")
    private Set<TestEntity> entities;

}
