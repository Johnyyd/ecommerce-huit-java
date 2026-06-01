package com.huitshop.service;

import com.huitshop.dao.UserDao;
import com.huitshop.model.Address;
import com.huitshop.model.User;

import java.util.List;

public class UserService {
    private final UserDao userDao = new UserDao();

    public boolean updateProfile(int userId, String fullName, String phone, String newPassword) {
        User user = userDao.findById(userId);
        if (user == null) {
            return false;
        }
        user.setFullName(fullName);
        user.setPhone(phone);
        if (newPassword != null && !newPassword.trim().isEmpty()) {
            user.setPasswordHash(newPassword.trim());
        }
        userDao.update(user);
        return true;
    }

    public List<Address> getAddresses(int userId) {
        return userDao.getAddressesByUserId(userId);
    }

    public boolean addAddress(int userId, Address addr) {
        addr.setUserId(userId);
        if (addr.isDefault()) {
            userDao.clearDefaultAddress(userId);
        }
        return userDao.insertAddress(addr);
    }

    public boolean removeAddress(int addressId, int userId) {
        return userDao.deleteAddress(addressId, userId);
    }

    public boolean setDefaultAddress(int addressId, int userId) {
        userDao.clearDefaultAddress(userId);
        String sql = "UPDATE addresses SET is_default = 1 WHERE id = ? AND user_id = ?";
        try (java.sql.Connection conn = com.huitshop.config.DbConnection.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, addressId);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<User> getUsers(String search, String role, String status) {
        return userDao.getUsers(search, role, status);
    }

    public boolean updateUserRoleAndStatus(int targetUserId, String role, String status) {
        User user = userDao.findById(targetUserId);
        if (user == null) {
            return false;
        }
        user.setRole(role);
        user.setStatus(status);
        userDao.update(user);
        return true;
    }
}
