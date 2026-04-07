package com.apple.chain.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Per-user data visibility scope.
 * <p>
 * scope_type:
 * <ul>
 *   <li>{@code GLOBAL}   — sees all data (e.g. ADMIN, GOV)</li>
 *   <li>{@code PROVINCE} — limited to a province (scope_value = "山东")</li>
 *   <li>{@code CITY}     — limited to a city ("烟台")</li>
 *   <li>{@code OWN}      — only own records (default for FARMER/BUYER)</li>
 * </ul>
 */
@Getter
@Setter
@TableName("sys_user_data_scope")
public class SysUserDataScope implements Serializable {

    @TableId(type = IdType.INPUT)
    private Long userId;

    private String scopeType;
    private String scopeValue;

    @TableField(fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
