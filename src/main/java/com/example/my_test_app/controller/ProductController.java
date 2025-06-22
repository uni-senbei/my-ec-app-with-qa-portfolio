// src/main/java/com/example/my_test_app/controller/ProductController.java

package com.example.my_test_app.controller;

import com.example.my_test_app.model.Product;
import com.example.my_test_app.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.http.HttpStatus; // DELETEやPOSTのResponseStatusで使う
import jakarta.validation.Valid; // ★これがあることを確認

import java.util.List;
import java.util.Optional; // ★この重複しているOptionalを削除してください

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    /**
     * 全ての商品一覧を取得するAPI
     * GET /api/products
     * @return 全ての商品リスト
     */
    @GetMapping
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    /**
     * 指定されたIDの商品詳細を取得するAPI
     * GET /api/products/{id}
     * @param id 商品ID
     * @return 商品詳細、または404 Not Found
     */
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        Optional<Product> product = productRepository.findById(id);
        return product.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 新しい商品を追加するAPI
     * POST /api/products
     * @param product 登録する商品情報
     * @return 登録された商品情報 (IDが付与されたもの)
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Product createProduct(@Valid @RequestBody Product product){ // ★ ここに @Valid を追加
        return productRepository.save(product);
    }

    /**
     * 指定されたIDの商品情報を更新するAPI
     * PUT /api/products/{id}
     * @param id 更新対象の商品ID
     * @param product 新しい商品情報 (name, description, price, type)
     * @return 更新された商品情報、または404 Not Found
     */
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @Valid @RequestBody Product product) { // ★ ここに @Valid を追加
        Optional<Product> existingProductOptional = productRepository.findById(id);

        if (existingProductOptional.isPresent()) {
            Product existingProduct = existingProductOptional.get();
            // 受け取った商品情報で既存の情報を更新する
            existingProduct.setName(product.getName());
            existingProduct.setDescription(product.getDescription());
            existingProduct.setPrice(product.getPrice());
            existingProduct.setType(product.getType());

            Product updatedProduct = productRepository.save(existingProduct);
            return ResponseEntity.ok(updatedProduct);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 指定されたIDの商品を削除するAPI
     * DELETE /api/products/{id}
     * @param id 削除対象の商品ID
     * @return 削除成功時は204 No Content、存在しない場合は404 Not Found
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}