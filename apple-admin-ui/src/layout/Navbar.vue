<template>
  <el-header class="navbar">
    <div class="navbar-left">
      <el-icon class="collapse-btn" @click="appStore.toggleSidebar" :size="20">
        <Fold v-if="!appStore.sidebarCollapsed" />
        <Expand v-else />
      </el-icon>
      <el-breadcrumb separator="/">
        <el-breadcrumb-item v-for="item in breadcrumbs" :key="item.path">
          {{ item.title }}
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    <div class="navbar-right">
      <el-dropdown @command="handleCommand">
        <span class="user-dropdown">
          <el-avatar :size="32" icon="UserFilled" />
          <span class="username">{{ authStore.user?.username || '用户' }}</span>
          <el-icon><ArrowDown /></el-icon>
        </span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="logout">
              <el-icon><SwitchButton /></el-icon>
              退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </el-header>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/store/auth.js'
import { useAppStore } from '@/store/app.js'
import { Fold, Expand, ArrowDown, SwitchButton } from '@element-plus/icons-vue'

const route = useRoute()
const authStore = useAuthStore()
const appStore = useAppStore()

const titleMap = {
  '/': '控制台',
  '/bigdata/data-asset': '数据资产',
  '/bigdata/data-source': '数据源管理',
  '/bigdata/data-quality': '数据质量',
  '/bigdata/collect-job': '采集任务',
  '/bigdata/lineage': '数据血缘',
  '/bigdata/metric-center': '指标中心',
  '/bigdata/report': '分析报告',
  '/bigdata/audit-log': '审计日志',
  '/bigdata/open-api': '开放API',
  '/bigdata/screen-config': '大屏配置',
  // 种植生产管理
  '/planting/orchards': '果园管理',
  '/planting/farmers': '农户管理',
  '/planting/harvest': '采收批次',
  '/planting/cultivation': '种植批次',
  '/planting/maturity': '成熟度管理',
  '/planting/task-plan': '作业计划',
  '/planting/growth-record': '农事记录',
  '/planting/analysis': '种植分析',
  // 农资管理
  '/input/suppliers': '供应商管理',
  '/input/products': '农资产品',
  '/input/purchases': '采购管理',
  '/input/inventory': '库存管理',
  '/input/usage': '使用记录',
  '/input/trace': '溯源链路',
  // 全流程溯源
  '/trace/batches': '溯源批次',
  '/trace/codes': '溯源码管理',
  '/trace/records': '链路记录',
  '/trace/anomaly': '异常追溯',
  // 收购交易撮合
  '/trade/supply': '供货信息',
  '/trade/need': '采购需求',
  '/trade/match': '撮合管理',
  '/trade/orders': '交易订单',
  '/trade/inspection': '质量检验',
  '/trade/statistics': '交易统计',
  // 仓储管理
  '/warehouse/warehouses': '仓库管理',
  '/warehouse/receipts': '仓单管理',
  '/warehouse/records': '出入库记录',
  '/warehouse/statistics': '仓储统计',
  // 冷链物流
  '/coldchain/vehicles': '车辆管理',
  '/coldchain/tasks': '运输任务',
  '/coldchain/deliveries': '配送管理',
  '/coldchain/precool': '预冷任务',
  '/coldchain/temperatures': '温度记录',
  '/coldchain/logistics': '物流查询',
  '/coldchain/statistics': '冷链统计',
  // 供应链金融
  '/finance/loans': '贷款管理',
  '/finance/pledges': '质押管理',
  '/finance/credits': '信用评级',
  '/finance/risks': '风控记录',
  '/finance/statistics': '金融统计'
}

const breadcrumbs = computed(() => {
  const items = [{ path: '/', title: '首页' }]
  const title = titleMap[route.path]
  if (title && route.path !== '/') {
    items.push({ path: route.path, title })
  }
  return items
})

function handleCommand(cmd) {
  if (cmd === 'logout') {
    authStore.logout()
  }
}
</script>

<style scoped>
.navbar {
  height: 60px;
  background: #FFFFFF;
  border-bottom: 1px solid #E4E7ED;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}
.navbar-left {
  display: flex;
  align-items: center;
  gap: 16px;
}
.collapse-btn {
  cursor: pointer;
  color: #606266;
}
.collapse-btn:hover {
  color: #303133;
}
.navbar-right {
  display: flex;
  align-items: center;
}
.user-dropdown {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: #606266;
}
.username {
  font-size: 14px;
}
</style>
