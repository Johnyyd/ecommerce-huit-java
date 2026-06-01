package com.huitshop.service;

import com.huitshop.dao.CartDao;
import com.huitshop.dao.VoucherDao;
import com.huitshop.model.Cart;
import com.huitshop.model.CartItem;
import com.huitshop.model.Voucher;

import java.time.LocalDateTime;

public class CartService {
    private final CartDao cartDao = new CartDao();
    private final VoucherDao voucherDao = new VoucherDao();

    public Cart getCartByUserId(int userId) {
        return cartDao.getCartByUserId(userId);
    }

    public void addItemToCart(int userId, int variantId, int quantity) {
        Cart cart = cartDao.getCartByUserId(userId);
        CartItem existing = cartDao.getCartItem(cart.getId(), variantId);
        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + quantity);
            cartDao.updateCartItem(existing);
        } else {
            CartItem item = new CartItem();
            item.setCartId(cart.getId());
            item.setVariantId(variantId);
            item.setQuantity(quantity);
            cartDao.insertCartItem(item);
        }
    }

    public void updateItemQuantity(int userId, int cartItemId, int quantity) {
        if (quantity <= 0) {
            cartDao.deleteCartItem(cartItemId);
        } else {
            CartItem item = new CartItem();
            item.setId(cartItemId);
            item.setQuantity(quantity);
            cartDao.updateCartItem(item);
        }
    }

    public void removeItemFromCart(int userId, int cartItemId) {
        cartDao.deleteCartItem(cartItemId);
    }

    public void applyVoucher(int userId, String voucherCode) {
        Cart cart = cartDao.getCartByUserId(userId);
        if (voucherCode == null || voucherCode.trim().isEmpty()) {
            cartDao.updateCartVoucher(cart.getId(), null);
            return;
        }

        Voucher v = voucherDao.findByCode(voucherCode.trim());
        if (v == null || !v.isActive()) {
            throw new IllegalArgumentException("Mã giảm giá không hợp lệ hoặc đã bị khóa");
        }

        LocalDateTime now = LocalDateTime.now();
        if (v.getStartDate().isAfter(now) || v.getEndDate().isBefore(now)) {
            throw new IllegalArgumentException("Mã giảm giá đã hết hạn hoặc chưa đến thời gian sử dụng");
        }

        if (v.getUsageLimit() != null && v.getUsageCount() >= v.getUsageLimit()) {
            throw new IllegalArgumentException("Mã giảm giá đã hết lượt sử dụng");
        }

        cartDao.updateCartVoucher(cart.getId(), v.getCode());
    }

    public void clearCart(int userId) {
        Cart cart = cartDao.getCartByUserId(userId);
        cartDao.deleteCartItems(cart.getId());
        cartDao.updateCartVoucher(cart.getId(), null);
    }
}
