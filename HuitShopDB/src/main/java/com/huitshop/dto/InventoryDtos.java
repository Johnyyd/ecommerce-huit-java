package com.huitshop.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class InventoryDtos {

    public static class InventoryDto {
        private int warehouseId;
        private String warehouseName;
        private String warehouseCode;
        private int variantId;
        private String sku;
        private String productName;
        private String variantName;
        private int quantityOnHand;
        private int quantityReserved;
        private int availableQuantity;
        private int reorderPoint;

        public int getWarehouseId() { return warehouseId; }
        public void setWarehouseId(int warehouseId) { this.warehouseId = warehouseId; }
        public String getWarehouseName() { return warehouseName; }
        public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }
        public String getWarehouseCode() { return warehouseCode; }
        public void setWarehouseCode(String warehouseCode) { this.warehouseCode = warehouseCode; }
        public int getVariantId() { return variantId; }
        public void setVariantId(int variantId) { this.variantId = variantId; }
        public String getSku() { return sku; }
        public void setSku(String sku) { this.sku = sku; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public String getVariantName() { return variantName; }
        public void setVariantName(String variantName) { this.variantName = variantName; }
        public int getQuantityOnHand() { return quantityOnHand; }
        public void setQuantityOnHand(int quantityOnHand) { this.quantityOnHand = quantityOnHand; }
        public int getQuantityReserved() { return quantityReserved; }
        public void setQuantityReserved(int quantityReserved) { this.quantityReserved = quantityReserved; }
        public int getAvailableQuantity() { return availableQuantity; }
        public void setAvailableQuantity(int availableQuantity) { this.availableQuantity = availableQuantity; }
        public int getReorderPoint() { return reorderPoint; }
        public void setReorderPoint(int reorderPoint) { this.reorderPoint = reorderPoint; }
    }

    public static class LowStockDto {
        private int warehouseId;
        private String warehouseName;
        private String warehouseCode;
        private int productId;
        private String productName;
        private int variantId;
        private String sku;
        private String variantName;
        private int quantityOnHand;
        private int quantityReserved;
        private int availableQuantity;
        private int reorderPoint;

        public int getWarehouseId() { return warehouseId; }
        public void setWarehouseId(int warehouseId) { this.warehouseId = warehouseId; }
        public String getWarehouseName() { return warehouseName; }
        public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }
        public String getWarehouseCode() { return warehouseCode; }
        public void setWarehouseCode(String warehouseCode) { this.warehouseCode = warehouseCode; }
        public int getProductId() { return productId; }
        public void setProductId(int productId) { this.productId = productId; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public int getVariantId() { return variantId; }
        public void setVariantId(int variantId) { this.variantId = variantId; }
        public String getSku() { return sku; }
        public void setSku(String sku) { this.sku = sku; }
        public String getVariantName() { return variantName; }
        public void setVariantName(String variantName) { this.variantName = variantName; }
        public int getQuantityOnHand() { return quantityOnHand; }
        public void setQuantityOnHand(int quantityOnHand) { this.quantityOnHand = quantityOnHand; }
        public int getQuantityReserved() { return quantityReserved; }
        public void setQuantityReserved(int quantityReserved) { this.quantityReserved = quantityReserved; }
        public int getAvailableQuantity() { return availableQuantity; }
        public void setAvailableQuantity(int availableQuantity) { this.availableQuantity = availableQuantity; }
        public int getReorderPoint() { return reorderPoint; }
        public void setReorderPoint(int reorderPoint) { this.reorderPoint = reorderPoint; }
    }

    public static class StockMovementDto {
        private int warehouseId;
        private String warehouseName;
        private int variantId;
        private String sku;
        private String productName;
        private String variantName;
        private int quantity;
        private String movementType;
        private String note;
        private LocalDateTime createdAt;

        public int getWarehouseId() { return warehouseId; }
        public void setWarehouseId(int warehouseId) { this.warehouseId = warehouseId; }
        public String getWarehouseName() { return warehouseName; }
        public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }
        public int getVariantId() { return variantId; }
        public void setVariantId(int variantId) { this.variantId = variantId; }
        public String getSku() { return sku; }
        public void setSku(String sku) { this.sku = sku; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public String getVariantName() { return variantName; }
        public void setVariantName(String variantName) { this.variantName = variantName; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public String getMovementType() { return movementType; }
        public void setMovementType(String movementType) { this.movementType = movementType; }
        public String getNote() { return note; }
        public void setNote(String note) { this.note = note; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }

    public static class WarehouseStatsDto {
        private int warehouseId;
        private String warehouseName;
        private String warehouseCode;
        private int totalItems;
        private int reservedItems;
        private int availableItems;
        private int SKUCount;
        private int lowStockCount;

        public int getWarehouseId() { return warehouseId; }
        public void setWarehouseId(int warehouseId) { this.warehouseId = warehouseId; }
        public String getWarehouseName() { return warehouseName; }
        public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }
        public String getWarehouseCode() { return warehouseCode; }
        public void setWarehouseCode(String warehouseCode) { this.warehouseCode = warehouseCode; }
        public int getTotalItems() { return totalItems; }
        public void setTotalItems(int totalItems) { this.totalItems = totalItems; }
        public int getReservedItems() { return reservedItems; }
        public void setReservedItems(int reservedItems) { this.reservedItems = reservedItems; }
        public int getAvailableItems() { return availableItems; }
        public void setAvailableItems(int availableItems) { this.availableItems = availableItems; }
        public int getSKUCount() { return SKUCount; }
        public void setSKUCount(int SKUCount) { this.SKUCount = SKUCount; }
        public int getLowStockCount() { return lowStockCount; }
        public void setLowStockCount(int lowStockCount) { this.lowStockCount = lowStockCount; }
    }

    public static class WarehouseAnalyticsDto {
        private int totalWarehouses;
        private int totalSKUs;
        private int totalItemsInStock;
        private int totalItemsReserved;
        private int lowStockItemsCount;
        private List<WarehouseStatsDto> warehouseStats = new ArrayList<>();

        public int getTotalWarehouses() { return totalWarehouses; }
        public void setTotalWarehouses(int totalWarehouses) { this.totalWarehouses = totalWarehouses; }
        public int getTotalSKUs() { return totalSKUs; }
        public void setTotalSKUs(int totalSKUs) { this.totalSKUs = totalSKUs; }
        public int getTotalItemsInStock() { return totalItemsInStock; }
        public void setTotalItemsInStock(int totalItemsInStock) { this.totalItemsInStock = totalItemsInStock; }
        public int getTotalItemsReserved() { return totalItemsReserved; }
        public void setTotalItemsReserved(int totalItemsReserved) { this.totalItemsReserved = totalItemsReserved; }
        public int getLowStockItemsCount() { return lowStockItemsCount; }
        public void setLowStockItemsCount(int lowStockItemsCount) { this.lowStockItemsCount = lowStockItemsCount; }
        public List<WarehouseStatsDto> getWarehouseStats() { return warehouseStats; }
        public void setWarehouseStats(List<WarehouseStatsDto> warehouseStats) { this.warehouseStats = warehouseStats; }
    }

    public static class WarehouseStockDto {
        private int warehouseId;
        private String warehouseName;
        private int quantity;
        private int reserved;

        public int getWarehouseId() { return warehouseId; }
        public void setWarehouseId(int warehouseId) { this.warehouseId = warehouseId; }
        public String getWarehouseName() { return warehouseName; }
        public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public int getReserved() { return reserved; }
        public void setReserved(int reserved) { this.reserved = reserved; }
    }

    public static class InventoryReorderReportDto {
        private int productId;
        private String productName;
        private String sku;
        private int variantId;
        private String variantName;
        private int totalQuantityAcrossWarehouses;
        private int reorderPoint;
        private String reorderStatus; // OK, WARNING, URGENT
        private List<WarehouseStockDto> stockByWarehouse = new ArrayList<>();

        public int getProductId() { return productId; }
        public void setProductId(int productId) { this.productId = productId; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public String getSku() { return sku; }
        public void setSku(String sku) { this.sku = sku; }
        public int getVariantId() { return variantId; }
        public void setVariantId(int variantId) { this.variantId = variantId; }
        public String getVariantName() { return variantName; }
        public void setVariantName(String variantName) { this.variantName = variantName; }
        public int getTotalQuantityAcrossWarehouses() { return totalQuantityAcrossWarehouses; }
        public void setTotalQuantityAcrossWarehouses(int totalQuantityAcrossWarehouses) { this.totalQuantityAcrossWarehouses = totalQuantityAcrossWarehouses; }
        public int getReorderPoint() { return reorderPoint; }
        public void setReorderPoint(int reorderPoint) { this.reorderPoint = reorderPoint; }
        public String getReorderStatus() { return reorderStatus; }
        public void setReorderStatus(String reorderStatus) { this.reorderStatus = reorderStatus; }
        public List<WarehouseStockDto> getStockByWarehouse() { return stockByWarehouse; }
        public void setStockByWarehouse(List<WarehouseStockDto> stockByWarehouse) { this.stockByWarehouse = stockByWarehouse; }
    }

    public static class ImportStockRequest {
        private int warehouseId;
        private int variantId;
        private int quantity;
        private Integer supplierId;
        private BigDecimal costPrice;
        private List<String> serials = new ArrayList<>();

        public int getWarehouseId() { return warehouseId; }
        public void setWarehouseId(int warehouseId) { this.warehouseId = warehouseId; }
        public int getVariantId() { return variantId; }
        public void setVariantId(int variantId) { this.variantId = variantId; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public Integer getSupplierId() { return supplierId; }
        public void setSupplierId(Integer supplierId) { this.supplierId = supplierId; }
        public BigDecimal getCostPrice() { return costPrice; }
        public void setCostPrice(BigDecimal costPrice) { this.costPrice = costPrice; }
        public List<String> getSerials() { return serials; }
        public void setSerials(List<String> serials) { this.serials = serials; }
    }

    public static class TransferStockRequest {
        private int fromWarehouseId;
        private int toWarehouseId;
        private int variantId;
        private int quantity;
        private String note;

        public int getFromWarehouseId() { return fromWarehouseId; }
        public void setFromWarehouseId(int fromWarehouseId) { this.fromWarehouseId = fromWarehouseId; }
        public int getToWarehouseId() { return toWarehouseId; }
        public void setToWarehouseId(int toWarehouseId) { this.toWarehouseId = toWarehouseId; }
        public int getVariantId() { return variantId; }
        public void setVariantId(int variantId) { this.variantId = variantId; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public String getNote() { return note; }
        public void setNote(String note) { this.note = note; }
    }

    public static class AdjustStockRequest {
        private int warehouseId;
        private int variantId;
        private int quantityChange;
        private String note;

        public int getWarehouseId() { return warehouseId; }
        public void setWarehouseId(int warehouseId) { this.warehouseId = warehouseId; }
        public int getVariantId() { return variantId; }
        public void setVariantId(int variantId) { this.variantId = variantId; }
        public int getQuantityChange() { return quantityChange; }
        public void setQuantityChange(int quantityChange) { this.quantityChange = quantityChange; }
        public String getNote() { return note; }
        public void setNote(String note) { this.note = note; }
    }

    public static class StockMovementFilterRequest {
        private Integer warehouseId;
        private Integer variantId;
        private String movementType;
        private LocalDateTime fromDate;
        private LocalDateTime toDate;
        private int pageNumber = 1;
        private int pageSize = 20;

        public Integer getWarehouseId() { return warehouseId; }
        public void setWarehouseId(Integer warehouseId) { this.warehouseId = warehouseId; }
        public Integer getVariantId() { return variantId; }
        public void setVariantId(Integer variantId) { this.variantId = variantId; }
        public String getMovementType() { return movementType; }
        public void setMovementType(String movementType) { this.movementType = movementType; }
        public LocalDateTime getFromDate() { return fromDate; }
        public void setFromDate(LocalDateTime fromDate) { this.fromDate = fromDate; }
        public LocalDateTime getToDate() { return toDate; }
        public void setToDate(LocalDateTime toDate) { this.toDate = toDate; }
        public int getPageNumber() { return pageNumber; }
        public void setPageNumber(int pageNumber) { this.pageNumber = pageNumber; }
        public int getPageSize() { return pageSize; }
        public void setPageSize(int pageSize) { this.pageSize = pageSize; }
    }
}
