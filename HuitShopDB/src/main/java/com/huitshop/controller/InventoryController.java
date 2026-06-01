package com.huitshop.controller;

import com.huitshop.dto.InventoryDtos.*;
import com.huitshop.model.ProductVariant;
import com.huitshop.model.Warehouse;
import com.huitshop.service.InventoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {
    private final InventoryService inventoryService = new InventoryService();

    @GetMapping("/stock/{warehouseId}")
    public ResponseEntity<List<InventoryDto>> getStockLevel(@PathVariable int warehouseId) {
        return ResponseEntity.ok(inventoryService.getStockLevelByWarehouse(warehouseId));
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<LowStockDto>> getLowStock(@RequestParam(required = false) Integer warehouseId) {
        return ResponseEntity.ok(inventoryService.getLowStockVariants(warehouseId));
    }

    @PostMapping("/adjust")
    public ResponseEntity<?> adjustStock(@RequestBody AdjustStockRequest request) {
        boolean success = inventoryService.adjustStock(request);
        if (!success) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Điều chỉnh kho thất bại");
        }
        return ResponseEntity.ok("Điều chỉnh kho thành công");
    }

    @PostMapping("/import")
    public ResponseEntity<?> importStock(@RequestBody ImportStockRequest request) {
        boolean success = inventoryService.importStock(request);
        if (!success) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Nhập kho thất bại");
        }
        return ResponseEntity.ok("Nhập kho thành công");
    }

    @PostMapping("/transfer")
    public ResponseEntity<?> transferStock(@RequestBody TransferStockRequest request) {
        boolean success = inventoryService.transferStock(request);
        if (!success) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Chuyển kho thất bại hoặc không đủ hàng");
        }
        return ResponseEntity.ok("Chuyển kho thành công");
    }

    @GetMapping("/movements")
    public ResponseEntity<List<StockMovementDto>> getStockMovements(
            @RequestParam int warehouseId,
            @RequestParam(required = false) Integer variantId
    ) {
        return ResponseEntity.ok(inventoryService.getStockMovements(warehouseId, variantId));
    }

    @GetMapping("/warehouses")
    public ResponseEntity<List<Warehouse>> getWarehouses() {
        return ResponseEntity.ok(inventoryService.getWarehouses());
    }

    @GetMapping("/suppliers")
    public ResponseEntity<List<com.huitshop.model.Supplier>> getSuppliers() {
        return ResponseEntity.ok(inventoryService.getSuppliers());
    }

    @GetMapping("/variants")
    public ResponseEntity<List<ProductVariant>> getVariants() {
        return ResponseEntity.ok(inventoryService.getProductVariants());
    }

    @GetMapping("/analytics")
    public ResponseEntity<WarehouseAnalyticsDto> getAnalytics() {
        return ResponseEntity.ok(inventoryService.getWarehouseAnalytics());
    }

    @GetMapping("/reorder-report")
    public ResponseEntity<List<InventoryReorderReportDto>> getReorderReport() {
        return ResponseEntity.ok(inventoryService.getReorderReport());
    }

    @PostMapping("/movements/filter")
    public ResponseEntity<List<StockMovementDto>> getStockMovementsFiltered(@RequestBody StockMovementFilterRequest filter) {
        return ResponseEntity.ok(inventoryService.getStockMovementsFiltered(filter));
    }
}
