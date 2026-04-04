package com.apple.chain.coldchain.entity;

import com.apple.chain.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@TableName("cc_transport_task")
public class TransportTask extends BaseEntity {

    private String taskCode;
    private Long vehicleId;
    private Long orderId;
    private String origin;
    private String destination;
    private String cargoDesc;
    private BigDecimal cargoWeight;
    private BigDecimal requiredTemp;
    private LocalDateTime planDepart;
    private LocalDateTime actualDepart;
    private LocalDateTime planArrive;
    private LocalDateTime actualArrive;
    private BigDecimal distance;
    private BigDecimal cost;
    /** PENDING/IN_TRANSIT/DELIVERED/CANCELLED */
    private String status;
    private String remark;
}
