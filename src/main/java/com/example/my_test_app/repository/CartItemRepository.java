package com.example.my_test_app.repository;

import com.example.my_test_app.model.CartItem;
import com.example.my_test_app.model.Cart;
import com.example.my_test_app.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    // 特定のカートと商品に紐づくカートアイテムを取得
    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);
}