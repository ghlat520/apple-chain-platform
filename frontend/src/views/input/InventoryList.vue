<template>
  <div class="page-container">
    <div class="section-header">
      <span class="section-title">库存管理</span>
      <div>
        <van-button type="warning" size="small" icon="warning-o" @click="openAlerts" style="margin-right:8px;">预警</van-button>
        <van-button type="primary" size="small" icon="plus" @click="openAddSheet">新增库存</van-button>
      </div>
    </div>

    <van-search v-model="query.keyword" placeholder="搜索产品名称" @search="loadList(true)" shape="round" />

    <van-dropdown-menu>
      <van-dropdown-item v-model="query.status" :options="statusOptions" @change="loadList(true)" />
    </van-dropdown-menu>

    <van-pull-refresh v-model="refreshing" @refresh="() => loadList(true)">
      <van-list v-model:loading="loading" :finished="finished" finished-text="没有更多了" @load="loadList">
        <van-cell-group inset style="margin-bottom:8px;" v-for="item in list" :key="item.id">
          <van-cell is-link @click="showDetail(item)">
            <template #title>
              <span style="font-weight:600;">{{ item.productName }}</span>
              <van-tag :type="statusTagType(item.status)" style="margin-left:6px;">
                {{ statusLabel(item.status) }}
              </van-tag>
            </template>
            <template #label>
              库存: {{ item.stockQuantity }} {{ item.unit }} · 预警: {{ item.warningLevel }} {{ item.unit }}
            </template>
          </van-cell>
          <van-cell title="存放位置" :value="item.warehouse || '-'" />
          <div style="padding:8px 16px;">
            <van-button size="mini" type="primary" @click.stop="openAdjustSheet(item)">调整库存</van-button>
          </div>
        </van-cell-group>
        <van-empty v-if="!loading && list.length === 0" description="暂无库存数据" />
      </van-list>
    </van-pull-refresh>

    <!-- 新增库存弹窗 -->
    <van-action-sheet v-model:show="showAddSheet" title="新增库存">
      <div style="padding:16px;">
        <van-form @submit="handleSave">
          <van-field v-model="addForm.productId" label="产品ID" type="digit" placeholder="请输入产品ID" :rules="[{required:true}]" />
          <van-field v-model="addForm.farmerId" label="农户ID" type="digit" placeholder="请输入农户ID" />
          <van-field v-model="addForm.stockQuantity" label="初始库存" type="number" placeholder="请输入" :rules="[{required:true}]" />
          <van-field v-model="addForm.unit" label="单位" placeholder="如 kg/L/袋" :rules="[{required:true}]" />
          <van-field v-model="addForm.warningLevel" label="预警阈值" type="number" placeholder="低于此值预警" />
          <van-field v-model="addForm.maxLevel" label="超储上限" type="number" placeholder="高于此值预警" />
          <van-field v-model="addForm.warehouse" label="存放位置" placeholder="请输入" />
          <van-field v-model="addForm.remark" label="备注" type="textarea" placeholder="请输入" rows="2" />
          <van-button block type="primary" native-type="submit" style="margin-top:16px;" color="#07c160">提交</van-button>
        </van-form>
      </div>
    </van-action-sheet>

    <!-- 库存调整弹窗 -->
    <van-action-sheet v-model:show="showAdjustSheet" title="库存调整">
      <div style="padding:16px;">
        <div style="margin-bottom:12px; font-size:14px; color:#666;">
          当前库存: <b>{{ adjustTarget?.stockQuantity }} {{ adjustTarget?.unit }}</b>
        </div>
        <van-form @submit="handleAdjust">
          <van-field v-model="adjustForm.delta" label="调整量" type="number" placeholder="正数=增加，负数=减少" :rules="[{required:true}]" />
          <van-field v-model="adjustForm.reason" label="调整原因" type="textarea" placeholder="请输入原因" rows="2" />
          <van-button block type="primary" native-type="submit" style="margin-top:16px;" color="#07c160">确认调整</van-button>
        </van-form>
      </div>
    </van-action-sheet>

    <!-- 预警列表弹窗 -->
    <van-popup v-model:show="showAlertPopup" round position="bottom" style="height:60%;">
      <div style="padding:16px;">
        <h3 style="margin-bottom:12px;">库存预警</h3>
        <van-empty v-if="alertList.length === 0" description="暂无预警" />
        <van-cell-group inset v-for="alert in alertList" :key="alert.id" style="margin-bottom:8px;">
          <van-cell :title="alert.productName" :value="`${alert.stockQuantity} ${alert.unit}`">
            <template #label>
              预警阈值: {{ alert.warningLevel }} {{ alert.unit }} · {{ alert.warehouse || '-' }}
            </template>
          </van-cell>
        </van-cell-group>
      </div>
    </van-popup>

    <!-- 详情弹窗 -->
    <van-popup v-model:show="showDetailPopup" round position="bottom" style="height:60%;">
      <div style="padding:16px;" v-if="selectedItem">
        <h3 style="margin-bottom:12px;">{{ selectedItem.productName }}</h3>
        <van-cell-group>
          <van-cell title="状态" :value="statusLabel(selectedItem.status)" />
          <van-cell title="当前库存" :value="`${selectedItem.stockQuantity} ${selectedItem.unit}`" />
          <van-cell title="预警阈值" :value="`${selectedItem.warningLevel} ${selectedItem.unit}`" />
          <van-cell title="超储上限" :value="selectedItem.maxLevel != null ? `${selectedItem.maxLevel} ${selectedItem.unit}` : '-'" />
          <van-cell title="存放位置" :value="selectedItem.warehouse || '-'" />
          <van-cell title="备注" :value="selectedItem.remark || '-'" />
        </van-cell-group>
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { inputApi } from '@/api/input'
import { showToast } from 'vant'

const statusMap = {
  NORMAL: { label: '正常', type: 'success' },
  LOW: { label: '库存不足', type: 'warning' },
  EMPTY: { label: '缺货', type: 'danger' },
  OVERSTOCKED: { label: '超储', type: 'primary' }
}

const statusOptions = [
  { text: '全部状态', value: '' },
  { text: '正常', value: 'NORMAL' },
  { text: '库存不足', value: 'LOW' },
  { text: '缺货', value: 'EMPTY' },
  { text: '超储', value: 'OVERSTOCKED' }
]

function statusLabel(status) {
  return statusMap[status]?.label || status
}

function statusTagType(status) {
  return statusMap[status]?.type || 'default'
}

const list = ref([])
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const showAddSheet = ref(false)
const showDetailPopup = ref(false)
const showAdjustSheet = ref(false)
const showAlertPopup = ref(false)
const selectedItem = ref(null)
const adjustTarget = ref(null)
const alertList = ref([])

const query = reactive({ keyword: '', page: 1, size: 10, status: '' })
const addForm = reactive({
  productId: '', farmerId: '', stockQuantity: '', unit: '',
  warningLevel: '', maxLevel: '', warehouse: '', remark: ''
})
const adjustForm = reactive({ delta: '', reason: '' })

async function loadList(reset = false) {
  if (reset) { query.page = 1; list.value = []; finished.value = false }
  loading.value = true
  try {
    const res = await inputApi.inventoryList({ ...query })
    const records = res.data?.records || []
    if (reset) list.value = records
    else list.value.push(...records)
    finished.value = list.value.length >= (res.data?.total || 0)
    query.page++
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

function showDetail(item) {
  selectedItem.value = item
  showDetailPopup.value = true
}

function openAddSheet() {
  Object.assign(addForm, {
    productId: '', farmerId: '', stockQuantity: '', unit: '',
    warningLevel: '', maxLevel: '', warehouse: '', remark: ''
  })
  showAddSheet.value = true
}

function openAdjustSheet(item) {
  adjustTarget.value = item
  adjustForm.delta = ''
  adjustForm.reason = ''
  showAdjustSheet.value = true
}

async function openAlerts() {
  const res = await inputApi.inventoryAlerts()
  alertList.value = res.data || []
  showAlertPopup.value = true
}

async function handleSave() {
  const payload = {
    ...addForm,
    productId: Number(addForm.productId),
    farmerId: addForm.farmerId ? Number(addForm.farmerId) : null,
    stockQuantity: Number(addForm.stockQuantity),
    warningLevel: addForm.warningLevel ? Number(addForm.warningLevel) : null,
    maxLevel: addForm.maxLevel ? Number(addForm.maxLevel) : null
  }
  await inputApi.inventoryCreate(payload)
  showToast('库存记录创建成功')
  showAddSheet.value = false
  loadList(true)
}

async function handleAdjust() {
  if (!adjustTarget.value) return
  await inputApi.inventoryAdjust(adjustTarget.value.id, {
    delta: Number(adjustForm.delta),
    reason: adjustForm.reason || null
  })
  showToast('库存调整成功')
  showAdjustSheet.value = false
  loadList(true)
}

loadList(true)
</script>
