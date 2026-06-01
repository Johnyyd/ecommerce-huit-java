package com.huitshop.dao;

import com.huitshop.config.DbConnection;
import com.huitshop.model.Review;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReviewDao {

    public List<Review> getReviewsByProductId(int productId) {
        List<Review> list = new ArrayList<>();
        String sql = "SELECT r.*, u.full_name, pv.variant_name " +
                     "FROM reviews r " +
                     "JOIN users u ON r.user_id = u.id " +
                     "LEFT JOIN product_variants pv ON r.variant_id = pv.id " +
                     "WHERE r.product_id = ? AND r.is_approved = 1 " +
                     "ORDER BY r.created_at DESC";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Review r = new Review();
                    r.setId(rs.getInt("id"));
                    r.setUserId(rs.getInt("user_id"));
                    r.setProductId(rs.getInt("product_id"));
                    int vId = rs.getInt("variant_id");
                    if (!rs.wasNull()) {
                        r.setVariantId(vId);
                    }
                    r.setRating(rs.getInt("rating"));
                    r.setTitle(rs.getNString("title"));
                    r.setContent(rs.getNString("content"));
                    r.setVerifiedPurchase(rs.getBoolean("is_verified_purchase"));
                    r.setApproved(rs.getBoolean("is_approved"));
                    
                    Timestamp ts = rs.getTimestamp("created_at");
                    if (ts != null) {
                        r.setCreatedAt(ts.toLocalDateTime());
                    }
                    
                    r.setUserName(rs.getNString("full_name"));
                    r.setVariantName(rs.getNString("variant_name"));
                    list.add(r);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void insertReview(Review r) {
        String sql = "INSERT INTO reviews (user_id, product_id, variant_id, rating, title, content, is_verified_purchase, is_approved, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, r.getUserId());
            ps.setInt(2, r.getProductId());
            if (r.getVariantId() != null) {
                ps.setInt(3, r.getVariantId());
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            ps.setInt(4, r.getRating());
            ps.setNString(5, r.getTitle());
            ps.setNString(6, r.getContent());
            ps.setBoolean(7, r.isVerifiedPurchase());
            ps.setBoolean(8, r.isApproved());
            ps.setTimestamp(9, Timestamp.valueOf(LocalDateTime.now()));
            ps.setTimestamp(10, Timestamp.valueOf(LocalDateTime.now()));

            ps.executeUpdate();
            try (ResultSet gk = ps.getGeneratedKeys()) {
                if (gk.next()) {
                    r.setId(gk.getInt(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
