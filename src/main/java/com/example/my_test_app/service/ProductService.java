package com.example.my_test_app.service;

import com.example.my_test_app.dto.ProductDto;
import com.example.my_test_app.exceptions.ResourceNotFoundException; // ★ 追加
import com.example.my_test_app.model.Product;
import com.example.my_test_app.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    @Autowired
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // ========== DTO変換ヘルパーメソッド ==========
    private ProductDto convertToDto(Product product) {
        return new ProductDto(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getType(),
                product.getImageUrl()
        );
    }

    private Product convertToEntity(ProductDto productDto) {
        Product product = new Product();
        product.setId(productDto.getId()); // IDは更新時に使用
        product.setName(productDto.getName());
        product.setDescription(productDto.getDescription());
        product.setPrice(productDto.getPrice());
        product.setType(productDto.getType());
        product.setImageUrl(productDto.getImageUrl());
        return product;
    }

    // ========== CRUD 操作 ==========

    /**
     * 全ての商品を取得する
     * @return 全ての商品DTOのリスト
     */
    public List<ProductDto> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * IDで商品を検索する
     * @param id 商品ID
     * @return 該当する商品DTO (Optional)
     */
    public Optional<ProductDto> getProductById(Long id) {
        return productRepository.findById(id)
                .map(this::convertToDto);
    }

    /**
     * 新しい商品を作成する
     * @param productDto 作成する商品のDTO
     * @return 作成された商品DTO
     */
    @Transactional
    public ProductDto createProduct(ProductDto productDto) {
        Product product = convertToEntity(productDto);
        Product savedProduct = productRepository.save(product);
        return convertToDto(savedProduct);
    }

    /**
     * 商品を更新する
     * @param id 更新する商品のID
     * @param productDto 更新内容を含むDTO
     * @return 更新された商品DTO
     * @throws ResourceNotFoundException 指定されたIDの商品が見つからない場合
     */
    @Transactional
    public ProductDto updateProduct(Long id, ProductDto productDto) { // ★ 戻り値を Optional<ProductDto> から ProductDto に変更
        return productRepository.findById(id)
                .map(existingProduct -> {
                    existingProduct.setName(productDto.getName());
                    existingProduct.setDescription(productDto.getDescription());
                    existingProduct.setPrice(productDto.getPrice());
                    existingProduct.setType(productDto.getType());
                    existingProduct.setImageUrl(productDto.getImageUrl());
                    Product updatedProduct = productRepository.save(existingProduct);
                    return convertToDto(updatedProduct);
                })
                .orElseThrow(() -> new ResourceNotFoundException("指定されたIDの商品が見つかりません: " + id)); // ★ 例外をスロー
    }

    /**
     * 商品を削除する
     * @param id 削除する商品のID
     * @return 削除が成功した場合は true、見つからない場合は ResourceNotFoundException をスロー
     * @throws ResourceNotFoundException 指定されたIDの商品が見つからない場合
     */
    @Transactional
    public boolean deleteProduct(Long id) { // ★ 戻り値を boolean から boolean に変更なしだが、例外スローを追加
        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
            return true;
        } else {
            throw new ResourceNotFoundException("指定されたIDの商品が見つかりません: " + id); // ★ 例外をスロー
        }
    }
}