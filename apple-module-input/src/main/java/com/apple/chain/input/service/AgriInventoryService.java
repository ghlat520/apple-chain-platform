package com.apple.chain.input.service;

import com.apple.chain.input.entity.AgriInventory;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

/**
 * Agricultural input inventory service interface.
 */
public interface AgriInventoryService extends IService<AgriInventory> {

    IPage<AgriInventory> listInventory(int page, int size, String keyword, String status, Long farmerId);

    AgriInventory getDetail(Long id);

    AgriInventory createInventory(AgriInventory inventory);

    AgriInventory updateInventory(Long id, AgriInventory inventory);

    void deleteInventory(Long id);

    void exportInventory(String keyword, String status, Long farmerId, HttpServletResponse response);

    /** 查询 LOW/EMPTY 预警库存列表 */
    List<AgriInventory> listAlerts();
}
