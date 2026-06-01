package com.huitshop.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huitshop.dao.CartDao;
import com.huitshop.dao.OrderDao;
import com.huitshop.dao.VoucherDao;
import com.huitshop.dto.OrderDtos.*;
import com.huitshop.model.Cart;
import com.huitshop.model.CartItem;
import com.huitshop.model.Order;
import com.huitshop.model.OrderItem;
import com.huitshop.model.OrderStatusHistory;
import com.huitshop.model.StockMovement;
import com.huitshop.model.Voucher;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class OrderService {
    private final OrderDao orderDao = new OrderDao();
    private final CartDao cartDao = new CartDao();
    private final VoucherDao voucherDao = new VoucherDao();
    private final ObjectMapper mapper = new ObjectMapper();

    public OrderResponseDto createOrder(int userId, CreateOrderRequest request) {
        Cart cart = cartDao.getCartByUserId(userId);
        if (cart == null || cart.getCartItems().isEmpty()) {
            throw new IllegalStateException("Giỏ hàng trống");
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartItem ci : cart.getCartItems()) {
            BigDecimal price = ci.getProductVariant() != null ? ci.getProductVariant().getPrice() : BigDecimal.ZERO;
            subtotal = subtotal.add(price.multiply(BigDecimal.valueOf(ci.getQuantity())));
        }

        BigDecimal discount = BigDecimal.ZERO;
        Integer voucherId = null;
        if (cart.getVoucherCode() != null && !cart.getVoucherCode().trim().isEmpty()) {
            Voucher v = voucherDao.findByCode(cart.getVoucherCode());
            if (v != null && v.isActive() 
                    && !v.getStartDate().isAfter(LocalDateTime.now()) 
                    && !v.getEndDate().isBefore(LocalDateTime.now())
                    && (v.getUsageLimit() == null || v.getUsageCount() < v.getUsageLimit())
                    && subtotal.compareTo(v.getMinOrderValue()) >= 0) {
                
                if ("PERCENT".equals(v.getDiscountType())) {
                    discount = subtotal.multiply(v.getDiscountValue().divide(BigDecimal.valueOf(100)));
                    if (v.getMaxDiscountAmount() != null && discount.compareTo(v.getMaxDiscountAmount()) > 0) {
                        discount = v.getMaxDiscountAmount();
                    }
                } else if ("FIXED".equals(v.getDiscountType())) {
                    discount = v.getDiscountValue();
                }

                if (discount.compareTo(subtotal) > 0) {
                    discount = subtotal;
                }
                voucherId = v.getId();
            }
        }

        // Shipping fee: free if subtotal >= 500,000 VND, else 30,000 VND
        BigDecimal shippingFee = subtotal.compareTo(BigDecimal.valueOf(500000)) >= 0 ? BigDecimal.ZERO : BigDecimal.valueOf(30000);
        BigDecimal taxAmount = BigDecimal.ZERO;
        BigDecimal total = subtotal.subtract(discount).add(shippingFee).add(taxAmount);

        String orderCode = "ORD" + UUID.randomUUID().toString().replace("-", "").substring(0, 14).toUpperCase();

        Order order = new Order();
        order.setUserId(userId);
        order.setCode(orderCode);
        order.setSubtotal(subtotal);
        order.setDiscount(discount);
        order.setShippingFee(shippingFee);
        order.setTaxAmount(taxAmount);
        order.setTotal(total);
        order.setPaymentMethod(request.getPaymentMethod());
        order.setShippingAddress(request.getShippingAddressJson());
        order.setNote(request.getNote());
        order.setStatus("PENDING");
        order.setPaymentStatus("COD".equals(request.getPaymentMethod()) ? "PENDING" : "PAID");
        order.setOrderType("ONLINE");
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        // Insert order first to get ID
        orderDao.insertOrder(order);

        List<OrderItem> items = new ArrayList<>();
        int defaultWarehouseId = 1;
        
        for (CartItem ci : cart.getCartItems()) {
            OrderItem item = new OrderItem();
            item.setOrderId(order.getId());
            item.setVariantId(ci.getVariantId());
            
            String fullName = ci.getProductVariant().getProduct().getName() + 
                              (ci.getProductVariant().getVariantName() == null || ci.getProductVariant().getVariantName().isEmpty() ? 
                               "" : " " + ci.getProductVariant().getVariantName());
            item.setProductName(fullName);
            item.setSku(ci.getProductVariant().getSku());
            item.setQuantity(ci.getQuantity());
            item.setUnitPrice(ci.getProductVariant().getPrice());
            item.setTotalPrice(ci.getProductVariant().getPrice().multiply(BigDecimal.valueOf(ci.getQuantity())));
            item.setDiscountAmount(BigDecimal.ZERO);
            item.setCostPrice(ci.getProductVariant().getCostPrice());
            
            // Insert item
            orderDao.insertOrderItem(item);
            items.add(item);

            // Reserve stock in database
            orderDao.updateInventoryReserved(defaultWarehouseId, ci.getVariantId(), ci.getQuantity());

            // Log Stock movement
            StockMovement sm = new StockMovement();
            sm.setWarehouseId(defaultWarehouseId);
            sm.setVariantId(ci.getVariantId());
            sm.setQuantity(-ci.getQuantity());
            sm.setMovementType("SALE_RESERVED");
            sm.setReferenceId(order.getId());
            sm.setReferenceType("ORDER");
            sm.setNote("Khóa tồn kho cho đơn hàng " + orderCode);
            orderDao.insertStockMovement(sm);
        }

        // Record voucher usage
        if (voucherId != null) {
            orderDao.insertVoucherUsage(voucherId, userId, order.getId(), discount);
            orderDao.incrementVoucherUsageCount(voucherId);
        }

        // Record status history
        orderDao.insertOrderStatusHistory(order.getId(), "PENDING", "Đơn hàng được tạo thành công", null);

        // Clear cart
        cartDao.deleteCartItems(cart.getId());
        cartDao.updateCartVoucher(cart.getId(), null);

        return getOrderByCode(orderCode);
    }

    public List<OrderResponseDto> getOrdersByUserId(int userId, int page, int pageSize) {
        List<Order> orders = orderDao.getOrdersByUserId(userId, page, pageSize);
        List<OrderResponseDto> dtos = new ArrayList<>();
        for (Order o : orders) {
            dtos.add(mapToResponseDto(o));
        }
        return dtos;
    }

    public OrderResponseDto getOrderByCode(String orderCode) {
        Order o = orderDao.getOrderByCode(orderCode);
        return o != null ? mapToResponseDto(o) : null;
    }

    public OrderResponseDto getOrderById(int orderId) {
        Order o = orderDao.getOrderById(orderId);
        return o != null ? mapToResponseDto(o) : null;
    }

    public List<OrderResponseDto> getAllOrders(String status, String keyword, int page, int pageSize) {
        List<Order> orders = orderDao.getAllOrders(status, keyword, page, pageSize);
        List<OrderResponseDto> dtos = new ArrayList<>();
        for (Order o : orders) {
            dtos.add(mapToResponseDto(o));
        }
        return dtos;
    }

    public int getAllOrdersCount(String status, String keyword) {
        return orderDao.getAllOrdersCount(status, keyword);
    }

    public boolean cancelOrder(int orderId, String reason) {
        Order o = orderDao.getOrderById(orderId);
        if (o == null || (!"PENDING".equals(o.getStatus()) && !"CONFIRMED".equals(o.getStatus()))) {
            return false;
        }

        o.setStatus("CANCELLED");
        orderDao.updateOrder(o);

        // Load items to release reservation
        List<OrderItem> items = orderDao.getOrderItems(orderId);
        int defaultWarehouseId = 1;
        for (OrderItem oi : items) {
            // Revert reserved stock
            orderDao.updateInventoryReserved(defaultWarehouseId, oi.getVariantId(), -oi.getQuantity());

            StockMovement sm = new StockMovement();
            sm.setWarehouseId(defaultWarehouseId);
            sm.setVariantId(oi.getVariantId());
            sm.setQuantity(oi.getQuantity());
            sm.setMovementType("RETURN");
            sm.setReferenceId(orderId);
            sm.setReferenceType("ORDER");
            sm.setNote("Hoàn trả kho do hủy đơn " + o.getCode() + ". Lý do: " + reason);
            orderDao.insertStockMovement(sm);
        }

        orderDao.insertOrderStatusHistory(orderId, "CANCELLED", reason == null || reason.isEmpty() ? "Đơn hàng bị hủy" : reason, null);
        return true;
    }

    public boolean confirmOrder(int orderId, int staffId) {
        Order o = orderDao.getOrderById(orderId);
        if (o == null || !"PENDING".equals(o.getStatus())) {
            return false;
        }

        o.setStatus("CONFIRMED");
        orderDao.updateOrder(o);

        orderDao.insertOrderStatusHistory(orderId, "CONFIRMED", "Đơn hàng đã được xác nhận", staffId);
        return true;
    }

    public boolean shipOrder(int orderId, int warehouseId, String serialNumbersJson) {
        Order o = orderDao.getOrderById(orderId);
        if (o == null || !"CONFIRMED".equals(o.getStatus())) {
            return false;
        }

        o.setStatus("SHIPPING");
        orderDao.updateOrder(o);

        // Save order item serials
        if (serialNumbersJson != null && !serialNumbersJson.trim().isEmpty()) {
            try {
                Map<String, List<String>> serialMap = mapper.readValue(
                    serialNumbersJson, 
                    new TypeReference<Map<String, List<String>>>() {}
                );
                
                List<OrderItem> items = orderDao.getOrderItems(orderId);
                for (OrderItem oi : items) {
                    String key = String.valueOf(oi.getId());
                    if (serialMap.containsKey(key)) {
                        for (String sn : serialMap.get(key)) {
                            orderDao.insertOrderItemSerial(oi.getId(), sn);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        orderDao.insertOrderStatusHistory(orderId, "SHIPPING", "Đơn hàng đang được vận chuyển", null);
        return true;
    }

    public boolean completeOrder(int orderId) {
        Order o = orderDao.getOrderById(orderId);
        if (o == null || !"SHIPPING".equals(o.getStatus())) {
            return false;
        }

        o.setStatus("COMPLETED");
        o.setPaymentStatus("PAID");
        orderDao.updateOrder(o);

        // Deduct actual stock
        List<OrderItem> items = orderDao.getOrderItems(orderId);
        int defaultWarehouseId = 1;
        for (OrderItem oi : items) {
            orderDao.updateInventoryOnHand(defaultWarehouseId, oi.getVariantId(), -oi.getQuantity());
            orderDao.updateInventoryReserved(defaultWarehouseId, oi.getVariantId(), -oi.getQuantity());

            StockMovement sm = new StockMovement();
            sm.setWarehouseId(defaultWarehouseId);
            sm.setVariantId(oi.getVariantId());
            sm.setQuantity(-oi.getQuantity());
            sm.setMovementType("SALE_SHIP");
            sm.setReferenceId(orderId);
            sm.setReferenceType("ORDER");
            sm.setNote("Xuất kho hoàn tất đơn hàng " + o.getCode());
            orderDao.insertStockMovement(sm);
        }

        orderDao.insertOrderStatusHistory(orderId, "COMPLETED", "Đơn hàng đã được giao thành công", null);
        return true;
    }

    private OrderResponseDto mapToResponseDto(Order o) {
        // Parse shipping address
        String recipientName = "";
        String recipientPhone = "";
        String fullAddress = "";
        
        if (o.getShippingAddress() != null && !o.getShippingAddress().isEmpty()) {
            try {
                JsonNode addr = mapper.readTree(o.getShippingAddress());
                if (addr != null) {
                    recipientName = addr.has("receiver_name") ? addr.get("receiver_name").asText() : "";
                    if (recipientName.isEmpty() && addr.has("full_name")) {
                        recipientName = addr.get("full_name").asText();
                    }
                    
                    recipientPhone = addr.has("receiver_phone") ? addr.get("receiver_phone").asText() : "";
                    if (recipientPhone.isEmpty() && addr.has("phone")) {
                        recipientPhone = addr.get("phone").asText();
                    }
                    
                    String street = addr.has("street_address") ? addr.get("street_address").asText() : "";
                    if (street.isEmpty() && addr.has("address_line")) {
                        street = addr.get("address_line").asText();
                    }
                    
                    String ward = addr.has("ward") ? addr.get("ward").asText() : "";
                    String district = addr.has("district") ? addr.get("district").asText() : "";
                    String province = addr.has("province") ? addr.get("province").asText() : "";
                    if (province.isEmpty() && addr.has("city")) {
                        province = addr.get("city").asText();
                    }
                    
                    fullAddress = String.format("%s, %s, %s, %s", street, ward, district, province)
                                        .replaceAll(",\\s*,", ",")
                                        .replaceAll("^,\\s*|,\\s*$", "")
                                        .trim();
                }
            } catch (Exception e) {
                fullAddress = o.getShippingAddress(); // fallback
            }
        }

        OrderResponseDto dto = new OrderResponseDto();
        dto.setId(o.getId());
        dto.setCode(o.getCode());
        dto.setSubtotal(o.getSubtotal());
        dto.setDiscount(o.getDiscount());
        dto.setShippingFee(o.getShippingFee());
        dto.setTotal(o.getTotal());
        dto.setPaymentMethod(o.getPaymentMethod());
        dto.setPaymentStatus(o.getPaymentStatus());
        dto.setStatus(o.getStatus());
        dto.setShippingAddressJson(o.getShippingAddress());
        dto.setRecipientName(recipientName);
        dto.setRecipientPhone(recipientPhone);
        dto.setFullAddress(fullAddress);
        dto.setNote(o.getNote());
        dto.setCreatedAt(o.getCreatedAt());
        dto.setUserId(o.getUserId());
        dto.setUserName(o.getUser() != null ? o.getUser().getFullName() : "");
        dto.setUserEmail(o.getUser() != null ? o.getUser().getEmail() : "");

        // Load items
        List<OrderItem> items = orderDao.getOrderItems(o.getId());
        for (OrderItem item : items) {
            OrderItemDto itemDto = new OrderItemDto();
            itemDto.setId(item.getId());
            itemDto.setVariantId(item.getVariantId());
            itemDto.setProductName(item.getProductName());
            itemDto.setSku(item.getSku());
            itemDto.setQuantity(item.getQuantity());
            itemDto.setUnitPrice(item.getUnitPrice());
            itemDto.setTotalPrice(item.getTotalPrice());
            itemDto.setThumbnailUrl(item.getProductVariant() != null ? item.getProductVariant().getThumbnailUrl() : "");
            itemDto.setSerialNumbers(item.getSerialNumbers());
            dto.getItems().add(itemDto);
        }

        // Load history
        List<OrderStatusHistory> history = orderDao.getOrderStatusHistory(o.getId());
        for (OrderStatusHistory h : history) {
            OrderStatusHistoryDto hDto = new OrderStatusHistoryDto();
            hDto.setId(h.getId());
            hDto.setStatus(h.getStatus());
            hDto.setNote(h.getNote());
            hDto.setCreatedAt(h.getCreatedAt());
            dto.getStatusHistory().add(hDto);
        }

        return dto;
    }
}
