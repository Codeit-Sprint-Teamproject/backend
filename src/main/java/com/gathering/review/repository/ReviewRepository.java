package com.gathering.review.repository;

import com.gathering.gathering.model.entity.SearchType;
import com.gathering.review.model.constant.BookReviewTagType;
import com.gathering.review.model.constant.ReviewType;
import com.gathering.review.model.dto.*;
import org.springframework.data.domain.Pageable;

public interface ReviewRepository {
    ReviewDto createReview(CreateReviewDto createReviewDto, String username, ReviewType type);

    ReviewCommentDto createReviewComment(CreateReviewCommentDto createReviewCommentDto, String username);

    ReviewListDto selectUserReviewList(String username, ReviewType type, Pageable pageable);

    ReviewListDto selectBookReviewList(String username);

    ReviewListDto findReviews(BookReviewTagType tag, Pageable pageable, String username);

    ReviewDto selectBookReviewDetail(long reviewId, String username);

    ReviewListDto searchReviews(SearchType type, String searchParam, Pageable pageable, String username);

    int DeleteReview(long reviewId,ReviewType type, String username);

    void UpdateReview(CreateReviewDto createReviewDto, long reviewId, ReviewType type, String username);

    void UpdateReviewLike(ReviewLikeDto reviewLikeDto, String username);
}
