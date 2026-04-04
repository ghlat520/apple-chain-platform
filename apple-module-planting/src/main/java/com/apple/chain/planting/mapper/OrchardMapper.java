package com.apple.chain.planting.mapper;

import com.apple.chain.planting.entity.Orchard;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * Orchard mapper.
 */
@Mapper
public interface OrchardMapper extends BaseMapper<Orchard> {

    @Select("SELECT COALESCE(MAX(CAST(SUBSTRING(orchard_no, 12) AS BIGINT)), 0) + 1 " +
            "FROM pt_orchard WHERE orchard_no LIKE CONCAT('ORD', #{prefix}, '%')")
    int nextSeq(String prefix);
}
