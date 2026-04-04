package com.apple.chain.coldchain.mapper;

import com.apple.chain.coldchain.entity.Delivery;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DeliveryMapper extends BaseMapper<Delivery> {

    @Select("SELECT COALESCE(MAX(CAST(SUBSTRING(delivery_code, 11) AS UNSIGNED)), 0) + 1 " +
            "FROM cc_delivery WHERE delivery_code LIKE CONCAT('DL', #{prefix}, '%')")
    int nextSeq(String prefix);
}
