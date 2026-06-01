package com.huitshop.model;

import java.time.LocalDateTime;

public class Review {
    private int id;
    private int userId;
    private int productId;
    private Integer variantId;
    private int rating; // 1 to 5
    private String title;
    private String content;
    private boolean isVerifiedPurchase;
    private boolean isApproved;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Helper references
    private String userName; // UI Display
    private String variantName; // UI Display

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }
    public Integer getVariantId() { return variantId; }
    public void setVariantId(Integer variantId) { this.variantId = variantId; }
    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public boolean isVerifiedPurchase() { return isVerifiedPurchase; }
    public void setVerifiedPurchase(boolean verifiedPurchase) { isVerifiedPurchase = verifiedPurchase; }
    public boolean isApproved() { return isApproved; }
    public void setApproved(boolean approved) { isApproved = approved; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getVariantName() { return variantName; }
    public void setVariantName(String variantName) { this.variantName = variantName; }
}
