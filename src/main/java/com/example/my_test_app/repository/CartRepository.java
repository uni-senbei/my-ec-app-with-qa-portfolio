package com.example.my_test_app.repository;

import com.example.my_test_app.model.Cart;
import com.example.my_test_app.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    // 特定のユーザーに紐づくカートを取得
    Optional<Cart> findByUser(User user);
}