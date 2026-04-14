<template>
  <div class="page-container">
    <div class="section-header">
      <span class="section-title">溯源链路查询</span>
    </div>

    <van-search v-model="traceCode" placeholder="输入溯源码查询" @search="handleSearch" shape="round" />

    <div style="padding:0 16px; margin-bottom:12px;">
      <van-button block type="primary" @click="handleSearch" :loading="searching" color="#07c160">查询溯源链路</van-button>
    </div>

    <van-empty v-if="!chain && !searching && searched" description="未找到相关溯源信息" />
    <van-empty v-if="!chain && !searching && !searched" image="search" description="请输入溯源码进行查询" />

    <div v-if="chain" style="padding:0 16px;">
      <van-cell-group inset style="margin-bottom:16px;">
        <van-cell title="溯源码" :value="chain.traceCode" />
      </van-cell-group>

      <!-- 溯源链路折叠面板 -->
      <van-collapse v-model="activeNames">
        <!-- 第一级：供应商信息 -->
        <van-collapse-item title="供应商信息" name="supplier" v-if="chain.suppliers && chain.suppliers.length > 0">
          <template #label>
            <span style="color:#999;">共 {{ chain.suppliers.length }} 条</span>
          </template>
          <van-cell-group inset v-for="supplier in chain.suppliers" :key="supplier.id" style="margin-bottom:8px;">
            <van-cell :title="supplier.name">
              <template #label>
                <div>
                  <van-tag :type="supplierStatusTagType(supplier.status)" size="small">{{ supplierStatusLabel(supplier.status) }}</van-tag>
                </div>
              </template>
            </van-cell>
            <van-cell title="联系人" :value="supplier.contactPerson || '-'" />
            <van-cell title="联系电话" :value="supplier.phone || '-'" />
            <van-cell title="信用评分" :value="supplier.creditScore != null ? supplier.creditScore + '分' : '-'" />
            <van-cell title="地址" :value="supplier.address || '-'" />
          </van-cell-group>
        </van-collapse-item>

        <!-- 第二级：采购记录 -->
        <van-collapse-item title="采购记录" name="purchase" v-if="chain.purchases && chain.purchases.length > 0">
          <template #label>
            <span style="color:#999;">共 {{ chain.purchases.length }} 条</span>
          </template>
          <van-cell-group inset v-for="purchase in chain.purchases" :key="purchase.id" style="margin-bottom:8px;">
            <van-cell :title="purchase.purchaseNo">
              <template #right-icon>
                <van-tag :type="purchaseStatusTagType(purchase.status)" size="small">{{ purchaseStatusLabel(purchase.status) }}</van-tag>
              </template>
            </van-cell>
            <van-cell title="产品" :value="purchase.productName || '-'" />
            <van-cell title="供应商" :value="purchase.supplierName || '-'" />
            <van-cell title="数量" :value="`${purchase.quantity} ${purchase.unit || ''}`" />
            <van-cell title="总金额" :value="purchase.totalAmount != null ? `¥${purchase.totalAmount}` : '-'" />
            <van-cell title="采购日期" :value="purchase.purchaseDate || '-'" />
          </van-cell-group>
        </van-collapse-item>

        <!-- 第三级：使用记录 -->
        <van-collapse-item title="使用记录" name="usage" v-if="chain.usages && chain.usages.length > 0">
          <template #label>
            <span style="color:#999;">共 {{ chain.usages.length }} 条</span>
          </template>
          <van-cell-group inset v-for="usage in chain.usages" :key="usage.id" style="margin-bottom:8px;">
            <van-cell :title="usage.productName || '-'">
              <template #right-icon>
                <van-tag type="primary" size="small">{{ usage.method || '-' }}</van-tag>
              </template>
            </van-cell>
            <van-cell title="果园" :value="usage.orchardName || '-'" />
            <van-cell title="使用量" :value="`${usage.quantity} ${usage.unit || ''}`" />
            <van-cell title="使用日期" :value="usage.usageDate || '-'" />
            <van-cell title="操作人" :value="usage.operator || '-'" />
          </van-cell-group>
        </van-collapse-item>
      </van-collapse>

      <!-- 溯源流程可视化 -->
      <div style="margin-top:16px;">
        <van-steps direction="vertical" :active="activeStep" active-color="#07c160">
          <van-step v-if="chain.suppliers && chain.suppliers.length > 0">
            <h4>供应商</h4>
            <p style="color:#666; font-size:12px;">{{ chain.suppliers.map(s => s.name).join(', ') }}</p>
          </van-step>
          <van-step v-if="chain.purchases && chain.purchases.length > 0">
            <h4>采购入库</h4>
            <p style="color:#666; font-size:12px;">{{ chain.purchases.length }} 笔采购</p>
          </van-step>
          <van-step v-if="chain.usages && chain.usages.length > 0">
            <h4>投入使用</h4>
            <p style="color:#666; font-size:12px;">{{ chain.usages.length }} 条使用记录</p>
          </van-step>
        </van-steps>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { inputApi } from '@/api/input'
import { showToast } from 'vant'

const supplierStatusMap = {
  PENDING: { label: '待审核', type: 'warning' },
  APPROVED: { label: '已通过', type: 'success' },
  REJECTED: { label: '已拒绝', type: 'danger' },
  BLACKLISTED: { label: '已拉黑', type: 'default' }
}

const purchaseStatusMap = {
  PENDING: { label: '待审批', type: 'warning' },
  APPROVED: { label: '已审批', type: 'success' },
  RECEIVED: { label: '已收货', type: 'primary' },
  CANCELLED: { label: '已取消', type: 'default' }
}

function supplierStatusLabel(status) { return supplierStatusMap[status]?.label || status }
function supplierStatusTagType(status) { return supplierStatusMap[status]?.type || 'default' }
function purchaseStatusLabel(status) { return purchaseStatusMap[status]?.label || status }
function purchaseStatusTagType(status) { return purchaseStatusMap[status]?.type || 'default' }

const traceCode = ref('')
const chain = ref(null)
const searching = ref(false)
const searched = ref(false)
const activeNames = ref(['supplier', 'purchase', 'usage'])

const activeStep = computed(() => {
  if (!chain.value) return 0
  let step = -1
  if (chain.value.suppliers?.length) step = 0
  if (chain.value.purchases?.length) step = 1
  if (chain.value.usages?.length) step = 2
  return step
})

async function handleSearch() {
  const code = traceCode.value.trim()
  if (!code) {
    showToast('请输入溯源码')
    return
  }
  searching.value = true
  chain.value = null
  try {
    const res = await inputApi.traceChain(code)
    chain.value = res.data
    searched.value = true
    if (!chain.value) {
      showToast('未找到相关溯源信息')
    }
  } catch {
    searched.value = true
  } finally {
    searching.value = false
  }
}
</script>
