package com.apple.chain.trade.mapper;

import com.apple.chain.trade.entity.SupplyInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * Supply info mapper.
 */
@Mapper
public interface SupplyInfoMapper extends BaseMapper<SupplyInfo> {

    @Select("SELECT COALESCE(MAX(CAST(SUBSTRING(supply_no, 12) AS BIGINT)), 0) + 1 " +
            "FROM td_supply_info WHERE supply_no LIKE CONCAT('SUP', #{prefix}, '%')")
    int nextSeq(String prefix);
}
