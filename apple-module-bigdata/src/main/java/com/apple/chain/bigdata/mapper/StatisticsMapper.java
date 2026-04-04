package com.apple.chain.bigdata.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Statistics mapper.
 * All queries use real SQL aggregation — COUNT(*), SUM, DATE_FORMAT + GROUP BY, LIMIT.
 * Tables: farm_orchard, trace_batch, trade_order, uc_user, farm_farmer
 */
@Mapper
public interface StatisticsMapper {

    // ===== Summary counts =====

    @Select("SELECT COUNT(*) FROM farm_orchard WHERE deleted = 0")
    long countTotalOrchards();

    @Select("SELECT COUNT(*) FROM farm_orchard WHERE deleted = 0 AND status = 'ACTIVE'")
    long countActiveOrchards();

    @Select("SELECT COUNT(*) FROM trace_batch WHERE deleted = 0")
    long countTotalBatches();

    @Select("SELECT COUNT(*) FROM trade_order WHERE deleted = 0")
    long countTotalOrders();

    @Select("SELECT COUNT(*) FROM uc_user WHERE deleted = 0")
    long countTotalUsers();

    @Select("SELECT COUNT(*) FROM trade_order WHERE deleted = 0 AND status = 'COMPLETED'")
    long countCompletedOrders();

    @Select("SELECT COALESCE(SUM(total_amount), 0) FROM trade_order WHERE deleted = 0 AND status = 'COMPLETED'")
    BigDecimal sumCompletedTradeAmount();

    // ===== Pending counts =====

    @Select("SELECT COUNT(*) FROM trade_order WHERE deleted = 0 AND status = 'PENDING'")
    long countPendingOrders();

    @Select("SELECT COUNT(*) FROM trade_order WHERE deleted = 0 AND payment_status = 'UNPAID' AND status NOT IN ('CANCELLED')")
    long countUnpaidOrders();

    @Select("SELECT COUNT(*) FROM trace_batch WHERE deleted = 0 AND status = 'CREATED'")
    long countDraftBatches();

    @Select("SELECT COUNT(*) FROM farm_farmer WHERE deleted = 0 AND status = 'INACTIVE'")
    long countInactiveFarmers();

    // ===== Monthly trend: last 6 months, GROUP BY month =====

    @Select("SELECT DATE_FORMAT(create_time, '%Y-%m') AS month, " +
            "COUNT(*) AS orderCount, " +
            "COALESCE(SUM(total_amount), 0) AS totalAmount, " +
            "COALESCE(SUM(quantity), 0) AS totalQuantity " +
            "FROM trade_order " +
            "WHERE deleted = 0 " +
            "AND status IN ('COMPLETED', 'SHIPPED', 'CONFIRMED') " +
            "AND create_time >= DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 5 MONTH), '%Y-%m-01') " +
            "GROUP BY DATE_FORMAT(create_time, '%Y-%m') " +
            "ORDER BY month ASC")
    List<Map<String, Object>> monthlyOrderTrendLast6Months();

    // ===== Top 5 varieties by order count =====

    @Select("SELECT variety, " +
            "COUNT(*) AS orderCount, " +
            "COALESCE(SUM(total_amount), 0) AS totalAmount, " +
            "COALESCE(SUM(quantity), 0) AS totalQuantity " +
            "FROM trade_order " +
            "WHERE deleted = 0 " +
            "AND variety IS NOT NULL " +
            "AND variety != '' " +
            "GROUP BY variety " +
            "ORDER BY COUNT(*) DESC " +
            "LIMIT 5")
    List<Map<String, Object>> topVarietiesByOrderCount();
}
