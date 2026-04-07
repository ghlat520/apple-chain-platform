package com.apple.chain.planting.service.impl;

import com.apple.chain.common.exception.BizException;
import com.apple.chain.common.result.ResultCode;
import com.apple.chain.planting.dto.OrchardGeoVO;
import com.apple.chain.planting.entity.Orchard;
import com.apple.chain.planting.geo.GeoJsonAreaCalculator;
import com.apple.chain.planting.mapper.OrchardMapper;
import com.apple.chain.planting.service.OrchardGeoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrchardGeoServiceImpl implements OrchardGeoService {

    private final OrchardMapper orchardMapper;

    @Override
    public List<OrchardGeoVO> findInBoundingBox(double lng1, double lat1, double lng2, double lat2) {
        // Normalize so lng1<=lng2 and lat1<=lat2 regardless of how the client sent the bbox
        double minLng = Math.min(lng1, lng2);
        double maxLng = Math.max(lng1, lng2);
        double minLat = Math.min(lat1, lat2);
        double maxLat = Math.max(lat1, lat2);

        return orchardMapper.findInBoundingBox(minLng, minLat, maxLng, maxLat)
                .stream()
                .map(OrchardGeoVO::from)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Orchard saveBoundary(Long orchardId, String geoJson) {
        Orchard orchard = orchardMapper.selectById(orchardId);
        if (orchard == null) {
            throw new BizException(ResultCode.NOT_FOUND, "果园不存在: " + orchardId);
        }

        GeoJsonAreaCalculator.GeoResult result;
        try {
            result = GeoJsonAreaCalculator.parse(geoJson);
        } catch (IllegalArgumentException e) {
            throw new BizException(ResultCode.PARAM_ERROR, "GeoJSON 解析失败: " + e.getMessage());
        }

        orchard.setBoundaryGeojson(geoJson);
        orchard.setCenterLng(result.centerLng());
        orchard.setCenterLat(result.centerLat());
        orchard.setAreaMu(result.areaMu());
        orchardMapper.updateById(orchard);
        return orchard;
    }
}
