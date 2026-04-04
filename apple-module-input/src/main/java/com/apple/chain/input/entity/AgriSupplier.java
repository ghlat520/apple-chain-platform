package com.apple.chain.input.entity;

import com.apple.chain.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

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

    /** ACTIVE/SUSPENDED/BLACKLISTED */
    private String status;

    private String remark;
}
