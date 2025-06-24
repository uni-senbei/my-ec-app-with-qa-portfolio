// src/main/java/com/example/my_test_app/model/Cart.java
package com.example.my_test_app.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.HashSet;
import java.util.Set; // Setをインポート

@Entity
@Table(name = "cart")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    // ★追加または修正: CartItemとのOneToManyリレーションシップ
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CartItem> cartItems = new HashSet<>();
    // Getter, SetterはLombokが生成しますが、初期化のために new HashSet<>() を明示的に追加することが推奨されます。
    // また、このフィールドに対するGetter/SetterはLombokが@Dataで自動生成してくれます。
}