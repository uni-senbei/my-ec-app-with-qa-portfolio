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
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/products") // このコントローラーのベースパス
public class ProductController {

    @Autowired
    private ProductRepository productRepository; // ProductRepositoryを自動注入

    /**
     * 全ての商品一覧を取得するAPI
     * GET /api/products
     * @return 全ての商品リスト
     */
    @GetMapping // /api/products へのGETリクエスト
    public List<Product> getAllProducts() {
        return productRepository.findAll(); // 全ての商品を取得して返す
    }

    /**
     * 指定されたIDの商品詳細を取得するAPI
     * GET /api/products/{id}
     * @param id 商品ID
     * @return 商品詳細、または404 Not Found
     */
    @GetMapping("/{id}") // /api/products/{id} へのGETリクエスト
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        Optional<Product> product = productRepository.findById(id); // IDで商品を検索
        return product.map(ResponseEntity::ok) // 商品が見つかれば200 OKと商品を返す
                .orElse(ResponseEntity.notFound().build()); // 見つからなければ404 Not Foundを返す
    } // <<< ここに } が必要でした！ getProductById メソッドの閉じ括弧

    /**
     * 新しい商品を追加するAPI
     * POST /api/products
     * @param product 登録する商品情報
     * @return 登録された商品情報 (IDが付与されたもの)
     */
    @PostMapping // /api/products へのPOSTリクエスト
    @ResponseStatus(HttpStatus.CREATED) // HTTPステータスコード 201 Created を返す
    public Product createProduct(@RequestBody Product product){
        // ProductRepositoryのsaveメソッドを使ってDBに保存し、保存されたエンティティを返す
        return productRepository.save(product);
    } // <<< createProduct メソッドの閉じ括弧

} // <<< ProductController クラス全体の閉じ括弧