package com.apple.chain.trade.mapper;

import com.apple.chain.trade.entity.TradeMatch;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TradeMatchMapper extends BaseMapper<TradeMatch> {

    @Select("SELECT * FROM td_trade_match " +
            "WHERE deleted = 0 AND supply_id = #{supplyId} " +
            "ORDER BY match_score DESC LIMIT #{k}")
    List<TradeMatch> findTopForSupply(@Param("supplyId") Long supplyId, @Param("k") int k);
}
