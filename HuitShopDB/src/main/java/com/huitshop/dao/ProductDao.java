package com.huitshop.dao;

import com.huitshop.config.DbConnection;
import com.huitshop.dto.ProductDtos.*;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ProductDao {

    public List<ProductListDto> getProducts(ProductQueryParams query) {
        List<ProductListDto> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT p.id, p.name, p.slug, p.short_description, p.is_featured, p.status, " +
            "b.id AS brand_id, b.name AS brand_name, b.origin AS brand_origin, b.logo_url AS brand_logo, " +
            "c.id AS category_id, c.name AS category_name, c.slug AS category_slug, " +
            "(SELECT MIN(v.price) FROM product_variants v WHERE v.product_id = p.id AND v.is_active = 1) AS price_from, " +
            "(SELECT MAX(v.price) FROM product_variants v WHERE v.product_id = p.id AND v.is_active = 1) AS price_to, " +
            "(SELECT TOP 1 v.thumbnail_url FROM product_variants v WHERE v.product_id = p.id AND v.is_active = 1 ORDER BY v.display_order) AS thumbnail_url, " +
            "(SELECT TOP 1 v.id FROM product_variants v WHERE v.product_id = p.id AND v.is_active = 1 ORDER BY v.display_order) AS default_variant_id " +
            "FROM products p " +
            "LEFT JOIN brands b ON p.brand_id = b.id " +
            "LEFT JOIN categories c ON p.category_id = c.id " +
            "WHERE p.status = 'ACTIVE' " +
            "  AND EXISTS ( " +
            "      SELECT 1 FROM product_variants v " +
            "      JOIN inventories i ON v.id = i.variant_id " +
            "      WHERE v.product_id = p.id AND v.is_active = 1 AND (i.quantity_on_hand - i.quantity_reserved > 0) " +
            "  )"
        );

        List<Object> params = new ArrayList<>();

        if (query.getCategoryId() != null) {
            sql.append(" AND p.category_id = ?");
            params.add(query.getCategoryId());
        }

        if (query.getBrandId() != null) {
            sql.append(" AND p.brand_id = ?");
            params.add(query.getBrandId());
        }

        if (query.getSearch() != null && !query.getSearch().trim().isEmpty()) {
            sql.append(" AND (p.name LIKE ? OR p.description LIKE ?)");
            String searchPattern = "%" + query.getSearch().trim() + "%";
            params.add(searchPattern);
            params.add(searchPattern);
        }

        if (query.getMinPrice() != null) {
            sql.append(" AND EXISTS (SELECT 1 FROM product_variants v WHERE v.product_id = p.id AND v.price >= ?)");
            params.add(query.getMinPrice());
        }

        if (query.getMaxPrice() != null) {
            sql.append(" AND EXISTS (SELECT 1 FROM product_variants v WHERE v.product_id = p.id AND v.price <= ?)");
            params.add(query.getMaxPrice());
        }

        // Sorting
        String sortBy = query.getSortBy();
        if ("price_asc".equals(sortBy)) {
            sql.append(" ORDER BY (SELECT MIN(v.price) FROM product_variants v WHERE v.product_id = p.id AND v.is_active = 1) ASC");
        } else if ("price_desc".equals(sortBy)) {
            sql.append(" ORDER BY (SELECT MAX(v.price) FROM product_variants v WHERE v.product_id = p.id AND v.is_active = 1) DESC");
        } else if ("name".equals(sortBy)) {
            sql.append(" ORDER BY p.name ASC");
        } else {
            sql.append(" ORDER BY p.created_at DESC");
        }

        // Pagination
        sql.append(" OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
        params.add((query.getPage() - 1) * query.getPageSize());
        params.add(query.getPageSize());

        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapProductListDto(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public int getProductsCount(ProductQueryParams query) {
        StringBuilder sql = new StringBuilder(
            "SELECT COUNT(DISTINCT p.id) FROM products p " +
            "LEFT JOIN product_variants v ON p.id = v.product_id " +
            "LEFT JOIN inventories i ON v.id = i.variant_id " +
            "WHERE p.status = 'ACTIVE' AND v.is_active = 1 AND (i.quantity_on_hand - i.quantity_reserved > 0)"
        );

        List<Object> params = new ArrayList<>();

        if (query.getCategoryId() != null) {
            sql.append(" AND p.category_id = ?");
            params.add(query.getCategoryId());
        }

        if (query.getBrandId() != null) {
            sql.append(" AND p.brand_id = ?");
            params.add(query.getBrandId());
        }

        if (query.getSearch() != null && !query.getSearch().trim().isEmpty()) {
            sql.append(" AND (p.name LIKE ? OR p.description LIKE ?)");
            String searchPattern = "%" + query.getSearch().trim() + "%";
            params.add(searchPattern);
            params.add(searchPattern);
        }

        if (query.getMinPrice() != null) {
            sql.append(" AND v.price >= ?");
            params.add(query.getMinPrice());
        }

        if (query.getMaxPrice() != null) {
            sql.append(" AND v.price <= ?");
            params.add(query.getMaxPrice());
        }

        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public ProductDetailDto getProductDetail(int productId) {
        String sql = "SELECT p.id, p.name, p.slug, p.description, p.specifications, p.status, p.is_featured, " +
                     "b.id AS brand_id, b.name AS brand_name, b.origin AS brand_origin, b.logo_url AS brand_logo, " +
                     "c.id AS category_id, c.name AS category_name, c.slug AS category_slug " +
                     "FROM products p " +
                     "LEFT JOIN brands b ON p.brand_id = b.id " +
                     "LEFT JOIN categories c ON p.category_id = c.id " +
                     "WHERE p.id = ? AND p.status = 'ACTIVE'";

        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ProductDetailDto dto = new ProductDetailDto();
                    dto.setId(rs.getInt("id"));
                    dto.setName(rs.getNString("name"));
                    dto.setSlug(rs.getString("slug"));
                    dto.setDescription(rs.getNString("description"));
                    dto.setSpecifications(rs.getNString("specifications"));
                    dto.setStatus(rs.getString("status"));
                    dto.setFeatured(rs.getBoolean("is_featured"));

                    // Map brand
                    int bId = rs.getInt("brand_id");
                    if (!rs.wasNull()) {
                        BrandDto b = new BrandDto();
                        b.setId(bId);
                        b.setName(rs.getNString("brand_name"));
                        b.setOrigin(rs.getNString("brand_origin"));
                        b.setLogoUrl(rs.getString("brand_logo"));
                        dto.setBrand(b);
                    }

                    // Map category
                    CategoryDto c = new CategoryDto();
                    c.setId(rs.getInt("category_id"));
                    c.setName(rs.getNString("category_name"));
                    c.setSlug(rs.getString("category_slug"));
                    dto.setCategory(c);

                    // Load active variants
                    dto.setVariants(getProductVariants(conn, productId, true));
                    // Load images
                    dto.setImages(getProductImages(conn, productId));

                    return dto;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<CategoryDto> getCategories() {
        List<CategoryDto> all = new ArrayList<>();
        String sql = "SELECT * FROM categories WHERE is_active = 1 ORDER BY sort_order";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                CategoryDto c = new CategoryDto();
                c.setId(rs.getInt("id"));
                c.setName(rs.getNString("name"));
                c.setSlug(rs.getString("slug"));
                c.setDescription(rs.getNString("description"));
                int pId = rs.getInt("parent_id");
                if (!rs.wasNull()) {
                    c.setParentId(pId);
                }
                all.add(c);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Build tree
        List<CategoryDto> roots = new ArrayList<>();
        for (CategoryDto c : all) {
            if (c.getParentId() == null) {
                roots.add(c);
                buildChildren(c, all);
            }
        }
        return roots;
    }

    private void buildChildren(CategoryDto parent, List<CategoryDto> all) {
        for (CategoryDto c : all) {
            if (c.getParentId() != null && c.getParentId() == parent.getId()) {
                parent.getChildren().add(c);
                buildChildren(c, all);
            }
        }
    }

    public List<BrandDto> getBrands() {
        List<BrandDto> list = new ArrayList<>();
        String sql = "SELECT * FROM brands ORDER BY name";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                BrandDto b = new BrandDto();
                b.setId(rs.getInt("id"));
                b.setName(rs.getNString("name"));
                b.setOrigin(rs.getNString("origin"));
                b.setLogoUrl(rs.getString("logo_url"));
                list.add(b);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Admin Methods
    public List<ProductListDto> getAdminProducts(String search, Integer categoryId, String status, int page, int pageSize) {
        List<ProductListDto> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT p.id, p.name, p.slug, p.short_description, p.is_featured, p.status, " +
            "b.id AS brand_id, b.name AS brand_name, b.origin AS brand_origin, b.logo_url AS brand_logo, " +
            "c.id AS category_id, c.name AS category_name, c.slug AS category_slug, " +
            "(SELECT MIN(v.price) FROM product_variants v WHERE v.product_id = p.id) AS price_from, " +
            "(SELECT MAX(v.price) FROM product_variants v WHERE v.product_id = p.id) AS price_to, " +
            "(SELECT TOP 1 v.thumbnail_url FROM product_variants v WHERE v.product_id = p.id ORDER BY v.display_order) AS thumbnail_url, " +
            "(SELECT TOP 1 v.id FROM product_variants v WHERE v.product_id = p.id ORDER BY v.display_order) AS default_variant_id " +
            "FROM products p " +
            "LEFT JOIN brands b ON p.brand_id = b.id " +
            "LEFT JOIN categories c ON p.category_id = c.id " +
            "WHERE 1=1"
        );

        List<Object> params = new ArrayList<>();

        if (search != null && !search.trim().isEmpty()) {
            sql.append(" AND (p.name LIKE ? OR p.description LIKE ?)");
            String pattern = "%" + search.trim() + "%";
            params.add(pattern);
            params.add(pattern);
        }

        if (categoryId != null) {
            sql.append(" AND p.category_id = ?");
            params.add(categoryId);
        }

        if (status != null && !"ALL".equalsIgnoreCase(status)) {
            sql.append(" AND p.status = ?");
            params.add(status);
        }

        sql.append(" ORDER BY p.created_at DESC");
        sql.append(" OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
        params.add((page - 1) * pageSize);
        params.add(pageSize);

        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapProductListDto(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public int getAdminProductsCount(String search, Integer categoryId, String status) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(1) FROM products p WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (search != null && !search.trim().isEmpty()) {
            sql.append(" AND (p.name LIKE ? OR p.description LIKE ?)");
            String pattern = "%" + search.trim() + "%";
            params.add(pattern);
            params.add(pattern);
        }

        if (categoryId != null) {
            sql.append(" AND p.category_id = ?");
            params.add(categoryId);
        }

        if (status != null && !"ALL".equalsIgnoreCase(status)) {
            sql.append(" AND p.status = ?");
            params.add(status);
        }

        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public ProductDetailDto getAdminProductDetail(int productId) {
        String sql = "SELECT p.id, p.name, p.slug, p.description, p.specifications, p.status, p.is_featured, " +
                     "b.id AS brand_id, b.name AS brand_name, b.origin AS brand_origin, b.logo_url AS brand_logo, " +
                     "c.id AS category_id, c.name AS category_name, c.slug AS category_slug " +
                     "FROM products p " +
                     "LEFT JOIN brands b ON p.brand_id = b.id " +
                     "LEFT JOIN categories c ON p.category_id = c.id " +
                     "WHERE p.id = ?";

        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ProductDetailDto dto = new ProductDetailDto();
                    dto.setId(rs.getInt("id"));
                    dto.setName(rs.getNString("name"));
                    dto.setSlug(rs.getString("slug"));
                    dto.setDescription(rs.getNString("description"));
                    dto.setSpecifications(rs.getNString("specifications"));
                    dto.setStatus(rs.getString("status"));
                    dto.setFeatured(rs.getBoolean("is_featured"));

                    int bId = rs.getInt("brand_id");
                    if (!rs.wasNull()) {
                        BrandDto b = new BrandDto();
                        b.setId(bId);
                        b.setName(rs.getNString("brand_name"));
                        b.setOrigin(rs.getNString("brand_origin"));
                        b.setLogoUrl(rs.getString("brand_logo"));
                        dto.setBrand(b);
                    }

                    CategoryDto c = new CategoryDto();
                    c.setId(rs.getInt("category_id"));
                    c.setName(rs.getNString("category_name"));
                    c.setSlug(rs.getString("category_slug"));
                    dto.setCategory(c);

                    // Load ALL variants (including inactive ones)
                    dto.setVariants(getProductVariants(conn, productId, false));
                    // Load images
                    dto.setImages(getProductImages(conn, productId));

                    return dto;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int insertProduct(ProductCreateDto dto) {
        String sql = "INSERT INTO products (name, slug, brand_id, category_id, short_description, description, specifications, status, is_featured, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setNString(1, dto.getName());
            ps.setString(2, CSharpMigrationHelper.toFriendlySlug(dto.getName()));
            if (dto.getBrandId() != null) {
                ps.setInt(3, dto.getBrandId());
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            ps.setInt(4, dto.getCategoryId());
            ps.setNString(5, dto.getShortDescription());
            ps.setNString(6, dto.getDescription());
            ps.setNString(7, dto.getSpecifications());
            ps.setString(8, dto.getStatus() != null ? dto.getStatus() : "DRAFT");
            ps.setBoolean(9, dto.isFeatured());
            ps.setTimestamp(10, Timestamp.valueOf(LocalDateTime.now()));
            ps.setTimestamp(11, Timestamp.valueOf(LocalDateTime.now()));

            ps.executeUpdate();
            try (ResultSet gk = ps.getGeneratedKeys()) {
                if (gk.next()) {
                    return gk.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public boolean updateProduct(int id, ProductEditDto dto) {
        String sql = "UPDATE products SET name = ?, slug = ?, brand_id = ?, category_id = ?, short_description = ?, description = ?, specifications = ?, status = ?, is_featured = ?, updated_at = ? WHERE id = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setNString(1, dto.getName());
            ps.setString(2, CSharpMigrationHelper.toFriendlySlug(dto.getName()));
            if (dto.getBrandId() != null) {
                ps.setInt(3, dto.getBrandId());
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            ps.setInt(4, dto.getCategoryId());
            ps.setNString(5, dto.getShortDescription());
            ps.setNString(6, dto.getDescription());
            ps.setNString(7, dto.getSpecifications());
            ps.setString(8, dto.getStatus());
            ps.setBoolean(9, dto.isFeatured());
            ps.setTimestamp(10, Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(11, id);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean toggleProductStatus(int id, String status) {
        String sql = "UPDATE products SET status = ?, updated_at = ? WHERE id = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(3, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean insertVariant(int productId, VariantCreateDto dto) {
        String sql = "INSERT INTO product_variants (product_id, sku, variant_name, price, original_price, thumbnail_url, display_order, is_active, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            ps.setString(2, dto.getSku());
            ps.setNString(3, dto.getVariantName());
            ps.setBigDecimal(4, dto.getPrice());
            ps.setBigDecimal(5, dto.getOriginalPrice());
            ps.setString(6, dto.getThumbnailUrl());
            ps.setInt(7, dto.getDisplayOrder());
            ps.setBoolean(8, dto.isActive());
            ps.setTimestamp(9, Timestamp.valueOf(LocalDateTime.now()));
            ps.setTimestamp(10, Timestamp.valueOf(LocalDateTime.now()));

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateVariant(int variantId, VariantEditDto dto) {
        String sql = "UPDATE product_variants SET variant_name = ?, sku = ?, price = ?, original_price = ?, thumbnail_url = COALESCE(?, thumbnail_url), is_active = ?, display_order = ?, updated_at = ? WHERE id = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setNString(1, dto.getVariantName());
            ps.setString(2, dto.getSku());
            ps.setBigDecimal(3, dto.getPrice());
            ps.setBigDecimal(4, dto.getOriginalPrice());
            ps.setString(5, dto.getThumbnailUrl());
            ps.setBoolean(6, dto.isActive());
            ps.setInt(7, dto.getDisplayOrder());
            ps.setTimestamp(8, Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(9, variantId);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean addProductImage(int variantId, String imageUrl, String altText, int sortOrder) {
        String sql = "INSERT INTO product_images (variant_id, image_url, alt_text, sort_order, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, variantId);
            ps.setString(2, imageUrl);
            ps.setNString(3, altText);
            ps.setInt(4, sortOrder);
            ps.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            ps.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteProductImage(int imageId) {
        String sql = "DELETE FROM product_images WHERE id = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, imageId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Private helper query maps
    private ProductListDto mapProductListDto(ResultSet rs) throws SQLException {
        ProductListDto dto = new ProductListDto();
        dto.setId(rs.getInt("id"));
        dto.setName(rs.getNString("name"));
        dto.setSlug(rs.getString("slug"));
        dto.setShortDescription(rs.getNString("short_description"));
        dto.setFeatured(rs.getBoolean("is_featured"));
        dto.setStatus(rs.getString("status"));
        dto.setActive("ACTIVE".equals(rs.getString("status")));

        // Map Brand
        int brandId = rs.getInt("brand_id");
        if (!rs.wasNull()) {
            BrandDto b = new BrandDto();
            b.setId(brandId);
            b.setName(rs.getNString("brand_name"));
            b.setOrigin(rs.getNString("brand_origin"));
            b.setLogoUrl(rs.getString("brand_logo"));
            dto.setBrand(b);
        }

        // Map Category
        CategoryDto c = new CategoryDto();
        c.setId(rs.getInt("category_id"));
        c.setName(rs.getNString("category_name"));
        c.setSlug(rs.getString("category_slug"));
        dto.setCategory(c);

        dto.setPriceFrom(rs.getBigDecimal("price_from"));
        dto.setPriceTo(rs.getBigDecimal("price_to"));
        dto.setThumbnailUrl(rs.getString("thumbnail_url"));
        dto.setDefaultVariantId(rs.getInt("default_variant_id"));

        dto.setRatingAverage(0.0);
        dto.setReviewCount(0);

        return dto;
    }

    private List<ProductVariantDto> getProductVariants(Connection conn, int productId, boolean activeOnly) throws SQLException {
        List<ProductVariantDto> list = new ArrayList<>();
        String sql = "SELECT v.id, v.sku, v.variant_name, v.price, v.original_price, v.thumbnail_url, v.is_active, " +
                     "  (SELECT ISNULL(SUM(i.quantity_on_hand - i.quantity_reserved), 0) FROM inventories i WHERE i.variant_id = v.id) AS qty_available " +
                     "FROM product_variants v WHERE v.product_id = ?";
        if (activeOnly) {
            sql += " AND v.is_active = 1";
        }
        sql += " ORDER BY v.display_order";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ProductVariantDto v = new ProductVariantDto();
                    v.setId(rs.getInt("id"));
                    v.setSku(rs.getString("sku"));
                    v.setVariantName(rs.getNString("variant_name"));
                    v.setPrice(rs.getBigDecimal("price"));
                    v.setOriginalPrice(rs.getBigDecimal("original_price"));
                    v.setThumbnailUrl(rs.getString("thumbnail_url"));
                    v.setQuantityAvailable(rs.getInt("qty_available"));
                    v.setActive(rs.getBoolean("is_active"));
                    list.add(v);
                }
            }
        }
        return list;
    }

    private List<ProductImageDto> getProductImages(Connection conn, int productId) throws SQLException {
        List<ProductImageDto> list = new ArrayList<>();
        String sql = "SELECT img.id, img.image_url, img.alt_text, img.sort_order " +
                     "FROM product_images img " +
                     "JOIN product_variants v ON img.variant_id = v.id " +
                     "WHERE v.product_id = ? " +
                     "ORDER BY img.sort_order";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ProductImageDto img = new ProductImageDto();
                    img.setId(rs.getInt("id"));
                    img.setImageUrl(rs.getString("image_url"));
                    img.setAltText(rs.getNString("alt_text"));
                    img.setSortOrder(rs.getInt("sort_order"));
                    list.add(img);
                }
            }
        }
        return list;
    }

    // Helper static class to handle C# Slug logic migration
    public static class CSharpMigrationHelper {
        public static String toFriendlySlug(String title) {
            if (title == null || title.isEmpty()) return "";
            title = title.toLowerCase().trim();

            String[] arr1 = new String[] { "á", "à", "ả", "ã", "ạ", "â", "ấ", "ầ", "ẩ", "ẫ", "ậ", "ă", "ắ", "ằ", "ẳ", "ẵ", "ặ",
                "đ",
                "é", "è", "ẻ", "ẽ", "ẹ", "ê", "ế", "ề", "ể", "ễ", "ệ",
                "í", "ì", "ỉ", "ĩ", "ị",
                "ó", "ò", "ỏ", "õ", "ọ", "ô", "ố", "ồ", "ổ", "ỗ", "ộ", "ơ", "ớ", "ờ", "ở", "ỡ", "ợ",
                "ú", "ù", "ủ", "ũ", "ụ", "ư", "ứ", "ừ", "ử", "ữ", "ự",
                "ý", "ỳ", "ỷ", "ỹ", "ỵ" };
            String[] arr2 = new String[] { "a", "a", "a", "a", "a", "a", "a", "a", "a", "a", "a", "a", "a", "a", "a", "a", "a",
                "d",
                "e", "e", "e", "e", "e", "e", "e", "e", "e", "e", "e",
                "i", "i", "i", "i", "i",
                "o", "o", "o", "o", "o", "o", "o", "o", "o", "o", "o", "o", "o", "o", "o", "o", "o",
                "u", "u", "u", "u", "u", "u", "u", "u", "u", "u", "u",
                "y", "y", "y", "y", "y" };
            for (int i = 0; i < arr1.length; i++) {
                title = title.replace(arr1[i], arr2[i]);
            }

            title = title.replaceAll("[^a-z0-9\\s-]", "");
            title = title.replaceAll("\\s+", "-");
            title = title.replaceAll("-+", "-");
            return title.replaceAll("^-|-$", "");
        }
    }
}
