package com.huitshop.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ProductDtos {

    public static class BrandDto {
        private int id;
        private String name;
        private String origin;
        private String logoUrl;

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getOrigin() { return origin; }
        public void setOrigin(String origin) { this.origin = origin; }
        public String getLogoUrl() { return logoUrl; }
        public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
    }

    public static class CategoryDto {
        private int id;
        private Integer parentId;
        private String name;
        private String slug;
        private String description;
        private List<CategoryDto> children = new ArrayList<>();

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public Integer getParentId() { return parentId; }
        public void setParentId(Integer parentId) { this.parentId = parentId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getSlug() { return slug; }
        public void setSlug(String slug) { this.slug = slug; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public List<CategoryDto> getChildren() { return children; }
        public void setChildren(List<CategoryDto> children) { this.children = children; }
    }

    public static class ProductVariantDto {
        private int id;
        private String sku;
        private String variantName;
        private BigDecimal price;
        private BigDecimal originalPrice;
        private String thumbnailUrl;
        private int quantityAvailable;
        private boolean isActive;

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public String getSku() { return sku; }
        public void setSku(String sku) { this.sku = sku; }
        public String getVariantName() { return variantName; }
        public void setVariantName(String variantName) { this.variantName = variantName; }
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        public BigDecimal getOriginalPrice() { return originalPrice; }
        public void setOriginalPrice(BigDecimal originalPrice) { this.originalPrice = originalPrice; }
        public String getThumbnailUrl() { return thumbnailUrl; }
        public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }
        public int getQuantityAvailable() { return quantityAvailable; }
        public void setQuantityAvailable(int quantityAvailable) { this.quantityAvailable = quantityAvailable; }
        public boolean isActive() { return isActive; }
        public void setActive(boolean active) { isActive = active; }
    }

    public static class ProductImageDto {
        private int id;
        private String imageUrl;
        private String altText;
        private int sortOrder;

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
        public String getAltText() { return altText; }
        public void setAltText(String altText) { this.altText = altText; }
        public int getSortOrder() { return sortOrder; }
        public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }
    }

    public static class ProductListDto {
        private int id;
        private String name;
        private String slug;
        private String shortDescription;
        private boolean isFeatured;
        private BrandDto brand;
        private CategoryDto category;
        private BigDecimal priceFrom;
        private BigDecimal priceTo;
        private String thumbnailUrl;
        private int defaultVariantId;
        private double ratingAverage;
        private int reviewCount;
        private String status;
        private boolean isActive;

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getSlug() { return slug; }
        public void setSlug(String slug) { this.slug = slug; }
        public String getShortDescription() { return shortDescription; }
        public void setShortDescription(String shortDescription) { this.shortDescription = shortDescription; }
        public boolean isFeatured() { return isFeatured; }
        public void setFeatured(boolean featured) { isFeatured = featured; }
        public BrandDto getBrand() { return brand; }
        public void setBrand(BrandDto brand) { this.brand = brand; }
        public CategoryDto getCategory() { return category; }
        public void setCategory(CategoryDto category) { this.category = category; }
        public BigDecimal getPriceFrom() { return priceFrom; }
        public void setPriceFrom(BigDecimal priceFrom) { this.priceFrom = priceFrom; }
        public BigDecimal getPriceTo() { return priceTo; }
        public void setPriceTo(BigDecimal priceTo) { this.priceTo = priceTo; }
        public String getThumbnailUrl() { return thumbnailUrl; }
        public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }
        public int getDefaultVariantId() { return defaultVariantId; }
        public void setDefaultVariantId(int defaultVariantId) { this.defaultVariantId = defaultVariantId; }
        public double getRatingAverage() { return ratingAverage; }
        public void setRatingAverage(double ratingAverage) { this.ratingAverage = ratingAverage; }
        public int getReviewCount() { return reviewCount; }
        public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public boolean isActive() { return isActive; }
        public void setActive(boolean active) { isActive = active; }
    }

    public static class ProductDetailDto {
        private int id;
        private String name;
        private String slug;
        private String description;
        private String specifications; // JSON
        private BrandDto brand;
        private CategoryDto category;
        private List<ProductVariantDto> variants = new ArrayList<>();
        private List<ProductImageDto> images = new ArrayList<>();
        private double ratingAverage;
        private int reviewCount;
        private String status;
        private boolean isFeatured;

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getSlug() { return slug; }
        public void setSlug(String slug) { this.slug = slug; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getSpecifications() { return specifications; }
        public void setSpecifications(String specifications) { this.specifications = specifications; }
        public BrandDto getBrand() { return brand; }
        public void setBrand(BrandDto brand) { this.brand = brand; }
        public CategoryDto getCategory() { return category; }
        public void setCategory(CategoryDto category) { this.category = category; }
        public List<ProductVariantDto> getVariants() { return variants; }
        public void setVariants(List<ProductVariantDto> variants) { this.variants = variants; }
        public List<ProductImageDto> getImages() { return images; }
        public void setImages(List<ProductImageDto> images) { this.images = images; }
        public double getRatingAverage() { return ratingAverage; }
        public void setRatingAverage(double ratingAverage) { this.ratingAverage = ratingAverage; }
        public int getReviewCount() { return reviewCount; }
        public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public boolean isFeatured() { return isFeatured; }
        public void setFeatured(boolean featured) { isFeatured = featured; }
    }

    public static class ProductQueryParams {
        private int page = 1;
        private int pageSize = 12;
        private Integer categoryId;
        private Integer brandId;
        private BigDecimal minPrice;
        private BigDecimal maxPrice;
        private String search;
        private boolean inStockOnly = false;
        private String sortBy; // price_asc, price_desc, name, date

        public int getPage() { return page; }
        public void setPage(int page) { this.page = page; }
        public int getPageSize() { return pageSize; }
        public void setPageSize(int pageSize) { this.pageSize = pageSize; }
        public Integer getCategoryId() { return categoryId; }
        public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }
        public Integer getBrandId() { return brandId; }
        public void setBrandId(Integer brandId) { this.brandId = brandId; }
        public BigDecimal getMinPrice() { return minPrice; }
        public void setMinPrice(BigDecimal minPrice) { this.minPrice = minPrice; }
        public BigDecimal getMaxPrice() { return maxPrice; }
        public void setMaxPrice(BigDecimal maxPrice) { this.maxPrice = maxPrice; }
        public String getSearch() { return search; }
        public void setSearch(String search) { this.search = search; }
        public boolean isInStockOnly() { return inStockOnly; }
        public void setInStockOnly(boolean inStockOnly) { this.inStockOnly = inStockOnly; }
        public String getSortBy() { return sortBy; }
        public void setSortBy(String sortBy) { this.sortBy = sortBy; }
    }

    public static class ProductCreateDto {
        private String name;
        private int categoryId;
        private Integer brandId;
        private String shortDescription;
        private String description;
        private String specifications;
        private String status;
        private boolean isFeatured;
        private String defaultVariantName;
        private String defaultSku;
        private BigDecimal defaultPrice;
        private BigDecimal defaultOriginalPrice;
        private String defaultThumbnailUrl;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getCategoryId() { return categoryId; }
        public void setCategoryId(int categoryId) { this.categoryId = categoryId; }
        public Integer getBrandId() { return brandId; }
        public void setBrandId(Integer brandId) { this.brandId = brandId; }
        public String getShortDescription() { return shortDescription; }
        public void setShortDescription(String shortDescription) { this.shortDescription = shortDescription; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getSpecifications() { return specifications; }
        public void setSpecifications(String specifications) { this.specifications = specifications; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public boolean isFeatured() { return isFeatured; }
        public void setFeatured(boolean featured) { isFeatured = featured; }
        public String getDefaultVariantName() { return defaultVariantName; }
        public void setDefaultVariantName(String defaultVariantName) { this.defaultVariantName = defaultVariantName; }
        public String getDefaultSku() { return defaultSku; }
        public void setDefaultSku(String defaultSku) { this.defaultSku = defaultSku; }
        public BigDecimal getDefaultPrice() { return defaultPrice; }
        public void setDefaultPrice(BigDecimal defaultPrice) { this.defaultPrice = defaultPrice; }
        public BigDecimal getDefaultOriginalPrice() { return defaultOriginalPrice; }
        public void setDefaultOriginalPrice(BigDecimal defaultOriginalPrice) { this.defaultOriginalPrice = defaultOriginalPrice; }
        public String getDefaultThumbnailUrl() { return defaultThumbnailUrl; }
        public void setDefaultThumbnailUrl(String defaultThumbnailUrl) { this.defaultThumbnailUrl = defaultThumbnailUrl; }
    }

    public static class ProductEditDto {
        private String name;
        private int categoryId;
        private Integer brandId;
        private String shortDescription;
        private String description;
        private String specifications;
        private String status;
        private boolean isFeatured;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getCategoryId() { return categoryId; }
        public void setCategoryId(int categoryId) { this.categoryId = categoryId; }
        public Integer getBrandId() { return brandId; }
        public void setBrandId(Integer brandId) { this.brandId = brandId; }
        public String getShortDescription() { return shortDescription; }
        public void setShortDescription(String shortDescription) { this.shortDescription = shortDescription; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getSpecifications() { return specifications; }
        public void setSpecifications(String specifications) { this.specifications = specifications; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public boolean isFeatured() { return isFeatured; }
        public void setFeatured(boolean featured) { isFeatured = featured; }
    }

    public static class VariantCreateDto {
        private String variantName;
        private String sku;
        private BigDecimal price;
        private BigDecimal originalPrice;
        private String thumbnailUrl;
        private boolean isActive = true;
        private int displayOrder;

        public String getVariantName() { return variantName; }
        public void setVariantName(String variantName) { this.variantName = variantName; }
        public String getSku() { return sku; }
        public void setSku(String sku) { this.sku = sku; }
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        public BigDecimal getOriginalPrice() { return originalPrice; }
        public void setOriginalPrice(BigDecimal originalPrice) { this.originalPrice = originalPrice; }
        public String getThumbnailUrl() { return thumbnailUrl; }
        public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }
        public boolean isActive() { return isActive; }
        public void setActive(boolean active) { isActive = active; }
        public int getDisplayOrder() { return displayOrder; }
        public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }
    }

    public static class VariantEditDto {
        private String variantName;
        private String sku;
        private BigDecimal price;
        private BigDecimal originalPrice;
        private String thumbnailUrl;
        private boolean isActive;
        private int displayOrder;

        public String getVariantName() { return variantName; }
        public void setVariantName(String variantName) { this.variantName = variantName; }
        public String getSku() { return sku; }
        public void setSku(String sku) { this.sku = sku; }
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        public BigDecimal getOriginalPrice() { return originalPrice; }
        public void setOriginalPrice(BigDecimal originalPrice) { this.originalPrice = originalPrice; }
        public String getThumbnailUrl() { return thumbnailUrl; }
        public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }
        public boolean isActive() { return isActive; }
        public void setActive(boolean active) { isActive = active; }
        public int getDisplayOrder() { return displayOrder; }
        public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }
    }
}
