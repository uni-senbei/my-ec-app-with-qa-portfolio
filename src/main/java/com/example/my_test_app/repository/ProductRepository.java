// src/main/java/com/example/my_test_app/repository/ProductRepository.java

package com.example.my_test_app.repository;

import com.example.my_test_app.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    // Spring Data JPAが自動的に基本的なCRUD操作を提供してくれるため、
    // ここに特別なメソッドを記述する必要はありません。
    // 必要に応じて、findBy* などのカスタムクエリメソッドを定義できますが、
    // 今回はまずは不要です。
}