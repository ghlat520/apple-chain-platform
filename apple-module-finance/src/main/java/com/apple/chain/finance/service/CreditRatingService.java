package com.apple.chain.finance.service;

import com.apple.chain.finance.entity.CreditRating;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletResponse;

public interface CreditRatingService extends IService<CreditRating> {
    IPage<CreditRating> listRatings(int page, int size, String keyword, String entityType, String creditLevel);
    CreditRating getRatingDetail(Long id);
    CreditRating createRating(CreditRating rating);
    CreditRating updateRating(Long id, CreditRating rating);
    void deleteRating(Long id);
    void exportRatings(String keyword, String entityType, String creditLevel, HttpServletResponse response);
}
