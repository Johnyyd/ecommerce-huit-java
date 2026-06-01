package com.huitshop.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrderDtos {

    public static class CreateOrderRequest {
        private String paymentMethod; // COD, BANK_TRANSFER, etc.
        private String shippingAddressJson;
        private String note;

        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
        public String getShippingAddressJson() { return shippingAddressJson; }
        public void setShippingAddressJson(String shippingAddressJson) { this.shippingAddressJson = shippingAddressJson; }
        public String getNote() { return note; }
        public void setNote(String note) { this.note = note; }
    }

    public static class OrderItemDto {
        private int id;
        private int variantId;
        private String productName;
        private String sku;
        private int quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
        private String thumbnailUrl;
        private List<String> serialNumbers = new ArrayList<>();

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public int getVariantId() { return variantId; }
        public void setVariantId(int variantId) { this.variantId = variantId; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public String getSku() { return sku; }
        public void setSku(String sku) { this.sku = sku; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public BigDecimal getUnitPrice() { return unitPrice; }
        public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
        public BigDecimal getTotalPrice() { return totalPrice; }
        public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
        public String getThumbnailUrl() { return thumbnailUrl; }
        public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }
        public List<String> getSerialNumbers() { return serialNumbers; }
        public void setSerialNumbers(List<String> serialNumbers) { this.serialNumbers = serialNumbers; }
    }

    public static class OrderStatusHistoryDto {
        private int id;
        private String status;
        private String note;
        private LocalDateTime createdAt;

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getNote() { return note; }
        public void setNote(String note) { this.note = note; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }

    public static class OrderResponseDto {
        private int id;
        private String code;
        private BigDecimal subtotal;
        private BigDecimal discount;
        private BigDecimal shippingFee;
        private BigDecimal total;
        private String paymentMethod;
        private String paymentStatus;
        private String status;
        private String shippingAddressJson;
        private String recipientName;
        private String recipientPhone;
        private String fullAddress;
        private String note;
        private LocalDateTime createdAt;
        private int userId;
        private String userName;
        private String userEmail;
        private List<OrderItemDto> items = new ArrayList<>();
        private List<OrderStatusHistoryDto> statusHistory = new ArrayList<>();

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public BigDecimal getSubtotal() { return subtotal; }
        public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
        public BigDecimal getDiscount() { return discount; }
        public void setDiscount(BigDecimal discount) { this.discount = discount; }
        public BigDecimal getShippingFee() { return shippingFee; }
        public void setShippingFee(BigDecimal shippingFee) { this.shippingFee = shippingFee; }
        public BigDecimal getTotal() { return total; }
        public void setTotal(BigDecimal total) { this.total = total; }
        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
        public String getPaymentStatus() { return paymentStatus; }
        public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getShippingAddressJson() { return shippingAddressJson; }
        public void setShippingAddressJson(String shippingAddressJson) { this.shippingAddressJson = shippingAddressJson; }
        public String getRecipientName() { return recipientName; }
        public void setRecipientName(String recipientName) { this.recipientName = recipientName; }
        public String getRecipientPhone() { return recipientPhone; }
        public void setRecipientPhone(String recipientPhone) { this.recipientPhone = recipientPhone; }
        public String getFullAddress() { return fullAddress; }
        public void setFullAddress(String fullAddress) { this.fullAddress = fullAddress; }
        public String getNote() { return note; }
        public void setNote(String note) { this.note = note; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        public int getUserId() { return userId; }
        public void setUserId(int userId) { this.userId = userId; }
        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }
        public String getUserEmail() { return userEmail; }
        public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
        public List<OrderItemDto> getItems() { return items; }
        public void setItems(List<OrderItemDto> items) { this.items = items; }
        public List<OrderStatusHistoryDto> getStatusHistory() { return statusHistory; }
        public void setStatusHistory(List<OrderStatusHistoryDto> statusHistory) { this.statusHistory = statusHistory; }
    }
}
