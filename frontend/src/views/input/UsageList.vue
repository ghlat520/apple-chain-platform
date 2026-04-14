<template>
  <div class="page-container">
    <div class="section-header">
      <span class="section-title">使用记录</span>
      <van-button type="primary" size="small" icon="plus" @click="openAddSheet">新增记录</van-button>
    </div>

    <van-search v-model="query.keyword" placeholder="搜索产品/果园/操作人" @search="loadList(true)" shape="round" />

    <van-dropdown-menu>
      <van-dropdown-item v-model="query.method" :options="methodOptions" @change="loadList(true)" />
    </van-dropdown-menu>

    <van-pull-refresh v-model="refreshing" @refresh="() => loadList(true)">
      <van-list v-model:loading="loading" :finished="finished" finished-text="没有更多了" @load="loadList">
        <van-cell-group inset style="margin-bottom:8px;" v-for="item in list" :key="item.id">
          <van-cell is-link @click="showDetail(item)">
            <template #title>
              <span style="font-weight:600;">{{ item.productName }}</span>
              <van-tag type="primary" style="margin-left:6px;">{{ item.method || '-' }}</van-tag>
            </template>
            <template #label>
              {{ item.orchardName || '-' }} · {{ item.operator || '-' }} · {{ item.usageDate || '-' }}
            </template>
          </van-cell>
          <van-cell title="使用量" :value="`${item.quantity} ${item.unit || ''}`" />
        </van-cell-group>
        <van-empty v-if="!loading && list.length === 0" description="暂无使用记录" />
      </van-list>
    </van-pull-refresh>

    <!-- 新增使用记录弹窗 -->
    <van-action-sheet v-model:show="showAddSheet" title="新增使用记录">
      <div style="padding:16px;">
        <van-form @submit="handleSave">
          <van-field v-model="addForm.productId" label="产品ID" type="digit" placeholder="请输入产品ID" :rules="[{required:true}]" />
          <van-field v-model="addForm.batchId" label="批次ID" type="digit" placeholder="请输入批次ID" />
          <van-field v-model="addForm.orchardId" label="果园ID" type="digit" placeholder="请输入果园ID" :rules="[{required:true}]" />
          <van-field v-model="addForm.quantity" label="使用量" type="number" placeholder="请输入" :rules="[{required:true}]" />
          <van-field v-model="addForm.unit" label="单位" placeholder="如 kg/L" :rules="[{required:true}]" />
          <van-field v-model="addForm.usageDate" is-link readonly label="使用日期" placeholder="请选择"
            :rules="[{required:true}]" @click="showDatePicker = true" />
          <van-field v-model="addForm.operator" label="操作人" placeholder="请输入" />
          <van-field v-model="addForm.method" is-link readonly label="施用方式" placeholder="请选择"
            :rules="[{required:true}]" @click="showMethodPicker = true" />
          <van-field v-model="addForm.traceCode" label="溯源码" placeholder="请输入（可选）" />
          <van-field v-model="addForm.remark" label="备注" type="textarea" placeholder="请输入" rows="2" />
          <van-button block type="primary" native-type="submit" style="margin-top:16px;" color="#07c160">提交</van-button>
        </van-form>
      </div>
    </van-action-sheet>

    <!-- 施用方式选择器 -->
    <van-popup v-model:show="showMethodPicker" round position="bottom">
      <van-picker :columns="methodPickerColumns" @confirm="onMethodConfirm" @cancel="showMethodPicker = false" />
    </van-popup>

    <!-- 日期选择器 -->
    <van-popup v-model:show="showDatePicker" round position="bottom">
      <van-date-picker v-model="datePickerValue" title="选择使用日期"
        :min-date="new Date(2020, 0, 1)" :max-date="new Date()"
        @confirm="onDateConfirm" @cancel="showDatePicker = false" />
    </van-popup>

    <!-- 详情弹窗 -->
    <van-popup v-model:show="showDetailPopup" round position="bottom" style="height:65%;">
      <div style="padding:16px;" v-if="selectedItem">
        <h3 style="margin-bottom:12px;">{{ selectedItem.productName }}</h3>
        <van-cell-group>
          <van-cell title="使用量" :value="`${selectedItem.quantity} ${selectedItem.unit || ''}`" />
          <van-cell title="果园" :value="selectedItem.orchardName || '-'" />
          <van-cell title="批次编码" :value="selectedItem.batchCode || '-'" />
          <van-cell title="使用日期" :value="selectedItem.usageDate || '-'" />
          <van-cell title="操作人" :value="selectedItem.operator || '-'" />
          <van-cell title="施用方式" :value="selectedItem.method || '-'" />
          <van-cell title="溯源码" :value="selectedItem.traceCode || '-'" />
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

const methodOptions = [
  { text: '全部方式', value: '' },
  { text: '撒施', value: '撒施' },
  { text: '喷洒', value: '喷洒' },
  { text: '滴灌', value: '滴灌' },
  { text: '穴施', value: '穴施' }
]

const methodPickerColumns = [
  { text: '撒施', value: '撒施' },
  { text: '喷洒', value: '喷洒' },
  { text: '滴灌', value: '滴灌' },
  { text: '穴施', value: '穴施' }
]

const list = ref([])
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const showAddSheet = ref(false)
const showDetailPopup = ref(false)
const showMethodPicker = ref(false)
const showDatePicker = ref(false)
const selectedItem = ref(null)

const now = new Date()
const datePickerValue = ref([String(now.getFullYear()), String(now.getMonth() + 1).padStart(2, '0'), String(now.getDate()).padStart(2, '0')])

const query = reactive({ keyword: '', page: 1, size: 10, method: '' })
const addForm = reactive({
  productId: '', batchId: '', orchardId: '', quantity: '', unit: '',
  usageDate: '', operator: '', method: '', traceCode: '', remark: ''
})

async function loadList(reset = false) {
  if (reset) { query.page = 1; list.value = []; finished.value = false }
  loading.value = true
  try {
    const res = await inputApi.usageList({ ...query })
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
    productId: '', batchId: '', orchardId: '', quantity: '', unit: '',
    usageDate: '', operator: '', method: '', traceCode: '', remark: ''
  })
  showAddSheet.value = true
}

function onMethodConfirm({ selectedValues }) {
  addForm.method = selectedValues[0]
  showMethodPicker.value = false
}

function onDateConfirm({ selectedValues }) {
  addForm.usageDate = selectedValues.join('-')
  showDatePicker.value = false
}

async function handleSave() {
  const payload = {
    ...addForm,
    productId: Number(addForm.productId),
    batchId: addForm.batchId ? Number(addForm.batchId) : null,
    orchardId: Number(addForm.orchardId),
    quantity: Number(addForm.quantity)
  }
  await inputApi.usageCreate(payload)
  showToast('使用记录创建成功')
  showAddSheet.value = false
  loadList(true)
}

loadList(true)
</script>
