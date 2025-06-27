package com.example.my_test_app.controller;

import com.example.my_test_app.dto.ProductDto;
import com.example.my_test_app.model.ProductType;
import com.example.my_test_app.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

// @Valid は ProductDto にバリデーションアノテーションが付いたときにインポートします
// import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * 全ての商品一覧を取得するAPI
     * GET /api/products
     * @return 全ての商品リスト
     */
    @GetMapping
    public List<ProductDto> getAllProducts() {
        return productService.getAllProducts();
    }

    /**
     * 指定されたIDの商品詳細を取得するAPI
     * GET /api/products/{id}
     * @param id 商品ID
     * @return 商品詳細、または404 Not Found
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProductById(@PathVariable Long id) {
        Optional<ProductDto> productDto = productService.getProductById(id);
        return productDto.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * 新しい商品を追加するAPI
     * POST /api/products
     * @param productDto 登録する商品情報
     * @return 登録された商品情報 (IDが付与されたもの)
     */
    @PostMapping
    public ResponseEntity<ProductDto> createProduct(@RequestBody ProductDto productDto){
        ProductDto createdProduct = productService.createProduct(productDto);
        return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
    }

    /**
     * 指定されたIDの商品情報を更新するAPI
     * PUT /api/products/{id}
     * @param id 更新対象の商品ID
     * @param productDto 新しい商品情報 (name, description, price, type, imageUrl)
     * @return 更新された商品情報、または404 Not Found
     */
    @PutMapping("/{id}")
    public ResponseEntity<Object> updateProduct(@PathVariable Long id, @RequestBody ProductDto productDto) {
        // 簡易的なバリデーション（@Valid を使う場合はProductDtoにアノテーションを付与し、ここを削除）
        if (productDto.getName() == null || productDto.getName().trim().isEmpty() ||
                productDto.getPrice() == null || productDto.getPrice().compareTo(BigDecimal.ZERO) <= 0 ||
                productDto.getType() == null) {
            return new ResponseEntity<>(Collections.singletonMap("message", "商品名、価格、商品種別は必須です。価格は0より大きい値にしてください。"), HttpStatus.BAD_REQUEST);
        }

        Optional<ProductDto> updatedProductDto = productService.updateProduct(id, productDto);

        if (updatedProductDto.isPresent()) {
            return new ResponseEntity<>(updatedProductDto.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(Collections.singletonMap("message", "指定されたIDの商品が見つかりません: " + id), HttpStatus.NOT_FOUND);
        }
    }

    /**
     * 指定されたIDの商品を削除するAPI
     * DELETE /api/products/{id}
     * @param id 削除対象の商品ID
     * @return 削除成功時は204 No Content、存在しない場合は404 Not Found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        boolean deleted = productService.deleteProduct(id);
        if (deleted) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}