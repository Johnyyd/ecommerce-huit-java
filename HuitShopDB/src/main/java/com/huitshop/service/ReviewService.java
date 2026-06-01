package com.huitshop.service;

import com.huitshop.dao.ReviewDao;
import com.huitshop.model.Review;

import java.util.List;

public class ReviewService {
    private final ReviewDao reviewDao = new ReviewDao();

    public List<Review> getReviewsByProductId(int productId) {
        return reviewDao.getReviewsByProductId(productId);
    }

    public void addReview(Review review) {
        // Automatically approve for local execution convenience
        review.setApproved(true);
        reviewDao.insertReview(review);
    }
}
