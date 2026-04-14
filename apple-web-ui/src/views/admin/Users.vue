<template>
  <div class="users-page">
    <!-- Search bar -->
    <van-search
      v-model="query.keyword"
      placeholder="搜索用户名 / 姓名 / 手机号"
      show-action
      @search="handleSearch"
      @clear="handleSearch"
    >
      <template #action>
        <div @click="handleSearch">搜索</div>
      </template>
    </van-search>

    <!-- Filter row: role + status -->
    <div class="filter-row">
      <van-dropdown-menu>
        <van-dropdown-item v-model="query.roleCode" :options="roleFilterOptions" @change="handleSearch" />
        <van-dropdown-item v-model="query.statusFilter" :options="statusFilterOptions" @change="handleSearch" />
      </van-dropdown-menu>
    </div>

    <!-- Action bar -->
    <div class="action-bar">
      <van-button type="primary" size="small" icon="plus" @click="openAddDrawer">新增用户</van-button>
      <van-button type="default" size="small" icon="down" @click="handleExport">导出</van-button>
    </div>

    <!-- List -->
    <van-pull-refresh v-model="refreshing" @refresh="loadList">
      <van-list v-model:loading="loading" :finished="finished" finished-text="没有更多了" @load="loadMore">
        <van-cell-group inset style="margin-bottom: 8px" v-for="item in list" :key="item.id">
          <van-cell is-link @click="openDetailDrawer(item)">
            <template #title>
              <span>{{ item.username }}</span>
            </template>
            <template #label>
              {{ item.realName || '-' }} &middot; {{ item.orgName || '-' }}
            </template>
            <template #value>
              <div class="cell-tags">
                <van-tag :type="roleTagType(item.roleCode)" size="medium">{{ roleLabel(item.roleCode) }}</van-tag>
                <van-tag :type="item.status === 1 ? 'success' : 'danger'" size="medium">
                  {{ item.status === 1 ? '启用' : '禁用' }}
                </van-tag>
              </div>
            </template>
          </van-cell>
          <van-cell style="padding-top: 0">
            <template #title>
              <span class="cell-sub">{{ item.phone || '-' }} &middot; 创建于 {{ item.createTime || '-' }}</span>
            </template>
          </van-cell>
        </van-cell-group>
        <van-empty v-if="!loading && list.length === 0" description="暂无用户" />
      </van-list>
    </van-pull-refresh>

    <!-- Add / Edit Drawer -->
    <van-popup
      v-model:show="showFormDrawer"
      position="bottom"
      round
      :style="{ maxHeight: '90vh', overflowY: 'auto' }"
    >
      <div class="drawer-header">
        <span>{{ isEditing ? '编辑用户' : '新增用户' }}</span>
        <van-icon name="cross" @click="showFormDrawer = false" />
      </div>
      <van-form @submit="handleSubmit" class="drawer-form">
        <van-cell-group inset>
          <van-field
            v-model="form.username"
            label="用户名"
            placeholder="请输入用户名"
            :rules="[{ required: true, message: '请填写用户名' }]"
          />
          <van-field
            v-if="!isEditing"
            v-model="form.password"
            label="密码"
            type="password"
            placeholder="请输入密码"
            :rules="[{ required: true, message: '请填写密码' }]"
          />
          <van-field
            v-model="form.realName"
            label="真实姓名"
            placeholder="请输入真实姓名"
            :rules="[{ required: true, message: '请填写真实姓名' }]"
          />
          <van-field
            v-model="form.phone"
            label="手机号"
            placeholder="请输入手机号"
          />
          <van-field
            v-model="form.email"
            label="邮箱"
            placeholder="请输入邮箱"
          />
          <van-field
            label="角色"
            readonly
            is-link
            :model-value="roleLabel(form.roleCode)"
            placeholder="请选择角色"
            :rules="[{ required: true, message: '请选择角色' }]"
            @click="showRolePicker = true"
          />
          <van-field label="状态">
            <template #input>
              <van-switch v-model="form.statusBool" size="20px" />
              <span style="margin-left: 8px; font-size: 14px; color: var(--color-text-secondary, #969799)">
                {{ form.statusBool ? '启用' : '禁用' }}
              </span>
            </template>
          </van-field>
          <van-field
            v-model="form.orgName"
            label="机构名称"
            placeholder="请输入所属机构"
          />
          <van-field
            v-model="form.remark"
            label="备注"
            type="textarea"
            rows="2"
            autosize
            placeholder="请输入备注"
          />
        </van-cell-group>
        <div class="drawer-actions">
          <van-button block type="primary" native-type="submit" :loading="submitting">
            {{ isEditing ? '保存修改' : '确认创建' }}
          </van-button>
        </div>
      </van-form>

      <!-- Role picker popup -->
      <van-popup v-model:show="showRolePicker" position="bottom" round>
        <van-picker
          :columns="rolePickerColumns"
          @confirm="onRoleConfirm"
          @cancel="showRolePicker = false"
        />
      </van-popup>
    </van-popup>

    <!-- Detail Drawer -->
    <van-popup
      v-model:show="showDetail"
      position="bottom"
      round
      :style="{ maxHeight: '90vh', overflowY: 'auto' }"
    >
      <div class="drawer-header">
        <span>用户详情</span>
        <van-icon name="cross" @click="showDetail = false" />
      </div>
      <van-cell-group inset v-if="currentItem">
        <van-cell title="用户名" :value="currentItem.username" />
        <van-cell title="真实姓名" :value="currentItem.realName || '-'" />
        <van-cell title="手机号" :value="currentItem.phone || '-'" />
        <van-cell title="邮箱" :value="currentItem.email || '-'" />
        <van-cell title="角色">
          <template #value>
            <van-tag :type="roleTagType(currentItem.roleCode)">{{ roleLabel(currentItem.roleCode) }}</van-tag>
          </template>
        </van-cell>
        <van-cell title="状态">
          <template #value>
            <van-tag :type="currentItem.status === 1 ? 'success' : 'danger'">
              {{ currentItem.status === 1 ? '启用' : '禁用' }}
            </van-tag>
          </template>
        </van-cell>
        <van-cell title="机构名称" :value="currentItem.orgName || '-'" />
        <van-cell title="机构类型" :value="currentItem.orgType || '-'" />
        <van-cell title="头像">
          <template #value>
            <van-image
              v-if="currentItem.avatar"
              round
              width="40"
              height="40"
              :src="currentItem.avatar"
              fit="cover"
            />
            <span v-else>-</span>
          </template>
        </van-cell>
        <van-cell title="备注" :value="currentItem.remark || '-'" />
        <van-cell title="创建时间" :value="currentItem.createTime || '-'" />
      </van-cell-group>

      <div class="detail-actions" v-if="currentItem">
        <van-button
          type="primary"
          block
          plain
          @click="openEditFromDetail(currentItem)"
        >
          编辑
        </van-button>
        <van-button
          type="danger"
          block
          plain
          style="margin-top: 8px"
          @click="handleDelete(currentItem)"
        >
          删除用户
        </van-button>
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { showToast, showConfirmDialog, showSuccessToast } from 'vant'
import { userApi } from '@/api/auth.js'

// ─── Constants ─────────────────────────────────────────────
const ROLES = [
  { value: 'ADMIN', label: '系统管理员' },
  { value: 'FARMER', label: '种植户/合作社' },
  { value: 'SUPPLIER', label: '供应商/加工方' },
  { value: 'BUYER', label: '收购商/经销商' },
  { value: 'LOGISTICS', label: '物流服务商' },
  { value: 'FINANCE', label: '金融机构' },
  { value: 'GOV', label: '监管部门' },
]

const roleLabel = (code) => ROLES.find(r => r.value === code)?.label || code || '-'

const ROLE_TAG_MAP = {
  ADMIN: 'primary',
  FARMER: 'success',
  SUPPLIER: 'warning',
  BUYER: 'default',
  WAREHOUSE: 'default',
  LOGISTICS: 'warning',
  FINANCE: 'primary',
  GOV: 'danger',
}
const roleTagType = (code) => ROLE_TAG_MAP[code] || 'default'

const roleFilterOptions = computed(() => [
  { text: '全部角色', value: '' },
  ...ROLES.map(r => ({ text: r.label, value: r.value })),
])

const statusFilterOptions = [
  { text: '全部状态', value: '' },
  { text: '启用', value: '1' },
  { text: '禁用', value: '0' },
]

const rolePickerColumns = ROLES.map(r => ({ text: r.label, value: r.value }))

// ─── State ─────────────────────────────────────────────────
const list = ref([])
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const submitting = ref(false)

const showFormDrawer = ref(false)
const showDetail = ref(false)
const showRolePicker = ref(false)
const isEditing = ref(false)
const editingId = ref(null)
const currentItem = ref(null)

const query = reactive({
  keyword: '',
  roleCode: '',
  statusFilter: '',
  page: 1,
  pageSize: 10,
})

const defaultForm = () => ({
  username: '',
  password: '',
  realName: '',
  phone: '',
  email: '',
  roleCode: '',
  statusBool: true,
  orgName: '',
  remark: '',
})

const form = reactive(defaultForm())

// ─── Data loading ──────────────────────────────────────────
function buildParams() {
  const params = { page: query.page, size: query.pageSize }
  if (query.keyword) params.keyword = query.keyword
  if (query.roleCode) params.roleCode = query.roleCode
  if (query.statusFilter !== '') params.status = Number(query.statusFilter)
  return params
}

async function loadList() {
  loading.value = true
  try {
    const res = await userApi.listUsers({ ...buildParams(), page: 1 })
    const records = res?.records || res || []
    list.value = records
    finished.value = records.length >= (res?.total || records.length)
    query.page = 1
  } catch (e) {
    showToast({ type: 'fail', message: '加载用户列表失败' })
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

async function loadMore() {
  if (finished.value) return
  query.page += 1
  try {
    const res = await userApi.listUsers(buildParams())
    const records = res?.records || []
    list.value = [...list.value, ...records]
    if (list.value.length >= (res?.total || 0) || records.length < query.pageSize) {
      finished.value = true
    }
  } catch (e) {
    showToast({ type: 'fail', message: '加载更多失败' })
  } finally {
    loading.value = false
  }
}

// ─── Actions ───────────────────────────────────────────────
function handleSearch() {
  query.page = 1
  finished.value = false
  list.value = []
  loadList()
}

function resetForm() {
  Object.assign(form, defaultForm())
}

function openAddDrawer() {
  resetForm()
  isEditing.value = false
  editingId.value = null
  showFormDrawer.value = true
}

function openDetailDrawer(item) {
  currentItem.value = item
  showDetail.value = true
}

function openEditFromDetail(item) {
  showDetail.value = false
  isEditing.value = true
  editingId.value = item.id
  Object.assign(form, {
    username: item.username || '',
    password: '',
    realName: item.realName || '',
    phone: item.phone || '',
    email: item.email || '',
    roleCode: item.roleCode || '',
    statusBool: item.status === 1,
    orgName: item.orgName || '',
    remark: item.remark || '',
  })
  showFormDrawer.value = true
}

function onRoleConfirm({ selectedValues }) {
  form.roleCode = selectedValues[0] || ''
  showRolePicker.value = false
}

async function handleSubmit() {
  submitting.value = true
  try {
    const payload = {
      username: form.username,
      realName: form.realName,
      phone: form.phone,
      email: form.email,
      roleCode: form.roleCode,
      status: form.statusBool ? 1 : 0,
      orgName: form.orgName,
      remark: form.remark,
    }
    if (isEditing.value) {
      await userApi.updateUser(editingId.value, payload)
      showSuccessToast('修改成功')
    } else {
      payload.password = form.password
      await userApi.createUser(payload)
      showSuccessToast('创建成功')
    }
    showFormDrawer.value = false
    handleSearch()
  } catch (e) {
    showToast({ type: 'fail', message: e?.message || '操作失败' })
  } finally {
    submitting.value = false
  }
}

async function handleDelete(item) {
  try {
    await showConfirmDialog({
      title: '确认删除',
      message: `确认删除用户「${item.username}」？删除后不可恢复。`,
    })
    await userApi.deleteUser(item.id)
    showSuccessToast('删除成功')
    showDetail.value = false
    handleSearch()
  } catch (e) {
    // user cancelled
  }
}

async function handleExport() {
  showToast('正在导出...')
  try {
    await userApi.exportUsers(buildParams())
  } catch (e) {
    showToast({ type: 'fail', message: '导出失败' })
  }
}

onMounted(loadList)
</script>

<style scoped>
.users-page {
  padding-bottom: 20px;
}

.filter-row {
  background: var(--color-bg-white, #fff);
}

.action-bar {
  display: flex;
  gap: 8px;
  padding: 8px 16px;
}

.cell-tags {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
}

.cell-sub {
  font-size: var(--font-size-xs, 12px);
  color: var(--color-text-secondary, #969799);
}

/* Drawer shared styles */
.drawer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  font-size: 16px;
  font-weight: 600;
  border-bottom: 1px solid var(--color-border-light, #ebedf0);
}

.drawer-form {
  padding-bottom: 20px;
}

.drawer-actions {
  padding: 16px;
}

/* Detail drawer action buttons */
.detail-actions {
  padding: 16px;
  border-top: 1px solid var(--color-border-light, #ebedf0);
  margin-top: 8px;
}
</style>
