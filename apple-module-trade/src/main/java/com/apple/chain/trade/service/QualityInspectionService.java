package com.apple.chain.trade.service;

import com.apple.chain.trade.entity.QualityInspection;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

public interface QualityInspectionService extends IService<QualityInspection> {

    IPage<QualityInspection> listInspections(int page, int size, Long orderId, String status, String result);

    QualityInspection getDetail(Long id);

    QualityInspection createInspection(QualityInspection inspection);

    QualityInspection inspect(Long id, QualityInspection data);

    QualityInspection accept(Long id);

    QualityInspection dispute(Long id, String reason);
}
