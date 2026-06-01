package com.huitshop.dao;

import com.huitshop.config.DbConnection;
import com.huitshop.model.Order;
import com.huitshop.model.OrderItem;
import com.huitshop.model.OrderStatusHistory;
import com.huitshop.model.StockMovement;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrderDao {

    public void insertOrder(Order o) {
        String sql = "INSERT INTO orders (code, user_id, order_type, subtotal, discount, shipping_fee, tax_amount, total, payment_method, payment_status, status, shipping_address, note, staff_note, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, o.getCode());
            ps.setInt(2, o.getUserId());
            ps.setString(3, o.getOrderType() != null ? o.getOrderType() : "ONLINE");
            ps.setBigDecimal(4, o.getSubtotal());
            ps.setBigDecimal(5, o.getDiscount());
            ps.setBigDecimal(6, o.getShippingFee());
            ps.setBigDecimal(7, o.getTaxAmount());
            ps.setBigDecimal(8, o.getTotal());
            ps.setString(9, o.getPaymentMethod());
            ps.setString(10, o.getPaymentStatus());
            ps.setString(11, o.getStatus());
            ps.setString(12, o.getShippingAddress());
            ps.setNString(13, o.getNote());
            ps.setNString(14, o.getStaffNote());
            ps.setTimestamp(15, Timestamp.valueOf(LocalDateTime.now()));
            ps.setTimestamp(16, Timestamp.valueOf(LocalDateTime.now()));

            ps.executeUpdate();
            try (ResultSet gk = ps.getGeneratedKeys()) {
                if (gk.next()) {
                    o.setId(gk.getInt(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertOrderItem(OrderItem item) {
        String sql = "INSERT INTO order_items (order_id, variant_id, product_name, sku, quantity, unit_price, cost_price, total_price, discount_amount, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, item.getOrderId());
            ps.setInt(2, item.getVariantId());
            ps.setNString(3, item.getProductName());
            ps.setString(4, item.getSku());
            ps.setInt(5, item.getQuantity());
            ps.setBigDecimal(6, item.getUnitPrice());
            if (item.getCostPrice() != null) {
                ps.setBigDecimal(7, item.getCostPrice());
            } else {
                ps.setNull(7, Types.DECIMAL);
            }
            ps.setBigDecimal(8, item.getTotalPrice());
            ps.setBigDecimal(9, item.getDiscountAmount());
            ps.setTimestamp(10, Timestamp.valueOf(LocalDateTime.now()));
            ps.setTimestamp(11, Timestamp.valueOf(LocalDateTime.now()));

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

    public void insertOrderStatusHistory(int orderId, String status, String note, Integer changedBy) {
        String sql = "INSERT INTO order_status_history (order_id, status, note, changed_by, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ps.setString(2, status);
            ps.setNString(3, note);
            if (changedBy != null) {
                ps.setInt(4, changedBy);
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            ps.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            ps.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateOrder(Order o) {
        String sql = "UPDATE orders SET status = ?, payment_status = ?, staff_note = ?, updated_at = ? WHERE id = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, o.getStatus());
            ps.setString(2, o.getPaymentStatus());
            ps.setNString(3, o.getStaffNote());
            ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(5, o.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateInventoryReserved(int warehouseId, int variantId, int qtyChange) {
        String sql = "UPDATE inventories SET quantity_reserved = quantity_reserved + ?, last_updated = ? WHERE warehouse_id = ? AND variant_id = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, qtyChange);
            ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(3, warehouseId);
            ps.setInt(4, variantId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateInventoryOnHand(int warehouseId, int variantId, int qtyChange) {
        String sql = "UPDATE inventories SET quantity_on_hand = quantity_on_hand + ?, last_updated = ? WHERE warehouse_id = ? AND variant_id = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, qtyChange);
            ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(3, warehouseId);
            ps.setInt(4, variantId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertStockMovement(StockMovement sm) {
        String sql = "INSERT INTO stock_movements (warehouse_id, variant_id, quantity, movement_type, reference_id, reference_type, supplier_id, note, created_by, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sm.getWarehouseId());
            ps.setInt(2, sm.getVariantId());
            ps.setInt(3, sm.getQuantity());
            ps.setString(4, sm.getMovementType());
            if (sm.getReferenceId() != null) {
                ps.setInt(5, sm.getReferenceId());
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            ps.setString(6, sm.getReferenceType());
            if (sm.getSupplierId() != null) {
                ps.setInt(7, sm.getSupplierId());
            } else {
                ps.setNull(7, Types.INTEGER);
            }
            ps.setNString(8, sm.getNote());
            if (sm.getCreatedBy() != null) {
                ps.setInt(9, sm.getCreatedBy());
            } else {
                ps.setNull(9, Types.INTEGER);
            }
            ps.setTimestamp(10, Timestamp.valueOf(LocalDateTime.now()));
            ps.setTimestamp(11, Timestamp.valueOf(LocalDateTime.now()));

            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertVoucherUsage(int voucherId, int userId, int orderId, BigDecimal discountAmount) {
        String sql = "INSERT INTO voucher_usages (voucher_id, user_id, order_id, discount_amount, used_at, updated_at) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, voucherId);
            ps.setInt(2, userId);
            ps.setInt(3, orderId);
            ps.setBigDecimal(4, discountAmount);
            ps.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            ps.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void incrementVoucherUsageCount(int voucherId) {
        String sql = "UPDATE vouchers SET usage_count = usage_count + 1, updated_at = ? WHERE id = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(2, voucherId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertOrderItemSerial(int orderItemId, String serialNumber) {
        String sql = "INSERT INTO order_item_serials (order_item_id, serial_number) VALUES (?, ?)";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderItemId);
            ps.setString(2, serialNumber);
            ps.executeUpdate();
            
            // Also update the serial status in warehouse
            String sqlSerial = "UPDATE product_serials SET status = 'SOLD', outbound_date = ? WHERE serial_number = ?";
            try (PreparedStatement ps2 = conn.prepareStatement(sqlSerial)) {
                ps2.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                ps2.setString(2, serialNumber);
                ps2.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Order getOrderById(int orderId) {
        String sql = "SELECT o.*, u.full_name, u.email FROM orders o JOIN users u ON o.user_id = u.id WHERE o.id = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapOrder(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Order getOrderByCode(String orderCode) {
        String sql = "SELECT o.*, u.full_name, u.email FROM orders o JOIN users u ON o.user_id = u.id WHERE o.code = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, orderCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapOrder(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Order> getOrdersByUserId(int userId, int page, int pageSize) {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT o.*, u.full_name, u.email FROM orders o JOIN users u ON o.user_id = u.id " +
                     "WHERE o.user_id = ? ORDER BY o.created_at DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, (page - 1) * pageSize);
            ps.setInt(3, pageSize);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapOrder(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Order> getAllOrders(String status, String keyword, int page, int pageSize) {
        List<Order> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT o.*, u.full_name, u.email FROM orders o JOIN users u ON o.user_id = u.id WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (status != null && !"ALL".equalsIgnoreCase(status)) {
            sql.append(" AND o.status = ?");
            params.add(status);
        }

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (o.code LIKE ? OR u.full_name LIKE ?)");
            String pattern = "%" + keyword.trim() + "%";
            params.add(pattern);
            params.add(pattern);
        }

        sql.append(" ORDER BY o.created_at DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
        params.add((page - 1) * pageSize);
        params.add(pageSize);

        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapOrder(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public int getAllOrdersCount(String status, String keyword) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(1) FROM orders o JOIN users u ON o.user_id = u.id WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (status != null && !"ALL".equalsIgnoreCase(status)) {
            sql.append(" AND o.status = ?");
            params.add(status);
        }

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (o.code LIKE ? OR u.full_name LIKE ?)");
            String pattern = "%" + keyword.trim() + "%";
            params.add(pattern);
            params.add(pattern);
        }

        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<OrderItem> getOrderItems(int orderId) {
        List<OrderItem> list = new ArrayList<>();
        String sql = "SELECT oi.*, v.thumbnail_url FROM order_items oi " +
                     "LEFT JOIN product_variants v ON oi.variant_id = v.id " +
                     "WHERE oi.order_id = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderItem item = new OrderItem();
                    item.setId(rs.getInt("id"));
                    item.setOrderId(rs.getInt("order_id"));
                    item.setVariantId(rs.getInt("variant_id"));
                    item.setProductName(rs.getNString("product_name"));
                    item.setSku(rs.getString("sku"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setUnitPrice(rs.getBigDecimal("unit_price"));
                    item.setTotalPrice(rs.getBigDecimal("total_price"));
                    item.setDiscountAmount(rs.getBigDecimal("discount_amount"));
                    
                    com.huitshop.model.ProductVariant pv = new com.huitshop.model.ProductVariant();
                    pv.setId(item.getVariantId());
                    pv.setThumbnailUrl(rs.getString("thumbnail_url"));
                    item.setProductVariant(pv);
                    
                    // Fetch serials
                    item.setSerialNumbers(getOrderItemSerials(conn, item.getId()));

                    list.add(item);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private List<String> getOrderItemSerials(Connection conn, int orderItemId) throws SQLException {
        List<String> list = new ArrayList<>();
        String sql = "SELECT serial_number FROM order_item_serials WHERE order_item_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderItemId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(rs.getString("serial_number"));
                }
            }
        }
        return list;
    }

    public List<OrderStatusHistory> getOrderStatusHistory(int orderId) {
        List<OrderStatusHistory> list = new ArrayList<>();
        String sql = "SELECT * FROM order_status_history WHERE order_id = ? ORDER BY created_at ASC";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderStatusHistory h = new OrderStatusHistory();
                    h.setId(rs.getInt("id"));
                    h.setOrderId(rs.getInt("order_id"));
                    h.setStatus(rs.getString("status"));
                    h.setNote(rs.getNString("note"));
                    
                    Timestamp ts = rs.getTimestamp("created_at");
                    if (ts != null) {
                        h.setCreatedAt(ts.toLocalDateTime());
                    }
                    
                    list.add(h);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private Order mapOrder(ResultSet rs) throws SQLException {
        Order o = new Order();
        o.setId(rs.getInt("id"));
        o.setCode(rs.getString("code"));
        o.setUserId(rs.getInt("user_id"));
        o.setOrderType(rs.getString("order_type"));
        o.setSubtotal(rs.getBigDecimal("subtotal"));
        o.setDiscount(rs.getBigDecimal("discount"));
        o.setShippingFee(rs.getBigDecimal("shipping_fee"));
        o.setTaxAmount(rs.getBigDecimal("tax_amount"));
        o.setTotal(rs.getBigDecimal("total"));
        o.setPaymentMethod(rs.getString("payment_method"));
        o.setPaymentStatus(rs.getString("payment_status"));
        o.setStatus(rs.getString("status"));
        o.setShippingAddress(rs.getNString("shipping_address"));
        o.setNote(rs.getNString("note"));
        o.setStaffNote(rs.getNString("staff_note"));

        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            o.setCreatedAt(ts.toLocalDateTime());
        }

        // Map user details
        com.huitshop.model.User u = new com.huitshop.model.User();
        u.setId(o.getUserId());
        u.setFullName(rs.getNString("full_name"));
        u.setEmail(rs.getString("email"));
        o.setUser(u);

        return o;
    }
}
