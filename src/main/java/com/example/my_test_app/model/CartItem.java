// src/main/java/com/example/my_test_app/model/CartItem.java

package com.example.my_test_app.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.NonNull;

// ProductクラスとCartItemクラスが同じパッケージにある場合、
// 通常このimport文は不要ですが、IDEが自動で追加したり削除したりする場合があります。
// 必要に応じて残してください。
// import com.example.my_test_app.model.Product;

@Entity
@Table(name = "cart_items")
@Data
@NoArgsConstructor // Lombokが引数なしコンストラクタを自動生成します（JPAで必要）
@RequiredArgsConstructor // Lombokが @NonNull が付いたフィールドのみを引数とするコンストラクタを自動生成します
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    @NonNull // RequiredArgsConstructorがこのフィールドをコンストラクタに含めるように指示
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @NonNull // RequiredArgsConstructorがこのフィールドをコンストラクタに含めるように指示
    private Product product;

    @Column(nullable = false)
    // プリミティブ型 (int) はnullになりえないため、@NonNull は不要ですが、
    // RequiredArgsConstructorに含める場合は、Integer (ラッパークラス) にするか、
    // このフィールドはコンストラクタに含めない設計にする場合があります。
    // しかし、今回の目的（new CartItem(cart, product, quantity)を可能にする）のためには、
    // int型のまま @NonNull を付けてRequiredArgsConstructorに含めるのが適切です。
    // Lombokは自動的にコンストラクタ引数として含めてくれますが、警告は出る可能性があります。
    @NonNull // RequiredArgsConstructorがこのフィールドをコンストラクタに含めるように指示
    private int quantity; // 商品の数量
}