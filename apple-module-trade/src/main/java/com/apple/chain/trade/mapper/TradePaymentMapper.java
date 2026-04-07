package com.apple.chain.trade.mapper;

import com.apple.chain.trade.entity.TradePayment;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface TradePaymentMapper extends BaseMapper<TradePayment> {

    @Select("SELECT * FROM td_trade_payment WHERE deleted = 0 AND payment_no = #{paymentNo} LIMIT 1")
    TradePayment findByPaymentNo(@Param("paymentNo") String paymentNo);
}
