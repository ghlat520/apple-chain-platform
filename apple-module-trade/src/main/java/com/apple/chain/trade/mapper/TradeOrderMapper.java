package com.apple.chain.trade.mapper;

import com.apple.chain.trade.entity.TradeOrder;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * Trade order mapper.
 */
@Mapper
public interface TradeOrderMapper extends BaseMapper<TradeOrder> {

    @Select("SELECT COALESCE(MAX(CAST(SUBSTRING(order_no, 12) AS SIGNED)), 0) + 1 " +
            "FROM td_trade_order WHERE order_no LIKE CONCAT('ORD', #{prefix}, '%')")
    int nextSeq(String prefix);
}
