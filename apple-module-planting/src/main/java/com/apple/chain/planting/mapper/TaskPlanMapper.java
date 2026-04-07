package com.apple.chain.planting.mapper;

import com.apple.chain.planting.entity.TaskPlan;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface TaskPlanMapper extends BaseMapper<TaskPlan> {

    @Select("SELECT * FROM pt_task_plan " +
            "WHERE deleted = 0 AND orchard_id = #{orchardId} " +
            "  AND plan_date BETWEEN #{from} AND #{to} " +
            "ORDER BY plan_date, priority DESC")
    List<TaskPlan> findInRange(@Param("orchardId") Long orchardId,
                               @Param("from") LocalDate from,
                               @Param("to") LocalDate to);
}
