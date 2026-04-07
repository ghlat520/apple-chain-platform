package com.apple.chain.iot.mapper;

import com.apple.chain.iot.entity.IotDevice;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface IotDeviceMapper extends BaseMapper<IotDevice> {

    @Select("SELECT * FROM iot_device WHERE deleted = 0 AND device_type = #{type} AND status = 1")
    List<IotDevice> findOnlineByType(@Param("type") String type);

    @Select("SELECT * FROM iot_device WHERE deleted = 0 AND device_sn = #{sn} LIMIT 1")
    IotDevice findBySn(@Param("sn") String sn);
}
