package com.example.my_test_app.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "users") // 'user' はDBの予約語と被る可能性があるので 'users' が一般的
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(nullable = false, unique = true) // ユーザー名は必須、かつユニーク
    private String name; // 'name' ではなく 'username' の方が一般的です

    @Column(nullable = false, unique = true) // メールアドレスも必須、かつユニーク
    private String email;

    // TODO: 後でパスワードなどを追加

    // ユーザーとカートは1対1の関係
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Cart cart;

    // 便利メソッド：ユーザーが作成されたときにカートも自動的に作成されるように
    public void createCartForUser() {
        if (this.cart == null) {
            this.cart = new Cart();
            this.cart.setUser(this); // 双方向参照の設定
        }
    }
}