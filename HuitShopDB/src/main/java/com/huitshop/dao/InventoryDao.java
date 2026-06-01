package com.huitshop.dao;

import com.huitshop.config.DbConnection;
import com.huitshop.dto.InventoryDtos.*;
import com.huitshop.model.Inventory;
import com.huitshop.model.ProductSerial;
import com.huitshop.model.ProductVariant;
import com.huitshop.model.StockMovement;
import com.huitshop.model.Warehouse;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.huitshop.dto.WarrantyDtos.WarrantyDto;

public class InventoryDao {

    public List<InventoryDto> getStockLevelByWarehouse(int warehouseId) {
        List<InventoryDto> list = new ArrayList<>();
        String sql = "SELECT inv.*, w.name AS w_name, w.code AS w_code, pv.sku, pv.variant_name, p.name AS p_name " +
                     "FROM inventories inv " +
                     "JOIN warehouses w ON inv.warehouse_id = w.id " +
                     "JOIN product_variants pv ON inv.variant_id = pv.id " +
                     "JOIN products p ON pv.product_id = p.id " +
                     "WHERE ? = 0 OR inv.warehouse_id = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, warehouseId);
            ps.setInt(2, warehouseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    InventoryDto dto = new InventoryDto();
                    dto.setWarehouseId(rs.getInt("warehouse_id"));
                    dto.setWarehouseName(rs.getNString("w_name"));
                    dto.setWarehouseCode(rs.getString("w_code"));
                    dto.setVariantId(rs.getInt("variant_id"));
                    dto.setSku(rs.getString("sku"));
                    dto.setProductName(rs.getNString("p_name"));
                    dto.setVariantName(rs.getNString("variant_name"));
                    dto.setQuantityOnHand(rs.getInt("quantity_on_hand"));
                    dto.setQuantityReserved(rs.getInt("quantity_reserved"));
                    dto.setAvailableQuantity(dto.getQuantityOnHand() - dto.getQuantityReserved());
                    dto.setReorderPoint(rs.getInt("reorder_point"));
                    list.add(dto);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<LowStockDto> getLowStockVariants(Integer warehouseId) {
        List<LowStockDto> list = new ArrayList<>();
        String sql = "SELECT inv.*, w.name AS w_name, w.code AS w_code, pv.product_id, pv.sku, pv.variant_name, p.name AS p_name " +
                     "FROM inventories inv " +
                     "JOIN warehouses w ON inv.warehouse_id = w.id " +
                     "JOIN product_variants pv ON inv.variant_id = pv.id " +
                     "JOIN products p ON pv.product_id = p.id " +
                     "WHERE (? IS NULL OR inv.warehouse_id = ?) AND inv.quantity_on_hand <= inv.reorder_point";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (warehouseId != null) {
                ps.setInt(1, warehouseId);
                ps.setInt(2, warehouseId);
            } else {
                ps.setNull(1, Types.INTEGER);
                ps.setNull(2, Types.INTEGER);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LowStockDto dto = new LowStockDto();
                    dto.setWarehouseId(rs.getInt("warehouse_id"));
                    dto.setWarehouseName(rs.getNString("w_name"));
                    dto.setWarehouseCode(rs.getString("w_code"));
                    dto.setProductId(rs.getInt("product_id"));
                    dto.setProductName(rs.getNString("p_name"));
                    dto.setVariantId(rs.getInt("variant_id"));
                    dto.setSku(rs.getString("sku"));
                    dto.setVariantName(rs.getNString("variant_name"));
                    dto.setQuantityOnHand(rs.getInt("quantity_on_hand"));
                    dto.setQuantityReserved(rs.getInt("quantity_reserved"));
                    dto.setAvailableQuantity(dto.getQuantityOnHand() - dto.getQuantityReserved());
                    dto.setReorderPoint(rs.getInt("reorder_point"));
                    list.add(dto);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Inventory getInventory(int warehouseId, int variantId) {
        String sql = "SELECT * FROM inventories WHERE warehouse_id = ? AND variant_id = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, warehouseId);
            ps.setInt(2, variantId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Inventory inv = new Inventory();
                    inv.setWarehouseId(rs.getInt("warehouse_id"));
                    inv.setVariantId(rs.getInt("variant_id"));
                    inv.setQuantityOnHand(rs.getInt("quantity_on_hand"));
                    inv.setQuantityReserved(rs.getInt("quantity_reserved"));
                    inv.setReorderPoint(rs.getInt("reorder_point"));
                    return inv;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void insertInventory(Inventory inv) {
        String sql = "INSERT INTO inventories (warehouse_id, variant_id, quantity_on_hand, quantity_reserved, reorder_point, last_updated) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, inv.getWarehouseId());
            ps.setInt(2, inv.getVariantId());
            ps.setInt(3, inv.getQuantityOnHand());
            ps.setInt(4, inv.getQuantityReserved());
            ps.setInt(5, inv.getReorderPoint());
            ps.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateInventoryOnHand(int warehouseId, int variantId, int qty) {
        String sql = "UPDATE inventories SET quantity_on_hand = ?, last_updated = ? WHERE warehouse_id = ? AND variant_id = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, qty);
            ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(3, warehouseId);
            ps.setInt(4, variantId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateProductVariantCostPrice(int variantId, BigDecimal costPrice) {
        String sql = "UPDATE product_variants SET cost_price = ?, updated_at = ? WHERE id = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, costPrice);
            ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(3, variantId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertProductSerial(ProductSerial serial) {
        String sql = "INSERT INTO product_serials (variant_id, serial_number, warehouse_id, status, inbound_date, warranty_expire_date, notes, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, serial.getVariantId());
            ps.setString(2, serial.getSerialNumber());
            ps.setInt(3, serial.getWarehouseId());
            ps.setString(4, serial.getStatus());
            ps.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            if (serial.getWarrantyExpireDate() != null) {
                ps.setDate(6, Date.valueOf(serial.getWarrantyExpireDate()));
            } else {
                ps.setNull(6, Types.DATE);
            }
            ps.setNString(7, serial.getNotes());
            ps.setTimestamp(8, Timestamp.valueOf(LocalDateTime.now()));
            ps.setTimestamp(9, Timestamp.valueOf(LocalDateTime.now()));
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertStockMovement(StockMovement sm) {
        String sql = "INSERT INTO stock_movements (warehouse_id, variant_id, quantity, movement_type, reference_id, reference_type, supplier_id, note, created_by, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sm.getWarehouseId());
            ps.setInt(2, sm.getVariantId());
            ps.setInt(3, sm.getQuantity());
            ps.setString(4, sm.getMovementType());
            if (sm.getReferenceId() != null) {
                ps.setInt(5, sm.getReferenceId());
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            ps.setString(6, sm.getReferenceType());
            if (sm.getSupplierId() != null) {
                ps.setInt(7, sm.getSupplierId());
            } else {
                ps.setNull(7, Types.INTEGER);
            }
            ps.setNString(8, sm.getNote());
            if (sm.getCreatedBy() != null) {
                ps.setInt(9, sm.getCreatedBy());
            } else {
                ps.setNull(9, Types.INTEGER);
            }
            ps.setTimestamp(10, Timestamp.valueOf(LocalDateTime.now()));
            ps.setTimestamp(11, Timestamp.valueOf(LocalDateTime.now()));
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<StockMovementDto> getStockMovements(int warehouseId, Integer variantId) {
        List<StockMovementDto> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT sm.*, w.name AS w_name, pv.sku, pv.variant_name, p.name AS p_name " +
            "FROM stock_movements sm " +
            "JOIN warehouses w ON sm.warehouse_id = w.id " +
            "JOIN product_variants pv ON sm.variant_id = pv.id " +
            "JOIN products p ON pv.product_id = p.id " +
            "WHERE 1=1"
        );
        List<Object> params = new ArrayList<>();

        if (warehouseId > 0) {
            sql.append(" AND sm.warehouse_id = ?");
            params.add(warehouseId);
        }

        if (variantId != null) {
            sql.append(" AND sm.variant_id = ?");
            params.add(variantId);
        }

        sql.append(" ORDER BY sm.created_at DESC");

        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapStockMovementDto(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Warehouse> getWarehouses() {
        List<Warehouse> list = new ArrayList<>();
        String sql = "SELECT * FROM warehouses WHERE is_active = 1";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Warehouse w = new Warehouse();
                w.setId(rs.getInt("id"));
                w.setCode(rs.getString("code"));
                w.setName(rs.getNString("name"));
                w.setAddress(rs.getNString("address"));
                w.setType(rs.getString("type"));
                w.setPhone(rs.getString("phone"));
                w.setManager(rs.getNString("manager"));
                w.setActive(rs.getBoolean("is_active"));
                list.add(w);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<ProductVariant> getProductVariants() {
        List<ProductVariant> list = new ArrayList<>();
        String sql = "SELECT v.*, p.name AS p_name FROM product_variants v JOIN products p ON v.product_id = p.id WHERE v.is_active = 1 ORDER BY p.name";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ProductVariant v = new ProductVariant();
                v.setId(rs.getInt("id"));
                v.setProductId(rs.getInt("product_id"));
                v.setSku(rs.getString("sku"));
                v.setVariantName(rs.getNString("variant_name"));
                v.setPrice(rs.getBigDecimal("price"));
                
                com.huitshop.model.Product p = new com.huitshop.model.Product();
                p.setId(v.getProductId());
                p.setName(rs.getNString("p_name"));
                v.setProduct(p);
                
                list.add(v);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public WarehouseAnalyticsDto getWarehouseAnalytics() {
        WarehouseAnalyticsDto analytics = new WarehouseAnalyticsDto();
        try (Connection conn = DbConnection.getConnection()) {
            // Warehouses count
            String sqlW = "SELECT COUNT(1) FROM warehouses";
            try (PreparedStatement ps = conn.prepareStatement(sqlW); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) analytics.setTotalWarehouses(rs.getInt(1));
            }

            // SKU count
            String sqlSKU = "SELECT COUNT(DISTINCT variant_id) FROM inventories";
            try (PreparedStatement ps = conn.prepareStatement(sqlSKU); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) analytics.setTotalSKUs(rs.getInt(1));
            }

            // Total in stock, reserved, low stock
            String sqlInv = "SELECT SUM(quantity_on_hand), SUM(quantity_reserved), COUNT(CASE WHEN quantity_on_hand <= reorder_point THEN 1 END) FROM inventories";
            try (PreparedStatement ps = conn.prepareStatement(sqlInv); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    analytics.setTotalItemsInStock(rs.getInt(1));
                    analytics.setTotalItemsReserved(rs.getInt(2));
                    analytics.setLowStockItemsCount(rs.getInt(3));
                }
            }

            // Warehouse statistics list
            String sqlStats = "SELECT w.id, w.name, w.code, " +
                              "SUM(inv.quantity_on_hand) AS total_items, " +
                              "SUM(inv.quantity_reserved) AS reserved_items, " +
                              "COUNT(DISTINCT inv.variant_id) AS sku_count, " +
                              "COUNT(CASE WHEN inv.quantity_on_hand <= inv.reorder_point THEN 1 END) AS low_stock_count " +
                              "FROM warehouses w " +
                              "LEFT JOIN inventories inv ON w.id = inv.warehouse_id " +
                              "GROUP BY w.id, w.name, w.code";
            try (PreparedStatement ps = conn.prepareStatement(sqlStats); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    WarehouseStatsDto stats = new WarehouseStatsDto();
                    stats.setWarehouseId(rs.getInt("id"));
                    stats.setWarehouseName(rs.getNString("name"));
                    stats.setWarehouseCode(rs.getString("code"));
                    stats.setTotalItems(rs.getInt("total_items"));
                    stats.setReservedItems(rs.getInt("reserved_items"));
                    stats.setAvailableItems(stats.getTotalItems() - stats.getReservedItems());
                    stats.setSKUCount(rs.getInt("sku_count"));
                    stats.setLowStockCount(rs.getInt("low_stock_count"));
                    analytics.getWarehouseStats().add(stats);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return analytics;
    }

    public List<InventoryReorderReportDto> getReorderReport() {
        List<InventoryReorderReportDto> report = new ArrayList<>();
        String sqlGroup = "SELECT inv.variant_id, pv.product_id, p.name AS p_name, pv.sku, pv.variant_name, " +
                          "SUM(inv.quantity_on_hand) AS total_qty, MAX(inv.reorder_point) AS rp " +
                          "FROM inventories inv " +
                          "JOIN product_variants pv ON inv.variant_id = pv.id " +
                          "JOIN products p ON pv.product_id = p.id " +
                          "GROUP BY inv.variant_id, pv.product_id, p.name, pv.sku, pv.variant_name " +
                          "HAVING SUM(inv.quantity_on_hand) <= MAX(inv.reorder_point)";
        
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlGroup);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                InventoryReorderReportDto item = new InventoryReorderReportDto();
                item.setVariantId(rs.getInt("variant_id"));
                item.setProductId(rs.getInt("product_id"));
                item.setProductName(rs.getNString("p_name"));
                item.setSku(rs.getString("sku"));
                item.setVariantName(rs.getNString("variant_name"));
                item.setTotalQuantityAcrossWarehouses(rs.getInt("total_qty"));
                item.setReorderPoint(rs.getInt("rp"));
                
                if (item.getTotalQuantityAcrossWarehouses() <= item.getReorderPoint() / 2) {
                    item.setReorderStatus("URGENT");
                } else {
                    item.setReorderStatus("WARNING");
                }

                // Get details per warehouse
                String sqlDetail = "SELECT inv.warehouse_id, w.name AS w_name, inv.quantity_on_hand, inv.quantity_reserved " +
                                   "FROM inventories inv JOIN warehouses w ON inv.warehouse_id = w.id WHERE inv.variant_id = ?";
                try (PreparedStatement psDetail = conn.prepareStatement(sqlDetail)) {
                    psDetail.setInt(1, item.getVariantId());
                    try (ResultSet rsDetail = psDetail.executeQuery()) {
                        while (rsDetail.next()) {
                            WarehouseStockDto ws = new WarehouseStockDto();
                            ws.setWarehouseId(rsDetail.getInt("warehouse_id"));
                            ws.setWarehouseName(rsDetail.getNString("w_name"));
                            ws.setQuantity(rsDetail.getInt("quantity_on_hand"));
                            ws.setReserved(rsDetail.getInt("quantity_reserved"));
                            item.getStockByWarehouse().add(ws);
                        }
                    }
                }
                report.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return report;
    }

    public List<StockMovementDto> getStockMovementsFiltered(StockMovementFilterRequest filter) {
        List<StockMovementDto> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT sm.*, w.name AS w_name, pv.sku, pv.variant_name, p.name AS p_name " +
            "FROM stock_movements sm " +
            "JOIN warehouses w ON sm.warehouse_id = w.id " +
            "JOIN product_variants pv ON sm.variant_id = pv.id " +
            "JOIN products p ON pv.product_id = p.id " +
            "WHERE 1=1"
        );
        List<Object> params = new ArrayList<>();

        if (filter.getWarehouseId() != null) {
            sql.append(" AND sm.warehouse_id = ?");
            params.add(filter.getWarehouseId());
        }

        if (filter.getVariantId() != null) {
            sql.append(" AND sm.variant_id = ?");
            params.add(filter.getVariantId());
        }

        if (filter.getMovementType() != null && !filter.getMovementType().isEmpty()) {
            sql.append(" AND sm.movement_type = ?");
            params.add(filter.getMovementType());
        }

        if (filter.getFromDate() != null) {
            sql.append(" AND sm.created_at >= ?");
            params.add(Timestamp.valueOf(filter.getFromDate()));
        }

        if (filter.getToDate() != null) {
            sql.append(" AND sm.created_at <= ?");
            params.add(Timestamp.valueOf(filter.getToDate().plusDays(1)));
        }

        sql.append(" ORDER BY sm.created_at DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
        params.add((filter.getPageNumber() - 1) * filter.getPageSize());
        params.add(filter.getPageSize());

        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapStockMovementDto(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private StockMovementDto mapStockMovementDto(ResultSet rs) throws SQLException {
        StockMovementDto dto = new StockMovementDto();
        dto.setWarehouseId(rs.getInt("warehouse_id"));
        dto.setWarehouseName(rs.getNString("w_name"));
        dto.setVariantId(rs.getInt("variant_id"));
        dto.setSku(rs.getString("sku"));
        dto.setProductName(rs.getNString("p_name"));
        dto.setVariantName(rs.getNString("variant_name"));
        dto.setQuantity(rs.getInt("quantity"));
        dto.setMovementType(rs.getString("movement_type"));
        dto.setNote(rs.getNString("note"));
        
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            dto.setCreatedAt(ts.toLocalDateTime());
        }
        return dto;
    }

    public WarrantyDto findProductSerialByNumber(String serialNumber) {
        String sql = "SELECT " +
                     "  ps.id AS serial_id, " +
                     "  ps.serial_number, " +
                     "  ps.variant_id, " +
                     "  pv.product_id, " +
                     "  p.name AS product_name, " +
                     "  pv.variant_name, " +
                     "  ps.outbound_date, " +
                     "  ps.warranty_expire_date, " +
                     "  ps.notes, " +
                     "  ps.status AS serial_status, " +
                     "  o.code AS order_code, " +
                     "  u.full_name AS customer_name " +
                     "FROM product_serials ps " +
                     "JOIN product_variants pv ON ps.variant_id = pv.id " +
                     "JOIN products p ON pv.product_id = p.id " +
                     "LEFT JOIN order_item_serials ois ON ps.serial_number = ois.serial_number " +
                     "LEFT JOIN order_items oi ON ois.order_item_id = oi.id " +
                     "LEFT JOIN orders o ON oi.order_id = o.id " +
                     "LEFT JOIN users u ON o.user_id = u.id " +
                     "WHERE ps.serial_number = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, serialNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapWarrantyDto(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<WarrantyDto> findRecentProductSerials() {
        List<WarrantyDto> list = new ArrayList<>();
        String sql = "SELECT TOP 50 " +
                     "  ps.id AS serial_id, " +
                     "  ps.serial_number, " +
                     "  ps.variant_id, " +
                     "  pv.product_id, " +
                     "  p.name AS product_name, " +
                     "  pv.variant_name, " +
                     "  ps.outbound_date, " +
                     "  ps.warranty_expire_date, " +
                     "  ps.notes, " +
                     "  ps.status AS serial_status, " +
                     "  o.code AS order_code, " +
                     "  u.full_name AS customer_name " +
                     "FROM product_serials ps " +
                     "JOIN product_variants pv ON ps.variant_id = pv.id " +
                     "JOIN products p ON pv.product_id = p.id " +
                     "LEFT JOIN order_item_serials ois ON ps.serial_number = ois.serial_number " +
                     "LEFT JOIN order_items oi ON ois.order_item_id = oi.id " +
                     "LEFT JOIN orders o ON oi.order_id = o.id " +
                     "LEFT JOIN users u ON o.user_id = u.id " +
                     "ORDER BY ps.updated_at DESC";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapWarrantyDto(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private WarrantyDto mapWarrantyDto(ResultSet rs) throws SQLException {
        WarrantyDto dto = new WarrantyDto();
        dto.setId(rs.getInt("serial_id"));
        dto.setSerialNumber(rs.getString("serial_number"));
        dto.setProductId(rs.getInt("product_id"));
        dto.setProductName(rs.getNString("product_name"));
        dto.setVariantName(rs.getNString("variant_name"));
        
        Timestamp outbound = rs.getTimestamp("outbound_date");
        if (outbound != null) {
            dto.setOutboundDate(outbound.toLocalDateTime());
        }
        
        Date expire = rs.getDate("warranty_expire_date");
        if (expire != null) {
            dto.setExpireDate(expire.toLocalDate());
        }
        
        dto.setNotes(rs.getNString("notes"));
        dto.setOrderCode(rs.getString("order_code"));
        dto.setCustomerName(rs.getNString("customer_name"));
        dto.setStatus(rs.getString("serial_status"));
        
        return dto;
    }
}
