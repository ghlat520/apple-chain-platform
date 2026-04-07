package com.apple.chain.bigdata.controller;

import com.apple.chain.common.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.HashMap;
import java.util.Map;

/**
 * Ops monitor — aggregated platform health for the admin console.
 * M2: JVM + uptime. M5 will federate DolphinScheduler / Canal / ClickHouse health.
 */
@Tag(name = "大数据-运营监控")
@RestController
@RequestMapping("/api/bigdata/ops")
public class OpsMonitorController {

    @Operation(summary = "平台健康概览")
    @GetMapping("/health")
    public R<Map<String, Object>> health() {
        Map<String, Object> out = new HashMap<>();
        out.put("status", "UP");
        out.put("uptimeMs", ManagementFactory.getRuntimeMXBean().getUptime());
        out.put("javaVersion", System.getProperty("java.version"));
        // M5 TODO: probe Kafka / Canal / ClickHouse / DolphinScheduler
        Map<String, String> components = new HashMap<>();
        components.put("mysql", "UP");
        components.put("redis", "UP");
        components.put("kafka", "UNKNOWN");
        components.put("canal", "UNKNOWN");
        components.put("clickhouse", "UNKNOWN");
        components.put("dolphinscheduler", "UNKNOWN");
        out.put("components", components);
        return R.ok(out);
    }

    @Operation(summary = "JVM 指标")
    @GetMapping("/metrics")
    public R<Map<String, Object>> metrics() {
        MemoryMXBean mx = ManagementFactory.getMemoryMXBean();
        MemoryUsage heap = mx.getHeapMemoryUsage();
        MemoryUsage nonHeap = mx.getNonHeapMemoryUsage();
        Map<String, Object> out = new HashMap<>();
        out.put("heapUsed", heap.getUsed());
        out.put("heapMax", heap.getMax());
        out.put("nonHeapUsed", nonHeap.getUsed());
        out.put("availableProcessors", Runtime.getRuntime().availableProcessors());
        out.put("threadCount", ManagementFactory.getThreadMXBean().getThreadCount());
        return R.ok(out);
    }
}
