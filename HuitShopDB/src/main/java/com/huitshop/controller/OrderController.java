package com.huitshop.controller;

import com.huitshop.dto.OrderDtos.CreateOrderRequest;
import com.huitshop.dto.OrderDtos.OrderResponseDto;
import com.huitshop.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService = new OrderService();

    @PostMapping("/{userId}")
    public ResponseEntity<?> createOrder(@PathVariable int userId, @RequestBody CreateOrderRequest request) {
        try {
            OrderResponseDto response = orderService.createOrder(userId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getOrdersByUserId(
            @PathVariable int userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        try {
            List<OrderResponseDto> orders = orderService.getOrdersByUserId(userId, page, pageSize);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/code/{orderCode}")
    public ResponseEntity<?> getOrderByCode(@PathVariable String orderCode) {
        OrderResponseDto order = orderService.getOrderByCode(orderCode);
        if (order == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Đơn hàng không tồn tại");
        }
        return ResponseEntity.ok(order);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrderById(@PathVariable int orderId) {
        OrderResponseDto order = orderService.getOrderById(orderId);
        if (order == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Đơn hàng không tồn tại");
        }
        return ResponseEntity.ok(order);
    }

    // Admin endpoints
    @GetMapping
    public ResponseEntity<?> getAllOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        List<OrderResponseDto> orders = orderService.getAllOrders(status, keyword, page, pageSize);
        int totalItems = orderService.getAllOrdersCount(status, keyword);

        Map<String, Object> response = new HashMap<>();
        response.put("items", orders);
        response.put("totalItems", totalItems);
        response.put("page", page);
        response.put("pageSize", pageSize);
        response.put("totalPages", (int) Math.ceil((double) totalItems / pageSize));

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable int orderId, @RequestBody Map<String, String> body) {
        String reason = body.get("reason");
        boolean cancelled = orderService.cancelOrder(orderId, reason);
        if (!cancelled) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Hủy đơn hàng thất bại. Đơn hàng không ở trạng thái chờ xử lý hoặc đã xác nhận.");
        }
        return ResponseEntity.ok("Đơn hàng đã được hủy");
    }

    @PostMapping("/{orderId}/confirm")
    public ResponseEntity<?> confirmOrder(@PathVariable int orderId, @RequestParam int staffId) {
        boolean confirmed = orderService.confirmOrder(orderId, staffId);
        if (!confirmed) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Xác nhận đơn hàng thất bại");
        }
        return ResponseEntity.ok("Đơn hàng đã được xác nhận");
    }

    @PostMapping("/{orderId}/ship")
    public ResponseEntity<?> shipOrder(
            @PathVariable int orderId,
            @RequestParam(defaultValue = "1") int warehouseId,
            @RequestBody(required = false) String serialNumbersJson
    ) {
        boolean shipped = orderService.shipOrder(orderId, warehouseId, serialNumbersJson);
        if (!shipped) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Giao hàng đơn hàng thất bại");
        }
        return ResponseEntity.ok("Đơn hàng bắt đầu giao");
    }

    @PostMapping("/{orderId}/complete")
    public ResponseEntity<?> completeOrder(@PathVariable int orderId) {
        boolean completed = orderService.completeOrder(orderId);
        if (!completed) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Hoàn tất đơn hàng thất bại");
        }
        return ResponseEntity.ok("Đơn hàng đã hoàn tất");
    }
}
