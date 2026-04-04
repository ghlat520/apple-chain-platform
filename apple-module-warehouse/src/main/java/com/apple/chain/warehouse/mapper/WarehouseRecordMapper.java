package com.apple.chain.warehouse.mapper;

import com.apple.chain.warehouse.entity.WarehouseRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * Warehouse record mapper.
 */
@Mapper
public interface WarehouseRecordMapper extends BaseMapper<WarehouseRecord> {

    @Select("SELECT COALESCE(MAX(CAST(SUBSTRING(record_no, 11) AS UNSIGNED)), 0) + 1 " +
            "FROM wh_record WHERE record_no LIKE CONCAT('WR', #{prefix}, '%')")
    int nextSeq(String prefix);
}
