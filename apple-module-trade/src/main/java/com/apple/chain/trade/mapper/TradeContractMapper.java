package com.apple.chain.trade.mapper;

import com.apple.chain.trade.entity.TradeContract;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface TradeContractMapper extends BaseMapper<TradeContract> {

    @Select("SELECT * FROM td_trade_contract WHERE deleted = 0 AND order_id = #{orderId} LIMIT 1")
    TradeContract findByOrderId(@Param("orderId") Long orderId);
}
