package com.example.my_test_app.service;

import com.example.my_test_app.model.Cart;
import com.example.my_test_app.model.CartItem;
import com.example.my_test_app.model.User;
import com.example.my_test_app.model.Product;
import com.example.my_test_app.dto.CartDto;
import com.example.my_test_app.dto.CartItemDto;
import com.example.my_test_app.dto.ProductDto;
import com.example.my_test_app.repository.CartRepository;
import com.example.my_test_app.repository.CartItemRepository;
import com.example.my_test_app.repository.UserRepository;
import com.example.my_test_app.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Autowired
    public CartService(CartRepository cartRepository, CartItemRepository cartItemRepository,
                       UserRepository userRepository, ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    // ========== カートに商品を追加するロジック (DTOを返すように変更) ==========
    @Transactional
    public Optional<CartItemDto> addProductToCart(Long userId, Long productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // ユーザーに紐づくカートを取得または作成
        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        Optional<CartItem> existingCartItemOptional = cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId());

        CartItem cartItem;
        if (existingCartItemOptional.isPresent()) {
            // 既存のアイテムがある場合、数量を更新
            cartItem = existingCartItemOptional.get();
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
            cartItem = cartItemRepository.save(cartItem);
        } else {
            // 新規アイテムの場合、種類数の最終チェック
            Set<CartItem> currentCartItems = cart.getCartItems();
            if (currentCartItems.size() >= 20) {
                throw new IllegalStateException("カートに追加できる商品の種類は20個までです。");
            }

            // 新しいアイテムとして追加
            cartItem = new CartItem(cart, product, quantity);
            cart.getCartItems().add(cartItem);

            cartItem = cartItemRepository.save(cartItem);
        }

        cartRepository.save(cart); // Cartエンティティの内部状態（cartItemsコレクション）が変更された可能性があるため保存

        // ★保存されたCartItemエンティティをDTOに変換して返す
        return Optional.of(convertToCartItemDto(cartItem));
    }

    // ========== カートから商品を削除するロジック (変更なし、booleanを返すため) ==========
    @Transactional
    public boolean removeProductFromCart(Long userId, Long productId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Cart not found for user: " + userId));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        Optional<CartItem> existingCartItemOptional = cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId());

        if (existingCartItemOptional.isPresent()) {
            CartItem cartItem = existingCartItemOptional.get();
            cart.getCartItems().remove(cartItem);
            cartItemRepository.delete(cartItem);
            cartRepository.save(cart);
            return true;
        }
        return false;
    }

    // ========== カートの内容を取得するロジック (CartDtoを返すように変更) ==========
    @Transactional(readOnly = true)
    public Optional<CartDto> getCartByUserId(Long userId) {
        return userRepository.findById(userId)
                .flatMap(user -> cartRepository.findByUserWithCartItems(user))
                .map(this::convertToCartDto);
    }

    // ========== カート内のアイテム数量を更新するロジック (DTOを返すように変更) ==========
    @Transactional
    public Optional<CartItemDto> updateCartItemQuantity(Long userId, Long productId, int newQuantity) {
        if (newQuantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative.");
        }

        if (newQuantity == 0) {
            boolean removed = removeProductFromCart(userId, productId);
            return removed ? Optional.empty() : Optional.empty();
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Cart not found for user: " + userId));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        Optional<CartItem> existingCartItemOptional = cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId());

        if (existingCartItemOptional.isPresent()) {
            CartItem cartItem = existingCartItemOptional.get();
            cartItem.setQuantity(newQuantity);
            cartItem = cartItemRepository.save(cartItem);
            cartRepository.save(cart);

            return Optional.of(convertToCartItemDto(cartItem));
        } else {
            return addProductToCart(userId, productId, newQuantity);
        }
    }
    // ========== カートをクリアするロジック（変更なし） ==========
    @Transactional
    public void clearCart(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Cart not found for user: " + userId));

        cartItemRepository.deleteByCartId(cart.getId());
        cart.getCartItems().clear();
        cartRepository.save(cart);
    }

    // ========== エンティティからDTOへの変換ヘルパーメソッド ==========

    // CartItemエンティティをCartItemDtoに変換
    private CartItemDto convertToCartItemDto(CartItem cartItem) {
        ProductDto productDto = new ProductDto(
                cartItem.getProduct().getId(),
                cartItem.getProduct().getName(),
                cartItem.getProduct().getDescription(),
                cartItem.getProduct().getPrice(),
                cartItem.getProduct().getType(),    // ★ type を先に変更
                cartItem.getProduct().getImageUrl() // ★ imageUrl を後に変更
        );
        return new CartItemDto(cartItem.getId(), cartItem.getQuantity(), productDto);
    }

    // CartエンティティをCartDtoに変換
    private CartDto convertToCartDto(Cart cart) {
        if (cart == null) {
            return null;
        }

        List<CartItemDto> cartItemDtos = cart.getCartItems().stream()
                .map(this::convertToCartItemDto)
                .collect(Collectors.toList());

        double totalPrice = cart.getCartItems().stream()
                .mapToDouble(item -> item.getProduct().getPrice().doubleValue() * item.getQuantity())
                .sum();

        return new CartDto(
                cart.getId(),
                cart.getUser().getId(),
                cartItemDtos,
                totalPrice
        );
    }
}