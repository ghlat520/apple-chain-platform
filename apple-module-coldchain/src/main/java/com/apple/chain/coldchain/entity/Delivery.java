package com.apple.chain.coldchain.entity;

import com.apple.chain.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@TableName("cc_delivery")
public class Delivery extends BaseEntity {

    private String deliveryCode;
    private Long taskId;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddr;
    private LocalDateTime deliveryTime;
    private LocalDateTime signTime;
    private String signPhoto;
    /** PENDING/PASSED/REJECTED */
    private String qualityCheck;
    private String qualityRemark;
    /** PENDING/DELIVERING/SIGNED/REJECTED */
    private String status;
    private String remark;
}
