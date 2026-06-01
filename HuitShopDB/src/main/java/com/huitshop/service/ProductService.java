package com.huitshop.service;

import com.huitshop.dao.ProductDao;
import com.huitshop.dto.ProductDtos.*;

import java.util.List;

public class ProductService {
    private final ProductDao productDao = new ProductDao();

    public List<ProductListDto> getProducts(ProductQueryParams query) {
        return productDao.getProducts(query);
    }

    public int getProductsCount(ProductQueryParams query) {
        return productDao.getProductsCount(query);
    }

    public ProductDetailDto getProductDetail(int productId) {
        return productDao.getProductDetail(productId);
    }

    public List<CategoryDto> getCategories() {
        return productDao.getCategories();
    }

    public List<BrandDto> getBrands() {
        return productDao.getBrands();
    }

    public List<ProductListDto> getAdminProducts(String search, Integer categoryId, String status, int page, int pageSize) {
        return productDao.getAdminProducts(search, categoryId, status, page, pageSize);
    }

    public int getAdminProductsCount(String search, Integer categoryId, String status) {
        return productDao.getAdminProductsCount(search, categoryId, status);
    }

    public ProductDetailDto getAdminProductDetail(int productId) {
        return productDao.getAdminProductDetail(productId);
    }

    public int createProduct(ProductCreateDto dto) {
        return productDao.insertProduct(dto);
    }

    public boolean updateProduct(int id, ProductEditDto dto) {
        return productDao.updateProduct(id, dto);
    }

    public boolean toggleProductStatus(int id, String status) {
        return productDao.toggleProductStatus(id, status);
    }

    public boolean createVariant(int productId, VariantCreateDto dto) {
        return productDao.insertVariant(productId, dto);
    }

    public boolean updateVariant(int variantId, VariantEditDto dto) {
        return productDao.updateVariant(variantId, dto);
    }

    public boolean addProductImage(int variantId, String imageUrl, String altText, int sortOrder) {
        return productDao.addProductImage(variantId, imageUrl, altText, sortOrder);
    }

    public boolean deleteProductImage(int imageId) {
        return productDao.deleteProductImage(imageId);
    }
}
