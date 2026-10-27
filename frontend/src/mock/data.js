/**
 * 苹果全产业链 Mock 数据 — 数据闭环演示
 *
 * 业务闭环：种植 → 农资 → 仓储 → 冷链 → 交易 → 溯源 → 金融 → 大数据
 *
 * 所有 ID 使用确定性数字，保证跨模块关联完整
 */

// ===== 农户/果园 =====
export const farmers = [
  { id: 1, name: '王建国', phone: '13900001001', village: '洛川县凤栖镇', area: 50, joinDate: '2023-03-15' },
  { id: 2, name: '李红梅', phone: '13900001002', village: '礼泉县石潭镇', area: 35, joinDate: '2023-05-20' },
  { id: 3, name: '张向阳', phone: '13900001003', village: '白水县雷牙镇', area: 80, joinDate: '2022-11-10' },
  { id: 4, name: '陈翠花', phone: '13900001004', village: '静宁县威戎镇', area: 45, joinDate: '2024-01-08' },
  { id: 5, name: '赵德明', phone: '13900001005', village: '栖霞市蛇窝泊镇', area: 120, joinDate: '2022-06-22' }
]

export const orchards = [
  { id: 1, name: '建国红富士园', farmerId: 1, farmerName: '王建国', location: '陕西省延安市洛川县', area: 50, variety: '红富士', plantYear: 2018, status: 'ACTIVE' },
  { id: 2, name: '红梅嘎啦园', farmerId: 2, farmerName: '李红梅', location: '陕西省咸阳市礼泉县', area: 35, variety: '嘎啦', plantYear: 2020, status: 'ACTIVE' },
  { id: 3, name: '向阳瑞雪园', farmerId: 3, farmerName: '张向阳', location: '陕西省渭南市白水县', area: 80, variety: '瑞雪', plantYear: 2019, status: 'ACTIVE' },
  { id: 4, name: '翠花秦冠园', farmerId: 4, farmerName: '陈翠花', location: '甘肃省平凉市静宁县', area: 45, variety: '秦冠', plantYear: 2017, status: 'ACTIVE' },
  { id: 5, name: '德明富士庄园', farmerId: 5, farmerName: '赵德明', location: '山东省烟台市栖霞市', area: 120, variety: '红富士', plantYear: 2016, status: 'ACTIVE' }
]

// ===== M2 农资 =====
export const suppliers = [
  { id: 1, supplierCode: 'SUP20240001', name: '陕西绿丰化肥有限公司', contactPerson: '刘经理', phone: '029-88881001', address: '西安市未央区', license: '91610000MA6X1', qualification: '农药经营许可证', creditScore: 92, status: 'APPROVED', auditTime: '2024-03-20 10:30:00', auditor: '系统管理员' },
  { id: 2, supplierCode: 'SUP20240002', name: '杨凌农科大种业', contactPerson: '马教授', phone: '029-87091002', address: '咸阳市杨凌区', license: '91610000MA6X2', qualification: '种子生产经营许可证', creditScore: 88, status: 'APPROVED', auditTime: '2024-04-15 14:00:00', auditor: '系统管理员' },
  { id: 3, supplierCode: 'SUP20240003', name: '山东农药厂直营店', contactPerson: '孙总', phone: '0535-5551003', address: '烟台市福山区', license: '91370000MA3X1', qualification: '农药登记证', creditScore: 75, status: 'APPROVED', auditTime: '2024-05-10 09:00:00', auditor: '系统管理员' },
  { id: 4, supplierCode: 'SUP20250001', name: '甘肃绿源农资', contactPerson: '王老板', phone: '0933-6661004', address: '平凉市崆峒区', license: '9620000MA5X1', qualification: '营业执照', creditScore: 65, status: 'PENDING', auditTime: null, auditor: null },
  { id: 5, supplierCode: 'SUP20240004', name: '陕西农机装备集团', contactPerson: '赵工', phone: '029-83331005', address: '西安市雁塔区', license: '91610000MA6X5', qualification: '工业产品生产许可证', creditScore: 80, status: 'APPROVED', auditTime: '2024-06-01 11:00:00', auditor: '系统管理员' }
]

export const products = [
  { id: 1, productCode: 'AP20240001', name: '复合肥 15-15-15', type: 'FERTILIZER', manufacturer: '陕西绿丰化肥有限公司', spec: '50kg/袋', batchNo: 'HF20240301', productionDate: '2024-03-01', expiryDate: '2025-03-01', registration: '农肥(2024)准字001号', status: 'ACTIVE' },
  { id: 2, productCode: 'AP20240002', name: '瑞雪苹果苗', type: 'SEED', manufacturer: '杨凌农科大种业', spec: '裸根苗', batchNo: 'ZM20240401', productionDate: '2024-04-01', expiryDate: '2025-04-01', registration: '陕审果2024001', status: 'ACTIVE' },
  { id: 3, productCode: 'AP20240003', name: '多菌灵可湿性粉剂', type: 'PESTICIDE', manufacturer: '山东农药厂直营店', spec: '500g/瓶', batchNo: 'NJ20240501', productionDate: '2024-05-01', expiryDate: '2026-05-01', registration: 'PD20050001', status: 'ACTIVE' },
  { id: 4, productCode: 'AP20240004', name: '有机生物菌肥', type: 'FERTILIZER', manufacturer: '陕西绿丰化肥有限公司', spec: '40kg/袋', batchNo: 'SJ20240601', productionDate: '2024-06-01', expiryDate: '2025-12-01', registration: '微生物肥(2024)准字002号', status: 'ACTIVE' },
  { id: 5, productCode: 'AP20240005', name: '高效氯氟氰菊酯', type: 'PESTICIDE', manufacturer: '山东农药厂直营店', spec: '100ml/瓶', batchNo: 'JN20240701', productionDate: '2024-07-01', expiryDate: '2026-07-01', registration: 'PD20080002', status: 'ACTIVE' },
  { id: 6, productCode: 'AP20240006', name: '果树修剪剪套装', type: 'TOOL', manufacturer: '陕西农机装备集团', spec: '3件套', batchNo: 'GJ20240801', productionDate: '2024-08-01', expiryDate: null, registration: null, status: 'ACTIVE' }
]

export const purchases = [
  { id: 1, purchaseNo: 'PU20260301001', productId: 1, productName: '复合肥 15-15-15', supplierId: 1, supplierName: '陕西绿丰化肥有限公司', quantity: 2000, unit: 'kg', unitPrice: 3.50, totalAmount: 7000.00, purchaseDate: '2026-03-01', farmerId: 1, status: 'RECEIVED', remark: '春季施肥备货' },
  { id: 2, purchaseNo: 'PU20260301002', productId: 3, productName: '多菌灵可湿性粉剂', supplierId: 3, supplierName: '山东农药厂直营店', quantity: 50, unit: '瓶', unitPrice: 28.00, totalAmount: 1400.00, purchaseDate: '2026-03-05', farmerId: 1, status: 'RECEIVED', remark: '病害防治用药' },
  { id: 3, purchaseNo: 'PU20260302001', productId: 4, productName: '有机生物菌肥', supplierId: 1, supplierName: '陕西绿丰化肥有限公司', quantity: 1000, unit: 'kg', unitPrice: 5.80, totalAmount: 5800.00, purchaseDate: '2026-03-10', farmerId: 3, status: 'RECEIVED', remark: '有机果园施肥' },
  { id: 4, purchaseNo: 'PU20260315001', productId: 5, productName: '高效氯氟氰菊酯', supplierId: 3, supplierName: '山东农药厂直营店', quantity: 30, unit: '瓶', unitPrice: 35.00, totalAmount: 1050.00, purchaseDate: '2026-03-15', farmerId: 5, status: 'APPROVED', remark: '虫害防治' },
  { id: 5, purchaseNo: 'PU20260401001', productId: 1, productName: '复合肥 15-15-15', supplierId: 1, supplierName: '陕西绿丰化肥有限公司', quantity: 3000, unit: 'kg', unitPrice: 3.50, totalAmount: 10500.00, purchaseDate: '2026-04-01', farmerId: 5, status: 'PENDING', remark: '大客户批量采购' }
]

export const inventory = [
  { id: 1, productId: 1, productName: '复合肥 15-15-15', farmerId: 1, stockQuantity: 1500, unit: 'kg', warningLevel: 200, maxLevel: 5000, warehouse: '王建国仓库', status: 'NORMAL', remark: '' },
  { id: 2, productId: 3, productName: '多菌灵可湿性粉剂', farmerId: 1, stockQuantity: 35, unit: '瓶', warningLevel: 10, maxLevel: 100, warehouse: '王建国仓库', status: 'NORMAL', remark: '' },
  { id: 3, productId: 4, productName: '有机生物菌肥', farmerId: 3, stockQuantity: 800, unit: 'kg', warningLevel: 100, maxLevel: 2000, warehouse: '向阳仓库', status: 'NORMAL', remark: '' },
  { id: 4, productId: 1, productName: '复合肥 15-15-15', farmerId: 5, stockQuantity: 50, unit: 'kg', warningLevel: 200, maxLevel: 5000, warehouse: '德明大仓', status: 'LOW', remark: '库存紧张，需补货' },
  { id: 5, productId: 5, productName: '高效氯氟氰菊酯', farmerId: 2, stockQuantity: 0, unit: '瓶', warningLevel: 5, maxLevel: 50, warehouse: '红梅仓库', status: 'EMPTY', remark: '已用完' }
]

export const usageRecords = [
  { id: 1, productId: 1, productName: '复合肥 15-15-15', batchId: 1, batchCode: 'BATCH20260101', orchardId: 1, orchardName: '建国红富士园', quantity: 500, unit: 'kg', usageDate: '2026-03-10', operator: '王建国', method: '撒施', traceCode: 'TC20260310001', remark: '春季追肥' },
  { id: 2, productId: 3, productName: '多菌灵可湿性粉剂', batchId: 1, batchCode: 'BATCH20260101', orchardId: 1, orchardName: '建国红富士园', quantity: 5, unit: '瓶', usageDate: '2026-03-15', operator: '王建国', method: '喷洒', traceCode: 'TC20260310001', remark: '花期病害预防' },
  { id: 3, productId: 4, productName: '有机生物菌肥', batchId: 2, batchCode: 'BATCH20260201', orchardId: 3, orchardName: '向阳瑞雪园', quantity: 300, unit: 'kg', usageDate: '2026-03-12', operator: '张向阳', method: '穴施', traceCode: 'TC20260312001', remark: '有机肥追施' },
  { id: 4, productId: 1, productName: '复合肥 15-15-15', batchId: 3, batchCode: 'BATCH20260301', orchardId: 5, orchardName: '德明富士庄园', quantity: 800, unit: 'kg', usageDate: '2026-03-18', operator: '赵德明', method: '撒施', traceCode: 'TC20260318001', remark: '大园追肥' }
]

// ===== M5 仓储 =====
export const warehouses = [
  { id: 1, warehouseCode: 'WH202401010001', name: '洛川冷链一号库', type: 'COLD', location: '延安市洛川县凤栖镇', capacity: 500.00, usedCapacity: 320.00, temperature: 2.5, humidity: 85.0, manager: '周仓管', phone: '13900002001', status: 'ACTIVE', remark: '主力冷库' },
  { id: 2, warehouseCode: 'WH202401010002', name: '礼泉气调保鲜库', type: 'ATMOSPHERE', location: '咸阳市礼泉县石潭镇', capacity: 200.00, usedCapacity: 200.00, temperature: 1.2, humidity: 90.0, manager: '吴仓管', phone: '13900002002', status: 'FULL', remark: '满载运行' },
  { id: 3, warehouseCode: 'WH202401020001', name: '白水普通仓储库', type: 'NORMAL', location: '渭南市白水县雷牙镇', capacity: 300.00, usedCapacity: 50.00, temperature: 18.0, humidity: 55.0, manager: '郑仓管', phone: '13900002003', status: 'ACTIVE', remark: '常温库' },
  { id: 4, warehouseCode: 'WH202402010001', name: '静宁冷链二号库', type: 'COLD', location: '平凉市静宁县威戎镇', capacity: 400.00, usedCapacity: 0, temperature: null, humidity: null, manager: '钱仓管', phone: '13900002004', status: 'MAINTENANCE', remark: '设备检修中' },
  { id: 5, warehouseCode: 'WH202403010001', name: '栖霞冷链物流仓', type: 'COLD', location: '烟台市栖霞市蛇窝泊镇', capacity: 800.00, usedCapacity: 550.00, temperature: 3.0, humidity: 82.0, manager: '孙仓管', phone: '13900002005', status: 'ACTIVE', remark: '最大冷库' }
]

export const receipts = [
  { id: 1, receiptNo: 'WR202604010001', warehouseId: 1, warehouseName: '洛川冷链一号库', farmerId: 1, farmerName: '王建国', batchCode: 'BATCH20260101', variety: '红富士', grade: 'A', quantity: 5000.00, unitValue: 3.50, totalValue: 17500.00, inboundDate: '2026-04-01', validUntil: '2026-10-01', traceCode: 'TC20260310001', status: 'VALID', statusChangeBy: 'SYSTEM', statusChangeTime: '2026-04-01 08:00:00', remark: '建国首批入库' },
  { id: 2, receiptNo: 'WR202604010002', warehouseId: 2, warehouseName: '礼泉气调保鲜库', farmerId: 2, farmerName: '李红梅', batchCode: 'BATCH20260202', variety: '嘎啦', grade: 'A', quantity: 3000.00, unitValue: 4.20, totalValue: 12600.00, inboundDate: '2026-04-01', validUntil: '2026-09-01', traceCode: null, status: 'PLEDGED', statusChangeBy: 'SYSTEM', statusChangeTime: '2026-04-02 10:00:00', remark: '已质押给银行' },
  { id: 3, receiptNo: 'WR202604020001', warehouseId: 5, warehouseName: '栖霞冷链物流仓', farmerId: 5, farmerName: '赵德明', batchCode: 'BATCH20260301', variety: '红富士', grade: 'A', quantity: 15000.00, unitValue: 3.80, totalValue: 57000.00, inboundDate: '2026-04-02', validUntil: '2026-11-02', traceCode: 'TC20260318001', status: 'VALID', statusChangeBy: 'SYSTEM', statusChangeTime: '2026-04-02 09:00:00', remark: '大客户入库' }
]

export const warehouseRecords = [
  { id: 1, recordNo: 'REC202604010001', warehouseId: 1, warehouseName: '洛川冷链一号库', recordType: 'INBOUND', batchCode: 'BATCH20260101', variety: '红富士', grade: 'A', quantity: 5000.00, temperature: 2.5, humidity: 85.0, operator: '周仓管', recordDate: '2026-04-01', traceCode: 'TC20260310001', remark: '王建国入库' },
  { id: 2, recordNo: 'REC202604010002', warehouseId: 2, warehouseName: '礼泉气调保鲜库', recordType: 'INBOUND', batchCode: 'BATCH20260202', variety: '嘎啦', grade: 'A', quantity: 3000.00, temperature: 1.0, humidity: 90.0, operator: '吴仓管', recordDate: '2026-04-01', traceCode: null, remark: '李红梅入库' },
  { id: 3, recordNo: 'REC202604020001', warehouseId: 5, warehouseName: '栖霞冷链物流仓', recordType: 'INBOUND', batchCode: 'BATCH20260301', variety: '红富士', grade: 'A', quantity: 15000.00, temperature: 3.0, humidity: 82.0, operator: '孙仓管', recordDate: '2026-04-02', traceCode: 'TC20260318001', remark: '赵德明大客户入库' },
  { id: 4, recordNo: 'REC202604050001', warehouseId: 1, warehouseName: '洛川冷链一号库', recordType: 'OUTBOUND', batchCode: 'BATCH20260101', variety: '红富士', grade: 'A', quantity: 2000.00, temperature: 2.5, humidity: 85.0, operator: '周仓管', recordDate: '2026-04-05', traceCode: 'TC20260310001', remark: '出库发往超市' }
]

// ===== M6 冷链 =====
export const vehicles = [
  { id: 1, vehicleCode: 'VH202401010001', plateNumber: '陕A·冷001', vehicleType: 'REFRIGERATED', brand: '福田欧马可', capacity: 8.00, volume: 35.00, temperatureMin: -5.0, temperatureMax: 5.0, driverName: '马师傅', driverPhone: '13900003001', status: 'IDLE', remark: '主力冷藏车' },
  { id: 2, vehicleCode: 'VH202401010002', plateNumber: '鲁F·冷002', vehicleType: 'REFRIGERATED', brand: '江淮帅铃', capacity: 12.00, volume: 50.00, temperatureMin: -3.0, temperatureMax: 4.0, driverName: '李师傅', driverPhone: '13900003002', status: 'IN_TRANSIT', remark: '运输中' },
  { id: 3, vehicleCode: 'VH202401020001', plateNumber: '陕B·保001', vehicleType: 'INSULATED', brand: '东风天锦', capacity: 10.00, volume: 42.00, temperatureMin: 0.0, temperatureMax: 10.0, driverName: '王师傅', driverPhone: '13900003003', status: 'IDLE', remark: '保温车' },
  { id: 4, vehicleCode: 'VH202403010001', plateNumber: '甘L·普001', vehicleType: 'NORMAL', brand: '解放J6', capacity: 15.00, volume: 60.00, temperatureMin: null, temperatureMax: null, driverName: '张师傅', driverPhone: '13900003004', status: 'MAINTENANCE', remark: '年检中' }
]

export const transports = [
  { id: 1, orderNo: 'TP202604050001', vehicleId: 2, vehiclePlate: '鲁F·冷002', warehouseId: 5, warehouseName: '栖霞冷链物流仓', destination: '上海市江桥批发市场', receiverName: '上海果品批发商', receiverPhone: '021-55550001', products: [{ name: '红富士', grade: 'A', quantity: 5000, unit: 'kg' }], totalWeight: 5000.00, temperatureReq: '0~4', actualTemp: 2.8, status: 'IN_TRANSIT', startTime: '2026-04-05 06:00:00', endTime: null, remark: '发往上海' },
  { id: 2, orderNo: 'TP202604040001', vehicleId: 1, vehiclePlate: '陕A·冷001', warehouseId: 1, warehouseName: '洛川冷链一号库', destination: '西安市胡家庙果品市场', receiverName: '西安鲜果连锁', receiverPhone: '029-88883001', products: [{ name: '红富士', grade: 'A', quantity: 2000, unit: 'kg' }], totalWeight: 2000.00, temperatureReq: '0~4', actualTemp: 2.2, status: 'COMPLETED', startTime: '2026-04-04 05:00:00', endTime: '2026-04-04 09:30:00', remark: '已完成' },
  { id: 3, orderNo: 'TP202604060001', vehicleId: 3, vehiclePlate: '陕B·保001', warehouseId: 3, warehouseName: '白水普通仓储库', destination: '洛川县凤栖镇直销点', receiverName: '洛川苹果直销店', receiverPhone: '13900003005', products: [{ name: '瑞雪', grade: 'B', quantity: 3000, unit: 'kg' }], totalWeight: 3000.00, temperatureReq: '0~8', actualTemp: null, status: 'PENDING', startTime: null, endTime: null, remark: '待发车' }
]

export const precoolingRecords = [
  { id: 1, batchCode: 'BATCH20260101', variety: '红富士', quantity: 5000.00, precoolMethod: 'FORCED_AIR', precoolTemp: 25.0, targetTemp: 2.0, startTime: '2026-04-01 06:00:00', endTime: '2026-04-01 10:00:00', operator: '周仓管', status: 'COMPLETED', remark: '冷风强制预冷' },
  { id: 2, batchCode: 'BATCH20260202', variety: '嘎啦', quantity: 3000.00, precoolMethod: 'COLD_ROOM', precoolTemp: 22.0, targetTemp: 1.0, startTime: '2026-04-01 07:00:00', endTime: '2026-04-01 12:00:00', operator: '吴仓管', status: 'COMPLETED', remark: '冷库自然降温' },
  { id: 3, batchCode: 'BATCH20260301', variety: '红富士', quantity: 15000.00, precoolMethod: 'VACUUM', precoolTemp: 20.0, targetTemp: 3.0, startTime: '2026-04-02 05:00:00', endTime: null, operator: '孙仓管', status: 'PROCESSING', remark: '真空预冷进行中' }
]

// ===== M7 金融 =====
export const loans = [
  { id: 1, loanNo: 'LN202603010001', borrowerId: 1, borrowerName: '王建国', loanType: 'PLEDGE', amount: 50000.00, rate: 4.35, termMonths: 12, startDate: '2026-03-01', endDate: '2027-03-01', status: 'DISBURSED', remark: '仓单质押贷款' },
  { id: 2, loanNo: 'LN202603150001', borrowerId: 3, borrowerName: '张向阳', loanType: 'CREDIT', amount: 80000.00, rate: 5.20, termMonths: 24, startDate: '2026-03-15', endDate: '2028-03-15', status: 'APPROVED', remark: '信用贷款' },
  { id: 3, loanNo: 'LN202604010001', borrowerId: 5, borrowerName: '赵德明', loanType: 'PLANT', amount: 200000.00, rate: 3.85, termMonths: 36, startDate: '2026-04-01', endDate: '2029-04-01', status: 'PENDING', remark: '种植经营贷款' },
  { id: 4, loanNo: 'LN202601010001', borrowerId: 2, borrowerName: '李红梅', loanType: 'WAREHOUSE', amount: 30000.00, rate: 4.50, termMonths: 6, startDate: '2026-01-01', endDate: '2026-07-01', status: 'REPAID', remark: '已还款' },
  { id: 5, loanNo: 'LN202512010001', borrowerId: 4, borrowerName: '陈翠花', loanType: 'RECEIVABLE', amount: 40000.00, rate: 4.80, termMonths: 12, startDate: '2025-12-01', endDate: '2026-12-01', status: 'OVERDUE', remark: '应收账款融资-已逾期' }
]

export const pledges = [
  { id: 1, receiptId: 2, receiptNo: 'WR202604010002', warehouseName: '礼泉气调保鲜库', farmerName: '李红梅', variety: '嘎啦', grade: 'A', quantity: 3000.00, pledgeAmount: 12600.00, status: 'PLEDGED', remark: '质押给农业银行' }
]

export const creditRatings = [
  { id: 1, farmerId: 1, farmerName: '王建国', score: 720, level: 'AA', plantingScale: 50, yearsExperience: 8, yieldRate: 85.5, qualityRate: 90.0, repaymentRate: 100.0, remark: '优质农户' },
  { id: 2, farmerId: 2, farmerName: '李红梅', score: 680, level: 'A', plantingScale: 35, yearsExperience: 5, yieldRate: 78.0, qualityRate: 85.0, repaymentRate: 100.0, remark: '信誉良好' },
  { id: 3, farmerId: 3, farmerName: '张向阳', score: 760, level: 'AA', plantingScale: 80, yearsExperience: 12, yieldRate: 88.0, qualityRate: 92.0, repaymentRate: 100.0, remark: '大型种植户' },
  { id: 4, farmerId: 4, farmerName: '陈翠花', score: 550, level: 'BB', plantingScale: 45, yearsExperience: 6, yieldRate: 70.0, qualityRate: 75.0, repaymentRate: 85.0, remark: '有逾期记录' },
  { id: 5, farmerId: 5, farmerName: '赵德明', score: 810, level: 'AAA', plantingScale: 120, yearsExperience: 15, yieldRate: 92.0, qualityRate: 95.0, repaymentRate: 100.0, remark: '顶级农户' }
]

export const riskRecords = [
  { id: 1, loanId: 5, loanNo: 'LN202512010001', borrowerName: '陈翠花', riskType: 'OVERDUE', riskLevel: 'MEDIUM', description: '贷款逾期30天未还款', status: 'PENDING', handlerName: null, handleResult: null, remark: '' },
  { id: 2, loanId: 1, loanNo: 'LN202603010001', borrowerName: '王建国', riskType: 'OTHER', riskLevel: 'LOW', description: '果园受冰雹影响，需关注还款能力', status: 'RESOLVED', handlerName: '风控专员张三', handleResult: '已确认果园有保险，风险可控', remark: '' }
]

// ===== M4 交易 =====
export const trades = [
  { id: 1, tradeNo: 'TR202604050001', buyerName: '上海鲜果集团', sellerName: '赵德明', farmerName: '赵德明', farmerId: 5, variety: '红富士', grade: 'A', quantity: 5000, unit: 'kg', unitPrice: 5.80, totalAmount: 29000.00, tradeDate: '2026-04-05', createdAt: '2026-04-05', status: 'COMPLETED', remark: '大批量交易' },
  { id: 2, tradeNo: 'TR202604040001', buyerName: '西安果品连锁', sellerName: '王建国', farmerName: '王建国', farmerId: 1, variety: '红富士', grade: 'A', quantity: 2000, unit: 'kg', unitPrice: 5.20, totalAmount: 10400.00, tradeDate: '2026-04-04', createdAt: '2026-04-04', status: 'COMPLETED', remark: '本地直销' },
  { id: 3, tradeNo: 'TR202604060001', buyerName: '北京新发地', sellerName: '张向阳', farmerName: '张向阳', farmerId: 3, variety: '瑞雪', grade: 'B', quantity: 3000, unit: 'kg', unitPrice: 6.50, totalAmount: 19500.00, tradeDate: '2026-04-06', createdAt: '2026-04-06', status: 'PENDING', remark: '待确认' }
]

// ===== 统计数据 =====
export const warehouseStats = {
  totalWarehouses: 5,
  totalCapacity: 2200.00,
  totalUsed: 1120.00,
  utilizationRate: 50.91,
  warehousesByType: { COLD: 3, NORMAL: 1, ATMOSPHERE: 1 },
  warehousesByStatus: { ACTIVE: 3, FULL: 1, MAINTENANCE: 1 }
}

export const financeStats = {
  totalLoans: 5,
  totalAmount: 400000.00,
  avgRate: 4.54,
  overdueCount: 1,
  overdueRate: 20.0,
  loansByStatus: { PENDING: 1, APPROVED: 1, DISBURSED: 1, REPAID: 1, OVERDUE: 1 },
  loansByType: { PLEDGE: 1, CREDIT: 1, PLANT: 1, WAREHOUSE: 1, RECEIVABLE: 1 }
}

export const coldchainStats = {
  vehicleCount: 4,
  transportCount: 3,
  completionRate: 33.3,
  avgTemp: 2.5,
  vehicleByStatus: { IDLE: 2, IN_TRANSIT: 1, MAINTENANCE: 1 },
  vehicleByType: { REFRIGERATED: 2, INSULATED: 1, NORMAL: 1 }
}

// ===== 种植时间轴 =====
export const plantingTimeline = [
  { id: 1, orchardId: 1, type: 'FERTILIZE', typeName: '施肥', description: '春季施有机肥，每棵树 10kg 生物菌肥，沟施覆土', material: '有机生物菌肥 40kg/袋', quantity: 500, unit: 'kg', operator: '王建国', operatedAt: '2026-03-10 09:00:00', weather: '晴 12C', remark: '开沟 20cm 深，距树干 60cm 环状沟施' },
  { id: 2, orchardId: 1, type: 'PRUNE', typeName: '修剪', description: '春季复剪，疏除背上枝、重叠枝，改善通风透光', material: null, quantity: null, unit: null, operator: '王建国', operatedAt: '2026-03-15 08:30:00', weather: '多云 15C', remark: '保留主枝延长头，单轴延伸' },
  { id: 3, orchardId: 2, type: 'FERTILIZE', typeName: '施肥', description: '有机肥追施，改善土壤肥力', material: '有机生物菌肥', quantity: 300, unit: 'kg', operator: '李红梅', operatedAt: '2026-03-12 10:00:00', weather: '晴 14C', remark: '穴施' },
  { id: 4, orchardId: 5, type: 'FERTILIZE', typeName: '施肥', description: '大园追肥，复合肥撒施', material: '复合肥 15-15-15', quantity: 800, unit: 'kg', operator: '赵德明', operatedAt: '2026-03-18 09:00:00', weather: '晴 16C', remark: '撒施后灌水' }
]

// ===== M8 采收 =====
export const harvestBatches = [
  { id: 2001, batchCode: 'HB20260406001', orchardId: 1001, orchardName: '张三红富士果园', farmerName: '张三', variety: '红富士', quantity: 5000, unit: 'kg', grade: 'A', harvestDate: '2026-04-06', workers: 4, sugarDegree: 14.5, fruitDiameter: '80mm+', pesticideCheck: 'PASS', pesticideCheckOrg: '洛川县农产品质检站', status: 'COMPLETED', remark: '手工采摘，套袋果剔除伤果后 5000kg 入库' },
  { id: 2002, batchCode: 'HB20260406002', orchardId: 1, orchardName: '建国红富士园', farmerName: '王建国', variety: '红富士', quantity: 5000, unit: 'kg', grade: 'A', harvestDate: '2026-03-01', workers: 3, sugarDegree: 14.2, fruitDiameter: '80mm+', pesticideCheck: 'PASS', pesticideCheckOrg: '洛川县农产品质检站', status: 'COMPLETED', remark: '首批采收' },
  { id: 2003, batchCode: 'HB20260406003', orchardId: 2, orchardName: '红梅嘎啦园', farmerName: '李红梅', variety: '嘎啦', quantity: 3000, unit: 'kg', grade: 'A', harvestDate: '2026-03-02', workers: 2, sugarDegree: 13.8, fruitDiameter: '75mm+', pesticideCheck: 'PASS', pesticideCheckOrg: '礼泉县质检中心', status: 'COMPLETED', remark: '嘎啦早熟品种' },
  { id: 2004, batchCode: 'HB20260406004', orchardId: 5, orchardName: '德明富士庄园', farmerName: '赵德明', variety: '红富士', quantity: 15000, unit: 'kg', grade: 'A', harvestDate: '2026-03-03', workers: 8, sugarDegree: 15.1, fruitDiameter: '85mm+', pesticideCheck: 'PASS', pesticideCheckOrg: '栖霞市果品检测中心', status: 'COMPLETED', remark: '大客户采收，有机认证' }
]

// ===== 交易趋势 (Dashboard) =====
export const tradeTrend = {
  months: ['2025-11', '2025-12', '2026-01', '2026-02', '2026-03', '2026-04'],
  values: [120, 95, 45, 30, 85, 145]
}
