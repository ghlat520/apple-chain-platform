package com.apple.chain.warehouse.mapper;

import com.apple.chain.warehouse.entity.WarehouseReceipt;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * Warehouse receipt mapper.
 */
@Mapper
public interface WarehouseReceiptMapper extends BaseMapper<WarehouseReceipt> {

    @Select("SELECT COALESCE(MAX(CAST(SUBSTRING(receipt_no, 11) AS UNSIGNED)), 0) + 1 " +
            "FROM wh_receipt WHERE receipt_no LIKE CONCAT('WR', #{prefix}, '%')")
    int nextSeq(String prefix);
}
