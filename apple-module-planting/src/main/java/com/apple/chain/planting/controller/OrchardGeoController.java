package com.apple.chain.planting.controller;

import com.apple.chain.common.auth.RequirePerm;
import com.apple.chain.common.result.R;
import com.apple.chain.planting.dto.OrchardGeoVO;
import com.apple.chain.planting.dto.SaveBoundaryRequest;
import com.apple.chain.planting.entity.Orchard;
import com.apple.chain.planting.service.OrchardGeoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * M4 GIS map endpoints. Separate from {@link OrchardController} to keep
 * the original CRUD untouched and to scope GIS permissions independently.
 *
 * Path convention follows the existing pattern: /api/planting/orchard/...
 */
@Tag(name = "果园 GIS 地图")
@RestController
@RequestMapping("/api/planting/orchard/geo")
@RequiredArgsConstructor
@Validated
public class OrchardGeoController {

    private final OrchardGeoService orchardGeoService;

    @Operation(summary = "视窗（bbox）果园查询", description = "返回中心点落在矩形范围内的果园（不含 boundary 大字段）")
    @GetMapping("/bbox")
    @RequirePerm("orchard:read")
    public R<List<OrchardGeoVO>> findInBoundingBox(
            @RequestParam @DecimalMin("-180") @DecimalMax("180") double lng1,
            @RequestParam @DecimalMin("-90")  @DecimalMax("90")  double lat1,
            @RequestParam @DecimalMin("-180") @DecimalMax("180") double lng2,
            @RequestParam @DecimalMin("-90")  @DecimalMax("90")  double lat2) {
        return R.ok(orchardGeoService.findInBoundingBox(lng1, lat1, lng2, lat2));
    }

    @Operation(summary = "保存果园边界", description = "传 GeoJSON Polygon，服务端自动计算质心和面积（亩）")
    @PostMapping("/{id}/boundary")
    @RequirePerm("orchard:write")
    public R<Orchard> saveBoundary(@PathVariable Long id,
                                   @Valid @RequestBody SaveBoundaryRequest request) {
        return R.ok(orchardGeoService.saveBoundary(id, request.getBoundaryGeojson()));
    }
}
