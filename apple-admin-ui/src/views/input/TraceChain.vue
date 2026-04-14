<template>
  <div class="page-container">
    <div class="page-header">
      <h2>溯源链路</h2>
    </div>
    <div class="filter-bar">
      <el-input
        v-model="traceCode"
        placeholder="请输入溯源码"
        clearable
        style="width:280px"
        @clear="onClear"
        @keyup.enter="handleQuery"
      />
      <el-button type="primary" @click="handleQuery" :loading="loading" icon="Search">查询</el-button>
    </div>

    <div style="margin-top:24px">
      <!-- No search yet -->
      <template v-if="!searched">
        <el-empty description="请输入溯源码进行查询" />
      </template>

      <!-- No result -->
      <template v-else-if="searched && !traceResult">
        <el-empty description="未找到溯源信息，请检查溯源码是否正确" />
      </template>

      <!-- Result -->
      <template v-else>
        <div class="trace-summary" style="margin-bottom:24px">
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item label="溯源码">{{ traceResult.traceCode }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <el-timeline>
          <!-- Suppliers -->
          <el-timeline-item
            v-for="(supplier, idx) in (traceResult.suppliers || [])"
            :key="'s-' + idx"
            type="primary"
            :timestamp="supplier.createTime || ''"
            placement="top"
          >
            <el-card shadow="never" class="trace-card">
              <div class="trace-stage-title">供应商信息</div>
              <el-descriptions :column="2" size="small">
                <el-descriptions-item label="供应商编号">{{ supplier.supplierCode || '-' }}</el-descriptions-item>
                <el-descriptions-item label="供应商名称">{{ supplier.name || '-' }}</el-descriptions-item>
                <el-descriptions-item label="联系人">{{ supplier.contactPerson || '-' }}</el-descriptions-item>
                <el-descriptions-item label="联系电话">{{ supplier.phone || '-' }}</el-descriptions-item>
                <el-descriptions-item label="营业执照号">{{ supplier.license || '-' }}</el-descriptions-item>
                <el-descriptions-item label="状态">{{ supplier.status || '-' }}</el-descriptions-item>
              </el-descriptions>
            </el-card>
          </el-timeline-item>

          <!-- Purchases -->
          <el-timeline-item
            v-for="(purchase, idx) in (traceResult.purchases || [])"
            :key="'p-' + idx"
            type="success"
            :timestamp="purchase.purchaseDate || purchase.createTime || ''"
            placement="top"
          >
            <el-card shadow="never" class="trace-card">
              <div class="trace-stage-title">采购记录</div>
              <el-descriptions :column="2" size="small">
                <el-descriptions-item label="采购单号">{{ purchase.purchaseNo || '-' }}</el-descriptions-item>
                <el-descriptions-item label="供应商">{{ purchase.supplierName || '-' }}</el-descriptions-item>
                <el-descriptions-item label="产品名称">{{ purchase.productName || '-' }}</el-descriptions-item>
                <el-descriptions-item label="数量">{{ purchase.quantity || '-' }} {{ purchase.unit || '' }}</el-descriptions-item>
                <el-descriptions-item label="单价" class-name="nums-tabular">{{ purchase.unitPrice ?? '-' }}</el-descriptions-item>
                <el-descriptions-item label="总金额" class-name="nums-tabular">{{ purchase.totalAmount ?? '-' }}</el-descriptions-item>
                <el-descriptions-item label="采购日期">{{ purchase.purchaseDate || '-' }}</el-descriptions-item>
                <el-descriptions-item label="状态">{{ purchase.status || '-' }}</el-descriptions-item>
              </el-descriptions>
            </el-card>
          </el-timeline-item>

          <!-- Usages -->
          <el-timeline-item
            v-for="(usage, idx) in (traceResult.usages || [])"
            :key="'u-' + idx"
            type="warning"
            :timestamp="usage.usageDate || usage.createTime || ''"
            placement="top"
          >
            <el-card shadow="never" class="trace-card">
              <div class="trace-stage-title">使用记录</div>
              <el-descriptions :column="2" size="small">
                <el-descriptions-item label="产品名称">{{ usage.productName || '-' }}</el-descriptions-item>
                <el-descriptions-item label="果园名称">{{ usage.orchardName || '-' }}</el-descriptions-item>
                <el-descriptions-item label="批次编码">{{ usage.batchCode || '-' }}</el-descriptions-item>
                <el-descriptions-item label="用量">{{ usage.quantity || '-' }} {{ usage.unit || '' }}</el-descriptions-item>
                <el-descriptions-item label="施用方式">{{ usage.method || '-' }}</el-descriptions-item>
                <el-descriptions-item label="操作人">{{ usage.operator || '-' }}</el-descriptions-item>
                <el-descriptions-item label="使用日期">{{ usage.usageDate || '-' }}</el-descriptions-item>
                <el-descriptions-item label="备注">{{ usage.remark || '-' }}</el-descriptions-item>
              </el-descriptions>
            </el-card>
          </el-timeline-item>

          <!-- Empty fallback -->
          <el-empty
            v-if="!(traceResult.suppliers?.length || traceResult.purchases?.length || traceResult.usages?.length)"
            description="该溯源码暂无关联记录"
          />
        </el-timeline>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { inputTraceApi } from '@/api/input.js'

const traceCode = ref('')
const loading = ref(false)
const searched = ref(false)
const traceResult = ref(null)

function onClear() {
  searched.value = false
  traceResult.value = null
}

async function handleQuery() {
  if (!traceCode.value.trim()) return
  loading.value = true
  searched.value = false
  traceResult.value = null
  try {
    const res = await inputTraceApi.query(traceCode.value.trim())
    traceResult.value = res || null
  } catch (e) {
    traceResult.value = null
    console.error(e)
  } finally {
    loading.value = false
    searched.value = true
  }
}
</script>

<style scoped>
.trace-stage-title {
  font-weight: 600;
  margin-bottom: 8px;
  font-size: 14px;
}
.trace-card {
  border-radius: 6px;
}
</style>
