package com.example.my_test_app.dto;

import com.example.my_test_app.model.ProductType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

// ★ バリデーション関連のインポートを追加
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {
    private Long id; // 更新時に使用。新規作成時は通常null

    @NotBlank(message = "商品名は必須です。") // 空文字列やnullを許可しない
    @Size(max = 255, message = "商品名は255文字以内で入力してください。") // 最大長
    private String name;

    @NotBlank(message = "商品説明は必須です。")
    @Size(max = 1000, message = "商品説明は1000文字以内で入力してください。")
    private String description;

    @NotNull(message = "価格は必須です。") // nullを許可しない
    @Min(value = 0, message = "価格は0以上でなければなりません。") // 最小値
    private BigDecimal price;

    @NotNull(message = "商品種別は必須です。")
    private ProductType type;

    // imageUrlは必須ではないが、もしURLとして有効な形式を強制するなら、
    // @Pattern(regexp = "^(http|https)://.*$", message = "無効なURL形式です。")
    // などを追加することも可能ですが、今回は必須チェックのみに留めます
    private String imageUrl;
}