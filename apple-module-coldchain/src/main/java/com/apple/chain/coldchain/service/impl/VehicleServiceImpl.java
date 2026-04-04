package com.apple.chain.coldchain.service.impl;

import com.apple.chain.coldchain.entity.Vehicle;
import com.apple.chain.coldchain.mapper.VehicleMapper;
import com.apple.chain.coldchain.service.VehicleService;
import com.apple.chain.common.exception.BizException;
import com.apple.chain.common.result.ResultCode;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VehicleServiceImpl extends ServiceImpl<VehicleMapper, Vehicle> implements VehicleService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    public IPage<Vehicle> listVehicles(int page, int size, String keyword, String vehicleType, String status) {
        LambdaQueryWrapper<Vehicle> wrapper = new LambdaQueryWrapper<Vehicle>()
                .and(StringUtils.hasText(keyword), w -> w
                        .like(Vehicle::getPlateNumber, keyword)
                        .or().like(Vehicle::getDriverName, keyword))
                .eq(StringUtils.hasText(vehicleType), Vehicle::getVehicleType, vehicleType)
                .eq(StringUtils.hasText(status), Vehicle::getStatus, status)
                .orderByDesc(Vehicle::getCreateTime);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    public Vehicle getVehicleDetail(Long id) {
        Vehicle vehicle = getById(id);
        if (vehicle == null) {
            throw new BizException(ResultCode.NOT_FOUND, "车辆不存在");
        }
        return vehicle;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Vehicle createVehicle(Vehicle vehicle) {
        String prefix = LocalDate.now().format(DATE_FMT);
        int seq = baseMapper.nextSeq(prefix);
        vehicle.setVehicleCode(String.format("VH%s%04d", prefix, seq));
        vehicle.setStatus("IDLE");
        save(vehicle);
        return vehicle;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Vehicle updateVehicle(Long id, Vehicle vehicle) {
        getVehicleDetail(id);
        vehicle.setId(id);
        vehicle.setVehicleCode(null);
        updateById(vehicle);
        return getById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteVehicle(Long id) {
        Vehicle vehicle = getVehicleDetail(id);
        if ("IN_TRANSIT".equals(vehicle.getStatus())) {
            throw new BizException("运输中的车辆不能删除");
        }
        removeById(id);
    }

    @Override
    public void exportVehicles(String keyword, String vehicleType, String status, HttpServletResponse response) {
        List<Vehicle> list = list(new LambdaQueryWrapper<Vehicle>()
                .and(StringUtils.hasText(keyword), w -> w
                        .like(Vehicle::getPlateNumber, keyword)
                        .or().like(Vehicle::getDriverName, keyword))
                .eq(StringUtils.hasText(vehicleType), Vehicle::getVehicleType, vehicleType)
                .eq(StringUtils.hasText(status), Vehicle::getStatus, status)
                .orderByDesc(Vehicle::getCreateTime));
        try {
            response.setContentType("text/csv;charset=UTF-8");
            response.setHeader("Content-Disposition",
                    "attachment;filename=" + URLEncoder.encode("车辆列表.csv", StandardCharsets.UTF_8));
            PrintWriter writer = response.getWriter();
            writer.write('\uFEFF');
            writer.println("车辆编码,车牌号,车辆类型,品牌,载重(吨),容积(m3),最低控温,最高控温,驾驶员,电话,状态");
            for (Vehicle v : list) {
                writer.println(v.getVehicleCode() + "," + v.getPlateNumber() + "," + v.getVehicleType() + "," +
                        v.getBrand() + "," + v.getCapacity() + "," + v.getVolume() + "," +
                        v.getTemperatureMin() + "," + v.getTemperatureMax() + "," +
                        v.getDriverName() + "," + v.getDriverPhone() + "," + v.getStatus());
            }
            writer.flush();
        } catch (Exception e) {
            throw new BizException("导出失败: " + e.getMessage());
        }
    }
}
