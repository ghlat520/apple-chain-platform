package com.apple.chain.trade.mapper;

import com.apple.chain.trade.entity.QualityInspection;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface QualityInspectionMapper extends BaseMapper<QualityInspection> {

    @Select("SELECT COALESCE(MAX(CAST(SUBSTRING(inspection_no, 11) AS UNSIGNED)), 0) + 1 FROM td_quality_inspection WHERE inspection_no LIKE CONCAT('QI', #{prefix}, '%')")
    int nextSeq(String prefix);
}
