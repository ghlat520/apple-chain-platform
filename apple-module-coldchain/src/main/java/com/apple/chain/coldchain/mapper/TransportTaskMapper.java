package com.apple.chain.coldchain.mapper;

import com.apple.chain.coldchain.entity.TransportTask;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface TransportTaskMapper extends BaseMapper<TransportTask> {

    @Select("SELECT COALESCE(MAX(CAST(SUBSTRING(task_code, 11) AS UNSIGNED)), 0) + 1 " +
            "FROM cc_transport_task WHERE task_code LIKE CONCAT('TT', #{prefix}, '%')")
    int nextSeq(String prefix);
}
