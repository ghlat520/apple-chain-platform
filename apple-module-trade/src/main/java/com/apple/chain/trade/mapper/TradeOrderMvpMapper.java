package com.apple.chain.trade.mapper;

import com.apple.chain.trade.entity.TradeOrderMvp;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * MVP trade_order mapper.
 */
@Mapper
public interface TradeOrderMvpMapper extends BaseMapper<TradeOrderMvp> {

    @Select("SELECT COALESCE(MAX(CAST(SUBSTRING(order_no, 12) AS BIGINT)), 0) + 1 " +
            "FROM trade_order WHERE order_no LIKE CONCAT('TO', #{prefix}, '%')")
    int nextSeq(String prefix);
}
