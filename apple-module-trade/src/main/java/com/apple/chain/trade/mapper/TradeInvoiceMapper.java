package com.apple.chain.trade.mapper;

import com.apple.chain.trade.entity.TradeInvoice;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface TradeInvoiceMapper extends BaseMapper<TradeInvoice> {

    @Select("SELECT * FROM td_trade_invoice WHERE deleted = 0 AND order_id = #{orderId} LIMIT 1")
    TradeInvoice findByOrderId(@Param("orderId") Long orderId);
}
