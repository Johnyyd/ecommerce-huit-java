package com.huitshop.dao;

import com.huitshop.config.DbConnection;
import com.huitshop.model.User;
import com.huitshop.model.Address;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UserDao {

    public User findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public User findById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean existsEmail(String email) {
        String sql = "SELECT 1 FROM users WHERE email = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean existsPhone(String phone) {
        String sql = "SELECT 1 FROM users WHERE phone = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, phone);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void insert(User user) {
        String sql = "INSERT INTO users (full_name, email, phone, password_hash, role, status, avatar_url, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getFullName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPhone());
            ps.setString(4, user.getPasswordHash());
            ps.setString(5, user.getRole());
            ps.setString(6, user.getStatus());
            ps.setString(7, user.getAvatarUrl());
            ps.setTimestamp(8, Timestamp.valueOf(LocalDateTime.now()));
            ps.setTimestamp(9, Timestamp.valueOf(LocalDateTime.now()));
            
            ps.executeUpdate();
            
            try (ResultSet gk = ps.getGeneratedKeys()) {
                if (gk.next()) {
                    user.setId(gk.getInt(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void update(User user) {
        String sql = "UPDATE users SET full_name = ?, phone = ?, password_hash = ?, role = ?, status = ?, avatar_url = ?, last_login = ?, updated_at = ? WHERE id = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getFullName());
            ps.setString(2, user.getPhone());
            ps.setString(3, user.getPasswordHash());
            ps.setString(4, user.getRole());
            ps.setString(5, user.getStatus());
            ps.setString(6, user.getAvatarUrl());
            ps.setTimestamp(7, user.getLastLogin() != null ? Timestamp.valueOf(user.getLastLogin()) : null);
            ps.setTimestamp(8, Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(9, user.getId());
            
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<User> getUsers(String search, String role, String status) {
        List<User> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM users WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (search != null && !search.trim().isEmpty()) {
            sql.append(" AND (full_name LIKE ? OR email LIKE ?)");
            String p = "%" + search.trim() + "%";
            params.add(p);
            params.add(p);
        }
        if (role != null && !"ALL".equals(role) && !role.trim().isEmpty()) {
            sql.append(" AND role = ?");
            params.add(role.trim());
        }
        if (status != null && !"ALL".equals(status) && !status.trim().isEmpty()) {
            sql.append(" AND status = ?");
            params.add(status.trim());
        }
        sql.append(" ORDER BY created_at DESC");

        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapUser(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Address> getAddressesByUserId(int userId) {
        List<Address> list = new ArrayList<>();
        String sql = "SELECT * FROM addresses WHERE user_id = ? ORDER BY is_default DESC, created_at DESC";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Address addr = new Address();
                    addr.setId(rs.getInt("id"));
                    addr.setUserId(rs.getInt("user_id"));
                    addr.setLabel(rs.getNString("label"));
                    addr.setReceiverName(rs.getNString("receiver_name"));
                    addr.setReceiverPhone(rs.getString("receiver_phone"));
                    addr.setProvince(rs.getNString("province"));
                    addr.setDistrict(rs.getNString("district"));
                    addr.setWard(rs.getNString("ward"));
                    addr.setStreetAddress(rs.getNString("street_address"));
                    addr.setDefault(rs.getBoolean("is_default"));
                    
                    Timestamp ts = rs.getTimestamp("created_at");
                    if (ts != null) addr.setCreatedAt(ts.toLocalDateTime());
                    Timestamp ts2 = rs.getTimestamp("updated_at");
                    if (ts2 != null) addr.setUpdatedAt(ts2.toLocalDateTime());
                    list.add(addr);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean insertAddress(Address addr) {
        String sql = "INSERT INTO addresses (user_id, label, receiver_name, receiver_phone, province, district, ward, street_address, is_default, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, addr.getUserId());
            ps.setNString(2, addr.getLabel());
            ps.setNString(3, addr.getReceiverName());
            ps.setString(4, addr.getReceiverPhone());
            ps.setNString(5, addr.getProvince());
            ps.setNString(6, addr.getDistrict());
            ps.setNString(7, addr.getWard());
            ps.setNString(8, addr.getStreetAddress());
            ps.setBoolean(9, addr.isDefault());
            ps.setTimestamp(10, Timestamp.valueOf(LocalDateTime.now()));
            ps.setTimestamp(11, Timestamp.valueOf(LocalDateTime.now()));
            ps.executeUpdate();
            try (ResultSet gk = ps.getGeneratedKeys()) {
                if (gk.next()) {
                    addr.setId(gk.getInt(1));
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteAddress(int addressId, int userId) {
        String sql = "DELETE FROM addresses WHERE id = ? AND user_id = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, addressId);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void clearDefaultAddress(int userId) {
        String sql = "UPDATE addresses SET is_default = 0 WHERE user_id = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private User mapUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setFullName(rs.getNString("full_name"));
        u.setEmail(rs.getString("email"));
        u.setPhone(rs.getString("phone"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setRole(rs.getString("role"));
        u.setStatus(rs.getString("status"));
        u.setAvatarUrl(rs.getString("avatar_url"));
        
        Timestamp lastLoginTs = rs.getTimestamp("last_login");
        if (lastLoginTs != null) {
            u.setLastLogin(lastLoginTs.toLocalDateTime());
        }
        
        Timestamp createdAtTs = rs.getTimestamp("created_at");
        if (createdAtTs != null) {
            u.setCreatedAt(createdAtTs.toLocalDateTime());
        }
        
        Timestamp updatedAtTs = rs.getTimestamp("updated_at");
        if (updatedAtTs != null) {
            u.setUpdatedAt(updatedAtTs.toLocalDateTime());
        }
        
        return u;
    }
}
