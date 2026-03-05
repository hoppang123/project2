package com.company.asset.domain.department;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Department {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    private Department parent;

    @Builder
    public Department(String name, Department parent) {
        this.name = name;
        this.parent = parent;
    }
}
