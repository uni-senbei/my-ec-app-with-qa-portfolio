// src/main/java/com/example/my_test_app/dto/CartItemDto.java (新規作成)
package com.example.my_test_app.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDto {
    private Long id;
    private int quantity;
    private ProductDto product; // CartItemが持つProduct情報をDTOとして表現
    // Cartへの参照は含めない
}