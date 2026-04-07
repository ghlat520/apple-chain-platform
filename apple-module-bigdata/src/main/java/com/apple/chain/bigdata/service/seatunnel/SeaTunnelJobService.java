package com.apple.chain.bigdata.service.seatunnel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Thin wrapper around the SeaTunnel Zeta REST API.
 *
 * <p>Endpoint base: configurable via {@code bigdata.seatunnel.rest-url}
 * (default {@code http://apple-seatunnel:5801}).</p>
 *
 * <p>Used by {@code CollectJobController#trigger} so that operators can
 * manually re-run a SeaTunnel job from the admin UI without going through
 * DolphinScheduler. See M3-readiness-pack.md §7.</p>
 *
 * <p>This is a deliberately small surface — only submit / status / stop —
 * because all scheduled execution is owned by DolphinScheduler.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SeaTunnelJobService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${bigdata.seatunnel.rest-url:http://apple-seatunnel:5801}")
    private String restUrl;

    /**
     * Submit a job by HOCON file path (path is the in-container path,
     * e.g. {@code /opt/seatunnel/jobs/external/ext-weather-hefeng.conf}).
     *
     * @return SeaTunnel jobId, or null if submission failed
     */
    @SuppressWarnings("unchecked")
    public String submitJob(String configPath, String jobName) {
        try {
            String url = restUrl + "/hazelcast/rest/maps/submit-job?jobName=" + jobName;
            Map<String, Object> body = new HashMap<>();
            body.put("configPath", configPath);
            Map<String, Object> resp = restTemplate.postForObject(url, body, Map.class);
            if (resp == null) {
                return null;
            }
            Object jobId = resp.get("jobId");
            return jobId != null ? jobId.toString() : null;
        } catch (Exception e) {
            log.error("SeaTunnel submit failed: configPath={} err={}", configPath, e.getMessage());
            return null;
        }
    }

    /** Query running status: RUNNING / FINISHED / FAILED / CANCELED */
    @SuppressWarnings("unchecked")
    public String getJobStatus(String jobId) {
        try {
            String url = restUrl + "/hazelcast/rest/maps/job-info/" + jobId;
            Map<String, Object> resp = restTemplate.getForObject(url, Map.class);
            if (resp == null) {
                return "UNKNOWN";
            }
            Object status = resp.get("jobStatus");
            return status != null ? status.toString() : "UNKNOWN";
        } catch (Exception e) {
            log.warn("SeaTunnel status query failed: jobId={} err={}", jobId, e.getMessage());
            return "UNKNOWN";
        }
    }

    /** Best-effort stop. */
    public boolean stopJob(String jobId) {
        try {
            String url = restUrl + "/hazelcast/rest/maps/stop-job";
            Map<String, Object> body = new HashMap<>();
            body.put("jobId", jobId);
            body.put("isStopWithSavePoint", false);
            restTemplate.postForObject(url, body, String.class);
            return true;
        } catch (Exception e) {
            log.error("SeaTunnel stop failed: jobId={} err={}", jobId, e.getMessage());
            return false;
        }
    }
}
