package com.example.api.config;

import com.example.api.model.entity.*;
import com.example.api.repository.CommodityRepository;
import com.example.api.repository.DistributionRepository;
import com.example.api.repository.DriverRepository;
import com.example.api.repository.VehicleRepository;
import com.example.api.service.*;
import com.example.api.utils.DataTimeUtil;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class SampleDataInitializer implements CommandLineRunner {

    @Resource
    private CommodityRepository commodityRepository;

    @Resource
    private DistributionRepository distributionRepository;

    @Resource
    private DriverRepository driverRepository;

    @Resource
    private VehicleRepository vehicleRepository;

    @Resource
    private CommodityService commodityService;

    @Resource
    private WarehouseService warehouseService;

    @Resource
    private DriverService driverService;

    @Resource
    private VehicleService vehicleService;

    @Resource
    private EmployeeService employeeService;

    @Resource
    private SaleService saleService;

    @Resource
    private InventoryRecordService inventoryRecordService;

    @Override
    public void run(String... args) throws Exception {
        if (commodityRepository.count() > 0) {
            return;
        }

        Warehouse northHub = new Warehouse();
        northHub.setName("北区中转站");
        northHub.setPrinciple("李航");
        northHub = warehouseService.save(northHub);

        Warehouse southHub = new Warehouse();
        southHub.setName("南区中转站");
        southHub.setPrinciple("周颖");
        southHub = warehouseService.save(southHub);

        Commodity cattle = new Commodity();
        cattle.setName("肉牛");
        cattle.setPrice(520.0);
        cattle.setDescription("健康良好，已完成检疫");
        cattle.setCount(0);
        cattle = commodityService.save(cattle);

        Commodity sheep = new Commodity();
        sheep.setName("山羊");
        sheep.setPrice(68.0);
        sheep.setDescription("体况稳定，需控温通风");
        sheep.setCount(0);
        sheep = commodityService.save(sheep);

        Commodity hog = new Commodity();
        hog.setName("生猪");
        hog.setPrice(230.0);
        hog.setDescription("已完成隔离观察");
        hog.setCount(0);
        hog = commodityService.save(hog);

        Driver driverA = new Driver();
        driverA.setName("王强");
        driverA.setGender("男性");
        driverA.setPhone("13800001111");
        driverA.setAddress("北区平原路 88 号");
        driverA.setIdCard("410000199001011234");
        driverA.setLicense("B2");
        driverA.setScore("12");
        driverA.setDriving(false);
        driverA = driverService.save(driverA);

        Driver driverB = new Driver();
        driverB.setName("赵敏");
        driverB.setGender("女性");
        driverB.setPhone("13900002222");
        driverB.setAddress("南区绿港路 16 号");
        driverB.setIdCard("320000199202021234");
        driverB.setLicense("B2");
        driverB.setScore("11");
        driverB.setDriving(false);
        driverB = driverService.save(driverB);

        Driver driverC = new Driver();
        driverC.setName("刘洋");
        driverC.setGender("男性");
        driverC.setPhone("13700003333");
        driverC.setAddress("西区牧场路 6 号");
        driverC.setIdCard("510000198812121234");
        driverC.setLicense("B2");
        driverC.setScore("12");
        driverC.setDriving(false);
        driverC = driverService.save(driverC);

        Vehicle vehicleA = new Vehicle();
        vehicleA.setNumber("京A8855");
        vehicleA.setType("货车");
        vehicleA.setDriving(false);
        vehicleA = vehicleService.save(vehicleA);

        Vehicle vehicleB = new Vehicle();
        vehicleB.setNumber("沪B6060");
        vehicleB.setType("卡车");
        vehicleB.setDriving(false);
        vehicleB = vehicleService.save(vehicleB);

        Vehicle vehicleC = new Vehicle();
        vehicleC.setNumber("粤C3322");
        vehicleC.setType("货车");
        vehicleC.setDriving(false);
        vehicleC = vehicleService.save(vehicleC);

        Employee employeeA = new Employee();
        employeeA.setName("陈杰");
        employeeA.setGender("男性");
        employeeA.setPhone("13600004444");
        employeeA.setAddress("北区安牧街 9 号");
        employeeA.setIdCard("410000199307071234");
        employeeA.setDepartment(northHub.getName());
        employeeService.save(employeeA);

        Employee employeeB = new Employee();
        employeeB.setName("苏晴");
        employeeB.setGender("女性");
        employeeB.setPhone("13500005555");
        employeeB.setAddress("南区汇港路 2 号");
        employeeB.setIdCard("320000199509091234");
        employeeB.setDepartment(southHub.getName());
        employeeService.save(employeeB);

        Sale saleA = new Sale();
        saleA.setCompany("牧源合作社");
        saleA.setNumber("6222-8800-1122-4455");
        saleA.setCommodity(cattle.getName());
        saleA.setCount("80");
        saleA.setPrice(80 * cattle.getPrice());
        saleA.setPhone("021-88990011");
        saleA.setDescription("季度转运结算");
        saleA.setPay(false);
        saleService.save(saleA);

        Sale saleB = new Sale();
        saleB.setCompany("青禾养殖场");
        saleB.setNumber("6214-3300-7788-0099");
        saleB.setCommodity(hog.getName());
        saleB.setCount("120");
        saleB.setPrice(120 * hog.getPrice());
        saleB.setPhone("0755-82223344");
        saleB.setDescription("应急调拨结算");
        saleB.setPay(true);
        saleService.save(saleB);

        InventoryRecord inCattle = new InventoryRecord();
        inCattle.setWid(northHub.getId());
        inCattle.setCid(cattle.getId());
        inCattle.setName(cattle.getName());
        inCattle.setCount(150);
        inCattle.setDescription("北区入栏");
        inventoryRecordService.in(inCattle);

        InventoryRecord outCattle = new InventoryRecord();
        outCattle.setWid(northHub.getId());
        outCattle.setCid(cattle.getId());
        outCattle.setName(cattle.getName());
        outCattle.setCount(30);
        outCattle.setDescription("调拨出栏");
        inventoryRecordService.out(outCattle);

        InventoryRecord inSheep = new InventoryRecord();
        inSheep.setWid(southHub.getId());
        inSheep.setCid(sheep.getId());
        inSheep.setName(sheep.getName());
        inSheep.setCount(200);
        inSheep.setDescription("南区入栏");
        inventoryRecordService.in(inSheep);

        InventoryRecord outSheep = new InventoryRecord();
        outSheep.setWid(southHub.getId());
        outSheep.setCid(sheep.getId());
        outSheep.setName(sheep.getName());
        outSheep.setCount(50);
        outSheep.setDescription("分批转运");
        inventoryRecordService.out(outSheep);

        InventoryRecord inHog = new InventoryRecord();
        inHog.setWid(northHub.getId());
        inHog.setCid(hog.getId());
        inHog.setName(hog.getName());
        inHog.setCount(180);
        inHog.setDescription("北区入栏");
        inventoryRecordService.in(inHog);

        Distribution dispatchA = new Distribution();
        dispatchA.setDid(driverA.getId());
        dispatchA.setVid(vehicleA.getId());
        dispatchA.setDriver(driverA.getName());
        dispatchA.setNumber(vehicleA.getNumber());
        dispatchA.setPhone("020-90001122");
        dispatchA.setAddress("广州市白云区物流大道 18 号");
        dispatchA.setOrigin("北区中转站");
        dispatchA.setDestination("广州市屠宰中心");
        dispatchA.setRoutePlan("北环高速 → 京港澳高速 → 广州北站");
        dispatchA.setRouteNodes("北区中转站-检查点A-检查点B-广州北站");
        dispatchA.setCurrentNode("北区中转站");
        dispatchA.setWarningLevel(0);
        dispatchA.setWarningNote("");
        dispatchA.setUrgent(false);
        dispatchA.setCare("控温通风");
        dispatchA.setTime(DataTimeUtil.getNowTimeString());
        dispatchA.setStatus(0);
        distributionRepository.save(dispatchA);

        Distribution dispatchB = new Distribution();
        dispatchB.setDid(driverB.getId());
        dispatchB.setVid(vehicleB.getId());
        dispatchB.setDriver(driverB.getName());
        dispatchB.setNumber(vehicleB.getNumber());
        dispatchB.setPhone("0571-88668899");
        dispatchB.setAddress("杭州市临平区中转站 3 号");
        dispatchB.setOrigin("南区中转站");
        dispatchB.setDestination("杭州市检疫中转站");
        dispatchB.setRoutePlan("沪昆高速 → 杭州绕城");
        dispatchB.setRouteNodes("南区中转站-检查点C-检查点D-杭州市检疫站");
        dispatchB.setCurrentNode("检查点D");
        dispatchB.setWarningLevel(1);
        dispatchB.setWarningNote("轻微应激，已加强通风");
        dispatchB.setUrgent(true);
        dispatchB.setCare("隔离区装载, 减少应激");
        dispatchB.setTime(DataTimeUtil.getNowTimeString());
        dispatchB.setStatus(1);
        distributionRepository.save(dispatchB);
        driverRepository.updateDriving(true, driverB.getId());
        vehicleRepository.updateDriving(true, vehicleB.getId());

        Distribution dispatchC = new Distribution();
        dispatchC.setDid(driverC.getId());
        dispatchC.setVid(vehicleC.getId());
        dispatchC.setDriver(driverC.getName());
        dispatchC.setNumber(vehicleC.getNumber());
        dispatchC.setPhone("023-55667788");
        dispatchC.setAddress("重庆市渝北区转运中心");
        dispatchC.setOrigin("北区中转站");
        dispatchC.setDestination("重庆市屠宰中心");
        dispatchC.setRoutePlan("沪蓉高速 → 渝北物流通道");
        dispatchC.setRouteNodes("北区中转站-检查点E-检查点F-重庆市屠宰中心");
        dispatchC.setCurrentNode("重庆市屠宰中心");
        dispatchC.setWarningLevel(0);
        dispatchC.setWarningNote("");
        dispatchC.setUrgent(false);
        dispatchC.setCare("控温通风");
        dispatchC.setTime(DataTimeUtil.getNowTimeString());
        dispatchC.setStatus(2);
        distributionRepository.save(dispatchC);
        driverRepository.updateDriving(false, driverC.getId());
        vehicleRepository.updateDriving(false, vehicleC.getId());
    }
}
