package com.example.my_test_app.service;

import com.example.my_test_app.model.Cart;
import com.example.my_test_app.model.CartItem;
import com.example.my_test_app.model.User;
import com.example.my_test_app.model.Product;
import com.example.my_test_app.repository.CartRepository;
import com.example.my_test_app.repository.CartItemRepository;
import com.example.my_test_app.repository.UserRepository;
import com.example.my_test_app.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional; // Optionalをインポート
import java.util.Set; // Setをインポート (getCartItems().size()を使うため)

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

    // ========== カートに商品を追加するロジック ==========
    @Transactional
    public Optional<CartItem> addProductToCart(Long userId, Long productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // ユーザーに紐づくカートを取得または作成
        Cart cart = cartRepository.findByUser(user) // CartRepositoryのfindByUserを使用
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        // ★修正: CartItemRepositoryのメソッド名を findByCartAndProduct から findByCartIdAndProductId に変更
        Optional<CartItem> existingCartItemOptional = cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId());

        CartItem cartItem;
        if (existingCartItemOptional.isPresent()) {
            // 既存のアイテムがある場合、数量を更新
            cartItem = existingCartItemOptional.get();
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
            cartItem = cartItemRepository.save(cartItem);
        } else {
            // 新規アイテムの場合、種類数の最終チェック
            // ここで getCartItems() を呼び出すとLAZYフェッチされるため、
            // カート取得時に findByUserWithCartItems を使っていない場合はN+1問題が発生する可能性あり
            // ただし、このメソッドはaddProductToCartなので、新しいCartItemを作成し、
            // それをcart.getCartItems().add(cartItem)で追加することで、
            // 次回同じCartがフェッチされたときにコレクションに含まれるようになる。
            // ここでのgetCartItems().size()は、現在のトランザクションでロードされたコレクションの状態を見る。
            // 常に最新の状態を保証するには、CartRepository.findByUserWithCartItems(user) でCartを取得すべき。
            // しかし、addProductToCartでは常にcartRepository.findByUser(user)を使っているため、
            // その時点ではcartItemsコレクションは初期化されていない（空か、以前のデータ）可能性がある。
            // 最も確実なのは、以下のようにgetCartItems().size()の前に明示的にロードするか、
            // このチェックロジックをより慎重に設計すること。
            // 例: cartItemRepository.countByCartId(cart.getId()) のようなメソッドをRepositoryに追加する。
            // 今回は、現在のCartエンティティのgetCartItems()が動作すると仮定します。

            // Cartエンティティの cartItems コレクションをロードしてサイズを取得
            // このadd/removeProductToCartにおいてはcartRepository.findByUser(user)を使用しているため
            // Cartエンティティの cartItems はLAZYロードされており、初回アクセス時にDBからロードされる。
            // そのため、ここで .size() を呼ぶとコレクションがロードされる。
            // ただし、既に存在するCartItemが追加された場合、そのCartItemはコレクションに含まれない場合がある。
            // ここでのチェックは「現在DBに登録されている種類数」を正確に反映しない可能性があるため注意が必要。
            // より確実なのは、countDistinctProductsInCart(Long cartId)のようなRepositoryメソッドを使うこと。
            // シンプル化のため、ここではcart.getCartItems().size()を使用しますが、実運用では要検討。

            // カートアイテムの種類数を正確に数えるため、Repositoryに countDistinctProductsInCart を追加するのが理想
            // 例： Long currentDistinctItems = cartItemRepository.countDistinctProductByCartId(cart.getId());
            // 今は既存のCartエンティティのコレクションでチェックする

            // カートアイテムをロードして、そのサイズをチェック
            // 以下の行を削除し、より正確なチェック方法を採用することも可能
            Set<CartItem> currentCartItems = cart.getCartItems(); // LAZYロードされる可能性あり
            if (currentCartItems.size() >= 20) {
                // 新規商品を追加しようとして、種類数が20を超過する場合
                throw new IllegalStateException("カートに追加できる商品の種類は20個までです。");
            }

            // 新しいアイテムとして追加
            cartItem = new CartItem(cart, product, quantity);
            // ★修正: cart.addCartItem(cartItem); を削除し、直接コレクションに追加
            cart.getCartItems().add(cartItem); // Cartエンティティのコレクションに直接追加

            cartItem = cartItemRepository.save(cartItem);
        }

        // CartItemの追加/更新によりCartエンティティの内部状態（cartItemsコレクション）が変更された可能性があるため保存
        // Cartエンティティの @OneToMany の cascade = CascadeType.ALL があれば、CartItemの保存でCartも更新されることが多いが、
        // コレクションへの追加の場合は明示的に親を保存した方が確実。
        cartRepository.save(cart);

        return Optional.of(cartItem);
    }

    // ========== カートから商品を削除するロジック ==========
    @Transactional
    public boolean removeProductFromCart(Long userId, Long productId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        Cart cart = cartRepository.findByUser(user) // CartRepositoryのfindByUserを使用
                .orElseThrow(() -> new RuntimeException("Cart not found for user: " + userId));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        // ★修正: CartItemRepositoryのメソッド名を findByCartAndProduct から findByCartIdAndProductId に変更
        Optional<CartItem> existingCartItemOptional = cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId());

        if (existingCartItemOptional.isPresent()) {
            CartItem cartItem = existingCartItemOptional.get();
            // ★修正: cart.removeCartItem(cartItem); を削除し、直接コレクションから削除
            cart.getCartItems().remove(cartItem); // Cartエンティティのコレクションから直接削除

            cartItemRepository.delete(cartItem); // CartItemを削除

            // 親エンティティのコレクションが変更されたので、親エンティティを保存しDBに同期
            // cart.getCartItems().remove() は @OneToMany の orphanRemoval=true が設定されていれば、
            // save(cart) を呼ぶことで関連する cartItem が削除されるはずだが、念のため明示的な delete を残す。
            cartRepository.save(cart); // カートを保存（コレクション変更をDBに同期）
            return true;
        }
        return false; // カートに該当商品がない場合
    }

    // ========== カートの内容を取得するロジック ==========
    @Transactional(readOnly = true)
    public Optional<Cart> getCartByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // CartRepositoryの findByUserWithCartItems メソッドを使用
        return cartRepository.findByUserWithCartItems(user);
    }

    // ========== カート内のアイテム数量を更新するロジック ==========
    @Transactional
    public Optional<CartItem> updateCartItemQuantity(Long userId, Long productId, int newQuantity) {
        if (newQuantity < 0) { // 数量が負の場合はエラー
            throw new IllegalArgumentException("Quantity cannot be negative.");
        }

        if (newQuantity == 0) {
            // 数量が0の場合は削除と見なす
            boolean removed = removeProductFromCart(userId, productId);
            return removed ? Optional.empty() : Optional.ofNullable(null); // 削除成功ならOptional.empty()
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        Cart cart = cartRepository.findByUser(user) // CartRepositoryのfindByUserを使用
                .orElseThrow(() -> new RuntimeException("Cart not found for user: " + userId));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        // ★修正: CartItemRepositoryのメソッド名を findByCartAndProduct から findByCartIdAndProductId に変更
        Optional<CartItem> existingCartItemOptional = cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId());

        if (existingCartItemOptional.isPresent()) {
            CartItem cartItem = existingCartItemOptional.get();
            cartItem.setQuantity(newQuantity);
            return Optional.of(cartItemRepository.save(cartItem));
        } else {
            // カートにアイテムがない場合は新規追加と見なす（数量 > 0 の場合のみ）
            return addProductToCart(userId, productId, newQuantity);
        }
    }

    // ========== カートをクリアするロジック（追加） ==========
    @Transactional
    public void clearCart(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Cart not found for user: " + userId));

        // カートアイテムを全て削除
        // cart.getCartItems().clear() を実行し、その後 cartRepository.save(cart) を呼ぶことで
        // orphanRemoval=true の設定により関連するCartItemが削除される。
        // ただし、明示的に削除することで意図が明確になる。
        cartItemRepository.deleteAll(cart.getCartItems()); // カートに紐づく全てのアイテムを削除

        // Cartエンティティのコレクションもクリア
        cart.getCartItems().clear();

        // コレクション変更をDBに同期するためにCartを保存
        cartRepository.save(cart);
    }
}