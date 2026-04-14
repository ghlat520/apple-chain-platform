package com.apple.chain.bigdata.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * MVP Dashboard mapper.
 * All queries use real SQL aggregations on MVP tables:
 * farm_orchard, trace_batch, trade_order
 */
@Mapper
public interface DashboardMvpMapper {

    // ===== KPI Stats =====

    @Select("SELECT COUNT(*) FROM farm_orchard WHERE deleted = 0 AND status = 'ACTIVE'")
    long countActiveOrchards();

    @Select("SELECT COUNT(*) FROM trace_batch WHERE deleted = 0")
    long countTotalBatches();

    @Select("SELECT COALESCE(SUM(total_amount), 0) FROM trade_order WHERE deleted = 0 AND status = 'COMPLETED'")
    BigDecimal sumCompletedTradeAmount();

    @Select("SELECT COUNT(*) FROM trade_order WHERE deleted = 0 AND status = 'PENDING'")
    long countPendingOrders();

    // ===== Trend: monthly trade volume last 6 months =====

    @Select("SELECT DATE_FORMAT(create_time, '%Y-%m') AS ym, " +
            "COUNT(*) AS orderCount, " +
            "COALESCE(SUM(total_amount), 0) AS totalAmount, " +
            "COALESCE(SUM(quantity), 0) AS totalQuantity " +
            "FROM trade_order " +
            "WHERE deleted = 0 " +
            "AND status IN ('COMPLETED', 'SHIPPED', 'CONFIRMED') " +
            "AND create_time >= DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 5 MONTH) " +
            "GROUP BY DATE_FORMAT(create_time, '%Y-%m') " +
            "ORDER BY ym ASC")
    List<Map<String, Object>> tradeTrendLast6Months();

    // ===== Top 5 orchards by trade volume =====

    @Select("SELECT o.id AS orchardId, " +
            "o.orchard_name AS orchardName, " +
            "o.variety, " +
            "COUNT(t.id) AS orderCount, " +
            "COALESCE(SUM(t.total_amount), 0) AS totalAmount, " +
            "COALESCE(SUM(t.quantity), 0) AS totalQuantity " +
            "FROM farm_orchard o " +
            "LEFT JOIN trade_order t ON t.orchard_id = o.id AND t.deleted = 0 " +
            "AND t.status IN ('COMPLETED', 'SHIPPED') " +
            "WHERE o.deleted = 0 " +
            "GROUP BY o.id, o.orchard_name, o.variety " +
            "ORDER BY totalAmount DESC " +
            "LIMIT 5")
    List<Map<String, Object>> topOrchardsByTradeVolume();

    // ===== Pending actions by type =====

    @Select("SELECT 'PENDING_ORDERS' AS actionType, COUNT(*) AS count " +
            "FROM trade_order WHERE deleted = 0 AND status = 'PENDING' " +
            "UNION ALL " +
            "SELECT 'UNPAID_ORDERS' AS actionType, COUNT(*) AS count " +
            "FROM trade_order WHERE deleted = 0 AND payment_status = 'UNPAID' " +
            "AND status NOT IN ('CANCELLED') " +
            "UNION ALL " +
            "SELECT 'CREATED_BATCHES' AS actionType, COUNT(*) AS count " +
            "FROM trace_batch WHERE deleted = 0 AND status = 'CREATED'")
    List<Map<String, Object>> pendingActionsCounts();
}
