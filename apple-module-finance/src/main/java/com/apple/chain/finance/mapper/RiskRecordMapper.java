package com.apple.chain.finance.mapper;

import com.apple.chain.finance.entity.RiskRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface RiskRecordMapper extends BaseMapper<RiskRecord> {

    @Select("SELECT COALESCE(MAX(CAST(SUBSTRING(risk_code, 11) AS UNSIGNED)), 0) + 1 " +
            "FROM sf_risk_record WHERE risk_code LIKE CONCAT('RK', #{prefix}, '%')")
    int nextSeq(String prefix);
}
