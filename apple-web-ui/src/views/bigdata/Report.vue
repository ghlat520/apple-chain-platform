<template>
  <div class="page-container" data-density="compact">
    <van-nav-bar title="分析报告" left-arrow @click-left="$router.back()" />

    <van-search v-model="keyword" placeholder="搜索报告标题" @search="onSearch" />

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list v-model:loading="loading" :finished="finished" :finished-text="list.length ? '没有更多了' : ''" @load="onLoad">
        <van-cell v-for="item in list" :key="item.id" :title="item.title"
          :label="`${item.reportNo} · ${item.reportType?.desc || item.reportType} · ${item.period}`" is-link @click="showDetail(item)">
          <template #value>
            <div style="display:flex;flex-direction:column;align-items:flex-end;gap:2px">
              <van-tag :type="statusType(item.status?.code)">{{ item.status?.desc || item.status }}</van-tag>
              <span style="font-size:var(--typography-caption-size,13px);color:var(--color-text-tertiary, #8A9099)">
                {{ item.createdAt }}
              </span>
            </div>
          </template>
        </van-cell>
        <van-empty v-if="!loading && list.length === 0" description="暂无报告" />
      </van-list>
    </van-pull-refresh>

    <van-button class="fab-btn" type="primary" icon="plus" round @click="showGenerate" />

    <!-- 生成报告弹窗 -->
    <van-popup v-model:show="generateVisible" position="bottom" round>
      <div class="popup-header">生成报告</div>
      <van-form @submit="onGenerate" class="popup-form">
        <van-field name="type" label="报告类型">
          <template #input>
            <van-radio-group v-model="genForm.type" direction="horizontal">
              <van-radio name="WEEKLY">周报</van-radio>
              <van-radio name="MONTHLY">月报</van-radio>
              <van-radio name="SEASONAL">季报</van-radio>
            </van-radio-group>
          </template>
        </van-field>
        <van-field v-model="genForm.period" label="报告周期" placeholder="如 2026-W15, 2026-04, 2026-Q2" required />
        <div class="popup-actions">
          <van-button block type="primary" native-type="submit" :loading="submitting">生成</van-button>
        </div>
      </van-form>
    </van-popup>

    <!-- 报告详情弹窗 -->
    <van-popup v-model:show="detailVisible" position="bottom" round :style="{ maxHeight: '85%' }">
      <div class="popup-header">报告详情</div>
      <div class="popup-form">
        <van-cell-group>
          <van-cell title="报告编号" :value="current?.reportNo" />
          <van-cell title="报告标题" :value="current?.title" />
          <van-cell title="报告类型" :value="current?.reportType?.desc || current?.reportType" />
          <van-cell title="报告周期" :value="current?.period" />
          <van-cell title="状态" :value="current?.status?.desc || current?.status" />
          <van-cell title="生成时间" :value="current?.generatedTime" />
          <van-cell title="摘要" :label="current?.summary || '暂无'" />
        </van-cell-group>
        <div class="popup-actions" style="display:flex;gap:var(--space-2, 8px)">
          <van-button type="primary" :disabled="current?.status?.code !== 'DRAFT'" :loading="publishing" @click="publishReport">发布</van-button>
          <van-button type="default" @click="detailVisible = false">关闭</van-button>
        </div>
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { showToast, showSuccessToast } from 'vant'
import { reportApi } from '@/api/bigdata.js'

const keyword = ref('')
const list = ref([])
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const page = ref(1)

const generateVisible = ref(false)
const submitting = ref(false)
const genForm = reactive({ type: 'WEEKLY', period: '' })

const detailVisible = ref(false)
const current = ref(null)
const publishing = ref(false)

function statusType(code) {
  const map = { DRAFT: 'default', PUBLISHED: 'success', GENERATING: 'warning' }
  return map[code] || 'default'
}

async function loadData() {
  try {
    const res = await reportApi.list({ page: page.value, size: 20 })
    const records = res.records || res
    if (page.value === 1) list.value = records
    else list.value.push(...records)
    finished.value = list.value.length >= (res.total ?? records.length)
  } catch (e) { showToast({ type: 'fail', message: e.message || '加载失败' }); finished.value = true }
}

function onLoad() { loadData().finally(() => { loading.value = false; refreshing.value = false }) }
function onRefresh() { list.value = []; page.value = 1; finished.value = false; }
function onSearch() { onRefresh(); onLoad() }

function showGenerate() { genForm.period = ''; generateVisible.value = true }

async function onGenerate() {
  if (!genForm.period) { showToast('请填写报告周期'); return }
  submitting.value = true
  try {
    await reportApi.generate({ type: genForm.type, period: genForm.period })
    showSuccessToast('报告生成中')
    generateVisible.value = false; onRefresh(); onLoad()
  } catch (e) { showToast({ type: 'fail', message: e.message || '生成失败' }) } finally { submitting.value = false }
}

function showDetail(item) { current.value = item; detailVisible.value = true }

async function publishReport() {
  if (!current.value?.id) return
  publishing.value = true
  try {
    await reportApi.publish(current.value.id)
    showSuccessToast('已发布')
    current.value.status = { code: 'PUBLISHED', desc: '已发布' }
    onRefresh(); onLoad()
  } catch (e) { showToast({ type: 'fail', message: e.message || '发布失败' }) } finally { publishing.value = false }
}
</script>

<style scoped>
.page-container { min-height: 100vh; background: var(--color-surface-base, #FAFAF8); }
.fab-btn { position: fixed; right: var(--space-5, 20px); bottom: calc(var(--space-6, 24px) + env(safe-area-inset-bottom)); z-index: var(--z-index-raised, 10); width: var(--density-button-height, 40px); height: var(--density-button-height, 40px); }
.popup-header { font-size: var(--typography-h3-size, 16px); font-weight: var(--typography-h3-weight, 500); padding: var(--space-4, 16px); text-align: center; }
.popup-form { padding: 0 var(--space-4, 16px) var(--space-6, 24px); }
.popup-actions { padding-top: var(--space-4, 16px); }
</style>
