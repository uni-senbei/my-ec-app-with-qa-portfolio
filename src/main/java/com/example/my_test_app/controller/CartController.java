package com.example.my_test_app.controller;

import com.example.my_test_app.dto.CartDto; // ★追加: CartDtoをインポート
import com.example.my_test_app.dto.CartItemDto; // ★CartItemDtoをインポート
import com.example.my_test_app.service.CartService; // CartServiceは既存
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List; // Listをインポート
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    @Autowired
    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    /**
     * カートに商品を追加するAPI
     * POST /api/cart/add
     * リクエストボディ: { "userId": 1, "productId": 101, "quantity": 1 }
     *
     * @param requestMap userId, productId, quantityを含むマップ
     * @return 追加されたCartItemDtoまたはエラーレスポンス
     */
    @PostMapping("/add")
    public ResponseEntity<Object> addProductToCart(@RequestBody Map<String, Long> requestMap) {
        Long userId = requestMap.get("userId");
        Long productId = requestMap.get("productId");
        Long quantityLong = requestMap.get("quantity");

        if (userId == null || productId == null || quantityLong == null) {
            return new ResponseEntity<>(Collections.singletonMap("message", "Required fields (userId, productId, quantity) are missing."), HttpStatus.BAD_REQUEST);
        }

        int quantity = quantityLong.intValue();
        if (quantity <= 0) {
            return new ResponseEntity<>(Collections.singletonMap("message", "Quantity must be positive."), HttpStatus.BAD_REQUEST);
        }

        try {
            // ★変更: CartServiceからCartItemDtoが返されることを想定
            Optional<CartItemDto> addedItemDto = cartService.addProductToCart(userId, productId, quantity);
            if (addedItemDto.isPresent()) {
                return new ResponseEntity<>(addedItemDto.get(), HttpStatus.OK); // 200 OK
            } else {
                return new ResponseEntity<>(Collections.singletonMap("message", "Failed to add product to cart (unknown reason)."), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(Collections.singletonMap("message", e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(Collections.singletonMap("message", e.getMessage()), HttpStatus.NOT_FOUND);
        }
    }

    /**
     * カートから商品を削除するAPI
     * DELETE /api/cart/remove
     * リクエストボディ: { "userId": 1, "productId": 101 }
     *
     * @param requestMap userId, productIdを含むマップ
     * @return 削除の成否（現状維持でMapを返すが、DTOを返すように変更も可能）
     */
    @DeleteMapping("/remove")
    public ResponseEntity<Map<String, String>> removeProductFromCart(@RequestBody Map<String, Long> requestMap) {
        Long userId = requestMap.get("userId");
        Long productId = requestMap.get("productId");

        if (userId == null || productId == null) {
            return new ResponseEntity<>(Collections.singletonMap("message", "Required fields (userId, productId) are missing."), HttpStatus.BAD_REQUEST);
        }

        try {
            boolean removed = cartService.removeProductFromCart(userId, productId);
            if (removed) {
                return new ResponseEntity<>(Collections.singletonMap("message", "Product removed from cart successfully."), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(Collections.singletonMap("message", "Product not found in cart or cart not found."), HttpStatus.NOT_FOUND);
            }
        } catch (RuntimeException e) {
            return new ResponseEntity<>(Collections.singletonMap("message", e.getMessage()), HttpStatus.NOT_FOUND);
        }
    }

    /**
     * 特定ユーザーのカート内容を取得するAPI
     * GET /api/cart/{userId}
     *
     * @param userId ユーザーID
     * @return CartDtoオブジェクトまたは404
     */
    @GetMapping("/{userId}")
    // ★変更: CartエンティティではなくCartDtoを返すようにする
    public ResponseEntity<CartDto> getCartByUserId(@PathVariable Long userId) {
        // ★CartServiceからCartDtoが返されることを想定
        Optional<CartDto> cartDto = cartService.getCartByUserId(userId);
        return cartDto.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * カート内の商品の数量を更新するAPI
     * PUT /api/cart/updateQuantity
     * リクエストボディ: { "userId": 1, "productId": 101, "newQuantity": 2 }
     *
     * @param requestMap userId, productId, newQuantityを含むマップ
     * @return 更新されたCartItemDtoまたはエラーレスポンス
     */
    @PutMapping("/updateQuantity")
    public ResponseEntity<Object> updateCartItemQuantity(@RequestBody Map<String, Long> requestMap) {
        Long userId = requestMap.get("userId");
        Long productId = requestMap.get("productId");
        Long newQuantityLong = requestMap.get("newQuantity");

        if (userId == null || productId == null || newQuantityLong == null) {
            return new ResponseEntity<>(Collections.singletonMap("message", "Required fields (userId, productId, newQuantity) are missing."), HttpStatus.BAD_REQUEST);
        }

        int newQuantity = newQuantityLong.intValue();

        try {
            // ★変更: CartServiceからCartItemDtoが返されることを想定
            Optional<CartItemDto> updatedItemDto = cartService.updateCartItemQuantity(userId, productId, newQuantity);
            if (updatedItemDto.isPresent()) {
                return new ResponseEntity<>(updatedItemDto.get(), HttpStatus.OK);
            } else {
                // 数量が0になりアイテムが削除された場合など
                return new ResponseEntity<>(Collections.singletonMap("message", "Product quantity updated or removed successfully."), HttpStatus.OK);
            }
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(Collections.singletonMap("message", e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(Collections.singletonMap("message", e.getMessage()), HttpStatus.NOT_FOUND);
        }
    }
}