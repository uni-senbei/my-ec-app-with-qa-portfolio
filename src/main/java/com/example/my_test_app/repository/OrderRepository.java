package com.example.my_test_app.repository;

import com.example.my_test_app.model.Order;
import com.example.my_test_app.model.User; // Userエンティティをインポート
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List; // Listをインポート
import java.util.Optional; // Optionalをインポート

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    // 特定のユーザーの注文履歴を検索するメソッド (オプション)
    List<Order> findByUser(User user);

    // 特定のユーザーの最新の注文を検索するメソッド (オプション)
    // Optional<Order> findTopByUserOrderByOrderDateDesc(User user);
}