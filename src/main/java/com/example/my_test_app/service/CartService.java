package com.example.my_test_app.service;

import com.example.my_test_app.model.Cart;
import com.example.my_test_app.model.CartItem;
import com.example.my_test_app.model.User;
import com.example.my_test_app.model.Product; // Productのパッケージに合わせて修正
import com.example.my_test_app.repository.CartRepository;
import com.example.my_test_app.repository.CartItemRepository;
import com.example.my_test_app.repository.UserRepository;
import com.example.my_test_app.repository.ProductRepository; // ProductRepositoryのパッケージに合わせて修正
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository; // ProductRepositoryをインジェクト

    // コンストラクタインジェクション
    @Autowired
    public CartService(CartRepository cartRepository, CartItemRepository cartItemRepository,
                       UserRepository userRepository, ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    // ========== カートに商品を追加するロジック ==========
    // ユーザーID、商品ID、数量を受け取る
    @Transactional
    public Optional<CartItem> addProductToCart(Long userId, Long productId, int quantity) {
        if (quantity <= 0) {
            // 数量が0以下の場合は追加しない
            return Optional.empty();
        }

        // 1. ユーザーを取得 (認証機能がないので、仮のユーザーを作成または取得)
        User user = userRepository.findById(userId).orElseGet(() -> {
            // ユーザーが存在しない場合、新しいユーザーを仮で作成（デバッグ・テスト用）
            // 実際にはログインしたユーザーの情報を利用する
            User newUser = new User();
            newUser.setName("GuestUser" + userId); // 仮の名前
            newUser.setEmail("guest" + userId + "@example.com"); // 仮のメール
            newUser.createCartForUser(); // ユーザー作成時にカートも作成
            return userRepository.save(newUser);
        });

        // 2. ユーザーに紐づくカートを取得または作成
        Cart cart = user.getCart();
        if (cart == null) {
            cart = new Cart();
            cart.setUser(user);
            user.setCart(cart); // 双方向参照設定
            cart = cartRepository.save(cart); // カートを保存してIDを生成
        }

        // 3. カートの最大商品数制限のチェック (20種類まで)
        // 「要件追加・変更リスト.txt」の「カートの最大商品数オーバー時」に対応
        if (cart.getCartItems().size() >= 20) {
            // 既に20種類の商品がある場合、既存の商品の数量を増やす場合を除き、新規追加は不可
            // 後続のロジックで既存商品かチェックするため、ここではまだ例外は投げない
            // ただし、このチェックはあくまで種類数なので、厳密にはfindByCartAndProduct後に再度チェックが必要
        }

        // 4. 商品を取得
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        // 5. カートに既存のアイテムがあるか確認
        Optional<CartItem> existingCartItemOptional = cartItemRepository.findByCartAndProduct(cart, product);

        CartItem cartItem;
        if (existingCartItemOptional.isPresent()) {
            // 既存のアイテムがある場合、数量を更新
            cartItem = existingCartItemOptional.get();
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
            cartItem = cartItemRepository.save(cartItem);
        } else {
            // 新規アイテムの場合、種類数の最終チェック
            if (cart.getCartItems().size() >= 20) {
                // 新規商品を追加しようとして、種類数が20を超過する場合
                throw new IllegalStateException("カートに追加できる商品の種類は20個までです。");
            }
            // 新しいアイテムとして追加
            cartItem = new CartItem(cart, product, quantity);
            cart.addCartItem(cartItem); // カートエンティティにアイテムを追加（双方向参照）
            cartItem = cartItemRepository.save(cartItem);
        }

        // カート自体を保存（CartItemの追加/更新により関連が変更された可能性があるため）
        cartRepository.save(cart);

        return Optional.of(cartItem);
    }

    // ========== カートから商品を削除するロジック ==========
    @Transactional
    public boolean removeProductFromCart(Long userId, Long productId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        Cart cart = user.getCart();
        if (cart == null) {
            return false; // カートがない場合は削除できない
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        Optional<CartItem> existingCartItemOptional = cartItemRepository.findByCartAndProduct(cart, product);

        if (existingCartItemOptional.isPresent()) {
            CartItem cartItem = existingCartItemOptional.get();
            cart.removeCartItem(cartItem); // Cartエンティティからアイテムを削除（双方向参照）
            cartItemRepository.delete(cartItem); // CartItemを削除
            cartRepository.save(cart); // カートを保存
            return true;
        }
        return false; // カートに該当商品がない場合
    }

    // ========== カートの内容を取得するロジック ==========
    @Transactional(readOnly = true) // 読み取り専用トランザクション
    public Optional<Cart> getCartByUserId(Long userId) {
        // ユーザーに紐づくカートをフェッチする際に、カートアイテムも同時にロードする
        // これにより、N+1問題を回避し、効率的にデータを取得
        return userRepository.findById(userId)
                .map(user -> {
                    // Lazy Loading の設定に依存するため、明示的に fetch するか、
                    // Cartエンティティの @OneToMany に FetchType.EAGER を設定することも可能
                    // ただしEAGERはパフォーマンスに影響を与える可能性があるので注意
                    // ここではシンプルにuser.getCart()を返す。後で最適化が必要になる可能性あり。
                    return user.getCart();
                });
    }

    // ========== カート内のアイテム数量を更新するロジック (オプション) ==========
    @Transactional
    public Optional<CartItem> updateCartItemQuantity(Long userId, Long productId, int newQuantity) {
        if (newQuantity <= 0) {
            // 数量が0以下の場合は削除と見なす
            return removeProductFromCart(userId, productId) ? Optional.empty() : Optional.ofNullable(null); // 削除成功ならOptional.empty()
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        Cart cart = user.getCart();
        if (cart == null) {
            throw new RuntimeException("Cart not found for user: " + userId);
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        Optional<CartItem> existingCartItemOptional = cartItemRepository.findByCartAndProduct(cart, product);

        if (existingCartItemOptional.isPresent()) {
            CartItem cartItem = existingCartItemOptional.get();
            cartItem.setQuantity(newQuantity);
            return Optional.of(cartItemRepository.save(cartItem));
        } else {
            // カートにアイテムがない場合は追加と見なす
            return addProductToCart(userId, productId, newQuantity);
        }
    }
}