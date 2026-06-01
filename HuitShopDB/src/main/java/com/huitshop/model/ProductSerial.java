package com.huitshop.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ProductSerial {
    private int id;
    private int variantId;
    private String serialNumber;
    private int warehouseId;
    private String status; // AVAILABLE, RESERVED, SOLD, RETURNED
    private LocalDateTime inboundDate;
    private LocalDateTime outboundDate;
    private LocalDate warrantyExpireDate;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getVariantId() { return variantId; }
    public void setVariantId(int variantId) { this.variantId = variantId; }
    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }
    public int getWarehouseId() { return warehouseId; }
    public void setWarehouseId(int warehouseId) { this.warehouseId = warehouseId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getInboundDate() { return inboundDate; }
    public void setInboundDate(LocalDateTime inboundDate) { this.inboundDate = inboundDate; }
    public LocalDateTime getOutboundDate() { return outboundDate; }
    public void setOutboundDate(LocalDateTime outboundDate) { this.outboundDate = outboundDate; }
    public LocalDate getWarrantyExpireDate() { return warrantyExpireDate; }
    public void setWarrantyExpireDate(LocalDate warrantyExpireDate) { this.warrantyExpireDate = warrantyExpireDate; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
