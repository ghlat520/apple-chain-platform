<template>
  <div class="page-container">
    <div class="section-header">
      <span class="section-title">预冷管理</span>
      <van-button type="primary" size="small" icon="plus" @click="openAddSheet()">新增批次</van-button>
    </div>

    <van-search v-model="query.keyword" placeholder="搜索批次编码/品种/操作人" @search="loadList(true)" shape="round" />

    <van-tabs v-model:active="activeStatus" @change="handleTabChange" style="margin-bottom:12px;">
      <van-tab title="全部" name="" />
      <van-tab title="待处理" name="PENDING" />
      <van-tab title="处理中" name="PROCESSING" />
      <van-tab title="已完成" name="COMPLETED" />
    </van-tabs>

    <van-pull-refresh v-model="refreshing" @refresh="() => loadList(true)">
      <van-list v-model:loading="loading" :finished="finished" finished-text="没有更多了" @load="loadList">
        <van-swipe-cell v-for="item in list" :key="item.id" style="margin-bottom:8px;">
          <van-cell-group inset>
            <van-cell is-link @click="showDetailPopupFn(item)">
              <template #title>
                <span style="font-weight:600;">{{ item.batchCode }}</span>
                <van-tag :type="statusTagMap[item.status]?.type || 'default'" style="margin-left:6px;">
                  {{ statusTagMap[item.status]?.label || item.status }}
                </van-tag>
              </template>
            </van-cell>
            <van-cell title="品种" :value="item.variety || '-'" />
            <van-cell title="数量" :value="item.quantity || '-'" />
            <van-cell title="预冷方式" :value="methodTagMap[item.precoolMethod]?.label || '-'" />
            <van-cell title="预冷温度" :value="`${item.precoolTemp ?? '-'}°C`" />
            <van-cell title="目标温度" :value="`${item.targetTemp ?? '-'}°C`" />
          </van-cell-group>
          <template #right>
            <van-button square type="primary" text="编辑" style="height:100%;" @click="openEditSheet(item)" />
            <van-button square type="danger" text="删除" style="height:100%;" @click="handleDelete(item.id)" />
          </template>
        </van-swipe-cell>
        <van-empty v-if="!loading && list.length === 0" description="暂无预冷记录" />
      </van-list>
    </van-pull-refresh>

    <!-- 新增/编辑弹窗 -->
    <van-action-sheet v-model:show="showFormSheet" :title="isEdit ? '编辑预冷批次' : '新增预冷批次'">
      <div style="padding:16px;">
        <van-form @submit="handleSubmit">
          <van-field v-model="form.batchCode" label="批次编码" placeholder="请输入" :rules="[{required:true}]" />
          <van-field v-model="form.variety" label="品种" placeholder="如：红富士" :rules="[{required:true}]" />
          <van-field v-model="form.quantity" label="数量" placeholder="请输入" :rules="[{required:true}]" />
          <van-field v-model="form.precoolMethod" is-link readonly label="预冷方式" placeholder="请选择" @click="showMethodPicker = true" :rules="[{required:true}]" />
          <van-field v-model="form.precoolTemp" label="预冷温度(°C)" type="number" placeholder="请输入" />
          <van-field v-model="form.targetTemp" label="目标温度(°C)" type="number" placeholder="请输入" />
          <van-field v-model="form.operator" label="操作人" placeholder="请输入" />
          <van-field v-model="form.remark" label="备注" type="textarea" placeholder="选填" rows="2" />
          <van-button block type="primary" native-type="submit" style="margin-top:16px;" color="#07c160">
            {{ isEdit ? '保存修改' : '提交' }}
          </van-button>
        </van-form>
      </div>
    </van-action-sheet>

    <!-- 预冷方式选择器 -->
    <van-popup v-model:show="showMethodPicker" round position="bottom">
      <van-picker :columns="methodPickerColumns" @confirm="onMethodConfirm" @cancel="showMethodPicker = false" />
    </van-popup>

    <!-- 详情弹窗 -->
    <van-popup v-model:show="showDetailPopup" round position="bottom" style="height:65%;">
      <div style="padding:16px;" v-if="selectedItem">
        <h3 style="margin-bottom:12px;">{{ selectedItem.batchCode }}</h3>
        <van-cell-group>
          <van-cell title="品种" :value="selectedItem.variety || '-'" />
          <van-cell title="数量" :value="selectedItem.quantity || '-'" />
          <van-cell title="预冷方式" :value="methodTagMap[selectedItem.precoolMethod]?.label || '-'" />
          <van-cell title="预冷温度" :value="`${selectedItem.precoolTemp ?? '-'}°C`" />
          <van-cell title="目标温度" :value="`${selectedItem.targetTemp ?? '-'}°C`" />
          <van-cell title="操作人" :value="selectedItem.operator || '-'" />
          <van-cell title="状态" :value="statusTagMap[selectedItem.status]?.label || '-'" />
          <van-cell title="开始时间" :value="selectedItem.startTime || '-'" />
          <van-cell title="结束时间" :value="selectedItem.endTime || '-'" />
          <van-cell title="备注" :value="selectedItem.remark || '-'" />
        </van-cell-group>
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { coldchainApi } from '@/api/coldchain'
import { showToast, showConfirmDialog } from 'vant'

const list = ref([])
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const showFormSheet = ref(false)
const showDetailPopup = ref(false)
const showMethodPicker = ref(false)
const selectedItem = ref(null)
const isEdit = ref(false)
const editingId = ref(null)
const activeStatus = ref('')

const query = reactive({ keyword: '', status: '', page: 1, size: 10 })
const form = reactive({
  batchCode: '', variety: '', quantity: '', precoolMethod: '',
  precoolTemp: '', targetTemp: '', operator: '', remark: ''
})

const statusTagMap = {
  PENDING: { label: '待处理', type: 'warning' },
  PROCESSING: { label: '处理中', type: 'primary' },
  COMPLETED: { label: '已完成', type: 'success' }
}

const methodTagMap = {
  VACUUM: { label: '真空预冷', type: 'primary' },
  FORCED_AIR: { label: '强制通风', type: 'success' },
  HYDROCOOLING: { label: '水预冷', type: 'warning' },
  COLD_ROOM: { label: '冷库预冷', type: 'default' }
}

const methodPickerColumns = [
  { text: '真空预冷', value: 'VACUUM' },
  { text: '强制通风', value: 'FORCED_AIR' },
  { text: '水预冷', value: 'HYDROCOOLING' },
  { text: '冷库预冷', value: 'COLD_ROOM' }
]

function resetForm() {
  Object.assign(form, {
    batchCode: '', variety: '', quantity: '', precoolMethod: '',
    precoolTemp: '', targetTemp: '', operator: '', remark: ''
  })
}

function openAddSheet() {
  isEdit.value = false
  editingId.value = null
  resetForm()
  showFormSheet.value = true
}

function openEditSheet(item) {
  isEdit.value = true
  editingId.value = item.id
  Object.assign(form, {
    batchCode: item.batchCode || '',
    variety: item.variety || '',
    quantity: item.quantity || '',
    precoolMethod: item.precoolMethod || '',
    precoolTemp: item.precoolTemp ?? '',
    targetTemp: item.targetTemp ?? '',
    operator: item.operator || '',
    remark: item.remark || ''
  })
  showFormSheet.value = true
}

function onMethodConfirm({ selectedOptions }) {
  form.precoolMethod = selectedOptions[0].value
  showMethodPicker.value = false
}

function showDetailPopupFn(item) {
  selectedItem.value = item
  showDetailPopup.value = true
}

function handleTabChange(val) {
  query.status = val
  loadList(true)
}

async function loadList(reset = false) {
  if (reset) { query.page = 1; list.value = []; finished.value = false }
  loading.value = true
  try {
    const res = await coldchainApi.precoolingList({ ...query })
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

async function handleSubmit() {
  if (isEdit.value) {
    await coldchainApi.precoolingUpdate(editingId.value, { ...form })
    showToast('预冷批次更新成功')
  } else {
    await coldchainApi.precoolingCreate({ ...form })
    showToast('预冷批次创建成功')
  }
  showFormSheet.value = false
  resetForm()
  loadList(true)
}

async function handleDelete(id) {
  await showConfirmDialog({ title: '确认删除', message: '确定要删除该预冷记录吗？' })
  await coldchainApi.precoolingDelete(id)
  showToast('预冷记录已删除')
  loadList(true)
}

loadList(true)
</script>
