package com.huitshop.model;

import java.time.LocalDateTime;

public class Inventory {
    private int warehouseId;
    private int variantId;
    private int quantityOnHand;
    private int quantityReserved;
    private int reorderPoint;
    private LocalDateTime lastUpdated;

    // References
    private Warehouse warehouse;
    private ProductVariant productVariant;

    // Getters and Setters
    public int getWarehouseId() { return warehouseId; }
    public void setWarehouseId(int warehouseId) { this.warehouseId = warehouseId; }
    public int getVariantId() { return variantId; }
    public void setVariantId(int variantId) { this.variantId = variantId; }
    public int getQuantityOnHand() { return quantityOnHand; }
    public void setQuantityOnHand(int quantityOnHand) { this.quantityOnHand = quantityOnHand; }
    public int getQuantityReserved() { return quantityReserved; }
    public void setQuantityReserved(int quantityReserved) { this.quantityReserved = quantityReserved; }
    public int getReorderPoint() { return reorderPoint; }
    public void setReorderPoint(int reorderPoint) { this.reorderPoint = reorderPoint; }
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }

    public Warehouse getWarehouse() { return warehouse; }
    public void setWarehouse(Warehouse warehouse) { this.warehouse = warehouse; }
    public ProductVariant getProductVariant() { return productVariant; }
    public void setProductVariant(ProductVariant productVariant) { this.productVariant = productVariant; }
}
