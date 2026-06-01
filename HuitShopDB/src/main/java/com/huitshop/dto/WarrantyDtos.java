package com.huitshop.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class WarrantyDtos {

    public static class WarrantyDto {
        private int id;
        private String serialNumber;
        private int productId;
        private String productName;
        private String variantName;
        private LocalDateTime outboundDate;
        private LocalDate expireDate;
        private String notes;
        private String orderCode;
        private String customerName;
        private String status;
        private int daysRemaining;

        // Getters and Setters
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public String getSerialNumber() { return serialNumber; }
        public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }
        public int getProductId() { return productId; }
        public void setProductId(int productId) { this.productId = productId; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public String getVariantName() { return variantName; }
        public void setVariantName(String variantName) { this.variantName = variantName; }
        public LocalDateTime getOutboundDate() { return outboundDate; }
        public void setOutboundDate(LocalDateTime outboundDate) { this.outboundDate = outboundDate; }
        public LocalDate getExpireDate() { return expireDate; }
        public void setExpireDate(LocalDate expireDate) { this.expireDate = expireDate; }
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
        public String getOrderCode() { return orderCode; }
        public void setOrderCode(String orderCode) { this.orderCode = orderCode; }
        public String getCustomerName() { return customerName; }
        public void setCustomerName(String customerName) { this.customerName = customerName; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public int getDaysRemaining() { return daysRemaining; }
        public void setDaysRemaining(int daysRemaining) { this.daysRemaining = daysRemaining; }
    }
}
