<template>
  <div class="page-container" data-density="compact">
    <van-nav-bar title="指标中心" left-arrow @click-left="$router.back()" />

    <van-search v-model="keyword" placeholder="搜索指标名称/编码" @search="onSearch" />

    <van-dropdown-menu>
      <van-dropdown-item v-model="filterCategory" :options="categoryOptions" @change="onSearch" />
    </van-dropdown-menu>

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list v-model:loading="loading" :finished="finished" :finished-text="list.length ? '没有更多了' : ''" @load="onLoad">
        <van-cell v-for="item in list" :key="item.id" :title="item.metricName"
          :label="`${item.metricCode} · ${item.category?.desc || item.category} · ${item.unit || '-'}`" is-link @click="showDetail(item)">
          <template #value>
            <div style="display:flex;flex-direction:column;align-items:flex-end;gap:2px">
              <van-tag v-if="item.isCore === 1" type="danger" size="medium">核心</van-tag>
              <span style="font-size:var(--typography-caption-size,13px);color:var(--color-text-tertiary, #8A9099)">
                {{ item.refreshCron || '手动' }}
              </span>
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
        <van-field v-model="form.metricCode" label="指标编码" placeholder="唯一编码" required :readonly="isEdit" />
        <van-field v-model="form.metricName" label="指标名称" placeholder="名称" required />
        <van-field name="category" label="指标分类">
          <template #input>
            <van-radio-group v-model="form.category" direction="horizontal">
              <van-radio name="PLANTING">种植</van-radio>
              <van-radio name="TRADE">交易</van-radio>
              <van-radio name="WAREHOUSE">仓储</van-radio>
              <van-radio name="LOGISTICS">物流</van-radio>
              <van-radio name="FINANCE">金融</van-radio>
              <van-radio name="TRACE">溯源</van-radio>
            </van-radio-group>
          </template>
        </van-field>
        <van-field v-model="form.unit" label="单位" placeholder="如: kg, 元, %" />
        <van-field v-model="form.calcFormula" label="计算公式" placeholder="可选" />
        <van-field v-model="form.sqlExpression" label="SQL 表达式" placeholder="可选" type="textarea" rows="2" />
        <van-field v-model.number="form.dataSourceId" label="数据源ID" placeholder="可选" type="digit" />
        <van-field v-model="form.refreshCron" label="刷新 Cron" placeholder="0 0 2 * * ?" />
        <van-field v-model="form.owner" label="负责人" placeholder="可选" />
        <van-field name="isCore" label="核心指标">
          <template #input>
            <van-switch v-model="form.isCoreBool" />
          </template>
        </van-field>
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
import { metricApi } from '@/api/bigdata.js'

const keyword = ref('')
const filterCategory = ref('')
const categoryOptions = [
  { text: '全部分类', value: '' },
  { text: '种植', value: 'PLANTING' },
  { text: '交易', value: 'TRADE' },
  { text: '仓储', value: 'WAREHOUSE' },
  { text: '物流', value: 'LOGISTICS' },
  { text: '金融', value: 'FINANCE' },
  { text: '溯源', value: 'TRACE' },
]

const list = ref([])
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const page = ref(1)

const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const submitting = ref(false)
const dialogTitle = ref('新增指标')

const defaultForm = { metricCode: '', metricName: '', category: 'PLANTING', unit: '', calcFormula: '', sqlExpression: '', dataSourceId: null, refreshCron: '', owner: '', isCoreBool: false, remark: '' }
const form = reactive({ ...defaultForm })

async function loadData() {
  try {
    const res = await metricApi.list({ page: page.value, size: 20, category: filterCategory.value || undefined })
    const records = res.records || res
    if (page.value === 1) list.value = records
    else list.value.push(...records)
    finished.value = list.value.length >= (res.total ?? records.length)
  } catch (e) { showToast({ type: 'fail', message: e.message || '加载失败' }); finished.value = true }
}

function onLoad() { loadData().finally(() => { loading.value = false; refreshing.value = false }) }
function onRefresh() { list.value = []; page.value = 1; finished.value = false; }
function onSearch() { onRefresh(); onLoad() }

function resetForm() { Object.assign(form, { ...defaultForm }) }
function showCreate() { isEdit.value = false; editId.value = null; dialogTitle.value = '新增指标'; resetForm(); dialogVisible.value = true }
function showDetail(item) { isEdit.value = true; editId.value = item.id; dialogTitle.value = '编辑指标'; Object.assign(form, { metricCode: item.metricCode, metricName: item.metricName, category: item.category?.code || 'PLANTING', unit: item.unit || '', calcFormula: item.calcFormula || '', sqlExpression: item.sqlExpression || '', dataSourceId: item.dataSourceId, refreshCron: item.refreshCron || '', owner: item.owner || '', isCoreBool: item.isCore === 1, remark: item.remark || '' }); dialogVisible.value = true }

async function onSubmit() {
  submitting.value = true
  try {
    const payload = { ...form, isCore: form.isCoreBool ? 1 : 0 }
    delete payload.isCoreBool
    if (isEdit.value) await metricApi.update(editId.value, payload)
    else await metricApi.create(payload)
    showSuccessToast(isEdit.value ? '更新成功' : '创建成功')
    dialogVisible.value = false; onRefresh(); onLoad()
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
