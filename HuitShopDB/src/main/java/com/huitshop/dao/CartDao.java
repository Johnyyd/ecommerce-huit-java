package com.huitshop.dao;

import com.huitshop.config.DbConnection;
import com.huitshop.model.Cart;
import com.huitshop.model.CartItem;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CartDao {

    public Cart getCartByUserId(int userId) {
        String sql = "SELECT * FROM carts WHERE user_id = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Cart cart = new Cart();
                    cart.setId(rs.getInt("id"));
                    cart.setUserId(rs.getInt("user_id"));
                    cart.setVoucherCode(rs.getString("voucher_code"));
                    
                    // Load items
                    cart.setCartItems(getCartItems(cart.getId()));
                    return cart;
                } else {
                    // Create one if it does not exist
                    return createCart(userId);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Cart createCart(int userId) {
        String sql = "INSERT INTO carts (user_id, created_at, updated_at) VALUES (?, ?, ?)";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, userId);
            ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            ps.executeUpdate();
            
            try (ResultSet gk = ps.getGeneratedKeys()) {
                if (gk.next()) {
                    Cart cart = new Cart();
                    cart.setId(gk.getInt(1));
                    cart.setUserId(userId);
                    return cart;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<CartItem> getCartItems(int cartId) {
        List<CartItem> list = new ArrayList<>();
        String sql = "SELECT ci.*, pv.sku, pv.variant_name, pv.price, p.name AS p_name, pv.thumbnail_url " +
                     "FROM cart_items ci " +
                     "JOIN product_variants pv ON ci.variant_id = pv.id " +
                     "JOIN products p ON pv.product_id = p.id " +
                     "WHERE ci.cart_id = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, cartId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CartItem item = new CartItem();
                    item.setId(rs.getInt("id"));
                    item.setCartId(rs.getInt("cart_id"));
                    item.setVariantId(rs.getInt("variant_id"));
                    item.setQuantity(rs.getInt("quantity"));
                    
                    com.huitshop.model.ProductVariant pv = new com.huitshop.model.ProductVariant();
                    pv.setId(item.getVariantId());
                    pv.setSku(rs.getString("sku"));
                    pv.setVariantName(rs.getNString("variant_name"));
                    pv.setPrice(rs.getBigDecimal("price"));
                    pv.setThumbnailUrl(rs.getString("thumbnail_url"));
                    
                    com.huitshop.model.Product p = new com.huitshop.model.Product();
                    p.setName(rs.getNString("p_name"));
                    pv.setProduct(p);
                    
                    item.setProductVariant(pv);
                    list.add(item);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public CartItem getCartItem(int cartId, int variantId) {
        String sql = "SELECT * FROM cart_items WHERE cart_id = ? AND variant_id = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, cartId);
            ps.setInt(2, variantId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    CartItem item = new CartItem();
                    item.setId(rs.getInt("id"));
                    item.setCartId(rs.getInt("cart_id"));
                    item.setVariantId(rs.getInt("variant_id"));
                    item.setQuantity(rs.getInt("quantity"));
                    return item;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void insertCartItem(CartItem item) {
        String sql = "INSERT INTO cart_items (cart_id, variant_id, quantity, added_at, updated_at) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, item.getCartId());
            ps.setInt(2, item.getVariantId());
            ps.setInt(3, item.getQuantity());
            ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            ps.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            ps.executeUpdate();
            
            try (ResultSet gk = ps.getGeneratedKeys()) {
                if (gk.next()) {
                    item.setId(gk.getInt(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateCartItem(CartItem item) {
        String sql = "UPDATE cart_items SET quantity = ?, updated_at = ? WHERE id = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, item.getQuantity());
            ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(3, item.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteCartItem(int id) {
        String sql = "DELETE FROM cart_items WHERE id = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteCartItems(int cartId) {
        String sql = "DELETE FROM cart_items WHERE cart_id = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, cartId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateCartVoucher(int cartId, String voucherCode) {
        String sql = "UPDATE carts SET voucher_code = ?, updated_at = ? WHERE id = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (voucherCode != null) {
                ps.setString(1, voucherCode);
            } else {
                ps.setNull(1, Types.VARCHAR);
            }
            ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(3, cartId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
