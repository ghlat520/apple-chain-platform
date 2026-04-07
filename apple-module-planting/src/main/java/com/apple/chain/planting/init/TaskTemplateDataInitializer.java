package com.apple.chain.planting.init;

import com.apple.chain.planting.entity.TaskTemplate;
import com.apple.chain.planting.mapper.TaskTemplateMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * M5: seed the {@code pt_task_template} table with 3 varieties × 12 months × 6
 * operation types = 216 baseline rules.
 *
 * <p>Idempotent: skips if {@code countAll() > 0}. Re-running on a populated DB
 * is a no-op so this initializer can stay in production code without ceremony.
 *
 * <p>The seed values are deliberately conservative — every (variety, month, op)
 * tuple gets a row even if the operation makes no sense in that month (e.g.
 * pruning in midsummer). The {@link #priorityForMonth} helper assigns low
 * priority (1) to "off-season" operations and high priority (5) to peak-season,
 * so the rule engine still surfaces something for every month while letting
 * downstream filtering by priority hide noise.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TaskTemplateDataInitializer implements ApplicationRunner {

    private static final String[] VARIETIES = {"红富士", "嘎啦", "国光"};

    @Override
    public void run(org.springframework.boot.ApplicationArguments args) {
        if (taskTemplateMapper.countAll() > 0) {
            log.debug("pt_task_template already populated, skipping seed");
            return;
        }
        int inserted = 0;
        for (String variety : VARIETIES) {
            for (int month = 1; month <= 12; month++) {
                for (String op : TaskTemplate.ALL_OPS) {
                    taskTemplateMapper.insert(buildTemplate(variety, month, op));
                    inserted++;
                }
            }
        }
        log.info("M5 task template seeded: {} rows", inserted);
    }

    private final TaskTemplateMapper taskTemplateMapper;

    private TaskTemplate buildTemplate(String variety, int month, String op) {
        TaskTemplate t = new TaskTemplate();
        t.setVariety(variety);
        t.setMonth(month);
        t.setOperationType(op);
        t.setTaskName(taskNameFor(op, variety, month));
        int[] window = windowFor(op, month);
        t.setSuggestedDayStart(window[0]);
        t.setSuggestedDayEnd(window[1]);
        t.setMaterialSuggestion(materialFor(op));
        t.setPriority(priorityForMonth(op, month));
        t.setDescription(op + " for " + variety + " in month " + month);
        return t;
    }

    private static String taskNameFor(String op, String variety, int month) {
        return switch (op) {
            case TaskTemplate.OP_FERTILIZE -> variety + " " + month + "月施肥";
            case TaskTemplate.OP_PRUNE     -> variety + " " + month + "月修剪";
            case TaskTemplate.OP_THIN      -> variety + " " + month + "月疏花疏果";
            case TaskTemplate.OP_PESTICIDE -> variety + " " + month + "月病虫害防治";
            case TaskTemplate.OP_IRRIGATE  -> variety + " " + month + "月灌溉";
            case TaskTemplate.OP_HARVEST   -> variety + " " + month + "月采收";
            default -> variety + " " + month + "月" + op;
        };
    }

    private static String materialFor(String op) {
        return switch (op) {
            case TaskTemplate.OP_FERTILIZE -> "尿素 30kg/亩 或 复合肥 25kg/亩";
            case TaskTemplate.OP_PESTICIDE -> "波尔多液 1:1:200 或低毒杀虫剂";
            case TaskTemplate.OP_IRRIGATE  -> "足量灌透根层 30cm";
            default -> null;
        };
    }

    /** Suggested day window per op type. Conservative defaults; not tied to month. */
    private static int[] windowFor(String op, int month) {
        return switch (op) {
            case TaskTemplate.OP_FERTILIZE -> new int[]{5, 15};
            case TaskTemplate.OP_PRUNE     -> new int[]{1, 10};
            case TaskTemplate.OP_THIN      -> new int[]{10, 20};
            case TaskTemplate.OP_PESTICIDE -> new int[]{1, 28};
            case TaskTemplate.OP_IRRIGATE  -> new int[]{15, 25};
            case TaskTemplate.OP_HARVEST   -> new int[]{20, 28};
            default -> new int[]{1, 28};
        };
    }

    /**
     * Crude priority based on apple phenology (peak season Apr–Oct gets higher
     * priority). Real product would consult a per-variety phenology table.
     */
    private static int priorityForMonth(String op, int month) {
        boolean inSeason = month >= 4 && month <= 10;
        return switch (op) {
            case TaskTemplate.OP_HARVEST -> (month == 9 || month == 10) ? 5 : 1;
            case TaskTemplate.OP_THIN    -> (month == 5 || month == 6) ? 5 : 1;
            case TaskTemplate.OP_PRUNE   -> (month == 1 || month == 2 || month == 12) ? 4 : 2;
            default -> inSeason ? 4 : 2;
        };
    }
}
