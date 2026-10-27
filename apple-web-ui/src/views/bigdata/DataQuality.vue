<template>
  <div class="page-container" data-density="compact">
    <van-nav-bar title="数据质量规则" left-arrow @click-left="$router.back()" />

    <van-search v-model="keyword" placeholder="搜索规则名称/编码" @search="onSearch" />

    <van-dropdown-menu>
      <van-dropdown-item v-model="filterSeverity" :options="severityOptions" @change="onSearch" />
    </van-dropdown-menu>

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list v-model:loading="loading" :finished="finished" :finished-text="list.length ? '没有更多了' : ''" @load="onLoad">
        <van-cell v-for="item in list" :key="item.id" :title="item.ruleName"
          :label="`${item.ruleCode} · ${item.fieldName || '-'} · ${item.ruleType?.desc || item.ruleType}`" is-link @click="showDetail(item)">
          <template #value>
            <div style="display:flex;flex-direction:column;align-items:flex-end;gap:2px">
              <van-tag :type="severityType(item.severity?.code)">{{ item.severity?.desc || item.severity }}</van-tag>
              <span v-if="item.lastScore != null" class="nums-tabular" style="font-size:var(--typography-caption-size,13px);color:var(--color-text-tertiary, #8A9099)">
                得分 {{ item.lastScore }}
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
        <van-field v-model="form.ruleCode" label="规则编码" placeholder="唯一编码" required :readonly="isEdit" />
        <van-field v-model="form.ruleName" label="规则名称" placeholder="名称" required />
        <van-field v-model="form.fieldName" label="检查字段" placeholder="可选" />
        <van-field name="ruleType" label="规则类型">
          <template #input>
            <van-radio-group v-model="form.ruleType" direction="horizontal">
              <van-radio name="NOT_NULL">非空</van-radio>
              <van-radio name="UNIQUE">唯一</van-radio>
              <van-radio name="RANGE">范围</van-radio>
              <van-radio name="REGEX">正则</van-radio>
              <van-radio name="ENUM">枚举</van-radio>
              <van-radio name="FRESHNESS">新鲜度</van-radio>
            </van-radio-group>
          </template>
        </van-field>
        <van-field v-model="form.ruleExpr" label="规则表达式" placeholder="如: value > 0" />
        <van-field name="severity" label="严重级别">
          <template #input>
            <van-radio-group v-model="form.severity" direction="horizontal">
              <van-radio name="BLOCK">阻断</van-radio>
              <van-radio name="ERROR">错误</van-radio>
              <van-radio name="WARN">警告</van-radio>
              <van-radio name="INFO">信息</van-radio>
            </van-radio-group>
          </template>
        </van-field>
        <van-field v-model="form.scheduleCron" label="定时 Cron" placeholder="0 0 2 * * ?" />
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
import { dqApi } from '@/api/bigdata.js'

const keyword = ref('')
const filterSeverity = ref('')
const severityOptions = [
  { text: '全部级别', value: '' },
  { text: '阻断', value: 'BLOCK' },
  { text: '错误', value: 'ERROR' },
  { text: '警告', value: 'WARN' },
  { text: '信息', value: 'INFO' },
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
const dialogTitle = ref('新增规则')

const defaultForm = { ruleCode: '', ruleName: '', fieldName: '', ruleType: 'NOT_NULL', ruleExpr: '', severity: 'WARN', scheduleCron: '', owner: '', remark: '' }
const form = reactive({ ...defaultForm })

function severityType(code) {
  const map = { BLOCK: 'danger', ERROR: 'warning', WARN: 'primary', INFO: 'success' }
  return map[code] || 'default'
}

async function loadData() {
  try {
    const res = await dqApi.list({ page: page.value, size: 20, severity: filterSeverity.value || undefined })
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
function showCreate() { isEdit.value = false; editId.value = null; dialogTitle.value = '新增规则'; resetForm(); dialogVisible.value = true }
function showDetail(item) { isEdit.value = true; editId.value = item.id; dialogTitle.value = '编辑规则'; Object.assign(form, { ruleCode: item.ruleCode, ruleName: item.ruleName, fieldName: item.fieldName || '', ruleType: item.ruleType?.code || 'NOT_NULL', ruleExpr: item.ruleExpr || '', severity: item.severity?.code || 'MEDIUM', scheduleCron: item.scheduleCron || '', owner: item.owner || '', remark: item.remark || '' }); dialogVisible.value = true }

async function onSubmit() {
  submitting.value = true
  try {
    if (isEdit.value) await dqApi.update(editId.value, form)
    else await dqApi.create(form)
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
