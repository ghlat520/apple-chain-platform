<template>
  <div class="orchards-page">
    <!-- Search Bar -->
    <van-search
      v-model="query.keyword"
      placeholder="搜索果园名称"
      @search="handleSearch"
      @clear="handleSearch"
    />

    <!-- Status Filter -->
    <div class="filter-bar">
      <van-tabs v-model:active="query.status" @change="handleSearch">
        <van-tab title="全部" name="" />
        <van-tab title="正常" name="ACTIVE" />
        <van-tab title="休耕" name="INACTIVE" />
      </van-tabs>
    </div>

    <!-- Action Bar -->
    <div class="action-bar">
      <van-button type="primary" size="small" icon="plus" @click="openAddDrawer">新增果园</van-button>
      <van-button type="default" size="small" icon="down" @click="handleExport">导出CSV</van-button>
    </div>

    <!-- List -->
    <van-pull-refresh v-model="refreshing" @refresh="loadList">
      <van-list
        v-model:loading="loading"
        :finished="finished"
        finished-text="没有更多了"
        @load="loadMore"
      >
        <van-cell-group inset style="margin-bottom:8px" v-for="item in list" :key="item.id">
          <van-cell
            :title="item.orchardName || '未命名果园'"
            :label="`${item.location || '位置未填写'} · ${item.area || 0} 亩`"
            is-link
            @click="openDetailDrawer(item)"
          >
            <template #value>
              <van-tag :type="statusTagType(item.status)">{{ statusLabel(item.status) }}</van-tag>
            </template>
          </van-cell>
        </van-cell-group>
        <van-empty v-if="!loading && list.length === 0" description="暂无数据" />
      </van-list>
    </van-pull-refresh>

    <!-- Add/Edit Drawer -->
    <van-popup
      v-model:show="showDrawer"
      position="bottom"
      round
      :style="{ maxHeight: '90vh', overflowY: 'auto' }"
    >
      <div class="drawer-header">
        <span>{{ editId ? '编辑果园' : '新增果园' }}</span>
        <van-icon name="cross" @click="showDrawer = false" />
      </div>
      <van-form @submit="handleSubmit" class="drawer-form">
        <van-cell-group inset>
          <van-field
            v-model="form.orchardName"
            label="果园名称"
            placeholder="请输入果园名称"
            :rules="[{ required: true, message: '请填写果园名称' }]"
          />
          <van-field
            v-model="form.location"
            label="地理位置"
            placeholder="请输入地理位置"
            :rules="[{ required: true, message: '请填写地理位置' }]"
          />
          <van-field
            v-model.number="form.area"
            label="面积（亩）"
            type="number"
            placeholder="请输入面积"
          />
          <van-field
            v-model="form.owner"
            label="负责人"
            placeholder="请输入负责人姓名"
          />
          <van-field
            v-model="form.phone"
            label="联系电话"
            placeholder="请输入联系电话"
          />
          <van-field
            v-model="form.remark"
            label="备注"
            type="textarea"
            placeholder="请输入备注"
            rows="2"
            autosize
          />
          <van-field label="状态">
            <template #input>
              <van-radio-group v-model="form.status" direction="horizontal">
                <van-radio name="ACTIVE">正常</van-radio>
                <van-radio name="INACTIVE">休耕</van-radio>
              </van-radio-group>
            </template>
          </van-field>
        </van-cell-group>
        <div class="drawer-actions">
          <van-button block type="primary" native-type="submit" :loading="submitting">
            {{ editId ? '保存修改' : '确认新增' }}
          </van-button>
        </div>
      </van-form>
    </van-popup>

    <!-- Detail Drawer -->
    <van-popup
      v-model:show="showDetail"
      position="bottom"
      round
      :style="{ maxHeight: '85vh', overflowY: 'auto' }"
    >
      <div class="drawer-header">
        <span>果园详情</span>
        <van-icon name="cross" @click="showDetail = false" />
      </div>
      <van-cell-group inset v-if="currentItem">
        <van-cell title="果园名称" :value="currentItem.orchardName || '-'" />
        <van-cell title="地理位置" :value="currentItem.location || '-'" />
        <van-cell title="面积（亩）" :value="currentItem.area ?? '-'" />
        <van-cell title="负责人" :value="currentItem.owner || '-'" />
        <van-cell title="联系电话" :value="currentItem.phone || '-'" />
        <van-cell title="备注" :value="currentItem.remark || '-'" />
        <van-cell title="状态">
          <template #value>
            <van-tag :type="statusTagType(currentItem.status)">{{ statusLabel(currentItem.status) }}</van-tag>
          </template>
        </van-cell>
        <van-cell title="创建时间" :value="currentItem.createTime || '-'" />
      </van-cell-group>
      <div class="drawer-actions">
        <van-button block type="primary" plain @click="openEditDrawer(currentItem)">编辑</van-button>
        <van-button block type="danger" plain @click="handleDelete(currentItem)" style="margin-top:8px">删除</van-button>
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { farmApi } from '@/api/farm.js'
import { showToast, showConfirmDialog } from 'vant'

const list = ref([])
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const showDrawer = ref(false)
const showDetail = ref(false)
const submitting = ref(false)
const editId = ref(null)
const currentItem = ref(null)

const query = reactive({ keyword: '', status: '', page: 1, pageSize: 10 })
const form = reactive({ orchardName: '', location: '', area: '', owner: '', phone: '', remark: '', status: 'ACTIVE' })

function statusTagType(status) {
  const s = (status || '').toUpperCase()
  const map = { ACTIVE: 'primary', INACTIVE: 'default' }
  return map[s] || 'default'
}

function statusLabel(status) {
  const s = (status || '').toUpperCase()
  const map = { ACTIVE: '正常', INACTIVE: '休耕' }
  return map[s] || status || '-'
}

function resetForm() {
  Object.assign(form, { orchardName: '', location: '', area: '', owner: '', phone: '', remark: '', status: 'ACTIVE' })
}

async function loadList() {
  loading.value = true
  try {
    const res = await farmApi.getOrchards({ ...query, page: 1 })
    const records = res?.records || res || []
    list.value = records
    finished.value = records.length >= (res?.total || records.length)
    query.page = 1
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

async function loadMore() {
  if (finished.value) return
  query.page += 1
  try {
    const res = await farmApi.getOrchards({ ...query })
    const records = res?.records || []
    list.value = [...list.value, ...records]
    if (list.value.length >= (res?.total || 0) || records.length < query.pageSize) {
      finished.value = true
    }
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.page = 1
  finished.value = false
  list.value = []
  loadList()
}

function openAddDrawer() {
  resetForm()
  editId.value = null
  showDrawer.value = true
}

function openEditDrawer(item) {
  Object.assign(form, {
    orchardName: item.orchardName || '',
    location: item.location || '',
    area: item.area || '',
    owner: item.owner || '',
    phone: item.phone || '',
    remark: item.remark || '',
    status: item.status || 'ACTIVE'
  })
  editId.value = item.id
  showDetail.value = false
  showDrawer.value = true
}

function openDetailDrawer(item) {
  currentItem.value = item
  showDetail.value = true
}

async function handleSubmit() {
  submitting.value = true
  try {
    if (editId.value) {
      await farmApi.updateOrchard(editId.value, form)
      showToast({ type: 'success', message: '修改成功' })
    } else {
      await farmApi.createOrchard(form)
      showToast({ type: 'success', message: '新增成功' })
    }
    showDrawer.value = false
    loadList()
  } finally {
    submitting.value = false
  }
}

async function handleDelete(item) {
  try {
    await showConfirmDialog({ title: '确认删除', message: `确认删除果园「${item.orchardName || '未命名果园'}」？` })
    await farmApi.deleteOrchard(item.id)
    showToast({ type: 'success', message: '删除成功' })
    showDetail.value = false
    loadList()
  } catch (e) {
    // user cancelled
  }
}

async function handleExport() {
  showToast('正在导出...')
  try {
    await farmApi.exportOrchards()
  } catch (e) {
    showToast('导出失败')
  }
}

onMounted(loadList)
</script>

<style scoped>
.orchards-page {
  padding-bottom: 20px;
}

.filter-bar {
  background: #fff;
  margin-bottom: 8px;
}

.action-bar {
  display: flex;
  gap: 8px;
  padding: 8px 16px;
}

.drawer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  font-size: 16px;
  font-weight: 600;
  border-bottom: 1px solid #ebedf0;
}

.drawer-form {
  padding-bottom: 20px;
}

.drawer-actions {
  padding: 16px;
}
</style>
