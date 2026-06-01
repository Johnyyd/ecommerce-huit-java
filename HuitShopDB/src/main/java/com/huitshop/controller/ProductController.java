package com.huitshop.controller;

import com.huitshop.dto.ProductDtos.*;
import com.huitshop.model.Review;
import com.huitshop.service.ProductService;
import com.huitshop.service.ReviewService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService = new ProductService();
    private final ReviewService reviewService = new ReviewService();

    @GetMapping
    public ResponseEntity<?> getProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int pageSize,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) Integer brandId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "false") boolean inStockOnly,
            @RequestParam(required = false) String sortBy
    ) {
        ProductQueryParams params = new ProductQueryParams();
        params.setPage(page);
        params.setPageSize(pageSize);
        params.setCategoryId(categoryId);
        params.setBrandId(brandId);
        params.setMinPrice(minPrice);
        params.setMaxPrice(maxPrice);
        params.setSearch(search);
        params.setInStockOnly(inStockOnly);
        params.setSortBy(sortBy);

        List<ProductListDto> products = productService.getProducts(params);
        int totalItems = productService.getProductsCount(params);

        Map<String, Object> response = new HashMap<>();
        response.put("items", products);
        response.put("totalItems", totalItems);
        response.put("page", page);
        response.put("pageSize", pageSize);
        response.put("totalPages", (int) Math.ceil((double) totalItems / pageSize));

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProductDetail(@PathVariable int id) {
        ProductDetailDto dto = productService.getProductDetail(id);
        if (dto == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Sản phẩm không tồn tại");
        }
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/categories")
    public ResponseEntity<List<CategoryDto>> getCategories() {
        return ResponseEntity.ok(productService.getCategories());
    }

    @GetMapping("/brands")
    public ResponseEntity<List<BrandDto>> getBrands() {
        return ResponseEntity.ok(productService.getBrands());
    }

    @GetMapping("/{id}/reviews")
    public ResponseEntity<List<Review>> getReviews(@PathVariable int id) {
        return ResponseEntity.ok(reviewService.getReviewsByProductId(id));
    }

    @PostMapping("/{id}/reviews")
    public ResponseEntity<?> addReview(@PathVariable int id, @RequestBody Review review) {
        review.setProductId(id);
        reviewService.addReview(review);
        return ResponseEntity.status(HttpStatus.CREATED).body(review);
    }

    // Admin endpoints
    @GetMapping("/admin")
    public ResponseEntity<?> getAdminProducts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        List<ProductListDto> products = productService.getAdminProducts(search, categoryId, status, page, pageSize);
        int totalItems = productService.getAdminProductsCount(search, categoryId, status);

        Map<String, Object> response = new HashMap<>();
        response.put("items", products);
        response.put("totalItems", totalItems);
        response.put("page", page);
        response.put("pageSize", pageSize);
        response.put("totalPages", (int) Math.ceil((double) totalItems / pageSize));

        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin/{id}")
    public ResponseEntity<?> getAdminProductDetail(@PathVariable int id) {
        ProductDetailDto dto = productService.getAdminProductDetail(id);
        if (dto == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Sản phẩm không tồn tại");
        }
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    public ResponseEntity<?> createProduct(@RequestBody ProductCreateDto dto) {
        try {
            int id = productService.createProduct(dto);
            Map<String, Object> resp = new HashMap<>();
            resp.put("id", id);
            resp.put("message", "Sản phẩm được tạo thành công");
            return ResponseEntity.status(HttpStatus.CREATED).body(resp);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable int id, @RequestBody ProductEditDto dto) {
        boolean updated = productService.updateProduct(id, dto);
        if (!updated) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy sản phẩm");
        }
        return ResponseEntity.ok("Sản phẩm đã được cập nhật");
    }

    @PutMapping("/{id}/toggle-status")
    public ResponseEntity<?> toggleProductStatus(@PathVariable int id, @RequestParam String status) {
        boolean updated = productService.toggleProductStatus(id, status);
        if (!updated) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy sản phẩm");
        }
        return ResponseEntity.ok("Trạng thái sản phẩm đã được thay đổi");
    }

    @PostMapping("/{id}/variants")
    public ResponseEntity<?> createVariant(@PathVariable int id, @RequestBody VariantCreateDto dto) {
        boolean created = productService.createVariant(id, dto);
        if (!created) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Tạo phiên bản sản phẩm thất bại");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body("Tạo phiên bản sản phẩm thành công");
    }

    @PutMapping("/variants/{variantId}")
    public ResponseEntity<?> updateVariant(@PathVariable int variantId, @RequestBody VariantEditDto dto) {
        boolean updated = productService.updateVariant(variantId, dto);
        if (!updated) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Phiên bản sản phẩm không tồn tại");
        }
        return ResponseEntity.ok("Phiên bản sản phẩm đã được cập nhật");
    }

    @PostMapping("/variants/{variantId}/images")
    public ResponseEntity<?> addProductImage(
            @PathVariable int variantId,
            @RequestParam String imageUrl,
            @RequestParam(defaultValue = "") String altText,
            @RequestParam(defaultValue = "0") int sortOrder
    ) {
        boolean added = productService.addProductImage(variantId, imageUrl, altText, sortOrder);
        if (!added) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Thêm ảnh sản phẩm thất bại");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body("Thêm ảnh sản phẩm thành công");
    }

    @DeleteMapping("/images/{imageId}")
    public ResponseEntity<?> deleteProductImage(@PathVariable int imageId) {
        boolean deleted = productService.deleteProductImage(imageId);
        if (!deleted) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Ảnh không tồn tại");
        }
        return ResponseEntity.ok("Xóa ảnh sản phẩm thành công");
    }
}
