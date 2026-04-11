package com.apple.chain.coldchain.mapper;

import com.apple.chain.coldchain.entity.PreCoolTask;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PreCoolTaskMapper extends BaseMapper<PreCoolTask> {

    @Select("SELECT COALESCE(MAX(CAST(SUBSTRING(task_no, 11) AS UNSIGNED)), 0) + 1 " +
            "FROM cc_pre_cool_task WHERE task_no LIKE CONCAT('PC', #{prefix}, '%')")
    int nextSeq(String prefix);
}
