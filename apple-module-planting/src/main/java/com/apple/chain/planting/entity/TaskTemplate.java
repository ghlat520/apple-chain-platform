package com.apple.chain.planting.entity;

import com.apple.chain.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * M5 物候期作业模板. Table: pt_task_template (V17).
 *
 * Defines the canonical "what should happen for variety X in month M" rules
 * that drive plan generation. Unique on (variety, month, operation_type).
 */
@Getter
@Setter
@TableName("pt_task_template")
public class TaskTemplate extends BaseEntity {

    private String variety;
    private Integer month;
    private String operationType;
    private String taskName;
    private Integer suggestedDayStart;
    private Integer suggestedDayEnd;
    private String materialSuggestion;
    private Integer priority;
    private String description;

    // ==== operation type constants ====
    public static final String OP_FERTILIZE = "FERTILIZE";
    public static final String OP_PRUNE     = "PRUNE";
    public static final String OP_THIN      = "THIN";
    public static final String OP_PESTICIDE = "PESTICIDE";
    public static final String OP_IRRIGATE  = "IRRIGATE";
    public static final String OP_HARVEST   = "HARVEST";

    public static final String[] ALL_OPS = {
            OP_FERTILIZE, OP_PRUNE, OP_THIN, OP_PESTICIDE, OP_IRRIGATE, OP_HARVEST
    };
}
