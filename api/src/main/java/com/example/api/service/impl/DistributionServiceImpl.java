package com.example.api.service.impl;

import com.example.api.model.entity.Distribution;
import com.example.api.model.entity.Driver;
import com.example.api.model.entity.Order;
import com.example.api.model.entity.Vehicle;
import com.example.api.repository.DistributionRepository;
import com.example.api.repository.DriverRepository;
import com.example.api.repository.OrderRepository;
import com.example.api.repository.VehicleRepository;
import com.example.api.service.DistributionService;
import com.example.api.service.NotificationService;
import com.example.api.utils.DataTimeUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

@Service
public class DistributionServiceImpl implements DistributionService {

    @Resource
    private DistributionRepository distributionRepository;

    @Resource
    private DriverRepository driverRepository;

    @Resource
    private VehicleRepository vehicleRepository;

    @Resource
    private OrderRepository orderRepository;

    @Resource
    private NotificationService notificationService;

    @Override
    public Distribution save(Distribution distribution) throws Exception {
        Distribution existing = null;
        String id = distribution.getId();
        if (id != null && !id.trim().isEmpty()) {
            existing = distributionRepository.findById(id).orElse(null);
        } else if (distribution.getOrderNo() != null && !distribution.getOrderNo().trim().isEmpty()) {
            existing = distributionRepository.findByOrderNo(distribution.getOrderNo().trim());
            if (existing != null) {
                distribution.setId(existing.getId());
            }
        }

        boolean isNew = existing == null;
        String did = distribution.getDid();
        String vid = distribution.getVid();
        boolean hasDid = did != null && !did.trim().isEmpty();
        boolean hasVid = vid != null && !vid.trim().isEmpty();
        if (hasDid != hasVid) {
            throw new Exception("司机和车辆必须同时指定");
        }

        if (hasDid) {
            if (isNew) {
                Optional<Driver> driver = driverRepository.findById(did);
                Optional<Vehicle> vehicle = vehicleRepository.findById(vid);
                if (driver.isEmpty() || vehicle.isEmpty())
                    throw new Exception("请求参数错误");
                if (driver.get().isDriving() || vehicle.get().isDriving())
                    throw new Exception("司机或车辆状态不可用");
                driverRepository.updateDriving(true, did);
                vehicleRepository.updateDriving(true, vid);
            } else {
                if (existing != null && !did.equals(existing.getDid())) {
                    Optional<Driver> driver = driverRepository.findById(did);
                    if (driver.isEmpty())
                        throw new Exception("请求参数错误");
                    if (driver.get().isDriving())
                        throw new Exception("司机状态不可用");
                    if (existing.getDid() != null && !existing.getDid().trim().isEmpty()) {
                        driverRepository.updateDriving(false, existing.getDid());
                    }
                    driverRepository.updateDriving(true, did);
                }
                if (existing != null && !vid.equals(existing.getVid())) {
                    Optional<Vehicle> vehicle = vehicleRepository.findById(vid);
                    if (vehicle.isEmpty())
                        throw new Exception("请求参数错误");
                    if (vehicle.get().isDriving())
                        throw new Exception("车辆状态不可用");
                    if (existing.getVid() != null && !existing.getVid().trim().isEmpty()) {
                        vehicleRepository.updateDriving(false, existing.getVid());
                    }
                    vehicleRepository.updateDriving(true, vid);
                }
            }
        }

        Distribution saved = distributionRepository.save(distribution);
        syncOrderStatus(saved);
        return saved;
    }

    @Override
    public List<Distribution> findAll() {
        return distributionRepository.findAll();
    }

    private void syncOrderStatus(Distribution distribution) {
        if (distribution.getOrderNo() == null || distribution.getOrderNo().trim().isEmpty()) {
            return;
        }
        Order order = orderRepository.findByOrderNo(distribution.getOrderNo().trim());
        if (order == null || order.getStatus() == null) {
            return;
        }
        if (order.getStatus() == -1) {
            return;
        }
        Integer distStatus = distribution.getStatus();
        Integer targetStatus = null;
        String statusMessage = null;
        if (distStatus != null) {
            if (distStatus == 1 && order.getStatus() == 1) {
                targetStatus = 2;
                statusMessage = "您的订单已开始运输";
            } else if (distStatus == 2 && (order.getStatus() == 1 || order.getStatus() == 2)) {
                targetStatus = 3;
                statusMessage = "您的订单已送达";
            }
        }
        if (targetStatus != null) {
            Integer oldStatus = order.getStatus();
            order.setStatus(targetStatus);
            order.setUpdateAt(DataTimeUtil.getNowTimeString());
            orderRepository.save(order);

            // 发送通知给用户
            if (order.getUserId() != null && statusMessage != null) {
                String title = "订单状态更新";
                String content = String.format("%s (订单号: %s)", statusMessage, order.getOrderNo());
                notificationService.createUserNotification(title, content, order.getUserId(), distribution.getId());
            }
        }
    }

}
