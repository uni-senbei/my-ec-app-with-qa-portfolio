package com.example.my_test_app.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties; // 追加
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "carts")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) // 追加
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    @ToString.Exclude
    @JsonIgnoreProperties("cart") // 追加
    private User user;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("cart-items") // 識別子を追加
    @ToString.Exclude // 既存（前回修正済み）
    private Set<CartItem> cartItems = new HashSet<>();

    // ... 他のフィールドは変更なし ...

    /*
    // ★ ここから lastModifiedDate 関連を再有効化
    @Column(name = "last_modified_date", nullable = false)
    @Temporal(TemporalType.TIMESTAMP) // 日時型を指定
    @UpdateTimestamp // エンティティが更新されるたびに自動的にタイムスタンプを更新
    private Date lastModifiedDate;

    // Optional: 作成日時も必要であれば追加
    // @Column(name = "created_date", nullable = false, updatable = false)
    // @Temporal(TemporalType.TIMESTAMP)
    // @CreationTimestamp
    // private Date createdDate;
    // ★ ここまで lastModifiedDate 関連を再有効化
    */

    // ユーティリティメソッド
    public void addCartItem(CartItem item) {
        cartItems.add(item);
        item.setCart(this);
    }

    public void removeCartItem(CartItem item) {
        cartItems.remove(item);
        item.setCart(null);
    }
}