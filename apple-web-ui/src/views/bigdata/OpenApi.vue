<template>
  <div class="page-container" data-density="compact">
    <van-nav-bar title="开放 API" left-arrow @click-left="$router.back()" />

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list v-model:loading="loading" :finished="finished" :finished-text="list.length ? '没有更多了' : ''" @load="onLoad">
        <van-cell v-for="item in list" :key="item.id" :title="item.clientName"
          :label="`${item.owner || '-'} · QPS ${(item.rateLimitQps ?? 'unlimited')} · 配额 ${item.dailyQuota ?? 'unlimited'}/日`" is-link @click="showDetail(item)">
          <template #value>
            <div style="display:flex;flex-direction:column;align-items:flex-end;gap:2px">
              <van-tag :type="statusType(item.status?.code)">{{ item.status?.desc || item.status }}</van-tag>
              <span style="font-size:var(--typography-caption-size,13px);color:var(--color-text-tertiary, #8A9099);font-family:var(--font-mono, monospace)">
                {{ maskKey(item.appKey) }}
              </span>
            </div>
          </template>
        </van-cell>
        <van-empty v-if="!loading && list.length === 0" description="暂无客户端" />
      </van-list>
    </van-pull-refresh>

    <van-button class="fab-btn" type="primary" icon="plus" round @click="showCreate" />

    <!-- 注册弹窗 -->
    <van-popup v-model:show="createVisible" position="bottom" round>
      <div class="popup-header">注册 API 客户端</div>
      <van-form @submit="onCreate" class="popup-form">
        <van-field v-model="createForm.clientName" label="客户端名称" placeholder="名称" required />
        <van-field v-model="createForm.owner" label="负责人" placeholder="可选" />
        <van-field v-model.number="createForm.rateLimitQps" label="限流 QPS" placeholder="不限" type="digit" />
        <van-field v-model.number="createForm.dailyQuota" label="日配额" placeholder="不限" type="digit" />
        <van-field v-model="createForm.validUntil" label="有效期至" placeholder="yyyy-MM-dd" />
        <van-field v-model="createForm.remark" label="备注" placeholder="可选" type="textarea" rows="2" />
        <div class="popup-actions">
          <van-button block type="primary" native-type="submit" :loading="submitting">注册</van-button>
        </div>
      </van-form>
    </van-popup>

    <!-- 详情弹窗 -->
    <van-popup v-model:show="detailVisible" position="bottom" round :style="{ maxHeight: '85%' }">
      <div class="popup-header">客户端详情</div>
      <div class="popup-form">
        <van-cell-group>
          <van-cell title="客户端名称" :value="current?.clientName" />
          <van-cell title="AppKey" :value="current?.appKey" is-link @click="copyKey(current?.appKey)">
            <template #right-icon>
              <van-icon name="description" style="margin-left:4px" />
            </template>
          </van-cell>
          <van-cell title="状态" :value="current?.status?.desc || current?.status">
            <template #right-icon>
              <van-button size="mini" type="primary" plain @click.stop="toggleStatus(current)">
                {{ current?.status?.code === 'ACTIVE' ? '禁用' : '启用' }}
              </van-button>
            </template>
          </van-cell>
          <van-cell title="限流 QPS" :value="String(current?.rateLimitQps ?? '不限')" />
          <van-cell title="日配额" :value="String(current?.dailyQuota ?? '不限')" />
          <van-cell title="有效期" :value="current?.validUntil || '永久'" />
          <van-cell title="注册时间" :value="current?.createdAt" />
        </van-cell-group>
        <div class="popup-actions">
          <van-button block type="warning" plain @click="rotateKey(current?.id)">轮转密钥</van-button>
          <van-button block type="default" @click="detailVisible = false" style="margin-top:var(--space-2, 8px)">关闭</van-button>
        </div>
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { showToast, showSuccessToast, showConfirmDialog } from 'vant'
import { openApi } from '@/api/bigdata.js'

const list = ref([])
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const page = ref(1)

const createVisible = ref(false)
const submitting = ref(false)
const createForm = reactive({ clientName: '', owner: '', rateLimitQps: null, dailyQuota: null, validUntil: '', remark: '' })

const detailVisible = ref(false)
const current = ref(null)

function statusType(code) {
  const map = { ACTIVE: 'success', DISABLED: 'default', REVOKED: 'danger' }
  return map[code] || 'default'
}

function maskKey(key) {
  if (!key || key.length <= 12) return key
  return key.slice(0, 8) + '...' + key.slice(-4)
}

async function loadData() {
  try {
    const res = await openApi.list({ page: page.value, size: 20 })
    const records = res.records || res
    if (page.value === 1) list.value = records
    else list.value.push(...records)
    finished.value = list.value.length >= (res.total ?? records.length)
  } catch (e) { finished.value = true }
}

function onLoad() { loadData().finally(() => { loading.value = false; refreshing.value = false }) }
function onRefresh() { list.value = []; page.value = 1; finished.value = false; }

function showCreate() { Object.assign(createForm, { clientName: '', owner: '', rateLimitQps: null, dailyQuota: null, validUntil: '', remark: '' }); createVisible.value = true }

async function onCreate() {
  submitting.value = true
  try {
    const res = await openApi.register(createForm)
    showSuccessToast('注册成功')
    createVisible.value = false
    if (res?.appKey) showToast({ message: `AppKey: ${res.appKey}\n请妥善保管密钥`, duration: 5000 })
    onRefresh(); onLoad()
  } catch (e) { showToast({ type: 'fail', message: e.message || '注册失败' }) } finally { submitting.value = false }
}

function showDetail(item) { current.value = item; detailVisible.value = true }

async function copyKey(key) {
  if (!key) return
  try { await navigator.clipboard.writeText(key); showSuccessToast('已复制 AppKey') }
  catch { showToast('复制失败') }
}

async function toggleStatus(item) {
  if (!item?.id) return
  const newStatus = item.status?.code === 'ACTIVE' ? 'DISABLED' : 'ACTIVE'
  try {
    await openApi.updateStatus(item.id, newStatus)
    item.status = { code: newStatus, desc: newStatus === 'ACTIVE' ? '启用' : '禁用' }
    showSuccessToast('状态已更新')
  } catch (e) { showToast({ type: 'fail', message: e.message || '状态更新失败' }) }
}

async function rotateKey(id) {
  if (!id) return
  try {
    await showConfirmDialog({ title: '轮转密钥', message: '旧密钥将立即失效，确认？' })
  } catch { return }
  try {
    const res = await openApi.rotateKey(id)
    showSuccessToast('密钥已轮转')
    if (res?.appKey) showToast({ message: `新 AppKey: ${res.appKey}`, duration: 5000 })
    onRefresh(); onLoad()
  } catch (e) { showToast({ type: 'fail', message: e.message || '轮转失败' }) }
}
</script>

<style scoped>
.page-container { min-height: 100vh; background: var(--color-surface-base, #FAFAF8); }
.fab-btn { position: fixed; right: var(--space-5, 20px); bottom: calc(var(--space-6, 24px) + env(safe-area-inset-bottom)); z-index: var(--z-index-raised, 10); width: var(--density-button-height, 40px); height: var(--density-button-height, 40px); }
.popup-header { font-size: var(--typography-h3-size, 16px); font-weight: var(--typography-h3-weight, 500); padding: var(--space-4, 16px); text-align: center; }
.popup-form { padding: 0 var(--space-4, 16px) var(--space-6, 24px); }
.popup-actions { padding-top: var(--space-4, 16px); }
</style>
