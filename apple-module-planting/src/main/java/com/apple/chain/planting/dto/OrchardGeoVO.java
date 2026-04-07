package com.apple.chain.planting.dto;

import com.apple.chain.planting.entity.Orchard;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * M4 GIS: lightweight orchard view-model for map viewport queries.
 * Excludes large fields like boundary_geojson to keep bbox responses small;
 * boundary is fetched on-demand via the detail endpoint.
 */
@Data
@Builder
public class OrchardGeoVO {

    private Long id;
    private String orchardNo;
    private String orchardName;
    private String variety;
    private String status;
    private BigDecimal centerLat;
    private BigDecimal centerLng;
    private BigDecimal areaMu;

    public static OrchardGeoVO from(Orchard o) {
        return OrchardGeoVO.builder()
                .id(o.getId())
                .orchardNo(o.getOrchardNo())
                .orchardName(o.getOrchardName())
                .variety(o.getVariety())
                .status(o.getStatus())
                .centerLat(o.getCenterLat())
                .centerLng(o.getCenterLng())
                .areaMu(o.getAreaMu())
                .build();
    }
}
