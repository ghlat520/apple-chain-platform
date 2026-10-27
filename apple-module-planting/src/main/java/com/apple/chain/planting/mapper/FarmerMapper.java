package com.apple.chain.planting.mapper;

import com.apple.chain.planting.entity.Farmer;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * Farmer mapper.
 */
@Mapper
public interface FarmerMapper extends BaseMapper<Farmer> {

    @Select("SELECT COALESCE(MAX(CAST(SUBSTRING(farmer_code, 11) AS SIGNED)), 0) + 1 " +
            "FROM farm_farmer WHERE farmer_code LIKE CONCAT('FC', #{prefix}, '%')")
    int nextSeq(String prefix);
}
