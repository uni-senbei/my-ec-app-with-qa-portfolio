package com.example.my_test_app.repository;

import com.example.my_test_app.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    // トークン文字列でPasswordResetTokenを検索するメソッド
    Optional<PasswordResetToken> findByToken(String token);

    // ユーザーでPasswordResetTokenを検索するメソッド（1ユーザーにつき1つの有効なトークンを想定）
    Optional<PasswordResetToken> findByUser(com.example.my_test_app.model.User user);
}