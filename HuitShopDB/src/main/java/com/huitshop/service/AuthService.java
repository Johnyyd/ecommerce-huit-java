package com.huitshop.service;

import com.huitshop.dao.UserDao;
import com.huitshop.dto.AuthDtos.*;
import com.huitshop.model.User;

import java.time.LocalDateTime;
import java.util.UUID;

public class AuthService {
    private final UserDao userDao = new UserDao();

    public AuthResponseDto login(LoginDto loginDto) {
        User user = userDao.findByEmail(loginDto.getEmail());
        if (user == null || !"ACTIVE".equals(user.getStatus())) {
            return null;
        }

        // Plain text check (as configured in dev C# project)
        if (!user.getPasswordHash().equals(loginDto.getPassword())) {
            return null;
        }

        user.setLastLogin(LocalDateTime.now());
        userDao.update(user);

        AuthResponseDto resp = new AuthResponseDto();
        resp.setId(user.getId());
        resp.setEmail(user.getEmail());
        resp.setFullName(user.getFullName());
        resp.setRole(user.getRole());
        resp.setAccessToken("ACCESS-JWT-" + UUID.randomUUID().toString().substring(0, 16));
        resp.setRefreshToken("REFRESH-JWT-" + UUID.randomUUID().toString().substring(0, 16));
        return resp;
    }

    public AuthResponseDto register(RegisterDto registerDto) {
        if (userDao.existsEmail(registerDto.getEmail())) {
            throw new IllegalArgumentException("Email đã được sử dụng");
        }

        if (registerDto.getPhone() != null && !registerDto.getPhone().trim().isEmpty()) {
            if (userDao.existsPhone(registerDto.getPhone())) {
                throw new IllegalArgumentException("Số điện thoại đã được sử dụng");
            }
        }

        User u = new User();
        u.setFullName(registerDto.getFullName());
        u.setEmail(registerDto.getEmail());
        u.setPhone(registerDto.getPhone());
        u.setPasswordHash(registerDto.getPassword()); // Plain text matching C#
        u.setRole("CUSTOMER");
        u.setStatus("ACTIVE");
        u.setCreatedAt(LocalDateTime.now());
        u.setUpdatedAt(LocalDateTime.now());

        userDao.insert(u);

        AuthResponseDto resp = new AuthResponseDto();
        resp.setId(u.getId());
        resp.setEmail(u.getEmail());
        resp.setFullName(u.getFullName());
        resp.setRole(u.getRole());
        resp.setAccessToken("ACCESS-JWT-" + UUID.randomUUID().toString().substring(0, 16));
        resp.setRefreshToken("REFRESH-JWT-" + UUID.randomUUID().toString().substring(0, 16));
        return resp;
    }
}
