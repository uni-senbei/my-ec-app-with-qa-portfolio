package com.example.my_test_app.repository;

import com.example.my_test_app.model.OrderItem;
import com.example.my_test_app.model.Order; // Orderエンティティをインポート
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List; // Listをインポート

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    // 特定の注文に属するすべての注文明細を検索するメソッド (オプション)
    List<OrderItem> findByOrder(Order order);
}