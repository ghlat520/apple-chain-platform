package com.apple.chain.planting.service.impl;

import com.apple.chain.planting.dto.PlantingAnalysisVO;
import com.apple.chain.planting.entity.CultivationBatch;
import com.apple.chain.planting.entity.CultivationOperation;
import com.apple.chain.planting.entity.HarvestBatch;
import com.apple.chain.planting.entity.Orchard;
import com.apple.chain.planting.mapper.CultivationBatchMapper;
import com.apple.chain.planting.mapper.CultivationOperationMapper;
import com.apple.chain.planting.mapper.HarvestBatchMapper;
import com.apple.chain.planting.mapper.OrchardMapper;
import com.apple.chain.planting.service.PlantingAnalysisService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Planting analysis service implementation.
 * Uses MyBatis-Plus LambdaQueryWrapper + Java streams for in-memory aggregation.
 */
@Service
@RequiredArgsConstructor
public class PlantingAnalysisServiceImpl implements PlantingAnalysisService {

    private static final int SCALE = 4;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    private final OrchardMapper orchardMapper;
    private final CultivationBatchMapper cultivationBatchMapper;
    private final HarvestBatchMapper harvestBatchMapper;
    private final CultivationOperationMapper cultivationOperationMapper;

    // ------------------------------------------------------------------ //
    //  getYieldRanking
    // ------------------------------------------------------------------ //

    @Override
    public List<PlantingAnalysisVO.YieldPerMu> getYieldRanking(String variety, Integer year) {
        // 1. Query batches filtered by variety and year
        LambdaQueryWrapper<CultivationBatch> batchQuery = new LambdaQueryWrapper<CultivationBatch>()
                .eq(StringUtils.hasText(variety), CultivationBatch::getAppleVariety, variety)
                .eq(year != null, CultivationBatch::getPlantYear, year)
                .isNotNull(CultivationBatch::getActualYield);

        List<CultivationBatch> batches = cultivationBatchMapper.selectList(batchQuery);
        if (batches.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. Aggregate totalYield per orchardId
        Map<Long, BigDecimal> totalYieldByOrchard = batches.stream()
                .collect(Collectors.groupingBy(
                        CultivationBatch::getOrchardId,
                        Collectors.reducing(BigDecimal.ZERO,
                                b -> b.getActualYield() != null ? b.getActualYield() : BigDecimal.ZERO,
                                BigDecimal::add)
                ));

        // 3. Collect variety per orchardId (take first variety encountered)
        Map<Long, String> varietyByOrchard = batches.stream()
                .collect(Collectors.toMap(
                        CultivationBatch::getOrchardId,
                        CultivationBatch::getAppleVariety,
                        (a, b) -> a
                ));

        // 4. Fetch orchard details for area and name
        List<Long> orchardIds = new ArrayList<>(totalYieldByOrchard.keySet());
        Map<Long, Orchard> orchardMap = fetchOrchardsById(orchardIds);

        // 5. Build VO list
        List<PlantingAnalysisVO.YieldPerMu> result = orchardIds.stream()
                .filter(id -> orchardMap.containsKey(id))
                .map(orchardId -> {
                    Orchard orchard = orchardMap.get(orchardId);
                    BigDecimal totalYield = totalYieldByOrchard.get(orchardId);
                    BigDecimal areaMu = orchard.getAreaMu();
                    BigDecimal yieldPerMu = (areaMu != null && areaMu.compareTo(BigDecimal.ZERO) > 0)
                            ? totalYield.divide(areaMu, SCALE, ROUNDING)
                            : BigDecimal.ZERO;
                    return PlantingAnalysisVO.YieldPerMu.builder()
                            .orchardId(orchardId)
                            .orchardName(orchard.getOrchardName())
                            .variety(varietyByOrchard.getOrDefault(orchardId, ""))
                            .areaMu(areaMu)
                            .totalYield(totalYield)
                            .yieldPerMu(yieldPerMu)
                            .build();
                })
                .sorted(Comparator.comparing(PlantingAnalysisVO.YieldPerMu::getYieldPerMu).reversed())
                .collect(Collectors.toList());

        return result;
    }

    // ------------------------------------------------------------------ //
    //  getPremiumRates
    // ------------------------------------------------------------------ //

    @Override
    public List<PlantingAnalysisVO.PremiumRate> getPremiumRates(String variety, Integer year) {
        // 1. If variety filter is needed, collect eligible orchardIds first
        Set<Long> eligibleOrchardIds = null;
        if (StringUtils.hasText(variety)) {
            LambdaQueryWrapper<Orchard> orchardQuery = new LambdaQueryWrapper<Orchard>()
                    .eq(Orchard::getVariety, variety)
                    .select(Orchard::getId);
            eligibleOrchardIds = orchardMapper.selectList(orchardQuery)
                    .stream().map(Orchard::getId).collect(Collectors.toSet());
            if (eligibleOrchardIds.isEmpty()) {
                return Collections.emptyList();
            }
        }

        // 2. Query harvest batches
        LambdaQueryWrapper<HarvestBatch> harvestQuery = new LambdaQueryWrapper<HarvestBatch>()
                .isNotNull(HarvestBatch::getTotalWeight);

        if (year != null) {
            harvestQuery.apply("YEAR(harvest_date) = {0}", year);
        }

        final Set<Long> finalEligibleIds = eligibleOrchardIds;
        if (finalEligibleIds != null) {
            harvestQuery.in(HarvestBatch::getOrchardId, finalEligibleIds);
        }

        List<HarvestBatch> harvests = harvestBatchMapper.selectList(harvestQuery);
        if (harvests.isEmpty()) {
            return Collections.emptyList();
        }

        // 3. Aggregate gradeA and totalWeight per orchardId
        Map<Long, BigDecimal> totalWeightMap = harvests.stream()
                .collect(Collectors.groupingBy(
                        HarvestBatch::getOrchardId,
                        Collectors.reducing(BigDecimal.ZERO,
                                h -> h.getTotalWeight() != null ? h.getTotalWeight() : BigDecimal.ZERO,
                                BigDecimal::add)
                ));

        Map<Long, BigDecimal> gradeAMap = harvests.stream()
                .collect(Collectors.groupingBy(
                        HarvestBatch::getOrchardId,
                        Collectors.reducing(BigDecimal.ZERO,
                                h -> h.getGradeA() != null ? h.getGradeA() : BigDecimal.ZERO,
                                BigDecimal::add)
                ));

        // 4. Fetch orchard names
        List<Long> orchardIds = new ArrayList<>(totalWeightMap.keySet());
        Map<Long, Orchard> orchardMap = fetchOrchardsById(orchardIds);

        // 5. Build VO list
        List<PlantingAnalysisVO.PremiumRate> result = orchardIds.stream()
                .map(orchardId -> {
                    Orchard orchard = orchardMap.get(orchardId);
                    String orchardName = orchard != null ? orchard.getOrchardName() : "Unknown";
                    BigDecimal gradeAWeight = gradeAMap.getOrDefault(orchardId, BigDecimal.ZERO);
                    BigDecimal totalWeight = totalWeightMap.getOrDefault(orchardId, BigDecimal.ZERO);
                    BigDecimal rate = (totalWeight.compareTo(BigDecimal.ZERO) > 0)
                            ? gradeAWeight.divide(totalWeight, SCALE, ROUNDING)
                            : BigDecimal.ZERO;
                    return PlantingAnalysisVO.PremiumRate.builder()
                            .orchardId(orchardId)
                            .orchardName(orchardName)
                            .gradeAWeight(gradeAWeight)
                            .totalWeight(totalWeight)
                            .premiumRate(rate)
                            .build();
                })
                .sorted(Comparator.comparing(PlantingAnalysisVO.PremiumRate::getPremiumRate).reversed())
                .collect(Collectors.toList());

        return result;
    }

    // ------------------------------------------------------------------ //
    //  getPestIncidences
    // ------------------------------------------------------------------ //

    @Override
    public List<PlantingAnalysisVO.PestIncidence> getPestIncidences(Integer year) {
        // 1. Determine batch scope (year filter → via CultivationBatch.plantYear)
        Set<Long> eligibleBatchIds = null;
        Map<Long, Long> batchToOrchard = new HashMap<>();

        if (year != null) {
            LambdaQueryWrapper<CultivationBatch> batchQuery = new LambdaQueryWrapper<CultivationBatch>()
                    .eq(CultivationBatch::getPlantYear, year)
                    .select(CultivationBatch::getId, CultivationBatch::getOrchardId);
            List<CultivationBatch> batches = cultivationBatchMapper.selectList(batchQuery);
            if (batches.isEmpty()) {
                return Collections.emptyList();
            }
            eligibleBatchIds = batches.stream().map(CultivationBatch::getId).collect(Collectors.toSet());
            batches.forEach(b -> batchToOrchard.put(b.getId(), b.getOrchardId()));
        } else {
            // No year filter — load all batches to build batchId→orchardId map
            LambdaQueryWrapper<CultivationBatch> batchQuery = new LambdaQueryWrapper<CultivationBatch>()
                    .select(CultivationBatch::getId, CultivationBatch::getOrchardId);
            cultivationBatchMapper.selectList(batchQuery)
                    .forEach(b -> batchToOrchard.put(b.getId(), b.getOrchardId()));
        }

        // 2. Query all operations (optionally scoped to eligible batches)
        LambdaQueryWrapper<CultivationOperation> opQuery = new LambdaQueryWrapper<>();
        if (eligibleBatchIds != null) {
            opQuery.in(CultivationOperation::getBatchId, eligibleBatchIds);
        }
        List<CultivationOperation> operations = cultivationOperationMapper.selectList(opQuery);
        if (operations.isEmpty()) {
            return Collections.emptyList();
        }

        // 3. Aggregate total and pesticide operations per orchardId
        Map<Long, Long> totalOpsMap = new HashMap<>();
        Map<Long, Long> pestOpsMap = new HashMap<>();

        for (CultivationOperation op : operations) {
            Long orchardId = batchToOrchard.get(op.getBatchId());
            if (orchardId == null) continue;
            totalOpsMap.merge(orchardId, 1L, Long::sum);
            if ("PESTICIDE".equals(op.getOperationType())) {
                pestOpsMap.merge(orchardId, 1L, Long::sum);
            }
        }

        // 4. Fetch orchard names
        List<Long> orchardIds = new ArrayList<>(totalOpsMap.keySet());
        Map<Long, Orchard> orchardMap = fetchOrchardsById(orchardIds);

        // 5. Build VO list
        List<PlantingAnalysisVO.PestIncidence> result = orchardIds.stream()
                .map(orchardId -> {
                    Orchard orchard = orchardMap.get(orchardId);
                    String orchardName = orchard != null ? orchard.getOrchardName() : "Unknown";
                    long total = totalOpsMap.getOrDefault(orchardId, 0L);
                    long pest = pestOpsMap.getOrDefault(orchardId, 0L);
                    BigDecimal rate = (total > 0)
                            ? BigDecimal.valueOf(pest).divide(BigDecimal.valueOf(total), SCALE, ROUNDING)
                            : BigDecimal.ZERO;
                    return PlantingAnalysisVO.PestIncidence.builder()
                            .orchardId(orchardId)
                            .orchardName(orchardName)
                            .totalOperations(total)
                            .pestOperations(pest)
                            .incidenceRate(rate)
                            .build();
                })
                .sorted(Comparator.comparing(PlantingAnalysisVO.PestIncidence::getIncidenceRate).reversed())
                .collect(Collectors.toList());

        return result;
    }

    // ------------------------------------------------------------------ //
    //  comparePlots
    // ------------------------------------------------------------------ //

    @Override
    public List<PlantingAnalysisVO.PlotComparison> comparePlots(List<Long> orchardIds) {
        if (CollectionUtils.isEmpty(orchardIds)) {
            return Collections.emptyList();
        }

        // Fetch all 3 metrics without filters, then index by orchardId
        List<PlantingAnalysisVO.YieldPerMu> yieldList = getYieldRanking(null, null);
        List<PlantingAnalysisVO.PremiumRate> premiumList = getPremiumRates(null, null);
        List<PlantingAnalysisVO.PestIncidence> pestList = getPestIncidences(null);

        Map<Long, PlantingAnalysisVO.YieldPerMu> yieldMap = yieldList.stream()
                .collect(Collectors.toMap(PlantingAnalysisVO.YieldPerMu::getOrchardId, v -> v, (a, b) -> a));
        Map<Long, PlantingAnalysisVO.PremiumRate> premiumMap = premiumList.stream()
                .collect(Collectors.toMap(PlantingAnalysisVO.PremiumRate::getOrchardId, v -> v, (a, b) -> a));
        Map<Long, PlantingAnalysisVO.PestIncidence> pestMap = pestList.stream()
                .collect(Collectors.toMap(PlantingAnalysisVO.PestIncidence::getOrchardId, v -> v, (a, b) -> a));

        Map<Long, Orchard> orchardMap = fetchOrchardsById(orchardIds);

        return orchardIds.stream()
                .map(orchardId -> {
                    Orchard orchard = orchardMap.get(orchardId);
                    String orchardName = orchard != null ? orchard.getOrchardName() : "Unknown";
                    return PlantingAnalysisVO.PlotComparison.builder()
                            .orchardId(orchardId)
                            .orchardName(orchardName)
                            .yieldMetrics(yieldMap.get(orchardId))
                            .premiumMetrics(premiumMap.get(orchardId))
                            .pestMetrics(pestMap.get(orchardId))
                            .build();
                })
                .collect(Collectors.toList());
    }

    // ------------------------------------------------------------------ //
    //  helpers
    // ------------------------------------------------------------------ //

    private Map<Long, Orchard> fetchOrchardsById(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return orchardMapper.selectList(
                new LambdaQueryWrapper<Orchard>().in(Orchard::getId, ids)
        ).stream().collect(Collectors.toMap(Orchard::getId, o -> o));
    }
}
