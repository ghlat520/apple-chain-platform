<template>
  <div class="page-container">
    <div class="page-header">
      <h2>审计日志</h2>
    </div>
    <div class="filter-bar">
      <el-input v-model="query.username" placeholder="用户名" clearable style="width:140px" @clear="loadData" />
      <el-select v-model="query.module" placeholder="模块" clearable style="width:140px" @change="loadData">
        <el-option label="大数据" value="bigdata" />
      </el-select>
      <el-select v-model="query.action" placeholder="操作" clearable style="width:180px" @change="loadData">
        <el-option label="查看报告" value="VIEW_REPORT" />
        <el-option label="列表资产" value="LIST_ASSETS" />
        <el-option label="查看指标" value="VIEW_METRIC" />
        <el-option label="触发任务" value="TRIGGER_JOB" />
        <el-option label="创建规则" value="CREATE_RULE" />
        <el-option label="查看大屏" value="VIEW_DASHBOARD" />
        <el-option label="发布报告" value="PUBLISH_REPORT" />
        <el-option label="注册客户端" value="REGISTER_CLIENT" />
        <el-option label="导出血缘" value="EXPORT_LINEAGE" />
      </el-select>
      <el-button type="primary" @click="loadData" icon="Search">查询</el-button>
    </div>
    <el-table :data="tableData" stripe v-loading="loading" border>
      <el-table-column prop="createTime" label="时间" width="180" />
      <el-table-column prop="username" label="用户" width="100" />
      <el-table-column prop="roleCode" label="角色" width="100" />
      <el-table-column prop="module" label="模块" width="100" />
      <el-table-column prop="action" label="操作" width="130">
        <template #default="{ row }">
          <el-tag size="small" :type="actionType(row.action)">{{ row.action }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="requestUri" label="目标" />
      <el-table-column prop="requestIp" label="IP" width="140" />
      <el-table-column prop="durationMs" label="耗时(ms)" width="100" class-name="nums-tabular" />
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.responseStatus === 200 ? 'success' : 'danger'" size="small">{{ row.responseStatus }}</el-tag>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      v-if="total > 0"
      style="margin-top:16px;justify-content:flex-end"
      background
      layout="total, prev, pager, next"
      :total="total"
      :page-size="query.size"
      :current-page="query.page"
      @current-change="(p) => { query.page = p; loadData() }"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { auditApi } from '@/api/bigdata.js'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const query = reactive({ page: 1, size: 20, username: '', module: '', action: '' })

function actionType(a) {
  if (a.startsWith('CREATE') || a.startsWith('REGISTER')) return 'success'
  if (a.startsWith('UPDATE') || a.startsWith('PUBLISH')) return 'primary'
  if (a.startsWith('DELETE')) return 'danger'
  if (a.startsWith('EXPORT')) return 'warning'
  return 'info'
}

async function loadData() {
  loading.value = true
  try {
    const res = await auditApi.list(query)
    tableData.value = res?.records || res?.list || []
    total.value = res?.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>
