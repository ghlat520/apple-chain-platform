<template>
  <div class="page-container" data-density="compact">
    <van-nav-bar title="采集任务" left-arrow @click-left="$router.back()" />

    <van-search v-model="keyword" placeholder="搜索任务名称/编码" @search="onSearch" />

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list v-model:loading="loading" :finished="finished" :finished-text="list.length ? '没有更多了' : ''" @load="onLoad">
        <van-cell v-for="item in list" :key="item.id" :title="item.jobName"
          :label="`${item.jobCode} · ${item.cronExpr || '手动'} → ${item.targetTable || '-'}`" is-link @click="showDetail(item)">
          <template #value>
            <div style="display:flex;flex-direction:column;align-items:flex-end;gap:4px">
              <van-tag :type="runStatusType(item.lastRunStatus?.code)">{{ item.lastRunStatus?.desc || item.lastRunStatus || '未运行' }}</van-tag>
              <van-button size="mini" type="primary" plain @click.stop="triggerJob(item.id)">触发</van-button>
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
        <van-field v-model="form.jobCode" label="任务编码" placeholder="唯一编码" required :readonly="isEdit" />
        <van-field v-model="form.jobName" label="任务名称" placeholder="名称" required />
        <van-field name="jobType" label="任务类型">
          <template #input>
            <van-radio-group v-model="form.jobType" direction="horizontal">
              <van-radio name="CDC">CDC</van-radio>
              <van-radio name="SCHEDULED">定时</van-radio>
              <van-radio name="MANUAL">手动</van-radio>
              <van-radio name="STREAMING">流式</van-radio>
            </van-radio-group>
          </template>
        </van-field>
        <van-field v-model.number="form.sourceId" label="数据源ID" placeholder="可选" type="digit" />
        <van-field v-model="form.cronExpr" label="Cron 表达式" placeholder="0 0 2 * * ?" />
        <van-field v-model="form.targetTable" label="目标表" placeholder="可选" />
        <van-field v-model="form.script" label="脚本" placeholder="可选" type="textarea" rows="3" />
        <van-field v-model="form.owner" label="负责人" placeholder="可选" />
        <van-field v-model.number="form.retryTimes" label="重试次数" placeholder="0" type="digit" />
        <van-field v-model.number="form.timeoutSeconds" label="超时(秒)" placeholder="3600" type="digit" />
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
import { jobApi } from '@/api/bigdata.js'

const keyword = ref('')
const list = ref([])
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const page = ref(1)

const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const submitting = ref(false)
const dialogTitle = ref('新增任务')

const defaultForm = { jobCode: '', jobName: '', jobType: 'CDC', sourceId: null, cronExpr: '', targetTable: '', script: '', owner: '', retryTimes: null, timeoutSeconds: null, remark: '' }
const form = reactive({ ...defaultForm })

function runStatusType(code) {
  const map = { SUCCESS: 'success', RUNNING: 'primary', FAILED: 'danger', PENDING: 'warning', TIMEOUT: 'danger' }
  return map[code] || 'default'
}

async function loadData() {
  try {
    const res = await jobApi.list({ page: page.value, size: 20 })
    const records = res.records || res
    if (page.value === 1) list.value = records
    else list.value.push(...records)
    finished.value = list.value.length >= (res.total ?? records.length)
  } catch (e) { showToast({ type: 'fail', message: e.message || '加载失败' }); finished.value = true }
}

function onLoad() { loadData().finally(() => { loading.value = false; refreshing.value = false }) }
function onRefresh() { list.value = []; page.value = 1; finished.value = false; }
function onSearch() { onRefresh(); onLoad() }

async function triggerJob(id) {
  try { await jobApi.trigger(id); showSuccessToast('已触发') }
  catch (e) { showToast({ type: 'fail', message: e.message || '触发失败' }) }
}

function resetForm() { Object.assign(form, { ...defaultForm }) }
function showCreate() { isEdit.value = false; editId.value = null; dialogTitle.value = '新增任务'; resetForm(); dialogVisible.value = true }
function showDetail(item) { isEdit.value = true; editId.value = item.id; dialogTitle.value = '编辑任务'; Object.assign(form, { jobCode: item.jobCode, jobName: item.jobName, jobType: item.jobType?.code || 'CDC', sourceId: item.sourceId, cronExpr: item.cronExpr || '', targetTable: item.targetTable || '', script: item.script || '', owner: item.owner || '', retryTimes: item.retryTimes, timeoutSeconds: item.timeoutSeconds, remark: item.remark || '' }); dialogVisible.value = true }

async function onSubmit() {
  submitting.value = true
  try {
    if (isEdit.value) await jobApi.update(editId.value, form)
    else await jobApi.create(form)
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
