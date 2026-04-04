package com.apple.chain.finance.mapper;

import com.apple.chain.finance.entity.Pledge;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PledgeMapper extends BaseMapper<Pledge> {

    @Select("SELECT COALESCE(MAX(CAST(SUBSTRING(pledge_code, 11) AS UNSIGNED)), 0) + 1 " +
            "FROM sf_pledge WHERE pledge_code LIKE CONCAT('PL', #{prefix}, '%')")
    int nextSeq(String prefix);
}
