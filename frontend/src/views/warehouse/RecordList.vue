<template>
  <div class="page-container">
    <div class="section-header">
      <span class="section-title">出入库记录</span>
      <van-button type="primary" size="small" icon="plus" @click="showAddSheet = true">新增记录</van-button>
    </div>

    <van-dropdown-menu>
      <van-dropdown-item v-model="query.type" :options="typeOptions" @change="loadList(true)" />
      <van-dropdown-item v-model="query.warehouseId" :options="warehouseOptions" @change="loadList(true)" />
    </van-dropdown-menu>

    <van-search v-model="query.keyword" placeholder="搜索记录编号/品种/操作人" @search="loadList(true)" shape="round" />

    <van-pull-refresh v-model="refreshing" @refresh="() => loadList(true)">
      <van-list v-model:loading="loading" :finished="finished" finished-text="没有更多了" @load="loadList">
        <van-cell-group inset style="margin-bottom:8px;" v-for="item in list" :key="item.id">
          <van-cell is-link @click="showDetail(item)">
            <template #title>
              <span style="font-weight:600;">{{ item.recordNo }}</span>
              <van-tag :type="item.type === 'INBOUND' ? 'success' : 'danger'" style="margin-left:6px;">
                {{ item.type === 'INBOUND' ? '入库' : '出库' }}
              </van-tag>
            </template>
            <template #label>
              {{ item.warehouseName }} · {{ item.operator || '-' }} · {{ item.recordDate || '-' }}
            </template>
          </van-cell>
          <van-cell title="品种" :value="item.variety" />
          <van-cell title="数量" :value="`${item.quantity} kg`" />
          <van-cell title="温度/湿度" :value="`${item.temperature || '-'}°C / ${item.humidity || '-'}%`" />
        </van-cell-group>
        <van-empty v-if="!loading && list.length === 0" description="暂无出入库记录" />
      </van-list>
    </van-pull-refresh>

    <!-- 新增记录 -->
    <van-action-sheet v-model:show="showAddSheet" title="新增出入库记录" style="max-height:85%;">
      <div style="padding:16px;max-height:70vh;overflow-y:auto;">
        <van-form @submit="handleCreate">
          <van-field v-model="addForm.warehouseId" label="仓库ID" placeholder="请输入" :rules="[{required:true}]" />
          <van-field
            v-model="addForm.type"
            is-link readonly label="类型" placeholder="请选择"
            @click="showTypePicker = true"
            :rules="[{required:true}]"
          />
          <van-field v-model="addForm.batchCode" label="批次编码" placeholder="请输入" :rules="[{required:true}]" />
          <van-field v-model="addForm.variety" label="品种" placeholder="如：红富士" :rules="[{required:true}]" />
          <van-field v-model="addForm.grade" label="等级" placeholder="如：A级" />
          <van-field v-model="addForm.quantity" label="数量(kg)" type="number" placeholder="请输入" :rules="[{required:true}]" />
          <van-field v-model="addForm.operator" label="操作人" placeholder="请输入" />
          <van-field v-model="addForm.recordDate" label="日期" placeholder="YYYY-MM-DD" />
          <van-field v-model="addForm.traceCode" label="溯源码" placeholder="请输入" />
          <van-button block type="primary" native-type="submit" style="margin-top:16px;" color="#07c160">提交</van-button>
        </van-form>
      </div>
    </van-action-sheet>

    <!-- 类型选择器 -->
    <van-popup v-model:show="showTypePicker" round position="bottom">
      <van-picker
        :columns="typePickerColumns"
        @confirm="onTypeConfirm"
        @cancel="showTypePicker = false"
      />
    </van-popup>

    <!-- 记录详情 -->
    <van-popup v-model:show="showDetailPopup" round position="bottom" style="height:60%;">
      <div style="padding:16px;" v-if="selectedRecord">
        <h3 style="margin-bottom:12px;">{{ selectedRecord.recordNo }}</h3>
        <van-cell-group>
          <van-cell title="仓库名称" :value="selectedRecord.warehouseName || '-'" />
          <van-cell title="类型">
            <template #value>
              <van-tag :type="selectedRecord.type === 'INBOUND' ? 'success' : 'danger'">
                {{ selectedRecord.type === 'INBOUND' ? '入库' : '出库' }}
              </van-tag>
            </template>
          </van-cell>
          <van-cell title="品种" :value="selectedRecord.variety || '-'" />
          <van-cell title="等级" :value="selectedRecord.grade || '-'" />
          <van-cell title="数量" :value="`${selectedRecord.quantity} kg`" />
          <van-cell title="温度" :value="`${selectedRecord.temperature || '-'}°C`" />
          <van-cell title="湿度" :value="`${selectedRecord.humidity || '-'}%`" />
          <van-cell title="批次编码" :value="selectedRecord.batchCode || '-'" />
          <van-cell title="操作人" :value="selectedRecord.operator || '-'" />
          <van-cell title="日期" :value="selectedRecord.recordDate || '-'" />
          <van-cell title="溯源码" :value="selectedRecord.traceCode || '-'" />
        </van-cell-group>
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { warehouseApi } from '@/api/warehouse'
import { showToast } from 'vant'

const list = ref([])
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const showAddSheet = ref(false)
const showDetailPopup = ref(false)
const showTypePicker = ref(false)
const selectedRecord = ref(null)
const warehouseOptions = ref([{ text: '全部仓库', value: '' }])

const query = reactive({ keyword: '', type: '', warehouseId: '', page: 1, size: 10 })
const addForm = reactive({
  warehouseId: '', type: '', batchCode: '', variety: '',
  grade: '', quantity: '', operator: '', recordDate: '', traceCode: ''
})

const typeOptions = [
  { text: '全部类型', value: '' },
  { text: '入库', value: 'INBOUND' },
  { text: '出库', value: 'OUTBOUND' }
]

const typePickerColumns = [
  { text: '入库', value: 'INBOUND' },
  { text: '出库', value: 'OUTBOUND' }
]

function onTypeConfirm({ selectedOptions }) {
  addForm.type = selectedOptions[0]?.value || ''
  showTypePicker.value = false
}

async function loadWarehouseOptions() {
  try {
    const res = await warehouseApi.list({ page: 1, size: 200 })
    const records = res.data?.records || []
    warehouseOptions.value = [
      { text: '全部仓库', value: '' },
      ...records.map(w => ({ text: w.name, value: String(w.id) }))
    ]
  } catch {
    // keep default option
  }
}

async function loadList(reset = false) {
  if (reset) { query.page = 1; list.value = []; finished.value = false }
  loading.value = true
  try {
    const res = await warehouseApi.recordList({ ...query })
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

function showDetail(record) {
  selectedRecord.value = record
  showDetailPopup.value = true
}

async function handleCreate() {
  await warehouseApi.recordCreate(addForm)
  showToast('记录创建成功')
  showAddSheet.value = false
  Object.assign(addForm, {
    warehouseId: '', type: '', batchCode: '', variety: '',
    grade: '', quantity: '', operator: '', recordDate: '', traceCode: ''
  })
  loadList(true)
}

onMounted(() => {
  loadWarehouseOptions()
  loadList(true)
})
</script>
