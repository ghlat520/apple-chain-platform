package com.apple.chain.coldchain.service;

import com.apple.chain.coldchain.entity.Vehicle;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletResponse;

public interface VehicleService extends IService<Vehicle> {

    IPage<Vehicle> listVehicles(int page, int size, String keyword, String vehicleType, String status);

    Vehicle getVehicleDetail(Long id);

    Vehicle createVehicle(Vehicle vehicle);

    Vehicle updateVehicle(Long id, Vehicle vehicle);

    void deleteVehicle(Long id);

    void exportVehicles(String keyword, String vehicleType, String status, HttpServletResponse response);
}
