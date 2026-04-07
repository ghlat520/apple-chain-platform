package com.apple.chain.planting.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * M4 GIS: request body for saving an orchard polygon boundary.
 * Validation guards against blank input and abusive payload sizes.
 */
@Data
public class SaveBoundaryRequest {

    /** GeoJSON Polygon as a JSON string. Max 64 KB to prevent JSON-bomb DoS. */
    @NotBlank(message = "boundaryGeojson 不能为空")
    @Size(max = 65_536, message = "boundaryGeojson 超过 64KB 上限")
    private String boundaryGeojson;
}
