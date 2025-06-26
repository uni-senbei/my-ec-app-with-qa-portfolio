// src/main/java/com/example/my_test_app/dto/ProductDto.java (新規作成)
package com.example.my_test_app.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private String imageUrl;
    // 必要に応じて他のフィールドも追加
}