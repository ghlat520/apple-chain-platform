<template>
  <div class="page-container" data-density="compact">
    <van-nav-bar title="数据资产目录" left-arrow @click-left="$router.back()" />

    <van-search v-model="keyword" placeholder="搜索资产编码/名称" @search="onSearch" />

    <van-dropdown-menu>
      <van-dropdown-item v-model="filterLevel" :options="levelOptions" @change="onSearch" />
    </van-dropdown-menu>

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list v-model:loading="loading" :finished="finished" :finished-text="list.length ? '没有更多了' : ''" @load="onLoad">
        <van-cell v-for="item in list" :key="item.id" :title="item.bizName"
          :label="`${item.assetCode} · ${item.databaseName}.${item.tableName}`" is-link @click="showDetail(item)">
          <template #value>
            <van-tag :type="levelType(item.securityLevel?.code)">{{ item.securityLevel?.desc || item.securityLevel }}</van-tag>
          </template>
        </van-cell>
        <van-empty v-if="!loading && list.length === 0" description="暂无数据" />
      </van-list>
    </van-pull-refresh>

    <van-button class="fab-btn" type="primary" icon="plus" round @click="showCreate" />

    <van-popup v-model:show="dialogVisible" position="bottom" round :style="{ maxHeight: '85%' }">
      <div class="popup-header">{{ dialogTitle }}</div>
      <van-form @submit="onSubmit" class="popup-form">
        <van-field v-model="form.assetCode" label="资产编码" placeholder="唯一编码" required :readonly="isEdit" />
        <van-field v-model="form.databaseName" label="数据库名" placeholder="数据库名" required />
        <van-field v-model="form.schemaName" label="Schema" placeholder="可选" />
        <van-field v-model="form.tableName" label="表名" placeholder="表名" required />
        <van-field v-model="form.bizName" label="业务名称" placeholder="业务名称" required />
        <van-field v-model="form.bizDescription" label="业务描述" placeholder="可选" type="textarea" rows="2" />
        <van-field v-model="form.owner" label="负责人" placeholder="可选" />
        <van-field name="securityLevel" label="安全等级">
          <template #input>
            <van-radio-group v-model="form.securityLevel" direction="horizontal">
              <van-radio name="PUBLIC">公开</van-radio>
              <van-radio name="INTERNAL">内部</van-radio>
              <van-radio name="SENSITIVE">敏感</van-radio>
              <van-radio name="SECRET">绝密</van-radio>
            </van-radio-group>
          </template>
        </van-field>
        <van-field v-model="form.sourceSystem" label="来源系统" placeholder="可选" />
        <van-field v-model="form.tags" label="标签" placeholder="逗号分隔" />
        <div class="popup-actions">
          <van-button block type="primary" native-type="submit" :loading="submitting">{{ isEdit ? '更新' : '创建' }}</van-button>
        </div>
      </van-form>
    </van-popup>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { showToast, showSuccessToast, showConfirmDialog } from 'vant'
import { assetApi } from '@/api/bigdata.js'

const keyword = ref('')
const filterLevel = ref('')
const levelOptions = [
  { text: '全部等级', value: '' },
  { text: '公开', value: 'PUBLIC' },
  { text: '内部', value: 'INTERNAL' },
  { text: '敏感', value: 'SENSITIVE' },
  { text: '绝密', value: 'SECRET' },
]

const list = ref([])
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const page = ref(1)
const pageSize = 20

const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const submitting = ref(false)
const dialogTitle = ref('新增资产')

const defaultForm = { assetCode: '', databaseName: '', schemaName: '', tableName: '', bizName: '', bizDescription: '', owner: '', securityLevel: 'PUBLIC', sourceSystem: '', tags: '' }
const form = reactive({ ...defaultForm })

function levelType(code) {
  const map = { PUBLIC: 'success', INTERNAL: 'primary', SENSITIVE: 'warning', SECRET: 'danger' }
  return map[code] || 'default'
}

async function loadData() {
  try {
    const res = await assetApi.list({ page: page.value, size: pageSize, keyword: keyword.value, securityLevel: filterLevel.value || undefined })
    const records = res.records || res
    if (page.value === 1) list.value = records
    else list.value.push(...records)
    finished.value = list.value.length >= (res.total ?? records.length)
  } catch (e) { showToast({ type: 'fail', message: e.message || '加载失败' }); finished.value = true }
}

function onLoad() { page.value === 1 && refreshing.value ? page.value = 1 : null; loadData().finally(() => { loading.value = false; refreshing.value = false }) }
function onRefresh() { list.value = []; page.value = 1; finished.value = false; }
function onSearch() { onRefresh(); onLoad() }

function resetForm() { Object.assign(form, { ...defaultForm }) }
function showCreate() { isEdit.value = false; editId.value = null; dialogTitle.value = '新增资产'; resetForm(); dialogVisible.value = true }
function showDetail(item) { isEdit.value = true; editId.value = item.id; dialogTitle.value = '编辑资产'; Object.assign(form, { assetCode: item.assetCode, databaseName: item.databaseName, schemaName: item.schemaName || '', tableName: item.tableName, bizName: item.bizName, bizDescription: item.bizDescription || '', owner: item.owner || '', securityLevel: item.securityLevel?.code || 'PUBLIC', sourceSystem: item.sourceSystem || '', tags: item.tags || '' }); dialogVisible.value = true }

async function onSubmit() {
  submitting.value = true
  try {
    if (isEdit.value) await assetApi.update(editId.value, form)
    else await assetApi.create(form)
    showSuccessToast(isEdit.value ? '更新成功' : '创建成功')
    dialogVisible.value = false
    onRefresh(); onLoad()
  } catch (e) { showToast({ type: 'fail', message: e.message || '操作失败' }) } finally { submitting.value = false }
}
</script>

<style scoped>
.page-container { min-height: 100vh; background: var(--color-surface-base, #FAFAF8); }
.fab-btn { position: fixed; right: var(--space-5, 20px); bottom: calc(var(--space-6, 24px) + env(safe-area-inset-bottom)); z-index: var(--z-index-raised, 10); width: var(--density-button-height, 40px); height: var(--density-button-height, 40px); }
.popup-header { font-size: var(--typography-h3-size, 16px); font-weight: var(--typography-h3-weight, 500); padding: var(--space-4, 16px); text-align: center; }
.popup-form { padding: 0 var(--space-4, 16px) var(--space-6, 24px); }
.popup-actions { padding-top: var(--space-4, 16px); }
</style>
