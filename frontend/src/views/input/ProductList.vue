<template>
  <div class="page-container">
    <div class="section-header">
      <span class="section-title">农资产品管理</span>
      <van-button type="primary" size="small" icon="plus" @click="openAddSheet">新增产品</van-button>
    </div>

    <van-search v-model="query.keyword" placeholder="搜索产品名称/编码" @search="loadList(true)" shape="round" />

    <van-dropdown-menu>
      <van-dropdown-item v-model="query.type" :options="typeOptions" @change="loadList(true)" />
    </van-dropdown-menu>

    <van-pull-refresh v-model="refreshing" @refresh="() => loadList(true)">
      <van-list v-model:loading="loading" :finished="finished" finished-text="没有更多了" @load="loadList">
        <van-cell-group inset style="margin-bottom:8px;" v-for="item in list" :key="item.id">
          <van-cell is-link @click="showDetail(item)">
            <template #title>
              <span style="font-weight:600;">{{ item.name }}</span>
              <van-tag :type="typeTagColor(item.type)" style="margin-left:6px;">
                {{ typeLabel(item.type) }}
              </van-tag>
              <van-tag :type="item.status === 'ACTIVE' ? 'success' : 'default'" style="margin-left:4px;">
                {{ item.status === 'ACTIVE' ? '在用' : '停用' }}
              </van-tag>
            </template>
            <template #label>
              {{ item.productCode }} · {{ item.manufacturer }}
            </template>
          </van-cell>
          <van-cell title="规格" :value="item.spec || '-'" />
        </van-cell-group>
        <van-empty v-if="!loading && list.length === 0" description="暂无产品数据" />
      </van-list>
    </van-pull-refresh>

    <!-- 新增/编辑产品弹窗 -->
    <van-action-sheet v-model:show="showAddSheet" :title="editingId ? '编辑产品' : '新增产品'">
      <div style="padding:16px;">
        <van-form @submit="handleSave">
          <van-field v-model="addForm.name" label="产品名称" placeholder="请输入" :rules="[{required:true}]" />
          <van-field v-model="addForm.type" is-link readonly label="产品类型" placeholder="请选择"
            :rules="[{required:true}]" @click="showTypePicker = true" />
          <van-field v-model="addForm.manufacturer" label="生产厂家" placeholder="请输入" />
          <van-field v-model="addForm.spec" label="规格" placeholder="如 50kg/袋" />
          <van-field v-model="addForm.batchNo" label="生产批号" placeholder="请输入" />
          <van-field v-model="addForm.productionDate" is-link readonly label="生产日期" placeholder="请选择"
            @click="showProdDatePicker = true" />
          <van-field v-model="addForm.expiryDate" is-link readonly label="保质期至" placeholder="请选择"
            @click="showExpiryDatePicker = true" />
          <van-field v-model="addForm.registration" label="登记证号" placeholder="请输入" />
          <van-field v-model="addForm.remark" label="备注" type="textarea" placeholder="请输入" rows="2" />
          <van-button block type="primary" native-type="submit" style="margin-top:16px;" color="#07c160">提交</van-button>
        </van-form>
      </div>
    </van-action-sheet>

    <!-- 类型选择器 -->
    <van-popup v-model:show="showTypePicker" round position="bottom">
      <van-picker :columns="typePickerColumns" @confirm="onTypeConfirm" @cancel="showTypePicker = false" />
    </van-popup>

    <!-- 生产日期选择器 -->
    <van-popup v-model:show="showProdDatePicker" round position="bottom">
      <van-date-picker v-model="prodDatePickerValue" title="选择生产日期"
        :min-date="new Date(2020, 0, 1)" :max-date="new Date()"
        @confirm="onProdDateConfirm" @cancel="showProdDatePicker = false" />
    </van-popup>

    <!-- 保质期选择器 -->
    <van-popup v-model:show="showExpiryDatePicker" round position="bottom">
      <van-date-picker v-model="expiryDatePickerValue" title="选择保质期至"
        :min-date="new Date()" :max-date="new Date(2030, 11, 31)"
        @confirm="onExpiryDateConfirm" @cancel="showExpiryDatePicker = false" />
    </van-popup>

    <!-- 详情弹窗 -->
    <van-popup v-model:show="showDetailPopup" round position="bottom" style="height:70%;">
      <div style="padding:16px;" v-if="selectedItem">
        <h3 style="margin-bottom:12px;">{{ selectedItem.name }}</h3>
        <van-cell-group>
          <van-cell title="产品编码" :value="selectedItem.productCode || '-'" />
          <van-cell title="类型" :value="typeLabel(selectedItem.type)" />
          <van-cell title="生产厂家" :value="selectedItem.manufacturer || '-'" />
          <van-cell title="规格" :value="selectedItem.spec || '-'" />
          <van-cell title="生产批号" :value="selectedItem.batchNo || '-'" />
          <van-cell title="生产日期" :value="selectedItem.productionDate || '-'" />
          <van-cell title="保质期至" :value="selectedItem.expiryDate || '-'" />
          <van-cell title="登记证号" :value="selectedItem.registration || '-'" />
          <van-cell title="状态" :value="selectedItem.status === 'ACTIVE' ? '在用' : '停用'" />
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

const typeMap = {
  FERTILIZER: { label: '肥料', color: 'success' },
  PESTICIDE: { label: '农药', color: 'danger' },
  SEED: { label: '种子', color: 'warning' },
  TOOL: { label: '工具', color: 'primary' }
}

const typeOptions = [
  { text: '全部类型', value: '' },
  { text: '肥料', value: 'FERTILIZER' },
  { text: '农药', value: 'PESTICIDE' },
  { text: '种子', value: 'SEED' },
  { text: '工具', value: 'TOOL' }
]

const typePickerColumns = [
  { text: '肥料', value: 'FERTILIZER' },
  { text: '农药', value: 'PESTICIDE' },
  { text: '种子', value: 'SEED' },
  { text: '工具', value: 'TOOL' }
]

function typeLabel(type) {
  return typeMap[type]?.label || type
}

function typeTagColor(type) {
  return typeMap[type]?.color || 'default'
}

const list = ref([])
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const showAddSheet = ref(false)
const showDetailPopup = ref(false)
const showTypePicker = ref(false)
const showProdDatePicker = ref(false)
const showExpiryDatePicker = ref(false)
const selectedItem = ref(null)
const editingId = ref(null)

const now = new Date()
const prodDatePickerValue = ref([String(now.getFullYear()), String(now.getMonth() + 1).padStart(2, '0'), String(now.getDate()).padStart(2, '0')])
const expiryDatePickerValue = ref([String(now.getFullYear()), String(now.getMonth() + 1).padStart(2, '0'), String(now.getDate()).padStart(2, '0')])

const query = reactive({ keyword: '', page: 1, size: 10, type: '' })
const addForm = reactive({
  name: '', type: '', manufacturer: '', spec: '',
  batchNo: '', productionDate: '', expiryDate: '',
  registration: '', remark: ''
})

async function loadList(reset = false) {
  if (reset) { query.page = 1; list.value = []; finished.value = false }
  loading.value = true
  try {
    const res = await inputApi.productList({ ...query })
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
  editingId.value = null
  Object.assign(addForm, {
    name: '', type: '', manufacturer: '', spec: '',
    batchNo: '', productionDate: '', expiryDate: '',
    registration: '', remark: ''
  })
  showAddSheet.value = true
}

function onTypeConfirm({ selectedValues }) {
  addForm.type = selectedValues[0]
  showTypePicker.value = false
}

function onProdDateConfirm({ selectedValues }) {
  addForm.productionDate = selectedValues.join('-')
  showProdDatePicker.value = false
}

function onExpiryDateConfirm({ selectedValues }) {
  addForm.expiryDate = selectedValues.join('-')
  showExpiryDatePicker.value = false
}

async function handleSave() {
  if (editingId.value) {
    await inputApi.productUpdate(editingId.value, { ...addForm })
    showToast('产品更新成功')
  } else {
    await inputApi.productCreate({ ...addForm })
    showToast('产品创建成功')
  }
  showAddSheet.value = false
  loadList(true)
}

loadList(true)
</script>
