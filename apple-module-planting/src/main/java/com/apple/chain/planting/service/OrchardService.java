package com.apple.chain.planting.service;

import com.apple.chain.planting.entity.Orchard;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Orchard service interface.
 */
public interface OrchardService extends IService<Orchard> {

    IPage<Orchard> listOrchards(int page, int size, String keyword, String status, Long farmerId);

    Orchard getOrchardDetail(Long id);

    Orchard createOrchard(Orchard orchard);

    Orchard updateOrchard(Long id, Orchard orchard);

    void deleteOrchard(Long id);

    void exportOrchards(String keyword, String status, Long farmerId, HttpServletResponse response);
}
