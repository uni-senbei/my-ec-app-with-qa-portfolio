package com.example.my_test_app.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties; // 追加
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import lombok.ToString; // 追加

@Entity
@Table(name = "cart_items")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) // 追加
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    @JsonBackReference("cart-items") // 識別子を追加
    @ToString.Exclude // ★ 追加：cart フィールドを toString() から除外
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnoreProperties("cartItems") // 追加
    @ToString.Exclude // 既存（前回修正済み）
    private Product product;

    @Column(nullable = false)
    private int quantity;

    // コンストラクタは変更なし
    public CartItem(Cart cart, Product product, int quantity) {
        this.cart = cart;
        this.product = product;
        this.quantity = quantity;
    }
}