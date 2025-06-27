package com.example.my_test_app.dto;

import com.example.my_test_app.model.ProductType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data // @Getter, @Setter, @EqualsAndHashCode, @ToStringを含む
@NoArgsConstructor // 引数なしコンストラクタを生成
@AllArgsConstructor // 全引数コンストラクタを生成
public class ProductDto {
    private Long id; // 更新時に使用
    private String name;
    private String description;
    private BigDecimal price;
    private String imageUrl; // ★ Product.java に合わせて再度追加
    private ProductType type; // enum型
}