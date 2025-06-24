package com.example.my_test_app.repository;

import com.example.my_test_app.model.Cart;
import com.example.my_test_app.model.User; // Userモデルのインポートが必要
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // @Queryアノテーションのインポートが必要
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    // 特定のユーザーに紐づくカートを取得するメソッド
    // Userエンティティを引数として受け取る
    Optional<Cart> findByUser(User user); // ★追加

    // 特定のユーザーに紐づくカートとそのカートアイテムを同時に取得するメソッド
    // N+1問題を避けるためにJOIN FETCHを使用
    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.cartItems WHERE c.user = :user") // ★追加
    Optional<Cart> findByUserWithCartItems(User user);
}