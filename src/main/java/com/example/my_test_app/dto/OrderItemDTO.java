package com.example.my_test_app.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class OrderItemDTO {
    private Long id;
    private Long productId;
    private String productName;
    private BigDecimal itemPrice;
    private int quantity;
}