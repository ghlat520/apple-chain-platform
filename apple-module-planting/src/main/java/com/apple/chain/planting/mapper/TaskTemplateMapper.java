package com.apple.chain.planting.mapper;

import com.apple.chain.planting.entity.TaskTemplate;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TaskTemplateMapper extends BaseMapper<TaskTemplate> {

    @Select("SELECT * FROM pt_task_template WHERE deleted = 0 AND variety = #{variety} AND month = #{month}")
    List<TaskTemplate> findByVarietyAndMonth(@Param("variety") String variety, @Param("month") int month);

    @Select("SELECT COUNT(*) FROM pt_task_template WHERE deleted = 0")
    long countAll();
}
