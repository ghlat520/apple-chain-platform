package com.apple.chain.coldchain.entity;

import com.apple.chain.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@TableName("cc_vehicle")
public class Vehicle extends BaseEntity {

    private String vehicleCode;
    private String plateNumber;
    /** REFRIGERATED/INSULATED/NORMAL */
    private String vehicleType;
    private String brand;
    private BigDecimal capacity;
    private BigDecimal volume;
    private BigDecimal temperatureMin;
    private BigDecimal temperatureMax;
    private String driverName;
    private String driverPhone;
    /** IDLE/IN_TRANSIT/MAINTENANCE/RETIRED */
    private String status;
    private String remark;
}
