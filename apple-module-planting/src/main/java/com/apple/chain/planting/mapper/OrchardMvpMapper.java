package com.apple.chain.planting.mapper;

import com.apple.chain.planting.entity.OrchardMvp;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * MVP Orchard mapper (farm_orchard table).
 */
@Mapper
public interface OrchardMvpMapper extends BaseMapper<OrchardMvp> {

    @Select("SELECT COALESCE(MAX(CAST(SUBSTRING(orchard_code, 11) AS SIGNED)), 0) + 1 " +
            "FROM farm_orchard WHERE orchard_code LIKE CONCAT('OC', #{prefix}, '%')")
    int nextSeq(String prefix);
}
