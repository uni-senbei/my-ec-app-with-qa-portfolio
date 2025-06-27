package com.example.my_test_app.service;

import com.example.my_test_app.model.Product;
import com.example.my_test_app.dto.ProductDto;
import com.example.my_test_app.model.ProductType; // ProductTypeのインポートが必要
import com.example.my_test_app.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.math.BigDecimal; // BigDecimalのインポートが必要

@Service
public class ProductService {

    private final ProductRepository productRepository;

    @Autowired
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // ========== 商品をDTOに変換するヘルパーメソッド ==========
    public ProductDto convertToDto(Product product) {
        return new ProductDto(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getImageUrl(), // ★ imageUrl を含める
                product.getType()
        );
    }

    // ========== DTOからエンティティに変換するヘルパーメソッド (新規作成・更新用) ==========
    public Product convertToEntity(ProductDto productDto) {
        Product product = new Product();
        // IDは新規作成時は不要、更新時のみ設定
        if (productDto.getId() != null) {
            product.setId(productDto.getId());
        }
        product.setName(productDto.getName());
        product.setDescription(productDto.getDescription());
        product.setPrice(productDto.getPrice());
        product.setImageUrl(productDto.getImageUrl()); // ★ imageUrl を含める
        product.setType(productDto.getType());
        return product;
    }

    // ========== 全ての商品を取得する ==========
    public List<ProductDto> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // ========== IDで商品を取得する ==========
    public Optional<ProductDto> getProductById(Long id) {
        return productRepository.findById(id)
                .map(this::convertToDto);
    }

    // ========== 新しい商品を作成する ==========
    @Transactional
    public ProductDto createProduct(ProductDto productDto) {
        Product product = convertToEntity(productDto);
        Product savedProduct = productRepository.save(product);
        return convertToDto(savedProduct);
    }

    // ========== 商品を更新する ==========
    @Transactional
    public Optional<ProductDto> updateProduct(Long id, ProductDto productDto) {
        // 1. 指定されたIDの商品をデータベースから検索
        Optional<Product> existingProductOptional = productRepository.findById(id);

        if (existingProductOptional.isPresent()) {
            // 2. 商品が見つかった場合、既存のエンティティを取得
            Product existingProduct = existingProductOptional.get();

            // 3. DTOのデータで既存のエンティティのフィールドを更新
            //    IDはURLパスから受け取ったものを使用し、DTOのIDは無視します。
            existingProduct.setName(productDto.getName());
            existingProduct.setDescription(productDto.getDescription());
            existingProduct.setPrice(productDto.getPrice());
            existingProduct.setImageUrl(productDto.getImageUrl()); // ★ imageUrl を含める
            existingProduct.setType(productDto.getType());

            // 4. 更新したエンティティをデータベースに保存
            Product updatedProduct = productRepository.save(existingProduct);

            // 5. 更新後のエンティティをDTOに変換して返す
            return Optional.of(convertToDto(updatedProduct));
        } else {
            // 6. 商品が見つからない場合は空のOptionalを返す
            return Optional.empty();
        }
    }

    // ========== 商品を削除する ==========
    @Transactional
    public boolean deleteProduct(Long id) {
        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
            return true;
        } else {
            return false;
        }
    }
}