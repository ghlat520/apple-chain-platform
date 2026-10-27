package com.apple.chain.trace.dto;

import com.apple.chain.trace.entity.TraceNode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class TraceFullChainVO {

    private String traceCode;
    private String batchNo;
    private String variety;
    private String currentStatus;

    // Planting info
    private String orchardName;
    private String farmerName;
    private String region;

    // Input materials used
    private List<Map<String, Object>> inputMaterials;

    // Warehouse events
    private List<Map<String, Object>> warehouseRecords;

    // Trade info
    private Map<String, Object> tradeInfo;

    // Timeline nodes
    private List<TraceNode> timeline;

    // Blockchain verification
    private String chainTxHash;
    private Integer chainStatus;
}
