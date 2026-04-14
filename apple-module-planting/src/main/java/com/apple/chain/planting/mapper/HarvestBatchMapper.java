package com.apple.chain.planting.mapper;

import com.apple.chain.planting.entity.HarvestBatch;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * Harvest batch mapper.
 */
@Mapper
public interface HarvestBatchMapper extends BaseMapper<HarvestBatch> {

    @Select("SELECT COALESCE(MAX(CAST(SUBSTRING(batch_no, 11) AS SIGNED)), 0) + 1 " +
            "FROM pt_harvest_batch WHERE batch_no LIKE CONCAT('HB', #{prefix}, '%')")
    int nextSeq(String prefix);
}
