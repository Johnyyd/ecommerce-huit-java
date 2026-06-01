package com.huitshop.dao;

import com.huitshop.config.DbConnection;
import com.huitshop.model.Voucher;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class VoucherDao {

    public Voucher findByCode(String code) {
        String sql = "SELECT * FROM vouchers WHERE code = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapVoucher(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Voucher> getVouchers() {
        List<Voucher> list = new ArrayList<>();
        String sql = "SELECT * FROM vouchers ORDER BY created_at DESC";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapVoucher(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void insertVoucher(Voucher v) {
        String sql = "INSERT INTO vouchers (code, name, description, discount_type, discount_value, max_discount_amount, min_order_value, start_date, end_date, usage_limit, usage_per_user, usage_count, is_active, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, v.getCode());
            ps.setNString(2, v.getName());
            ps.setNString(3, v.getDescription());
            ps.setString(4, v.getDiscountType());
            ps.setBigDecimal(5, v.getDiscountValue());
            if (v.getMaxDiscountAmount() != null) {
                ps.setBigDecimal(6, v.getMaxDiscountAmount());
            } else {
                ps.setNull(6, Types.DECIMAL);
            }
            ps.setBigDecimal(7, v.getMinOrderValue());
            ps.setTimestamp(8, Timestamp.valueOf(v.getStartDate()));
            ps.setTimestamp(9, Timestamp.valueOf(v.getEndDate()));
            if (v.getUsageLimit() != null) {
                ps.setInt(10, v.getUsageLimit());
            } else {
                ps.setNull(10, Types.INTEGER);
            }
            ps.setInt(11, v.getUsagePerUser());
            ps.setInt(12, v.getUsageCount());
            ps.setBoolean(13, v.isActive());
            ps.setTimestamp(14, Timestamp.valueOf(LocalDateTime.now()));
            ps.setTimestamp(15, Timestamp.valueOf(LocalDateTime.now()));

            ps.executeUpdate();
            try (ResultSet gk = ps.getGeneratedKeys()) {
                if (gk.next()) {
                    v.setId(gk.getInt(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Voucher mapVoucher(ResultSet rs) throws SQLException {
        Voucher v = new Voucher();
        v.setId(rs.getInt("id"));
        v.setCode(rs.getString("code"));
        v.setName(rs.getNString("name"));
        v.setDescription(rs.getNString("description"));
        v.setDiscountType(rs.getString("discount_type"));
        v.setDiscountValue(rs.getBigDecimal("discount_value"));
        v.setMaxDiscountAmount(rs.getBigDecimal("max_discount_amount"));
        v.setMinOrderValue(rs.getBigDecimal("min_order_value"));
        
        Timestamp start = rs.getTimestamp("start_date");
        if (start != null) v.setStartDate(start.toLocalDateTime());
        Timestamp end = rs.getTimestamp("end_date");
        if (end != null) v.setEndDate(end.toLocalDateTime());
        
        int usageLimit = rs.getInt("usage_limit");
        if (!rs.wasNull()) {
            v.setUsageLimit(usageLimit);
        }
        v.setUsagePerUser(rs.getInt("usage_per_user"));
        v.setUsageCount(rs.getInt("usage_count"));
        v.setActive(rs.getBoolean("is_active"));
        return v;
    }
}
