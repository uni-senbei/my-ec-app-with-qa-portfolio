package com.example.my_test_app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List; // CartItemDtoのリストを持つため

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartDto {
    private Long id;
    private Long userId; // ユーザーIDを直接持たせる
    private List<CartItemDto> cartItems; // CartItemDtoのリスト
    private double totalPrice; // カート合計金額など、DTOで計算して持たせたい情報があれば追加
}