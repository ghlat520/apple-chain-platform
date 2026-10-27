package com.apple.chain.input.service;

import com.apple.chain.input.entity.AgriSupplier;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Agricultural input supplier service interface.
 */
public interface AgriSupplierService extends IService<AgriSupplier> {

    IPage<AgriSupplier> listSuppliers(int page, int size, String keyword, String status);

    AgriSupplier getDetail(Long id);

    AgriSupplier createSupplier(AgriSupplier supplier);

    AgriSupplier updateSupplier(Long id, AgriSupplier supplier);

    void deleteSupplier(Long id);

    void exportSuppliers(String keyword, String status, HttpServletResponse response);

    AgriSupplier auditSupplier(Long id, String decision, String reason);

    AgriSupplier reinstateSupplier(Long id);
}
