package com.example.my_test_app.repository;

import com.example.my_test_app.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // usernameでユーザーを検索するためのメソッド
    // Spring Data JPAが自動的にこのメソッドの実装を生成します
    Optional<User> findByUsername(String username);

    // emailでユーザーを検索するためのメソッド（必要に応じて）
    Optional<User> findByEmail(String email);

    // usernameまたはemailでユーザーが存在するかどうかを確認するためのメソッド
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}