<template>
  <div class="page-container" data-density="compact">
    <van-nav-bar title="数据源注册" left-arrow @click-left="$router.back()" />

    <van-search v-model="keyword" placeholder="搜索名称/编码" @search="onSearch" />

    <van-dropdown-menu>
      <van-dropdown-item v-model="filterCategory" :options="categoryOptions" @change="onSearch" />
    </van-dropdown-menu>

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list v-model:loading="loading" :finished="finished" :finished-text="list.length ? '没有更多了' : ''" @load="onLoad">
        <van-cell v-for="item in list" :key="item.id" :title="item.sourceName"
          :label="`${item.sourceCode} · ${item.sourceType?.desc || item.sourceType}`" is-link @click="showDetail(item)">
          <template #value>
            <div style="display:flex;flex-direction:column;align-items:flex-end;gap:2px">
              <van-tag :type="statusType(item.status?.code)" size="medium">{{ item.status?.desc || item.status }}</van-tag>
              <van-button v-if="item.id" size="mini" type="primary" plain @click.stop="testConn(item.id)">测试</van-button>
            </div>
          </template>
        </van-cell>
        <van-empty v-if="!loading && list.length === 0" description="暂无数据" />
      </van-list>
    </van-pull-refresh>

    <van-button class="fab-btn" type="primary" icon="plus" round @click="showCreate" />

    <van-popup v-model:show="dialogVisible" position="bottom" round :style="{ maxHeight: '85%' }">
      <div class="popup-header">{{ dialogTitle }}</div>
      <van-form @submit="onSubmit" class="popup-form">
        <van-field v-model="form.sourceCode" label="数据源编码" placeholder="唯一编码" required :readonly="isEdit" />
        <van-field v-model="form.sourceName" label="数据源名称" placeholder="名称" required />
        <van-field name="sourceType" label="数据源类型">
          <template #input>
            <van-radio-group v-model="form.sourceType" direction="horizontal">
              <van-radio name="MYSQL">MySQL</van-radio>
              <van-radio name="KAFKA">Kafka</van-radio>
              <van-radio name="HTTP_API">HTTP</van-radio>
              <van-radio name="RSS">RSS</van-radio>
              <van-radio name="FILE">文件</van-radio>
              <van-radio name="CLICKHOUSE">CK</van-radio>
            </van-radio-group>
          </template>
        </van-field>
        <van-field name="category" label="分类">
          <template #input>
            <van-radio-group v-model="form.category" direction="horizontal">
              <van-radio name="INTERNAL">内部</van-radio>
              <van-radio name="EXTERNAL">外部</van-radio>
            </van-radio-group>
          </template>
        </van-field>
        <van-field v-model="form.connectUrl" label="连接地址" placeholder="jdbc:mysql://..." />
        <van-field v-model="form.username" label="用户名" placeholder="可选" />
        <van-field v-model="form.passwordEnc" label="密码" type="password" placeholder="可选" />
        <van-field v-model="form.owner" label="负责人" placeholder="可选" />
        <van-field v-model="form.remark" label="备注" placeholder="可选" type="textarea" rows="2" />
        <div class="popup-actions">
          <van-button block type="primary" native-type="submit" :loading="submitting">{{ isEdit ? '更新' : '创建' }}</van-button>
        </div>
      </van-form>
    </van-popup>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { showToast, showSuccessToast } from 'vant'
import { sourceApi } from '@/api/bigdata.js'

const keyword = ref('')
const filterCategory = ref('')
const categoryOptions = [
  { text: '全部分类', value: '' },
  { text: '内部', value: 'INTERNAL' },
  { text: '外部', value: 'EXTERNAL' },
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
const dialogTitle = ref('新增数据源')

const defaultForm = { sourceCode: '', sourceName: '', sourceType: 'MYSQL', category: 'INTERNAL', connectUrl: '', username: '', passwordEnc: '', owner: '', remark: '' }
const form = reactive({ ...defaultForm })

function statusType(code) {
  const map = { ACTIVE: 'success', DISABLED: 'default', ERROR: 'danger' }
  return map[code] || 'default'
}

async function loadData() {
  try {
    const res = await sourceApi.list({ page: page.value, size: pageSize, keyword: keyword.value, category: filterCategory.value || undefined })
    const records = res.records || res
    if (page.value === 1) list.value = records
    else list.value.push(...records)
    finished.value = list.value.length >= (res.total ?? records.length)
  } catch (e) { showToast({ type: 'fail', message: e.message || '加载失败' }); finished.value = true }
}

function onLoad() { loadData().finally(() => { loading.value = false; refreshing.value = false }) }
function onRefresh() { list.value = []; page.value = 1; finished.value = false; }
function onSearch() { onRefresh(); onLoad() }

async function testConn(id) {
  try { const msg = await sourceApi.test(id); showSuccessToast(msg || '连接成功') }
  catch (e) { showToast({ type: 'fail', message: e.message || '连接失败' }) }
}

function resetForm() { Object.assign(form, { ...defaultForm }) }
function showCreate() { isEdit.value = false; editId.value = null; dialogTitle.value = '新增数据源'; resetForm(); dialogVisible.value = true }
function showDetail(item) { isEdit.value = true; editId.value = item.id; dialogTitle.value = '编辑数据源'; Object.assign(form, { sourceCode: item.sourceCode, sourceName: item.sourceName, sourceType: item.sourceType?.code || 'MYSQL', category: item.category?.code || 'INTERNAL', connectUrl: item.connectUrl || '', username: item.username || '', passwordEnc: '', owner: item.owner || '', remark: item.remark || '' }); dialogVisible.value = true }

async function onSubmit() {
  submitting.value = true
  try {
    const payload = { ...form }
    if (!payload.passwordEnc) delete payload.passwordEnc
    if (isEdit.value) await sourceApi.update(editId.value, payload)
    else await sourceApi.create(payload)
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
