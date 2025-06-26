package com.example.my_test_app.repository;

import com.example.my_test_app.model.Cart;
import com.example.my_test_app.model.User; // Userモデルのインポートが必要
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // @Queryアノテーションのインポートが必要
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUser(User user);

    @Query("SELECT c FROM Cart c " +
            "LEFT JOIN FETCH c.cartItems ci " +
            "LEFT JOIN FETCH ci.product p " +
            "WHERE c.user = :user")
    Optional<Cart> findByUserWithCartItems(User user);
}
