package com.huitshop.service;

import com.huitshop.dao.InventoryDao;
import com.huitshop.dto.InventoryDtos.*;
import com.huitshop.model.Inventory;
import com.huitshop.model.ProductSerial;
import com.huitshop.model.ProductVariant;
import com.huitshop.model.StockMovement;
import com.huitshop.model.Warehouse;

import java.time.LocalDateTime;
import java.util.List;

public class InventoryService {
    private final InventoryDao inventoryDao = new InventoryDao();

    public List<InventoryDto> getStockLevelByWarehouse(int warehouseId) {
        return inventoryDao.getStockLevelByWarehouse(warehouseId);
    }

    public List<LowStockDto> getLowStockVariants(Integer warehouseId) {
        return inventoryDao.getLowStockVariants(warehouseId);
    }

    public boolean adjustStock(AdjustStockRequest request) {
        Inventory inv = inventoryDao.getInventory(request.getWarehouseId(), request.getVariantId());
        if (inv == null) {
            return false;
        }

        inv.setQuantityOnHand(inv.getQuantityOnHand() + request.getQuantityChange());
        inventoryDao.updateInventoryOnHand(request.getWarehouseId(), request.getVariantId(), inv.getQuantityOnHand());

        StockMovement sm = new StockMovement();
        sm.setWarehouseId(request.getWarehouseId());
        sm.setVariantId(request.getVariantId());
        sm.setQuantity(Math.abs(request.getQuantityChange()));
        sm.setMovementType(request.getQuantityChange() > 0 ? "ADJUSTMENT_IN" : "ADJUSTMENT_OUT");
        sm.setNote(request.getNote());
        inventoryDao.insertStockMovement(sm);

        return true;
    }

    public boolean importStock(ImportStockRequest request) {
        Inventory inv = inventoryDao.getInventory(request.getWarehouseId(), request.getVariantId());
        int quantity = !request.getSerials().isEmpty() ? request.getSerials().size() : (request.getQuantity() > 0 ? request.getQuantity() : 1);

        if (inv == null) {
            inv = new Inventory();
            inv.setWarehouseId(request.getWarehouseId());
            inv.setVariantId(request.getVariantId());
            inv.setQuantityOnHand(quantity);
            inv.setQuantityReserved(0);
            inv.setReorderPoint(10);
            inventoryDao.insertInventory(inv);
        } else {
            inv.setQuantityOnHand(inv.getQuantityOnHand() + quantity);
            inventoryDao.updateInventoryOnHand(request.getWarehouseId(), request.getVariantId(), inv.getQuantityOnHand());
        }

        // Update variant cost price
        if (request.getCostPrice() != null && request.getCostPrice().compareTo(java.math.BigDecimal.ZERO) > 0) {
            inventoryDao.updateProductVariantCostPrice(request.getVariantId(), request.getCostPrice());
        }

        // Log Stock movement
        StockMovement sm = new StockMovement();
        sm.setWarehouseId(request.getWarehouseId());
        sm.setVariantId(request.getVariantId());
        sm.setQuantity(quantity);
        sm.setMovementType("PURCHASE");
        sm.setSupplierId(request.getSupplierId());
        sm.setNote("Nhập kho từ nhà cung cấp");
        inventoryDao.insertStockMovement(sm);

        // Insert serial numbers if any
        for (String sn : request.getSerials()) {
            ProductSerial serial = new ProductSerial();
            serial.setVariantId(request.getVariantId());
            serial.setWarehouseId(request.getWarehouseId());
            serial.setSerialNumber(sn);
            serial.setStatus("AVAILABLE");
            serial.setNotes("Inbound import");
            inventoryDao.insertProductSerial(serial);
        }

        return true;
    }

    public boolean transferStock(TransferStockRequest request) {
        Inventory fromInv = inventoryDao.getInventory(request.getFromWarehouseId(), request.getVariantId());
        if (fromInv == null || fromInv.getQuantityOnHand() < request.getQuantity()) {
            return false;
        }

        Inventory toInv = inventoryDao.getInventory(request.getToWarehouseId(), request.getVariantId());

        // Subtract from source
        fromInv.setQuantityOnHand(fromInv.getQuantityOnHand() - request.getQuantity());
        inventoryDao.updateInventoryOnHand(request.getFromWarehouseId(), request.getVariantId(), fromInv.getQuantityOnHand());

        // Add to destination
        if (toInv == null) {
            toInv = new Inventory();
            toInv.setWarehouseId(request.getToWarehouseId());
            toInv.setVariantId(request.getVariantId());
            toInv.setQuantityOnHand(request.getQuantity());
            toInv.setQuantityReserved(0);
            toInv.setReorderPoint(10);
            inventoryDao.insertInventory(toInv);
        } else {
            toInv.setQuantityOnHand(toInv.getQuantityOnHand() + request.getQuantity());
            inventoryDao.updateInventoryOnHand(request.getToWarehouseId(), request.getVariantId(), toInv.getQuantityOnHand());
        }

        // Record TRANSFER_OUT
        StockMovement moveOut = new StockMovement();
        moveOut.setWarehouseId(request.getFromWarehouseId());
        moveOut.setVariantId(request.getVariantId());
        moveOut.setQuantity(request.getQuantity());
        moveOut.setMovementType("TRANSFER_OUT");
        moveOut.setNote("Chuyển kho đi kho " + request.getToWarehouseId() + ". " + request.getNote());
        inventoryDao.insertStockMovement(moveOut);

        // Record TRANSFER_IN
        StockMovement moveIn = new StockMovement();
        moveIn.setWarehouseId(request.getToWarehouseId());
        moveIn.setVariantId(request.getVariantId());
        moveIn.setQuantity(request.getQuantity());
        moveIn.setMovementType("TRANSFER_IN");
        moveIn.setNote("Nhận kho từ kho " + request.getFromWarehouseId() + ". " + request.getNote());
        inventoryDao.insertStockMovement(moveIn);

        return true;
    }

    public List<StockMovementDto> getStockMovements(int warehouseId, Integer variantId) {
        return inventoryDao.getStockMovements(warehouseId, variantId);
    }

    public List<Warehouse> getWarehouses() {
        return inventoryDao.getWarehouses();
    }

    public List<com.huitshop.model.Supplier> getSuppliers() {
        return inventoryDao.getSuppliers();
    }

    public List<ProductVariant> getProductVariants() {
        return inventoryDao.getProductVariants();
    }

    public WarehouseAnalyticsDto getWarehouseAnalytics() {
        return inventoryDao.getWarehouseAnalytics();
    }

    public List<InventoryReorderReportDto> getReorderReport() {
        return inventoryDao.getReorderReport();
    }

    public List<StockMovementDto> getStockMovementsFiltered(StockMovementFilterRequest filter) {
        return inventoryDao.getStockMovementsFiltered(filter);
    }
}
