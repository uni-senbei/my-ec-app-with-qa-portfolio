package com.example.my_test_app.repository;

import com.example.my_test_app.model.User; // この行に書き換える
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository // このインターフェースがリポジトリであることを示す
// JpaRepositoryを継承することで、基本的なCRUDメソッド（保存、検索、削除など）が自動的に提供される
public interface UserRepository extends JpaRepository<User, Long> {
    // 必要に応じて、emailでユーザーを検索するなどのカスタムメソッドを追加することも可能
    // User findByEmail(String email);
}