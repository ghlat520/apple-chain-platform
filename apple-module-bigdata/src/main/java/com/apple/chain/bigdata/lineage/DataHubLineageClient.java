package com.apple.chain.bigdata.lineage;

import com.apple.chain.bigdata.entity.BdCollectJob;
import com.apple.chain.bigdata.entity.BdCollectJobRun;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * OpenLineage / DataHub emitter — real HTTP client (v2.0 P0).
 *
 * <p>Sends an OpenLineage RunEvent (eventType=COMPLETE) to DataHub GMS at
 * {@code ${bigdata.datahub.gms-url}/openapi/openlineage/api/v1/lineage} for every collect
 * job run. The event captures job identity (namespace/name) and the output
 * dataset (ClickHouse table from {@code BdCollectJob#targetTable}).</p>
 *
 * <p>Hard guarantees:</p>
 * <ul>
 *   <li>Gated by {@code bigdata.datahub.enabled} (default false)</li>
 *   <li>Strict 3s connect / 5s read timeouts — never blocks the caller</li>
 *   <li>All exceptions swallowed — lineage emission MUST NOT fail the
 *       calling pipeline</li>
 *   <li>Inputs are left empty in M3; M4 will parse SeaTunnel sources to
 *       populate them.</li>
 * </ul>
 */
@Slf4j
@Component
public class DataHubLineageClient {

    private static final String PRODUCER = "https://github.com/apple-chain/bigdata";
    private static final String SCHEMA_URL =
            "https://openlineage.io/spec/2-0-2/OpenLineage.json#/definitions/RunEvent";

    @Value("${bigdata.datahub.enabled:false}")
    private boolean enabled;

    @Value("${bigdata.datahub.gms-url:http://datahub-gms:8080}")
    private String gmsUrl;

    @Value("${bigdata.datahub.namespace:apple_chain.bigdata}")
    private String namespace;

    @Value("${bigdata.datahub.connect-timeout-ms:3000}")
    private int connectTimeoutMs;

    @Value("${bigdata.datahub.read-timeout-ms:5000}")
    private int readTimeoutMs;

    private RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    private void init() {
        SimpleClientHttpRequestFactory f = new SimpleClientHttpRequestFactory();
        f.setConnectTimeout(connectTimeoutMs);
        f.setReadTimeout(readTimeoutMs);
        this.restTemplate = new RestTemplate(f);
    }

    /**
     * Emit a job run lineage event (eventType=COMPLETE) to DataHub.
     * Failure is logged at WARN and never thrown.
     */
    public void emitJobRun(BdCollectJob job, BdCollectJobRun run) {
        if (!enabled) {
            log.debug("[lineage:disabled] jobCode={} runId={}", job.getJobCode(), run.getId());
            return;
        }
        try {
            Map<String, Object> event = buildRunEvent(job, run);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String payload = objectMapper.writeValueAsString(event);
            HttpEntity<String> entity = new HttpEntity<>(payload, headers);

            // Verified correct endpoint via DataHub v0.13.3 swagger api-docs (2026-04-07).
            // Earlier /openapi/v2/lineage path returns 404 — LineageApiImpl lives under
            // /openapi/openlineage/api/v1/lineage.
            String url = gmsUrl + "/openapi/openlineage/api/v1/lineage";
            ResponseEntity<String> resp = restTemplate.postForEntity(url, entity, String.class);

            if (resp.getStatusCode().is2xxSuccessful()) {
                log.info("[lineage:emit-ok] job={} run={} target={} status={}",
                        job.getJobCode(), run.getId(), job.getTargetTable(),
                        resp.getStatusCode().value());
            } else {
                log.warn("[lineage:emit-non2xx] job={} run={} status={} body={}",
                        job.getJobCode(), run.getId(),
                        resp.getStatusCode().value(), resp.getBody());
            }
        } catch (Exception e) {
            log.warn("[lineage:emit-failed] jobCode={} runId={} err={}",
                    job.getJobCode(), run.getId(), e.getMessage());
        }
    }

    /**
     * Build an OpenLineage RunEvent JSON-shaped Map.
     * Spec: https://openlineage.io/docs/spec/object-model
     */
    private Map<String, Object> buildRunEvent(BdCollectJob job, BdCollectJobRun run) {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("eventType", mapEventType(run.getRunStatus()));
        event.put("eventTime", Instant.now().toString());
        event.put("producer", PRODUCER);
        event.put("schemaURL", SCHEMA_URL);

        // run
        Map<String, Object> runBlock = new LinkedHashMap<>();
        runBlock.put("runId", buildDeterministicRunId(job, run));
        runBlock.put("facets", Collections.emptyMap());
        event.put("run", runBlock);

        // job
        Map<String, Object> jobBlock = new LinkedHashMap<>();
        jobBlock.put("namespace", namespace);
        jobBlock.put("name", job.getJobCode());
        jobBlock.put("facets", Collections.emptyMap());
        event.put("job", jobBlock);

        // inputs (empty in M3; M4 parses SeaTunnel sources)
        event.put("inputs", Collections.emptyList());

        // outputs — single ClickHouse table from job.targetTable
        if (job.getTargetTable() != null && !job.getTargetTable().isEmpty()) {
            Map<String, Object> output = new LinkedHashMap<>();
            output.put("namespace", "clickhouse://apple-clickhouse:9000");
            output.put("name", job.getTargetTable());
            output.put("facets", Collections.emptyMap());
            event.put("outputs", Collections.singletonList(output));
        } else {
            event.put("outputs", Collections.emptyList());
        }

        return event;
    }

    /** Map BdCollectJobRun.runStatus → OpenLineage eventType. */
    private String mapEventType(String runStatus) {
        if (runStatus == null) {
            return "OTHER";
        }
        switch (runStatus.toUpperCase()) {
            case "RUNNING":
                return "RUNNING";
            case "SUCCESS":
                return "COMPLETE";
            case "FAILED":
                return "FAIL";
            case "TIMEOUT":
                return "ABORT";
            default:
                return "OTHER";
        }
    }

    /** Deterministic UUID so retries on the same run are idempotent in DataHub. */
    private String buildDeterministicRunId(BdCollectJob job, BdCollectJobRun run) {
        String seed = job.getJobCode() + ":" + (run.getId() != null ? run.getId() : "0");
        return UUID.nameUUIDFromBytes(seed.getBytes()).toString();
    }
}
