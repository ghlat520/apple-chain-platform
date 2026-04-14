package com.apple.chain.trade.mapper;

import com.apple.chain.trade.entity.PurchaseNeed;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * Purchase need mapper.
 */
@Mapper
public interface PurchaseNeedMapper extends BaseMapper<PurchaseNeed> {

    @Select("SELECT COALESCE(MAX(CAST(SUBSTRING(need_no, 12) AS SIGNED)), 0) + 1 " +
            "FROM td_purchase_need WHERE need_no LIKE CONCAT('NED', #{prefix}, '%')")
    int nextSeq(String prefix);
}
