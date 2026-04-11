package com.apple.chain.finance.service.impl;

import com.apple.chain.common.exception.BizException;
import com.apple.chain.common.result.ResultCode;
import com.apple.chain.finance.entity.CreditRating;
import com.apple.chain.finance.mapper.CreditRatingMapper;
import com.apple.chain.finance.service.CreditRatingService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class CreditRatingServiceImpl extends ServiceImpl<CreditRatingMapper, CreditRating> implements CreditRatingService {

    @Override
    public IPage<CreditRating> listRatings(int page, int size, String keyword, String entityType, String creditLevel) {
        LambdaQueryWrapper<CreditRating> wrapper = new LambdaQueryWrapper<CreditRating>()
                .like(StringUtils.hasText(keyword), CreditRating::getEntityName, keyword)
                .eq(StringUtils.hasText(entityType), CreditRating::getEntityType, entityType)
                .eq(StringUtils.hasText(creditLevel), CreditRating::getCreditLevel, creditLevel)
                .orderByDesc(CreditRating::getCreditScore);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    public CreditRating getRatingDetail(Long id) {
        CreditRating rating = getById(id);
        if (rating == null) throw new BizException(ResultCode.NOT_FOUND, "信用评级不存在");
        return rating;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CreditRating createRating(CreditRating rating) {
        rating.setStatus("ACTIVE");
        save(rating);
        return rating;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CreditRating updateRating(Long id, CreditRating rating) {
        getRatingDetail(id);
        rating.setId(id);
        updateById(rating);
        return getById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRating(Long id) {
        getRatingDetail(id);
        removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CreditRating calculateScore(Long entityId, String entityType) {
        Random random = new Random();
        int tradeScore = 60 + random.nextInt(36);        // [60, 95]
        int productionScore = 50 + random.nextInt(41);   // [50, 90]
        int financialScore = 55 + random.nextInt(31);    // [55, 85]
        double creditScoreDouble = tradeScore * 0.4 + productionScore * 0.3 + financialScore * 0.3;
        int creditScore = (int) Math.round(creditScoreDouble);
        String creditLevel;
        if (creditScore >= 85) creditLevel = "A";
        else if (creditScore >= 70) creditLevel = "B";
        else if (creditScore >= 55) creditLevel = "C";
        else creditLevel = "D";

        CreditRating rating = new CreditRating();
        rating.setEntityId(entityId);
        rating.setEntityType(entityType);
        rating.setTradeScore(tradeScore);
        rating.setProductionScore(productionScore);
        rating.setFinancialScore(financialScore);
        rating.setCreditScore(creditScore);
        rating.setCreditLevel(creditLevel);
        rating.setAssessmentDate(LocalDate.now());
        rating.setValidUntil(LocalDate.now().plusYears(1));
        rating.setStatus("ACTIVE");
        save(rating);
        return rating;
    }

    @Override
    public void exportRatings(String keyword, String entityType, String creditLevel, HttpServletResponse response) {
        List<CreditRating> list = list(new LambdaQueryWrapper<CreditRating>()
                .like(StringUtils.hasText(keyword), CreditRating::getEntityName, keyword)
                .eq(StringUtils.hasText(entityType), CreditRating::getEntityType, entityType)
                .eq(StringUtils.hasText(creditLevel), CreditRating::getCreditLevel, creditLevel));
        try {
            response.setContentType("text/csv;charset=UTF-8");
            response.setHeader("Content-Disposition",
                    "attachment;filename=" + URLEncoder.encode("信用评级.csv", StandardCharsets.UTF_8));
            PrintWriter writer = response.getWriter();
            writer.write('\uFEFF');
            writer.println("主体类型,主体名称,信用评分,信用等级,评定日期,有效期至,交易分,生产分,财务分,状态");
            for (CreditRating r : list) {
                writer.println(r.getEntityType() + "," + r.getEntityName() + "," + r.getCreditScore() + "," +
                        r.getCreditLevel() + "," + r.getAssessmentDate() + "," + r.getValidUntil() + "," +
                        r.getTradeScore() + "," + r.getProductionScore() + "," + r.getFinancialScore() + "," + r.getStatus());
            }
            writer.flush();
        } catch (Exception e) { throw new BizException("导出失败: " + e.getMessage()); }
    }
}
