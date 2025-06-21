package com.example.my_test_app.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal; // これを追加！

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2) // DECIMAL(10, 2)に対応
    private BigDecimal price; // ここを Double から BigDecimal に変更！

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductType type;

    }
