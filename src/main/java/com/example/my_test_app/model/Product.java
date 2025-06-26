package com.example.my_test_app.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal; // これがあることを確認

import jakarta.validation.constraints.NotBlank; // ★これがあることを確認
import jakarta.validation.constraints.NotNull;  // ★これがあることを確認
import jakarta.validation.constraints.Min;      // ★これがあることを確認
import jakarta.validation.constraints.Size;     // ★これがあることを確認

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ★重複している name の定義を削除し、バリデーションアノテーションを元々の name の上に移動
    @NotBlank(message = "商品名は必須です")
    @Size(max = 255, message = "商品名は255文字以内で入力してください")
    @Column(nullable = false, length = 255)
    private String name;

    // ★重複している description の定義を削除し、バリデーションアノテーションを元々の description の上に移動
    @NotBlank(message = "商品説明は必須です")
    @Size(max = 1000, message = "商品説明は1000文字以内で入力してください")
    @Column(nullable = false, length = 1000)
    private String description;

    // ★重複している price の定義を削除し、バリデーションアノテーションを元々の price の上に移動
    @NotNull(message = "価格は必須です")
    @Min(value = 0, message = "価格は0以上で入力してください")
    @Column(nullable = false, precision = 10, scale = 2) // DECIMAL(10, 2)に対応
    private BigDecimal price;

    // ★重複している type の定義を削除し、バリデーションアノテーションを元々の type の上に移動
    @NotNull(message = "商品の種類は必須です")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductType type;

    // ProductType enum は別の ProductType.java ファイルに分離済み

    // Product.java の既存のフィールドの下に追加
// ...

    @Column(length = 500) // URLの長さに合わせて調整
    private String imageUrl; // ★追加：商品画像のURL
}
