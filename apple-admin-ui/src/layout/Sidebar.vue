<template>
  <el-aside :width="collapsed ? '64px' : '220px'" class="sidebar">
    <div class="sidebar-logo">
      <span v-if="!collapsed" class="logo-text">苹果产业链</span>
      <span v-else class="logo-text-small">苹</span>
    </div>
    <el-menu
      :default-active="activeMenu"
      :collapse="collapsed"
      :collapse-transition="false"
      background-color="#1B2A1D"
      text-color="#A0B8A4"
      active-text-color="#FFFFFF"
      router
    >
      <el-menu-item index="/">
        <el-icon><Monitor /></el-icon>
        <template #title>控制台</template>
      </el-menu-item>

      <el-sub-menu index="planting">
        <template #title>
          <el-icon><Sunny /></el-icon>
          <span>种植生产管理</span>
        </template>
        <el-menu-item index="/planting/orchards">果园管理</el-menu-item>
        <el-menu-item index="/planting/farmers">农户管理</el-menu-item>
        <el-menu-item index="/planting/harvest">采收批次</el-menu-item>
        <el-menu-item index="/planting/cultivation">种植批次</el-menu-item>
        <el-menu-item index="/planting/maturity">成熟度管理</el-menu-item>
        <el-menu-item index="/planting/task-plan">作业计划</el-menu-item>
        <el-menu-item index="/planting/growth-record">农事记录</el-menu-item>
        <el-menu-item index="/planting/analysis">种植分析</el-menu-item>
      </el-sub-menu>

      <el-sub-menu index="input">
        <template #title>
          <el-icon><ShoppingCart /></el-icon>
          <span>农资管理</span>
        </template>
        <el-menu-item index="/input/suppliers">供应商管理</el-menu-item>
        <el-menu-item index="/input/products">农资产品</el-menu-item>
        <el-menu-item index="/input/purchases">采购管理</el-menu-item>
        <el-menu-item index="/input/inventory">库存管理</el-menu-item>
        <el-menu-item index="/input/usage">使用记录</el-menu-item>
        <el-menu-item index="/input/trace">溯源链路</el-menu-item>
      </el-sub-menu>

      <el-sub-menu index="trace">
        <template #title>
          <el-icon><Connection /></el-icon>
          <span>全流程溯源</span>
        </template>
        <el-menu-item index="/trace/batches">溯源批次</el-menu-item>
        <el-menu-item index="/trace/codes">溯源码管理</el-menu-item>
        <el-menu-item index="/trace/records">链路记录</el-menu-item>
        <el-menu-item index="/trace/anomaly">异常追溯</el-menu-item>
      </el-sub-menu>

      <el-sub-menu index="trade">
        <template #title>
          <el-icon><Goods /></el-icon>
          <span>收购交易撮合</span>
        </template>
        <el-menu-item index="/trade/supply">供货信息</el-menu-item>
        <el-menu-item index="/trade/need">采购需求</el-menu-item>
        <el-menu-item index="/trade/match">撮合管理</el-menu-item>
        <el-menu-item index="/trade/orders">交易订单</el-menu-item>
        <el-menu-item index="/trade/inspection">质量检验</el-menu-item>
        <el-menu-item index="/trade/statistics">交易统计</el-menu-item>
      </el-sub-menu>

      <el-sub-menu index="warehouse">
        <template #title>
          <el-icon><Box /></el-icon>
          <span>仓储管理</span>
        </template>
        <el-menu-item index="/warehouse/warehouses">仓库管理</el-menu-item>
        <el-menu-item index="/warehouse/receipts">仓单管理</el-menu-item>
        <el-menu-item index="/warehouse/records">出入库记录</el-menu-item>
        <el-menu-item index="/warehouse/statistics">仓储统计</el-menu-item>
      </el-sub-menu>

      <el-sub-menu index="coldchain">
        <template #title>
          <el-icon><Van /></el-icon>
          <span>冷链物流</span>
        </template>
        <el-menu-item index="/coldchain/vehicles">车辆管理</el-menu-item>
        <el-menu-item index="/coldchain/tasks">运输任务</el-menu-item>
        <el-menu-item index="/coldchain/deliveries">配送管理</el-menu-item>
        <el-menu-item index="/coldchain/precool">预冷任务</el-menu-item>
        <el-menu-item index="/coldchain/temperatures">温度记录</el-menu-item>
        <el-menu-item index="/coldchain/logistics">物流查询</el-menu-item>
        <el-menu-item index="/coldchain/statistics">冷链统计</el-menu-item>
      </el-sub-menu>

      <el-sub-menu index="finance">
        <template #title>
          <el-icon><Money /></el-icon>
          <span>供应链金融</span>
        </template>
        <el-menu-item index="/finance/loans">贷款管理</el-menu-item>
        <el-menu-item index="/finance/pledges">质押管理</el-menu-item>
        <el-menu-item index="/finance/credits">信用评级</el-menu-item>
        <el-menu-item index="/finance/risks">风控记录</el-menu-item>
        <el-menu-item index="/finance/statistics">金融统计</el-menu-item>
      </el-sub-menu>

      <el-sub-menu index="bigdata">
        <template #title>
          <el-icon><DataAnalysis /></el-icon>
          <span>大数据管理</span>
        </template>
        <el-menu-item index="/bigdata/data-asset">数据资产</el-menu-item>
        <el-menu-item index="/bigdata/data-source">数据源管理</el-menu-item>
        <el-menu-item index="/bigdata/data-quality">数据质量</el-menu-item>
        <el-menu-item index="/bigdata/collect-job">采集任务</el-menu-item>
        <el-menu-item index="/bigdata/lineage">数据血缘</el-menu-item>
        <el-menu-item index="/bigdata/metric-center">指标中心</el-menu-item>
        <el-menu-item index="/bigdata/report">分析报告</el-menu-item>
        <el-menu-item index="/bigdata/audit-log">审计日志</el-menu-item>
        <el-menu-item index="/bigdata/open-api">开放API</el-menu-item>
        <el-menu-item index="/bigdata/screen-config">大屏配置</el-menu-item>
      </el-sub-menu>
    </el-menu>
  </el-aside>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAppStore } from '@/store/app.js'
import { Monitor, DataAnalysis, Sunny, ShoppingCart, Connection, Goods, Box, Van, Money } from '@element-plus/icons-vue'

const route = useRoute()
const appStore = useAppStore()
const collapsed = computed(() => appStore.sidebarCollapsed)
const activeMenu = computed(() => route.path)
</script>

<style scoped>
.sidebar {
  background: #1B2A1D;
  overflow-y: auto;
  overflow-x: hidden;
  transition: width 0.3s;
}
.sidebar::-webkit-scrollbar {
  width: 0;
}
.sidebar-logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}
.logo-text {
  color: #FFFFFF;
  font-size: 18px;
  font-weight: 600;
  letter-spacing: 2px;
}
.logo-text-small {
  color: #FFFFFF;
  font-size: 20px;
  font-weight: 700;
}
:deep(.el-menu) {
  border-right: none;
}
:deep(.el-menu-item.is-active) {
  background-color: rgba(211, 47, 47, 0.8) !important;
}
:deep(.el-sub-menu .el-menu-item.is-active) {
  background-color: rgba(211, 47, 47, 0.8) !important;
}
</style>
