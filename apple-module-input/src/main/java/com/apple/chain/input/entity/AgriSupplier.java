package com.apple.chain.input.entity;

import com.apple.chain.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Agricultural input supplier (农资供应商).
 * Table: agri_supplier
 */
@Getter
@Setter
@TableName("agri_supplier")
public class AgriSupplier extends BaseEntity {

    /** 供应商编码 */
    private String supplierCode;

    /** 供应商名称 */
    private String name;

    /** 联系人 */
    private String contactPerson;

    /** 联系电话 */
    private String phone;

    /** 地址 */
    private String address;

    /** 营业执照号 */
    private String license;

    /** 资质证书 */
    private String qualification;

    /** 信用评分 1-100 */
    private Integer creditScore;

    /** PENDING/APPROVED/REJECTED/BLACKLISTED */
    private String status;

    /** 审核时间 */
    private LocalDateTime auditTime;

    /** 审核人 */
    private String auditor;

    /** 拒绝原因 */
    private String rejectReason;

    private String remark;
}
