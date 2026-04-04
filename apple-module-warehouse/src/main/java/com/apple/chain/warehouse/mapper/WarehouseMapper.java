package com.apple.chain.warehouse.mapper;

import com.apple.chain.warehouse.entity.Warehouse;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * Warehouse mapper.
 */
@Mapper
public interface WarehouseMapper extends BaseMapper<Warehouse> {

    @Select("SELECT COALESCE(MAX(CAST(SUBSTRING(warehouse_code, 11) AS UNSIGNED)), 0) + 1 " +
            "FROM wh_warehouse WHERE warehouse_code LIKE CONCAT('WH', #{prefix}, '%')")
    int nextSeq(String prefix);
}
