package com.example.my_test_app.controller;

import com.example.my_test_app.model.Order;
import com.example.my_test_app.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/orders") // すべての注文関連APIのベースパス
public class OrderController {

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * カート内容に基づいて新しい注文を作成するAPI
     * POST /api/orders/{userId}/checkout
     * リクエストボディ: { "shippingAddress": "東京都新宿区..." }
     *
     * @param userId 注文を行うユーザーID
     * @param requestBody 配送先住所を含むマップ
     * @return 作成された注文情報またはエラーレスポンス
     */
    @PostMapping("/{userId}/checkout")
    public ResponseEntity<Object> checkoutCart(
            @PathVariable Long userId,
            @RequestBody Map<String, String> requestBody) {

        String shippingAddress = requestBody.get("shippingAddress");

        if (shippingAddress == null || shippingAddress.isEmpty()) {
            return new ResponseEntity<>(Collections.singletonMap("message", "Shipping address is required."), HttpStatus.BAD_REQUEST);
        }

        try {
            Order order = orderService.createOrderFromCart(userId, shippingAddress);
            return new ResponseEntity<>(order, HttpStatus.CREATED); // 201 Created
        } catch (IllegalStateException e) {
            // カートが見つからない、またはカートが空の場合
            return new ResponseEntity<>(Collections.singletonMap("message", e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e) {
            // ユーザーが見つからない、商品が見つからないなどのサービス層からのエラー
            return new ResponseEntity<>(Collections.singletonMap("message", "Order creation failed: " + e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 特定のユーザーの注文履歴を取得するAPI
     * GET /api/orders/user/{userId}
     *
     * @param userId ユーザーID
     * @return ユーザーの注文履歴のリスト
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Order>> getOrdersForUser(@PathVariable Long userId) {
        try {
            List<Order> orders = orderService.getOrdersByUserId(userId);
            if (orders.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND); // 注文が見つからない場合
            }
            return new ResponseEntity<>(orders, HttpStatus.OK);
        } catch (RuntimeException e) {
            // ユーザーが見つからないなどのサービス層からのエラー
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 特定の注文の詳細を取得するAPI
     * GET /api/orders/{orderId}
     *
     * @param orderId 注文ID
     * @return 注文詳細
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrderDetails(@PathVariable Long orderId) {
        Optional<Order> order = orderService.getOrderById(orderId);
        return order.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}