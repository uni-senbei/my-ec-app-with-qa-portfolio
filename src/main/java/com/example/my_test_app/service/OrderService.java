package com.example.my_test_app.service;

import com.example.my_test_app.model.*; // Order, OrderItem, User, Cart, CartItem, Product をインポート
import com.example.my_test_app.repository.*; // OrderRepository, OrderItemRepository, UserRepository, ProductRepository, CartRepository をインポート
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // トランザクション管理用
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository; // CartServiceからCartRepositoryを直接使用
    private final UserRepository userRepository; // Userを取得するため
    private final ProductRepository productRepository; // Productを取得するため

    @Autowired
    public OrderService(OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        CartRepository cartRepository,
                        UserRepository userRepository,
                        ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    /**
     * カート内容に基づいて新しい注文を作成します。
     *
     * @param userId 注文を行うユーザーのID
     * @param shippingAddress 配送先住所
     * @return 作成された注文 (Orderエンティティ)
     * @throws IllegalStateException カートが見つからない、またはカートが空の場合
     * @throws RuntimeException その他のエラー（例: 商品が見つからないなど）
     */
    @Transactional // このメソッド全体をトランザクション管理下に置く
    public Order createOrderFromCart(Long userId, String shippingAddress) {
        // 1. ユーザーを取得
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        // 2. ユーザーのカートを取得
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new IllegalStateException("Cart not found for user ID: " + userId));

        // 3. カートが空でないことを確認
        if (cart.getCartItems().isEmpty()) {
            throw new IllegalStateException("Cannot create an order from an empty cart.");
        }

        // 4. Orderエンティティを作成
        Order order = new Order();
        order.setUser(user);
        order.setShippingAddress(shippingAddress);
        order.setPaymentStatus("PENDING"); // 仮に保留中に設定
        order.setOrderStatus("PROCESSING"); // 仮に処理中に設定

        BigDecimal totalAmount = BigDecimal.ZERO;
        Set<OrderItem> orderItems = new HashSet<>();

        // 5. CartItemからOrderItemを作成
        for (CartItem cartItem : cart.getCartItems()) {
            // 商品情報を取得（注文時点のスナップショットとして保持するため）
            Product product = productRepository.findById(cartItem.getProduct().getId())
                    .orElseThrow(() -> new RuntimeException("Product not found with ID: " + cartItem.getProduct().getId()));

            OrderItem orderItem = new OrderItem(
                    product.getId(),
                    product.getName(),
                    product.getPrice(), // 注文時の価格をスナップショット
                    cartItem.getQuantity()
            );
            order.addOrderItem(orderItem); // OrderエンティティにOrderItemを追加
            orderItems.add(orderItem); // OrderItemのセットにも追加

            // 合計金額を計算
            totalAmount = totalAmount.add(product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        }

        order.setTotalAmount(totalAmount);

        // 6. OrderとOrderItemを保存
        Order savedOrder = orderRepository.save(order); // Orderを保存するとOrderItemもcascadeで保存される

        // 7. カートをクリア
        cartRepository.delete(cart);

        return savedOrder;
    }

    /**
     * 特定のユーザーの注文履歴を取得します。
     * @param userId ユーザーID
     * @return ユーザーの注文リスト
     */
    @Transactional(readOnly = true) // 読み取り専用トランザクション
    public List<Order> getOrdersByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        return orderRepository.findByUser(user);
    }

    /**
     * 特定の注文の詳細を取得します。
     * @param orderId 注文ID
     * @return 注文 (Orderエンティティ)
     */
    @Transactional(readOnly = true)
    public Optional<Order> getOrderById(Long orderId) {
        return orderRepository.findById(orderId);
    }
}