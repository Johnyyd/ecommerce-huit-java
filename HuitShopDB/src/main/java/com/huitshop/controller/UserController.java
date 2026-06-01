package com.huitshop.controller;

import com.huitshop.model.Address;
import com.huitshop.model.User;
import com.huitshop.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService = new UserService();

    @PutMapping("/profile/{userId}")
    public ResponseEntity<?> updateProfile(@PathVariable int userId, @RequestBody Map<String, String> body) {
        String fullName = body.get("fullName");
        String phone = body.get("phone");
        String password = body.get("password");
        
        boolean updated = userService.updateProfile(userId, fullName, phone, password);
        if (!updated) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy người dùng");
        }
        return ResponseEntity.ok("Thông tin cá nhân đã được cập nhật");
    }

    @GetMapping("/{userId}/addresses")
    public ResponseEntity<List<Address>> getAddresses(@PathVariable int userId) {
        return ResponseEntity.ok(userService.getAddresses(userId));
    }

    @PostMapping("/{userId}/addresses")
    public ResponseEntity<?> addAddress(@PathVariable int userId, @RequestBody Address addr) {
        boolean added = userService.addAddress(userId, addr);
        if (!added) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Thêm địa chỉ thất bại");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(addr);
    }

    @DeleteMapping("/{userId}/addresses/{addressId}")
    public ResponseEntity<?> removeAddress(@PathVariable int userId, @PathVariable int addressId) {
        boolean removed = userService.removeAddress(addressId, userId);
        if (!removed) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Xóa địa chỉ thất bại");
        }
        return ResponseEntity.ok("Xóa địa chỉ thành công");
    }

    @PutMapping("/{userId}/addresses/{addressId}/default")
    public ResponseEntity<?> setDefaultAddress(@PathVariable int userId, @PathVariable int addressId) {
        boolean success = userService.setDefaultAddress(addressId, userId);
        if (!success) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Đặt địa chỉ mặc định thất bại");
        }
        return ResponseEntity.ok("Đã đặt làm địa chỉ mặc định");
    }

    // Admin endpoints
    @GetMapping
    public ResponseEntity<List<User>> getUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status
    ) {
        return ResponseEntity.ok(userService.getUsers(search, role, status));
    }

    @PutMapping("/{targetUserId}/role-status")
    public ResponseEntity<?> updateUserRoleAndStatus(
            @PathVariable int targetUserId,
            @RequestBody Map<String, String> body
    ) {
        String role = body.get("role");
        String status = body.get("status");
        boolean updated = userService.updateUserRoleAndStatus(targetUserId, role, status);
        if (!updated) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy người dùng");
        }
        return ResponseEntity.ok("Đã cập nhật vai trò và trạng thái thành công");
    }
}
