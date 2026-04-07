package com.apple.chain.iot.mapper;

import com.apple.chain.iot.entity.IotAlert;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface IotAlertMapper extends BaseMapper<IotAlert> {

    @Select("SELECT * FROM iot_alert WHERE deleted = 0 AND handled = 0 ORDER BY id DESC LIMIT #{limit}")
    List<IotAlert> findOpen(@Param("limit") int limit);
}
