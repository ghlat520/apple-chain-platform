package com.apple.chain.coldchain.mapper;

import com.apple.chain.coldchain.entity.Vehicle;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface VehicleMapper extends BaseMapper<Vehicle> {

    @Select("SELECT COALESCE(MAX(CAST(SUBSTRING(vehicle_code, 11) AS UNSIGNED)), 0) + 1 " +
            "FROM cc_vehicle WHERE vehicle_code LIKE CONCAT('VH', #{prefix}, '%')")
    int nextSeq(String prefix);
}
