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
import org.springframework.web.bind.annotation.PutMapping; // これを追加
import java.util.Optional; // これを追加 (既にある可能性あり)
import org.springframework.http.ResponseEntity; // これを追加 (既にある可能性あり)
import org.springframework.web.bind.annotation.DeleteMapping; // これを追加
// @ResponseStatus(HttpStatus.NO_CONTENT) のために既に HttpStatus がインポートされているはずです
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
// ProductController.java の既存のメソッド (getAllProducts, getProductById, createProduct) の後に追記

    /**
     * 指定されたIDの商品情報を更新するAPI
     * PUT /api/products/{id}
     * @param id 更新対象の商品ID
     * @param product 新しい商品情報 (name, description, price, type)
     * @return 更新された商品情報、または404 Not Found
     */
    @PutMapping("/{id}") // /api/products/{id} へのPUTリクエスト
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        // 1. まず、指定されたIDの商品がデータベースに存在するかを確認する
        Optional<Product> existingProductOptional = productRepository.findById(id);

        if (existingProductOptional.isPresent()) {
            // 2. 商品が存在すれば、その情報を取得
            Product existingProduct = existingProductOptional.get();

            // 3. 受け取った商品情報で既存の情報を更新する
            existingProduct.setName(product.getName());
            existingProduct.setDescription(product.getDescription());
            existingProduct.setPrice(product.getPrice());
            existingProduct.setType(product.getType());

            // 4. 更新した商品をデータベースに保存（IDは変わらないため、更新として扱われる）
            Product updatedProduct = productRepository.save(existingProduct);

            // 5. 更新された商品情報とHTTPステータス 200 OK を返す
            return ResponseEntity.ok(updatedProduct);
        } else {
            // 6. 商品が存在しなければ、HTTPステータス 404 Not Found を返す
            return ResponseEntity.notFound().build();
        }
    }

    // ProductController.java の既存のメソッドの後に追記

    /**
     * 指定されたIDの商品を削除するAPI
     * DELETE /api/products/{id}
     * @param id 削除対象の商品ID
     * @return 削除成功時は204 No Content、存在しない場合は404 Not Found
     */
    @DeleteMapping("/{id}") // /api/products/{id} へのDELETEリクエスト
    @ResponseStatus(HttpStatus.NO_CONTENT) // 成功時はHTTPステータスコード 204 No Content を返す
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        // 1. 指定されたIDの商品がデータベースに存在するかを確認する
        if (productRepository.existsById(id)) {
            // 2. 商品が存在すれば、削除する
            productRepository.deleteById(id);
            // 3. 削除成功（コンテンツなし）を返す
            return ResponseEntity.noContent().build();
        } else {
            // 4. 商品が存在しなければ、404 Not Found を返す
            return ResponseEntity.notFound().build();
        }
    }
} // <<< ProductController クラス全体の閉じ括弧