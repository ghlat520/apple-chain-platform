package com.apple.chain.planting.service;

import com.apple.chain.planting.entity.OrchardMvp;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * MVP Orchard service interface.
 */
public interface OrchardMvpService extends IService<OrchardMvp> {

    IPage<OrchardMvp> listOrchards(int page, int size, String keyword, String status);

    OrchardMvp getOrchardDetail(Long id);

    OrchardMvp createOrchard(OrchardMvp orchard);

    OrchardMvp updateOrchard(Long id, OrchardMvp orchard);

    void deleteOrchard(Long id);

    void exportOrchards(String keyword, String status, HttpServletResponse response);
}
