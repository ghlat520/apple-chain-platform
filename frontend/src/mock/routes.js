/**
 * Mock 路由匹配 — 纯函数，无副作用
 *
 * 被 request.js 的 interceptor 调用
 * 顶部护栏：VITE_ENABLE_MOCK=off 时直接返回 null，避免命中
 */

import * as data from './data.js'
import * as userMock from './user.js'
import { isMockDisabled } from './shouldMock.js'

function success(d, msg = 'success') {
  return { code: 200, message: msg, data: d }
}

function pageResult(records, total, page = 1, size = 10) {
  return success({ records, total, size, current: page })
}

function fuzzyMatch(str, keyword) {
  if (!keyword) return true
  return (str || '').toLowerCase().includes(keyword.toLowerCase())
}

export function routeMock(url, method, params, body) {
  if (isMockDisabled()) return null

  // --- 用户模块（contract/modules/user.md）---
  if (url.includes('/user/login') && method === 'post') {
    if (!body?.username || !body?.password) {
      return { code: 100002, message: '用户名或密码错误', data: null }
    }
    return success(userMock.loginSuccess, '操作成功')
  }
  if (url.includes('/user/auth/me') && method === 'get') {
    return success(userMock.currentUser, '操作成功')
  }
  if (url.includes('/user/logout') && method === 'post') {
    return success(null, '操作成功')
  }

  // --- Config (高德地图) ---
  if (url.includes('/config/amap'))
    return success({ key: 'e6d11945e4ee5cc9c6482f4f40eaf753', securityJsCode: '81d3a9118db3c65d62fb044144fb5bee' })

  // --- GIS 果园地理数据 ---
  if (url.includes('/planting/orchard/geo/bbox')) {
    const geoOrchards = [
      { id: 1, orchardName: '建国红富士园', centerLng: 109.43, centerLat: 35.76, areaMu: 50, variety: '红富士' },
      { id: 2, orchardName: '红梅嘎啦园', centerLng: 108.43, centerLat: 34.48, areaMu: 35, variety: '嘎啦' },
      { id: 3, orchardName: '向阳瑞雪园', centerLng: 109.59, centerLat: 35.18, areaMu: 80, variety: '瑞雪' },
      { id: 4, orchardName: '翠花秦冠园', centerLng: 105.73, centerLat: 35.52, areaMu: 45, variety: '秦冠' },
      { id: 5, orchardName: '德明富士庄园', centerLng: 120.83, centerLat: 37.29, areaMu: 120, variety: '红富士' }
    ]
    return success(geoOrchards)
  }
  if (url.match(/\/planting\/orchard\/geo\/\d+\/boundary$/) && method === 'post')
    return success({ id: +url.split('/')[4], boundarySet: true })

  // --- Auth ---
  if (url.includes('/auth/login') && method === 'post') {
    return success({
      token: 'mock-jwt-token-' + Date.now(),
      user: { id: 1, username: body.username || 'admin', name: '系统管理员', role: 'ADMIN' }
    })
  }
  if (url.includes('/auth/profile'))
    return success({ id: 1, username: 'admin', name: '系统管理员', role: 'ADMIN' })

  // --- M2 农资 ---
  if (url.includes('/input/suppliers/list'))
    return pageResult(
      data.suppliers.filter(s => fuzzyMatch(s.name, params.keyword) && (!params.status || s.status === params.status)),
      data.suppliers.length, +params.page || 1, +params.size || 10
    )
  if (url.match(/\/input\/suppliers\/\d+$/) && method === 'get') {
    const id = +url.split('/').pop()
    return success(data.suppliers.find(s => s.id === id))
  }
  if (url === '/input/suppliers' && method === 'post') {
    body.status = 'PENDING'; body.id = data.suppliers.length + 1
    return success(body)
  }
  if (url.match(/\/input\/suppliers\/\d+\/audit$/)) {
    const id = +url.split('/')[3]
    const s = data.suppliers.find(x => x.id === id)
    if (s) { s.status = body.decision === 'APPROVE' ? 'APPROVED' : body.decision === 'REJECT' ? 'REJECTED' : 'BLACKLISTED'; s.auditTime = new Date().toISOString() }
    return success(s)
  }
  if (url.match(/\/input\/suppliers\/\d+\/reinstate$/)) {
    const id = +url.split('/')[3]
    const s = data.suppliers.find(x => x.id === id)
    if (s) { s.status = 'PENDING'; s.rejectReason = '' }
    return success(s)
  }

  if (url.includes('/input/products/list'))
    return pageResult(
      data.products.filter(p => fuzzyMatch(p.name, params.keyword) && (!params.type || p.type === params.type) && (!params.status || p.status === params.status)),
      data.products.length
    )
  if (url === '/input/products' && method === 'post') {
    body.status = 'ACTIVE'; body.id = data.products.length + 1
    return success(body)
  }

  if (url.includes('/input/purchases/list'))
    return pageResult(
      data.purchases.filter(p => fuzzyMatch(p.purchaseNo, params.keyword) && (!params.status || p.status === params.status)),
      data.purchases.length
    )
  if (url === '/input/purchases' && method === 'post') {
    body.status = 'PENDING'; body.id = data.purchases.length + 1; body.totalAmount = body.quantity * body.unitPrice
    return success(body)
  }
  if (url.match(/\/input\/purchases\/\d+\/approve$/)) {
    const p = data.purchases.find(x => x.id === +url.split('/')[3])
    if (p) p.status = 'APPROVED'
    return success(p)
  }
  if (url.match(/\/input\/purchases\/\d+\/receive$/)) {
    const p = data.purchases.find(x => x.id === +url.split('/')[3])
    if (p) p.status = 'RECEIVED'
    return success(p)
  }
  if (url.match(/\/input\/purchases\/\d+\/cancel$/)) {
    const p = data.purchases.find(x => x.id === +url.split('/')[3])
    if (p) p.status = 'CANCELLED'
    return success(p)
  }

  if (url.includes('/input/inventory/list'))
    return pageResult(
      data.inventory.filter(i => fuzzyMatch(i.productName, params.keyword) && (!params.status || i.status === params.status)),
      data.inventory.length
    )
  if (url.includes('/input/inventory/alerts'))
    return success(data.inventory.filter(i => i.status === 'LOW' || i.status === 'EMPTY'))
  if (url.match(/\/input\/inventory\/\d+\/adjust$/)) {
    const inv = data.inventory.find(x => x.id === +url.split('/')[3])
    if (inv) { inv.stockQuantity += body.delta; inv.remark = body.reason || '' }
    return success(inv)
  }

  if (url.includes('/input/usage/list'))
    return pageResult(data.usageRecords, data.usageRecords.length)
  if (url === '/input/usage' && method === 'post') {
    body.id = data.usageRecords.length + 1
    return success(body)
  }

  if (url.match(/\/input\/trace\//)) {
    const tc = url.split('/input/trace/')[1]
    const usages = data.usageRecords.filter(u => u.traceCode === tc)
    const pIds = [...new Set(usages.map(u => u.productId))]
    const purchases = data.purchases.filter(p => pIds.includes(p.productId))
    const sIds = [...new Set(purchases.map(p => p.supplierId))]
    const suppliers = data.suppliers.filter(s => sIds.includes(s.id))
    return success({ traceCode: tc, usages, purchases, suppliers })
  }

  // --- M5 仓储 ---
  if (url.includes('/warehouse/warehouses/list'))
    return pageResult(
      data.warehouses.filter(w => fuzzyMatch(w.name, params.keyword) && (!params.type || w.type === params.type) && (!params.status || w.status === params.status)),
      data.warehouses.length
    )
  if (url.match(/\/warehouse\/warehouses\/\d+$/) && method === 'get')
    return success(data.warehouses.find(w => w.id === +url.split('/').pop()))
  if (url === '/warehouse/warehouses' && method === 'post') {
    body.status = 'ACTIVE'; body.usedCapacity = 0; body.id = data.warehouses.length + 1
    return success(body)
  }
  if (url.includes('/warehouse/warehouses/alerts'))
    return success(data.warehouses.filter(w => w.status !== 'CLOSED' && (w.usedCapacity / w.capacity >= 0.9 || (w.status === 'ACTIVE' && w.usedCapacity === 0) || w.status === 'MAINTENANCE')))

  if (url.includes('/warehouse/receipts/list'))
    return pageResult(data.receipts, data.receipts.length)
  if (url === '/warehouse/receipts' && method === 'post') {
    body.status = 'VALID'; body.id = data.receipts.length + 1; body.totalValue = body.unitValue * body.quantity
    const wh = data.warehouses.find(w => w.id === body.warehouseId)
    if (wh) body.warehouseName = wh.name
    return success(body)
  }
  if (url.match(/\/warehouse\/receipts\/\d+\/status$/)) {
    const r = data.receipts.find(x => x.id === +url.split('/')[3])
    if (r) r.status = body.status
    return success(r)
  }

  if (url.includes('/warehouse/records/list'))
    return pageResult(data.warehouseRecords, data.warehouseRecords.length)
  if (url === '/warehouse/records' && method === 'post') {
    body.id = data.warehouseRecords.length + 1
    return success(body)
  }

  if (url.includes('/warehouse/statistics/summary'))
    return success(data.warehouseStats)
  if (url.includes('/warehouse/statistics/turnover'))
    return success({ warehouseId: params.warehouseId || null, totalInbound: 23000, totalOutbound: 2000, turnoverRate: 0.16, recordCount: 4 })
  if (url.includes('/warehouse/statistics/loss'))
    return success({ totalInbound: 23000, totalOutbound: 2000, difference: 21000, lossRate: 91.3 })

  // --- M6 冷链 ---
  if (url.includes('/coldchain/vehicles/list'))
    return pageResult(
      data.vehicles.filter(v => fuzzyMatch(v.plateNumber, params.keyword) && (!params.vehicleType || v.vehicleType === params.vehicleType) && (!params.status || v.status === params.status)),
      data.vehicles.length
    )
  if (url === '/coldchain/vehicles' && method === 'post') {
    body.id = data.vehicles.length + 1; body.status = 'IDLE'
    return success(body)
  }
  if (url.match(/\/coldchain\/vehicles\/\d+$/) && method === 'get')
    return success(data.vehicles.find(v => v.id === +url.split('/').pop()))

  if (url.includes('/coldchain/transports/list'))
    return pageResult(
      data.transports.filter(t => (!params.status || t.status === params.status)),
      data.transports.length
    )
  if (url === '/coldchain/transports' && method === 'post') {
    body.status = 'PENDING'; body.id = data.transports.length + 1
    return success(body)
  }
  if (url.match(/\/coldchain\/transports\/\d+\/start$/)) {
    const t = data.transports.find(x => x.id === +url.split('/')[3])
    if (t) { t.status = 'IN_TRANSIT'; t.startTime = new Date().toISOString() }
    return success(t)
  }
  if (url.match(/\/coldchain\/transports\/\d+\/complete$/)) {
    const t = data.transports.find(x => x.id === +url.split('/')[3])
    if (t) { t.status = 'COMPLETED'; t.endTime = new Date().toISOString() }
    return success(t)
  }

  if (url.includes('/coldchain/precooling/list'))
    return pageResult(data.precoolingRecords, data.precoolingRecords.length)
  if (url === '/coldchain/precooling' && method === 'post') {
    body.status = 'PENDING'; body.id = data.precoolingRecords.length + 1
    return success(body)
  }

  if (url.includes('/coldchain/statistics/summary'))
    return success(data.coldchainStats)
  if (url.includes('/coldchain/statistics/transport-trend'))
    return success({ days: ['04-01','04-02','04-03','04-04','04-05','04-06','04-07','04-08','04-09','04-10'], counts: [2,3,1,4,2,3,5,2,1,3] })

  // --- M7 金融 ---
  if (url.includes('/finance/loans/list'))
    return pageResult(
      data.loans.filter(l => fuzzyMatch(l.loanNo, params.keyword) && fuzzyMatch(l.borrowerName, params.keyword) && (!params.loanType || l.loanType === params.loanType) && (!params.status || l.status === params.status)),
      data.loans.length
    )
  if (url === '/finance/loans' && method === 'post') {
    body.status = 'PENDING'; body.id = data.loans.length + 1
    return success(body)
  }
  if (url.match(/\/finance\/loans\/\d+\/approve$/)) {
    const l = data.loans.find(x => x.id === +url.split('/')[3])
    if (l) l.status = 'APPROVED'
    return success(l)
  }
  if (url.match(/\/finance\/loans\/\d+\/reject$/)) {
    const l = data.loans.find(x => x.id === +url.split('/')[3])
    if (l) l.status = 'REJECTED'
    return success(l)
  }
  if (url.match(/\/finance\/loans\/\d+\/disburse$/)) {
    const l = data.loans.find(x => x.id === +url.split('/')[3])
    if (l) l.status = 'DISBURSED'
    return success(l)
  }
  if (url.match(/\/finance\/loans\/\d+\/repay$/)) {
    const l = data.loans.find(x => x.id === +url.split('/')[3])
    if (l) l.status = 'REPAID'
    return success(l)
  }
  if (url.match(/\/finance\/loans\/\d+\/settle$/)) {
    const l = data.loans.find(x => x.id === +url.split('/')[3])
    if (l) l.status = 'SETTLED'
    return success(l)
  }

  if (url.includes('/finance/pledges/list'))
    return pageResult(data.pledges, data.pledges.length)
  if (url === '/finance/pledges' && method === 'post') {
    body.status = 'PLEDGED'; body.id = data.pledges.length + 1
    return success(body)
  }
  if (url.match(/\/finance\/pledges\/\d+\/release$/)) {
    const p = data.pledges.find(x => x.id === +url.split('/')[3])
    if (p) p.status = 'RELEASED'
    return success(p)
  }

  if (url.includes('/finance/credit-ratings/list'))
    return pageResult(data.creditRatings, data.creditRatings.length)
  if (url === '/finance/credit-ratings' && method === 'post') {
    body.id = data.creditRatings.length + 1
    return success(body)
  }
  if (url.match(/\/finance\/credit-ratings\/farmer\/\d+$/))
    return success(data.creditRatings.find(r => r.farmerId === +url.split('/').pop()))

  if (url.includes('/finance/risks/list'))
    return pageResult(data.riskRecords, data.riskRecords.length)
  if (url === '/finance/risks' && method === 'post') {
    body.status = 'PENDING'; body.id = data.riskRecords.length + 1
    return success(body)
  }
  if (url.match(/\/finance\/risks\/\d+\/resolve$/)) {
    const r = data.riskRecords.find(x => x.id === +url.split('/')[3])
    if (r) { r.status = 'RESOLVED'; r.handlerName = body.handlerName; r.handleResult = body.handleResult }
    return success(r)
  }

  if (url.includes('/finance/statistics/summary'))
    return success(data.financeStats)
  if (url.includes('/finance/statistics/trend'))
    return success({ months: ['2025-10','2025-11','2025-12','2026-01','2026-02','2026-03'], amounts: [35000,42000,28000,15000,22000,58000] })

  // --- Dashboard ---
  if (url.includes('/dashboard/stats'))
    return success({ totalOrchards: 5, totalFarmers: 5, totalTraceRecords: 12, pendingTrades: 1 })
  if (url.includes('/dashboard/trend'))
    return success(data.tradeTrend)
  if (url.includes('/dashboard/top-orchards'))
    return success(data.orchards.slice(0, 5))
  if (url.includes('/dashboard/todos'))
    return success([
      { id: 1, title: '赵德明种植贷款待审批', type: 'finance', urgent: true, createdAt: '2026-04-01' },
      { id: 2, title: '陈翠花贷款逾期30天', type: 'finance', urgent: true, createdAt: '2026-04-10' },
      { id: 3, title: '甘肃绿源农资供应商待审核', type: 'input', urgent: false, createdAt: '2026-04-08' },
      { id: 4, title: '德明复合肥库存偏低', type: 'input', urgent: false, createdAt: '2026-04-09' }
    ])

  // --- 交易 (GET /trades 或 /trade/order/list) ---
  if (url === '/trades' || url.includes('/trade/order/list') || (url.includes('/trades') && !url.includes('/trades/')))
    return pageResult(data.trades, data.trades.length)
  if (url.match(/\/trades\/\d+\/status$/) && method === 'put')
    return success({ id: +url.split('/')[2], status: body.status })
  if (url.match(/\/trade\/order\/\d+$/) && method === 'get')
    return success(data.trades.find(t => t.id === +url.split('/').pop()))
  if (url.match(/\/trade\/order\/\d+\/(confirm|deliver|complete|cancel)$/) && method === 'put')
    return success({ id: +url.split('/')[3], status: 'COMPLETED' })

  // --- 溯源 (GET /trace, GET /trace/list, GET /trace/:batchCode) ---
  if (url === '/trace' || url.includes('/trace/list') || (url.includes('/trace') && !url.match(/\/trace\/.+/)))
    return pageResult([
      { batchCode: 'BATCH20260101', variety: '红富士', orchardName: '建国红富士园', farmerName: '王建国', quantity: 5000, status: 'ACTIVE', harvestDate: '2026-03-01' },
      { batchCode: 'BATCH20260202', variety: '嘎啦', orchardName: '红梅嘎啦园', farmerName: '李红梅', quantity: 3000, status: 'ACTIVE', harvestDate: '2026-03-02' },
      { batchCode: 'BATCH20260301', variety: '红富士', orchardName: '德明富士庄园', farmerName: '赵德明', quantity: 15000, status: 'ACTIVE', harvestDate: '2026-03-03' }
    ], 3)

  // 溯源详情 — 跨模块数据闭环
  if (url.match(/\/trace\/(BATCH\w+)$/)) {
    const bc = url.split('/trace/')[1]
    // 从果园→仓储→冷链→交易 全链路拼装
    const batchMap = {
      BATCH20260101: {
        batchCode: 'BATCH20260101', variety: '红富士', orchardName: '建国红富士园', farmerName: '王建国',
        quantity: 5000, harvestDate: '2026-03-01', status: 'ACTIVE', stepsCompleted: 5,
        inspectionResult: '糖度14.2° · 果径80mm+ · 农残检测合格',
        warehouseName: '洛川冷链一号库 · 2026-04-01入库 · 5000kg · A级',
        logisticsInfo: '陕A·冷001 · 2026-04-04发车 · 西安胡家庙市场 · 已送达',
        salesInfo: '西安果品连锁 · 2000kg · ¥5.2/kg · 已完成',
        blockchainHash: '0x7f8a3b2c1d4e5f6a7b8c9d0e1f2a3b4c5d6e7f8a9b0c1d2e3f4a5b6c7d8e9f0',
        chainedAt: '2026-04-01 08:30:00'
      },
      BATCH20260202: {
        batchCode: 'BATCH20260202', variety: '嘎啦', orchardName: '红梅嘎啦园', farmerName: '李红梅',
        quantity: 3000, harvestDate: '2026-03-02', status: 'ACTIVE', stepsCompleted: 3,
        inspectionResult: '糖度13.8° · 果径75mm+ · 农残检测合格',
        warehouseName: '礼泉气调保鲜库 · 2026-04-01入库 · 3000kg · A级',
        logisticsInfo: '待发货',
        salesInfo: '待销售',
        blockchainHash: '0x1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a0b1c2d3e4f5a6b7c8d9e0f1a2b',
        chainedAt: '2026-04-01 09:15:00'
      },
      BATCH20260301: {
        batchCode: 'BATCH20260301', variety: '红富士', orchardName: '德明富士庄园', farmerName: '赵德明',
        quantity: 15000, harvestDate: '2026-03-03', status: 'ACTIVE', stepsCompleted: 4,
        inspectionResult: '糖度15.1° · 果径85mm+ · 农残检测合格 · 有机认证',
        warehouseName: '栖霞冷链物流仓 · 2026-04-02入库 · 15000kg · A级',
        logisticsInfo: '鲁F·冷002 · 2026-04-05发车 · 上海江桥批发市场 · 运输中(已发5000kg)',
        salesInfo: '上海鲜果集团 · 5000kg · ¥5.8/kg · 已完成',
        blockchainHash: '0x9e8d7c6b5a4f3e2d1c0b9a8f7e6d5c4b3a2f1e0d9c8b7a6f5e4d3c2b1a0f9e8d',
        chainedAt: '2026-04-02 10:00:00'
      }
    }
    return success(batchMap[bc] || {
      batchCode: bc, variety: '红富士', orchardName: '未知果园', farmerName: '-',
      quantity: 0, harvestDate: '-', status: 'UNKNOWN', stepsCompleted: 0,
      inspectionResult: '-', warehouseName: '-', logisticsInfo: '-', salesInfo: '-',
      blockchainHash: '-', chainedAt: '-'
    })
  }

  // --- 种植时间轴 (GET /planting/timeline?orchardId=xxx) ---
  if (url.includes('/planting/timeline')) {
    const orchardId = +params.orchardId
    const records = orchardId ? data.plantingTimeline.filter(p => p.orchardId === orchardId) : data.plantingTimeline
    return success(records)
  }

  // --- 果园 (GET /orchards 或 /planting/orchard/list) ---
  if (url === '/orchards' || url.includes('/orchards/list') || (url.includes('/orchards') && !url.includes('/orchards/')))
    return pageResult(data.orchards, data.orchards.length)
  if (url.match(/\/orchards\/\d+$/) || url.match(/\/planting\/orchard\/\d+$/))
    return success(data.orchards.find(o => o.id === +url.split('/').pop()))

  // --- 采收 (GET /harvest/list, GET /harvest/:id) ---
  if (url === '/harvest' || url.includes('/harvest/list'))
    return pageResult(data.harvestBatches, data.harvestBatches.length)
  if (url.match(/\/harvest\/\d+$/) && method === 'get')
    return success(data.harvestBatches.find(h => h.id === +url.split('/').pop()))

  // --- 果农 (GET /farmers) ---
  if (url === '/farmers' || (url.includes('/farmers') && !url.includes('/farmers/')))
    return pageResult(data.farmers, data.farmers.length)

  return null
}
