package com.apple.chain.planting.service;

import com.apple.chain.planting.entity.Farmer;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Farmer service interface.
 */
public interface FarmerService extends IService<Farmer> {

    IPage<Farmer> listFarmers(int page, int size, String keyword, String status);

    Farmer getFarmerDetail(Long id);

    Farmer createFarmer(Farmer farmer);

    Farmer updateFarmer(Long id, Farmer farmer);

    void deleteFarmer(Long id);

    void exportFarmers(String keyword, String status, HttpServletResponse response);
}
