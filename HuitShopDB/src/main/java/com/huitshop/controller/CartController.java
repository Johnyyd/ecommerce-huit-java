package com.huitshop.controller;

import com.huitshop.model.Cart;
import com.huitshop.service.CartService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartController {
    private final CartService cartService = new CartService();

    @GetMapping("/{userId}")
    public ResponseEntity<Cart> getCart(@PathVariable int userId) {
        return ResponseEntity.ok(cartService.getCartByUserId(userId));
    }

    @PostMapping("/{userId}/items")
    public ResponseEntity<?> addItem(@PathVariable int userId, @RequestBody Map<String, Integer> request) {
        Integer variantId = request.get("variantId");
        Integer quantity = request.get("quantity");
        if (variantId == null || quantity == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Thiếu variantId hoặc quantity");
        }
        try {
            cartService.addItemToCart(userId, variantId, quantity);
            return ResponseEntity.ok("Đã thêm sản phẩm vào giỏ hàng");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PutMapping("/{userId}/items/{cartItemId}")
    public ResponseEntity<?> updateItemQuantity(
            @PathVariable int userId,
            @PathVariable int cartItemId,
            @RequestBody Map<String, Integer> request
    ) {
        Integer quantity = request.get("quantity");
        if (quantity == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Thiếu quantity");
        }
        try {
            cartService.updateItemQuantity(userId, cartItemId, quantity);
            return ResponseEntity.ok("Cập nhật số lượng thành công");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/{userId}/items/{cartItemId}")
    public ResponseEntity<?> removeItem(@PathVariable int userId, @PathVariable int cartItemId) {
        try {
            cartService.removeItemFromCart(userId, cartItemId);
            return ResponseEntity.ok("Xóa sản phẩm khỏi giỏ hàng thành công");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/{userId}/voucher")
    public ResponseEntity<?> applyVoucher(@PathVariable int userId, @RequestBody Map<String, String> request) {
        String code = request.get("voucherCode");
        try {
            cartService.applyVoucher(userId, code);
            return ResponseEntity.ok("Áp dụng mã giảm giá thành công");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<?> clearCart(@PathVariable int userId) {
        try {
            cartService.clearCart(userId);
            return ResponseEntity.ok("Làm sạch giỏ hàng thành công");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
