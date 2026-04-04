package com.apple.domain.service;

import com.apple.common.exception.BusinessException;
import com.apple.domain.entity.Orchard;
import com.apple.domain.mapper.OrchardMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrchardService {

    private final OrchardMapper orchardMapper;

    public Page<Orchard> listPage(int page, int size, String keyword,
                                   String variety, String status, Long farmerUserId) {
        LambdaQueryWrapper<Orchard> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(keyword)) {
            wrapper.like(Orchard::getOrchardName, keyword)
                    .or().like(Orchard::getFarmerName, keyword)
                    .or().like(Orchard::getOrchardCode, keyword);
        }
        if (StringUtils.isNotBlank(variety)) {
            wrapper.eq(Orchard::getVariety, variety);
        }
        if (StringUtils.isNotBlank(status)) {
            wrapper.eq(Orchard::getStatus, status);
        }
        if (farmerUserId != null) {
            wrapper.eq(Orchard::getFarmerUserId, farmerUserId);
        }
        wrapper.orderByDesc(Orchard::getCreatedAt);
        return orchardMapper.selectPage(new Page<>(page, size), wrapper);
    }

    public List<Orchard> listAll() {
        return orchardMapper.selectList(new LambdaQueryWrapper<Orchard>()
                .orderByDesc(Orchard::getCreatedAt));
    }

    public Orchard getById(Long id) {
        Orchard orchard = orchardMapper.selectById(id);
        if (orchard == null) {
            throw BusinessException.of(404, "Orchard not found: " + id);
        }
        return orchard;
    }

    public Orchard create(Orchard orchard) {
        orchard.setOrchardCode(generateOrchardCode());
        if (orchard.getStatus() == null) {
            orchard.setStatus("active");
        }
        orchardMapper.insert(orchard);
        return orchard;
    }

    public Orchard update(Long id, Orchard update) {
        Orchard existing = getById(id);
        existing.setOrchardName(update.getOrchardName());
        existing.setFarmerName(update.getFarmerName());
        existing.setProvince(update.getProvince());
        existing.setCity(update.getCity());
        existing.setDistrict(update.getDistrict());
        existing.setAddress(update.getAddress());
        existing.setArea(update.getArea());
        existing.setVariety(update.getVariety());
        existing.setCertificationLevel(update.getCertificationLevel());
        existing.setStatus(update.getStatus());
        orchardMapper.updateById(existing);
        return existing;
    }

    public void delete(Long id) {
        getById(id);
        orchardMapper.deleteById(id);
    }

    public long countTotal() {
        return orchardMapper.selectCount(null);
    }

    public long countFarmers() {
        return orchardMapper.selectCount(
                new LambdaQueryWrapper<Orchard>()
                        .select(Orchard::getFarmerUserId)
                        .groupBy(Orchard::getFarmerUserId))
                .longValue();
    }

    private String generateOrchardCode() {
        String ts = java.time.LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "OC" + ts + String.format("%03d", (int) (Math.random() * 1000));
    }
}
