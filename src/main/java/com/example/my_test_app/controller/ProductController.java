package com.example.my_test_app.controller;

import com.example.my_test_app.dto.ProductDto;
import com.example.my_test_app.model.ProductType;
import com.example.my_test_app.service.ProductService;
import com.example.my_test_app.exceptions.ResourceNotFoundException; // ★ 追加
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal; // BigDecimalのインポートが必要な場合
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional; // Optionalのインポートが必要な場合

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
        // ProductServiceはOptionalを返すため、ここでは引き続きOptionalで処理
        return productDto.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
        // ★ 注意: 今回の404標準化では、このGETメソッドは変更しないことにします。
        // なぜなら、Optionalを返すことで、見つからない場合は空のボディで404を返すという明確な意図があるためです。
        // もし、GETでも{ "message": "..." } のような404ボディを返したい場合は、
        // productDto.orElseThrow(() -> new ResourceNotFoundException("商品が見つかりません: " + id));
        // とし、このコントローラーの@ExceptionHandlerで捕捉するように変更できます。
        // しかし、GETでは通常、存在しないリソースへのリクエストは空のボディで404が一般的です。
    }

    /**
     * 新しい商品を追加するAPI
     * POST /api/products
     * @param productDto 登録する商品情報
     * @return 登録された商品情報 (IDが付与されたもの)
     */
    @PostMapping
    public ResponseEntity<ProductDto> createProduct(@Valid @RequestBody ProductDto productDto){
        ProductDto createdProduct = productService.createProduct(productDto);
        return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
    }

    /**
     * 指定されたIDの商品情報を更新するAPI
     * PUT /api/products/{id}
     * @param id 更新対象の商品ID
     * @param productDto 新しい商品情報 (name, description, price, type, imageUrl)
     * @return 更新された商品情報
     * @throws ResourceNotFoundException 指定されたIDの商品が見つからない場合
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProductDto> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductDto productDto) { // ★ 戻り値をResponseEntity<ProductDto> に変更
        // ProductServiceがResourceNotFoundExceptionをスローするようになったため、Optionalのチェックは不要
        ProductDto updatedProduct = productService.updateProduct(id, productDto);
        return new ResponseEntity<>(updatedProduct, HttpStatus.OK);
    }

    /**
     * 指定されたIDの商品を削除するAPI
     * DELETE /api/products/{id}
     * @param id 削除対象の商品ID
     * @return 削除成功時は204 No Content
     * @throws ResourceNotFoundException 指定されたIDの商品が見つからない場合
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        // ProductServiceがResourceNotFoundExceptionをスローするようになったため、booleanのチェックは不要
        productService.deleteProduct(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // ========== 例外ハンドラ ==========

    /**
     * バリデーションエラー（@Validアノテーションによる検証失敗）を処理するハンドラ
     * @param ex MethodArgumentNotValidException
     * @return フィールドごとのエラーメッセージを含むMap
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST) // HTTPステータスコードを400 Bad Requestに設定
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));
        return errors;
    }

    /**
     * リソースが見つからない場合の例外（ResourceNotFoundException）を処理するハンドラ
     * @param ex ResourceNotFoundException
     * @return エラーメッセージを含むMap
     */
    @ResponseStatus(HttpStatus.NOT_FOUND) // HTTPステータスコードを404 Not Foundに設定
    @ExceptionHandler(ResourceNotFoundException.class)
    public Map<String, String> handleResourceNotFoundException(ResourceNotFoundException ex) {
        Map<String, String> errors = new HashMap<>();
        errors.put("message", ex.getMessage()); // 例外メッセージを"message"キーで返す
        return errors;
    }
}