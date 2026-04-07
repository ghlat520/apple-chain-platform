package com.apple.chain.iot.mapper;

import com.apple.chain.iot.entity.IotTelemetry;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface IotTelemetryMapper extends BaseMapper<IotTelemetry> {

    @Select("SELECT * FROM iot_telemetry WHERE deleted = 0 AND device_sn = #{sn} " +
            "ORDER BY collect_time DESC LIMIT 1")
    IotTelemetry findLatest(@Param("sn") String sn);

    @Select("SELECT * FROM iot_telemetry WHERE deleted = 0 AND device_sn = #{sn} " +
            "  AND collect_time >= #{from} ORDER BY collect_time ASC LIMIT #{limit}")
    List<IotTelemetry> findSince(@Param("sn") String sn,
                                  @Param("from") LocalDateTime from,
                                  @Param("limit") int limit);

    @Select("SELECT COUNT(*) FROM iot_telemetry WHERE deleted = 0 AND device_sn = #{sn} " +
            "  AND collect_time >= #{from} AND (temp_1 > #{maxTemp} OR temp_2 > #{maxTemp} " +
            "    OR temp_3 > #{maxTemp} OR temp_4 > #{maxTemp})")
    long countOverTempSince(@Param("sn") String sn, @Param("from") LocalDateTime from,
                            @Param("maxTemp") double maxTemp);
}
