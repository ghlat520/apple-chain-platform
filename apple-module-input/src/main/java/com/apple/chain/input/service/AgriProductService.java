package com.apple.chain.input.service;

import com.apple.chain.input.entity.AgriProduct;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Agricultural product service interface.
 */
public interface AgriProductService extends IService<AgriProduct> {

    IPage<AgriProduct> listProducts(int page, int size, String keyword, String type, String status);

    AgriProduct getDetail(Long id);

    AgriProduct createProduct(AgriProduct product);

    AgriProduct updateProduct(Long id, AgriProduct product);

    void deleteProduct(Long id);

    void exportProducts(String keyword, String type, String status, HttpServletResponse response);
}
