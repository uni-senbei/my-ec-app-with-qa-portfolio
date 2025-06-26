// src/main/java/com/example/my_test_app/repository/CartItemRepository.java

package com.example.my_test_app.repository;

import com.example.my_test_app.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional; // Optionalをインポート

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    // 特定のカートと商品IDに紐づくカートアイテムを見つけるカスタムクエリ
    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);

    // カート内の商品の種類数を数えるためのカスタムメソッド (オプション)
    // Long countDistinctProductByCartId(Long cartId); // 後で必要に応じて追加

    // カートIDに基づいて全てのカートアイテムを削除するメソッド (clearCartで利用)
    void deleteByCartId(Long cartId);
}