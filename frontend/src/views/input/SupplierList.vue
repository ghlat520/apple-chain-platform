<template>
  <div class="page-container">
    <div class="section-header">
      <span class="section-title">供应商管理</span>
      <van-button type="primary" size="small" icon="plus" @click="openAddSheet">新增供应商</van-button>
    </div>

    <van-search v-model="query.keyword" placeholder="搜索供应商名称/联系人" @search="loadList(true)" shape="round" />

    <van-dropdown-menu>
      <van-dropdown-item v-model="query.status" :options="statusOptions" @change="loadList(true)" />
    </van-dropdown-menu>

    <van-pull-refresh v-model="refreshing" @refresh="() => loadList(true)">
      <van-list v-model:loading="loading" :finished="finished" finished-text="没有更多了" @load="loadList">
        <van-cell-group inset style="margin-bottom:8px;" v-for="item in list" :key="item.id">
          <van-cell is-link @click="showDetail(item)">
            <template #title>
              <span style="font-weight:600;">{{ item.name }}</span>
              <van-tag :type="statusTagType(item.status)" style="margin-left:6px;">
                {{ statusLabel(item.status) }}
              </van-tag>
            </template>
            <template #label>
              {{ item.contactPerson }} · {{ item.phone }}
            </template>
          </van-cell>
          <van-cell title="信用评分" :value="item.creditScore != null ? item.creditScore + '分' : '-'" />
        </van-cell-group>
        <van-empty v-if="!loading && list.length === 0" description="暂无供应商数据" />
      </van-list>
    </van-pull-refresh>

    <!-- 新增/编辑供应商弹窗 -->
    <van-action-sheet v-model:show="showAddSheet" :title="editingId ? '编辑供应商' : '新增供应商'">
      <div style="padding:16px;">
        <van-form @submit="handleSave">
          <van-field v-model="addForm.name" label="供应商名称" placeholder="请输入" :rules="[{required:true}]" />
          <van-field v-model="addForm.contactPerson" label="联系人" placeholder="请输入" :rules="[{required:true}]" />
          <van-field v-model="addForm.phone" label="联系电话" placeholder="请输入" :rules="[{required:true}]" />
          <van-field v-model="addForm.address" label="地址" placeholder="请输入" />
          <van-field v-model="addForm.license" label="营业执照号" placeholder="请输入" />
          <van-field v-model="addForm.qualification" label="资质证书" placeholder="请输入" />
          <van-field v-model="addForm.remark" label="备注" type="textarea" placeholder="请输入" rows="2" />
          <van-button block type="primary" native-type="submit" style="margin-top:16px;" color="#07c160">提交</van-button>
        </van-form>
      </div>
    </van-action-sheet>

    <!-- 详情弹窗 -->
    <van-popup v-model:show="showDetailPopup" round position="bottom" style="height:70%;">
      <div style="padding:16px;" v-if="selectedItem">
        <h3 style="margin-bottom:12px;">{{ selectedItem.name }}</h3>
        <van-cell-group>
          <van-cell title="状态" :value="statusLabel(selectedItem.status)" />
          <van-cell title="联系人" :value="selectedItem.contactPerson || '-'" />
          <van-cell title="联系电话" :value="selectedItem.phone || '-'" />
          <van-cell title="地址" :value="selectedItem.address || '-'" />
          <van-cell title="营业执照号" :value="selectedItem.license || '-'" />
          <van-cell title="资质证书" :value="selectedItem.qualification || '-'" />
          <van-cell title="信用评分" :value="selectedItem.creditScore != null ? selectedItem.creditScore + '分' : '-'" />
          <van-cell title="审核人" :value="selectedItem.auditor || '-'" />
          <van-cell title="拒绝原因" :value="selectedItem.rejectReason || '-'" />
          <van-cell title="备注" :value="selectedItem.remark || '-'" />
        </van-cell-group>

        <div style="margin-top:16px;" v-if="selectedItem.status === 'PENDING'">
          <van-button type="success" size="small" @click="handleAudit('APPROVE')" style="margin-right:8px;">通过</van-button>
          <van-button type="danger" size="small" @click="auditAction = 'REJECT'; showAuditSheet = true" style="margin-right:8px;">拒绝</van-button>
          <van-button type="default" size="small" @click="handleAudit('BLACKLIST')">拉黑</van-button>
        </div>
        <div style="margin-top:16px;" v-if="selectedItem.status === 'REJECTED'">
          <van-button type="primary" size="small" @click="handleReinstate">重新提交审核</van-button>
        </div>
      </div>
    </van-popup>

    <!-- 审核弹窗（拒绝/拉黑需要填写原因） -->
    <van-action-sheet v-model:show="showAuditSheet" title="审核操作">
      <div style="padding:16px;">
        <van-field v-model="auditReason" label="原因" type="textarea" placeholder="请输入原因" rows="3" :rules="[{required:true}]" />
        <van-button block type="primary" style="margin-top:16px;" color="#07c160" @click="handleAudit(auditAction)">确认</van-button>
      </div>
    </van-action-sheet>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { inputApi } from '@/api/input'
import { showToast, showDialog } from 'vant'

const statusMap = {
  PENDING: { label: '待审核', type: 'warning' },
  APPROVED: { label: '已通过', type: 'success' },
  REJECTED: { label: '已拒绝', type: 'danger' },
  BLACKLISTED: { label: '已拉黑', type: 'default' }
}

const statusOptions = [
  { text: '全部状态', value: '' },
  { text: '待审核', value: 'PENDING' },
  { text: '已通过', value: 'APPROVED' },
  { text: '已拒绝', value: 'REJECTED' },
  { text: '已拉黑', value: 'BLACKLISTED' }
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
const showAuditSheet = ref(false)
const selectedItem = ref(null)
const editingId = ref(null)
const auditAction = ref('')
const auditReason = ref('')
const query = reactive({ keyword: '', page: 1, size: 10, status: '' })
const addForm = reactive({
  name: '', contactPerson: '', phone: '', address: '',
  license: '', qualification: '', remark: ''
})

async function loadList(reset = false) {
  if (reset) { query.page = 1; list.value = []; finished.value = false }
  loading.value = true
  try {
    const res = await inputApi.supplierList({ ...query })
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
    name: '', contactPerson: '', phone: '', address: '',
    license: '', qualification: '', remark: ''
  })
  showAddSheet.value = true
}

async function handleSave() {
  if (editingId.value) {
    await inputApi.supplierUpdate(editingId.value, { ...addForm })
    showToast('供应商更新成功')
  } else {
    await inputApi.supplierCreate({ ...addForm })
    showToast('供应商创建成功')
  }
  showAddSheet.value = false
  loadList(true)
}

async function handleAudit(decision) {
  if (!selectedItem.value) return
  const body = { decision }
  if (decision === 'REJECT' || decision === 'BLACKLIST') {
    if (!auditReason.value) {
      showToast('请填写原因')
      return
    }
    body.reason = auditReason.value
  }
  await inputApi.supplierAudit(selectedItem.value.id, body)
  showToast('审核操作成功')
  showAuditSheet.value = false
  auditReason.value = ''
  showDetailPopup.value = false
  loadList(true)
}

async function handleReinstate() {
  if (!selectedItem.value) return
  await inputApi.supplierReinstate(selectedItem.value.id)
  showToast('已重新提交审核')
  showDetailPopup.value = false
  loadList(true)
}

loadList(true)
</script>
