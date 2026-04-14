<template>
  <div class="blockchain-page" data-density="compact">
    <van-tabs v-model:active="activeTab" sticky offset-top="46" animated swipeable>

      <!-- ========== Tab 1: 上链记录列表 ========== -->
      <van-tab title="上链记录" name="records">

        <!-- Status Filter Dropdown -->
        <div class="filter-bar">
          <van-dropdown-menu>
            <van-dropdown-item
              v-model="filterStatus"
              :options="statusOptions"
              @change="onStatusChange"
            />
          </van-dropdown-menu>
        </div>

        <!-- Loading 态 -->
        <div v-if="listLoading && records.length === 0" class="state-loading">
          <van-loading type="spinner" color="var(--color-brand-primary)" size="36px" />
          <p class="state-text">加载中...</p>
        </div>

        <!-- Error 态 -->
        <div v-else-if="listError" class="state-error">
          <van-empty image="error" :description="listError">
            <template #bottom>
              <van-button type="primary" size="small" @click="loadRecords(true)">重试</van-button>
            </template>
          </van-empty>
        </div>

        <!-- Success 态 + Empty 态 -->
        <van-pull-refresh
          v-else
          v-model="refreshing"
          @refresh="loadRecords(true)"
        >
          <van-list
            v-model:loading="listLoading"
            :finished="listFinished"
            finished-text="没有更多记录了"
            @load="loadRecords(false)"
          >
            <!-- Empty 态 -->
            <van-empty
              v-if="!listLoading && records.length === 0"
              image="search"
              description="暂无上链记录"
            />

            <!-- Record Cards -->
            <div
              v-for="item in records"
              :key="item.id"
              class="record-card"
              @click="openDetail(item)"
              tabindex="0"
              role="button"
              :aria-label="`上链记录 ${item.traceCode}`"
              @keydown.enter="openDetail(item)"
              @keydown.space.prevent="openDetail(item)"
            >
              <!-- Card Header -->
              <div class="record-card__header">
                <span class="record-code nums-tabular">{{ item.traceCode }}</span>
                <van-tag
                  :color="statusColor(item.chainStatus?.code)"
                  text-color="var(--color-text-inverse)"
                  size="medium"
                >
                  {{ item.chainStatus?.desc || statusDesc(item.chainStatus?.code) }}
                </van-tag>
              </div>

              <!-- Card Body -->
              <div class="record-card__body">
                <div class="record-meta-row">
                  <span class="meta-label">业务类型</span>
                  <span class="meta-value">{{ businessTypeLabel(item.businessType) }}</span>
                </div>
                <div class="record-meta-row">
                  <span class="meta-label">数据哈希</span>
                  <span
                    class="meta-value meta-hash nums-tabular"
                    :title="item.dataHash"
                    @click.stop="copyToClipboard(item.dataHash)"
                  >{{ truncateHash(item.dataHash) }}</span>
                </div>
                <div class="record-meta-row" v-if="item.chainTxHash">
                  <span class="meta-label">交易哈希</span>
                  <span
                    class="meta-value meta-hash nums-tabular"
                    :title="item.chainTxHash"
                    @click.stop="copyToClipboard(item.chainTxHash)"
                  >
                    {{ truncateHash(item.chainTxHash) }}
                    <span v-if="isMockHash(item.chainTxHash)" class="mock-badge">[Mock]</span>
                  </span>
                </div>
                <div class="record-card__footer">
                  <span class="meta-label">重试 <span class="nums-tabular">{{ item.retryCount ?? 0 }}</span> 次</span>
                  <span class="meta-time">{{ item.createdAt }}</span>
                </div>
              </div>

              <!-- Retry Button for FAILED -->
              <div
                v-if="item.chainStatus?.code === STATUS_CODE.FAILED"
                class="record-card__retry"
                @click.stop
              >
                <van-button
                  size="small"
                  type="warning"
                  :loading="retryLoadingMap[item.id]"
                  @click="handleRetry(item)"
                >
                  重试上链
                </van-button>
              </div>
            </div>
          </van-list>
        </van-pull-refresh>

        <!-- Detail Popup -->
        <van-popup
          v-model:show="drawerVisible"
          position="bottom"
          :style="{ height: '78%' }"
          round
          safe-area-inset-bottom
        >
          <div class="drawer-wrap">
            <div class="drawer-header">
              <span class="drawer-title">上链记录详情</span>
              <van-icon
                name="cross"
                size="20"
                class="drawer-close"
                @click="drawerVisible = false"
              />
            </div>

            <div v-if="currentRecord" class="drawer-body">
              <van-cell-group inset>
                <van-cell title="溯源码" :value="currentRecord.traceCode" />
                <van-cell title="业务类型">
                  <template #value>
                    <van-tag plain>{{ businessTypeLabel(currentRecord.businessType) }}</van-tag>
                  </template>
                </van-cell>
                <van-cell v-if="currentRecord.businessId" title="业务ID" :value="String(currentRecord.businessId)" />
                <van-cell title="上链状态">
                  <template #value>
                    <van-tag
                      :color="statusColor(currentRecord.chainStatus?.code)"
                      text-color="var(--color-text-inverse)"
                    >
                      {{ currentRecord.chainStatus?.desc || statusDesc(currentRecord.chainStatus?.code) }}
                    </van-tag>
                  </template>
                </van-cell>
                <van-cell title="重试次数">
                  <template #value>
                    <span class="nums-tabular">{{ currentRecord.retryCount ?? 0 }}</span>
                    <span v-if="(currentRecord.retryCount ?? 0) >= MAX_RETRY" class="retry-max-hint">（已达上限）</span>
                  </template>
                </van-cell>
                <van-cell v-if="currentRecord.chainBlockHeight" title="区块高度">
                  <template #value>
                    <span class="nums-tabular">{{ currentRecord.chainBlockHeight }}</span>
                  </template>
                </van-cell>
                <van-cell title="创建时间" :value="currentRecord.createdAt" />
                <van-cell title="更新时间" :value="currentRecord.updatedAt" />
                <van-cell
                  v-if="currentRecord.errorMsg"
                  title="失败原因"
                  :label="currentRecord.errorMsg"
                  title-class="cell-title--error"
                />
              </van-cell-group>

              <!-- Hash blocks -->
              <div class="hash-block">
                <div class="hash-block__label">
                  数据哈希 (SHA-256)
                  <van-icon name="copy-o" class="copy-icon" @click="copyToClipboard(currentRecord.dataHash)" />
                </div>
                <p class="hash-block__value nums-tabular">{{ currentRecord.dataHash || '-' }}</p>
              </div>

              <div class="hash-block" v-if="currentRecord.chainTxHash">
                <div class="hash-block__label">
                  链上交易哈希
                  <span v-if="isMockHash(currentRecord.chainTxHash)" class="mock-badge">[Mock]</span>
                  <van-icon name="copy-o" class="copy-icon" @click="copyToClipboard(currentRecord.chainTxHash)" />
                </div>
                <p class="hash-block__value nums-tabular">{{ currentRecord.chainTxHash }}</p>
              </div>

              <!-- Snapshot -->
              <div class="snapshot-block" v-if="currentRecord.dataSnapshot">
                <p class="hash-block__label">业务快照</p>
                <pre class="snapshot-content nums-tabular">{{ formatSnapshot(currentRecord.dataSnapshot) }}</pre>
              </div>

              <!-- Retry Action -->
              <div
                class="drawer-action"
                v-if="currentRecord.chainStatus?.code === STATUS_CODE.FAILED && (currentRecord.retryCount ?? 0) < MAX_RETRY"
              >
                <van-button
                  type="warning"
                  block
                  :loading="retryLoadingMap[currentRecord.id]"
                  @click="handleRetry(currentRecord)"
                >
                  重试上链
                </van-button>
              </div>
              <div
                class="drawer-action"
                v-if="currentRecord.chainStatus?.code === STATUS_CODE.FAILED && (currentRecord.retryCount ?? 0) >= MAX_RETRY"
              >
                <p class="retry-max-msg">已达最大重试次数（{{ MAX_RETRY }}），请人工介入排查奥链节点</p>
              </div>
            </div>
          </div>
        </van-popup>
      </van-tab>

      <!-- ========== Tab 2: 公开验证 ========== -->
      <van-tab title="链上验证" name="verify">
        <div class="verify-section">
          <div class="verify-search">
            <van-search
              v-model="verifyCode"
              placeholder="输入溯源码验证链上存证"
              show-action
              clearable
              @search="handleVerify"
            >
              <template #action>
                <van-button
                  type="primary"
                  size="small"
                  :loading="verifyLoading"
                  :disabled="!verifyCode.trim()"
                  @click="handleVerify"
                >
                  验证
                </van-button>
              </template>
            </van-search>
          </div>

          <!-- 验证中 -->
          <div v-if="verifyLoading" class="state-loading">
            <van-loading type="spinner" color="var(--color-brand-primary)" size="36px" />
            <p class="state-text">验证中...</p>
          </div>

          <!-- 验证结果 -->
          <div v-else-if="verifyResult" class="verify-result">
            <!-- 通过/不通过 Badge -->
            <div
              class="verify-badge"
              :class="verifyResult.verified ? 'verify-badge--success' : 'verify-badge--fail'"
            >
              <van-icon
                :name="verifyResult.verified ? 'shield-o' : 'warning-o'"
                size="28"
                :color="verifyResult.verified ? 'var(--color-success)' : 'var(--color-error)'"
              />
              <div class="verify-badge__text">
                <span class="verify-badge__title">
                  {{ verifyResult.verified ? '链上验证通过' : '链上验证未通过' }}
                </span>
                <span class="verify-badge__sub">
                  共 <span class="nums-tabular">{{ verifyResult.recordCount }}</span> 条记录，
                  成功 <span class="nums-tabular">{{ verifyResult.successCount }}</span> 条
                </span>
              </div>
            </div>

            <!-- Summary -->
            <van-cell-group inset class="verify-summary">
              <van-cell title="溯源码" :value="verifyResult.traceCode" />
              <van-cell title="验证时间" :value="verifyResult.verifiedAt" />
            </van-cell-group>

            <!-- Records -->
            <p class="verify-records-title">关联上链记录</p>
            <div
              v-for="r in verifyResult.records"
              :key="r.id"
              class="verify-record-card"
            >
              <div class="verify-record-card__header">
                <span class="meta-label">{{ businessTypeLabel(r.businessType) }}</span>
                <van-tag
                  :color="statusColor(r.chainStatus?.code)"
                  text-color="var(--color-text-inverse)"
                  size="small"
                >
                  {{ r.chainStatus?.desc || statusDesc(r.chainStatus?.code) }}
                </van-tag>
              </div>
              <div class="record-meta-row">
                <span class="meta-label">数据哈希</span>
                <span
                  class="meta-value meta-hash nums-tabular"
                  @click="copyToClipboard(r.dataHash)"
                >{{ truncateHash(r.dataHash) }}</span>
              </div>
              <div class="record-meta-row" v-if="r.chainTxHash">
                <span class="meta-label">交易哈希</span>
                <span
                  class="meta-value meta-hash nums-tabular"
                  @click="copyToClipboard(r.chainTxHash)"
                >
                  {{ truncateHash(r.chainTxHash) }}
                  <span v-if="isMockHash(r.chainTxHash)" class="mock-badge">[Mock]</span>
                </span>
              </div>
              <div class="record-meta-row" v-if="r.chainBlockHeight">
                <span class="meta-label">区块高度</span>
                <span class="meta-value nums-tabular">{{ r.chainBlockHeight }}</span>
              </div>
              <div class="record-meta-row">
                <span class="meta-label">提交时间</span>
                <span class="meta-value">{{ r.createdAt }}</span>
              </div>
            </div>
          </div>

          <!-- 验证错误 -->
          <div v-else-if="verifyError" class="state-error">
            <van-empty image="error" :description="verifyError">
              <template #bottom>
                <van-button type="primary" size="small" plain @click="clearVerify">清除</van-button>
              </template>
            </van-empty>
          </div>

          <!-- 引导态 -->
          <div v-else class="state-guide">
            <van-empty image="search" description="输入溯源码，验证区块链存证记录" />
          </div>
        </div>
      </van-tab>

      <!-- ========== Tab 3: 手动提交上链 ========== -->
      <van-tab title="提交上链" name="submit">
        <div class="submit-section">
          <van-cell-group inset>
            <van-field
              v-model="submitForm.traceCode"
              label="溯源码"
              placeholder="请输入溯源码（如 BATCH-20260412-001）"
              required
              clearable
              :rules="[{ required: true, message: '请输入溯源码' }]"
            />
            <van-field
              v-model="submitForm.businessTypeDisplay"
              is-link
              readonly
              label="业务类型"
              placeholder="请选择"
              required
              @click="showTypePicker = true"
            />
            <van-field
              v-model="submitForm.businessIdStr"
              label="业务ID"
              placeholder="可选，业务记录主键"
              type="digit"
            />
            <van-field
              v-model="submitForm.snapshotJson"
              label="快照数据"
              type="textarea"
              placeholder='JSON 格式，如 {"batchCode":"B001","weight":500}'
              rows="5"
              autosize
              required
              :error="snapshotHasError"
              :error-message="snapshotErrMsg"
              @blur="validateSnapshot"
            />
          </van-cell-group>

          <div class="submit-action">
            <van-button
              type="primary"
              block
              :loading="submitLoading"
              :disabled="!isSubmitValid"
              @click="handleSubmit"
            >
              提交上链
            </van-button>
          </div>

          <!-- Submit Result -->
          <div v-if="submitResult" class="submit-result">
            <van-cell-group inset>
              <van-cell title="提交结果">
                <template #value>
                  <van-tag color="var(--color-info)" text-color="var(--color-text-inverse)">已排队</van-tag>
                </template>
              </van-cell>
              <van-cell title="记录ID" :value="String(submitResult.id)" />
              <van-cell title="溯源码" :value="submitResult.traceCode" />
              <van-cell title="数据哈希">
                <template #value>
                  <span class="meta-hash nums-tabular" @click="copyToClipboard(submitResult.dataHash)">
                    {{ truncateHash(submitResult.dataHash) }}
                  </span>
                </template>
              </van-cell>
              <van-cell title="提交时间" :value="submitResult.createdAt" />
            </van-cell-group>
          </div>
        </div>

        <!-- Business Type Picker -->
        <van-popup v-model:show="showTypePicker" position="bottom" round safe-area-inset-bottom>
          <van-picker
            :columns="businessTypeColumns"
            @confirm="onTypeConfirm"
            @cancel="showTypePicker = false"
            title="选择业务类型"
          />
        </van-popup>
      </van-tab>
    </van-tabs>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { traceApi } from '@/api/trace.js'
import { showToast, showSuccessToast } from 'vant'

// ==================== Constants ====================

const STATUS_CODE = Object.freeze({
  PENDING: 0,
  SUCCESS: 1,
  FAILED: 2,
  RETRYING: 3,
})

const MAX_RETRY = 3

/** chainStatus.code → Vant tag color (按 DESIGN.md 语义色) */
const STATUS_COLOR_MAP = {
  [STATUS_CODE.PENDING]:  'var(--color-info)',
  [STATUS_CODE.SUCCESS]:  'var(--color-success)',
  [STATUS_CODE.FAILED]:   'var(--color-error)',
  [STATUS_CODE.RETRYING]: 'var(--color-warning)',
}

const STATUS_DESC_MAP = {
  [STATUS_CODE.PENDING]:  '待上链',
  [STATUS_CODE.SUCCESS]:  '已上链',
  [STATUS_CODE.FAILED]:   '失败',
  [STATUS_CODE.RETRYING]: '重试中',
}

const BUSINESS_TYPE_MAP = {
  BATCH:       '批次',
  BOX:         '箱码',
  FRUIT:       '果码',
  CULTIVATION: '种植',
  HARVEST:     '采收',
  TRADE:       '交易',
}

const businessTypeColumns = Object.entries(BUSINESS_TYPE_MAP).map(([value, text]) => ({ text, value }))

const statusOptions = [
  { text: '全部状态', value: -1 },
  { text: '待上链',   value: STATUS_CODE.PENDING },
  { text: '已上链',   value: STATUS_CODE.SUCCESS },
  { text: '失败',     value: STATUS_CODE.FAILED },
  { text: '重试中',   value: STATUS_CODE.RETRYING },
]

// ==================== Helpers ====================

function statusColor(code) {
  return STATUS_COLOR_MAP[code] ?? 'var(--color-text-tertiary)'
}

function statusDesc(code) {
  return STATUS_DESC_MAP[code] ?? '未知'
}

function businessTypeLabel(type) {
  return BUSINESS_TYPE_MAP[type] || type || '-'
}

/**
 * 截短 hash：前 8 位 + ... + 后 6 位
 */
function truncateHash(hash) {
  if (!hash) return '-'
  if (hash.length <= 16) return hash
  return `${hash.slice(0, 8)}...${hash.slice(-6)}`
}

function isMockHash(hash) {
  return typeof hash === 'string' && hash.startsWith('mock_tx_hash_')
}

function formatSnapshot(data) {
  if (!data) return ''
  if (typeof data === 'string') {
    try { return JSON.stringify(JSON.parse(data), null, 2) } catch { return data }
  }
  return JSON.stringify(data, null, 2)
}

async function copyToClipboard(text) {
  if (!text || text === '-') return
  try {
    await navigator.clipboard.writeText(text)
    showSuccessToast({ message: '已复制', duration: 1200 })
  } catch {
    showToast({ type: 'fail', message: '复制失败，请手动复制' })
  }
}

// ==================== Tab State ====================

const activeTab = ref('records')

// ==================== Tab 1: Records ====================

const filterStatus = ref(-1)
const records = ref([])
const refreshing = ref(false)
const listLoading = ref(false)
const listFinished = ref(false)
const listError = ref('')

const drawerVisible = ref(false)
const currentRecord = ref(null)
/** Map<id, boolean> for per-row retry loading */
const retryLoadingMap = reactive({})

function onStatusChange() {
  loadRecords(true)
}

async function loadRecords(isRefresh = false) {
  if (isRefresh) {
    records.value = []
    listFinished.value = false
    listError.value = ''
  }

  listLoading.value = true
  try {
    const params = { limit: 100 }
    if (filterStatus.value !== -1) {
      params.status = filterStatus.value
    }

    const res = await traceApi.getChainRecords(params)
    // API returns R<List<ChainRecord>>: unwrap .data
    const list = Array.isArray(res?.data) ? res.data
                : Array.isArray(res)       ? res
                : []

    records.value = isRefresh ? list : [...records.value, ...list]
    // Non-paginated endpoint: always finish after one load
    listFinished.value = true
  } catch (e) {
    listError.value = e?.response?.data?.message || '加载失败，请重试'
    listFinished.value = true
  } finally {
    listLoading.value = false
    refreshing.value = false
  }
}

function openDetail(item) {
  currentRecord.value = item
  drawerVisible.value = true
}

async function handleRetry(item) {
  retryLoadingMap[item.id] = true
  try {
    const res = await traceApi.retryChain(item.id)
    showSuccessToast('重试请求已提交')
    // Update local record with returned data
    const updated = res?.data || res
    if (updated?.id) {
      const idx = records.value.findIndex(r => r.id === updated.id)
      if (idx !== -1) records.value[idx] = updated
      if (currentRecord.value?.id === updated.id) currentRecord.value = updated
    } else {
      // Fallback: full refresh
      drawerVisible.value = false
      loadRecords(true)
    }
  } catch (e) {
    const msg = e?.response?.data?.message || '重试失败'
    showToast({ type: 'fail', message: msg })
  } finally {
    retryLoadingMap[item.id] = false
  }
}

// ==================== Tab 2: Verify ====================

const verifyCode = ref('')
const verifyLoading = ref(false)
const verifyResult = ref(null)
const verifyError = ref('')

async function handleVerify() {
  const code = verifyCode.value.trim()
  if (!code) {
    showToast('请输入溯源码')
    return
  }

  verifyLoading.value = true
  verifyResult.value = null
  verifyError.value = ''

  try {
    const res = await traceApi.verifyChain(code)
    // R<ChainVerifyResponse>
    verifyResult.value = res?.data || res || null
    if (!verifyResult.value) {
      verifyError.value = '未找到链上记录'
    }
  } catch (e) {
    verifyError.value = e?.response?.data?.message || '验证失败，未找到该溯源码的链上记录'
  } finally {
    verifyLoading.value = false
  }
}

function clearVerify() {
  verifyCode.value = ''
  verifyResult.value = null
  verifyError.value = ''
}

// ==================== Tab 3: Submit ====================

const submitForm = reactive({
  traceCode: '',
  businessTypeDisplay: '',
  businessType: '',
  businessIdStr: '',
  snapshotJson: '',
})

const submitLoading = ref(false)
const snapshotHasError = ref(false)
const snapshotErrMsg = ref('')
const showTypePicker = ref(false)
const submitResult = ref(null)

const isSubmitValid = computed(() =>
  submitForm.traceCode.trim() &&
  submitForm.businessType &&
  submitForm.snapshotJson.trim() &&
  !snapshotHasError.value
)

function validateSnapshot() {
  const raw = submitForm.snapshotJson.trim()
  if (!raw) {
    snapshotHasError.value = false
    snapshotErrMsg.value = ''
    return true
  }
  try {
    JSON.parse(raw)
    snapshotHasError.value = false
    snapshotErrMsg.value = ''
    return true
  } catch {
    snapshotHasError.value = true
    snapshotErrMsg.value = '请输入合法的 JSON 格式'
    return false
  }
}

function onTypeConfirm({ selectedOptions }) {
  const opt = selectedOptions?.[0]
  if (opt) {
    submitForm.businessType = opt.value
    submitForm.businessTypeDisplay = opt.text
  }
  showTypePicker.value = false
}

async function handleSubmit() {
  if (!validateSnapshot()) return
  if (!isSubmitValid.value) {
    showToast('请填写必填项')
    return
  }

  submitLoading.value = true
  submitResult.value = null
  try {
    const snapshot = JSON.parse(submitForm.snapshotJson)
    const payload = {
      traceCode: submitForm.traceCode.trim(),
      businessType: submitForm.businessType,
      snapshot,
    }
    if (submitForm.businessIdStr.trim()) {
      payload.businessId = Number(submitForm.businessIdStr)
    }

    const res = await traceApi.submitChain(payload)
    submitResult.value = res?.data || res || null
    showSuccessToast('提交成功，已进入上链队列')

    // Reset form
    submitForm.traceCode = ''
    submitForm.businessType = ''
    submitForm.businessTypeDisplay = ''
    submitForm.businessIdStr = ''
    submitForm.snapshotJson = ''

    // Refresh records tab in background
    loadRecords(true)
  } catch (e) {
    const msg = e?.response?.data?.message || e?.message || '提交失败'
    showToast({ type: 'fail', message: msg })
  } finally {
    submitLoading.value = false
  }
}
</script>

<style scoped>
/* ========== Page Root ========== */
.blockchain-page {
  min-height: 100vh;
  background: var(--color-surface-base);
}

/* ========== Filter Bar ========== */
.filter-bar {
  background: var(--color-surface-raised);
  border-bottom: 1px solid var(--color-border-default);
}

/* ========== State Views ========== */
.state-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--space-12) var(--space-4);
}

.state-text {
  margin-top: var(--space-3);
  font-size: var(--font-size-body);
  color: var(--color-text-secondary);
}

.state-error,
.state-guide {
  padding: var(--space-8) var(--space-4);
}

/* ========== Record Card ========== */
.record-card {
  margin: var(--space-3) var(--space-4) 0;
  padding: var(--space-3) var(--space-4);
  background: var(--color-surface-raised);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-1);
  cursor: pointer;
  transition: box-shadow var(--motion-fast) var(--ease-out),
              transform var(--motion-fast) var(--ease-out);
}

.record-card:last-child {
  margin-bottom: var(--space-4);
}

.record-card:hover {
  box-shadow: var(--shadow-2);
}

.record-card:active {
  transform: scale(0.99);
  box-shadow: var(--shadow-1);
}

.record-card:focus-visible {
  outline: var(--outline-width) solid var(--color-brand-primary);
  outline-offset: 2px;
}

.record-card__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-2);
}

.record-code {
  font-size: var(--font-size-body);
  font-weight: 500;
  color: var(--color-text-primary);
  font-family: var(--font-mono);
}

.record-card__body {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.record-card__footer {
  display: flex;
  justify-content: space-between;
  margin-top: var(--space-2);
  padding-top: var(--space-2);
  border-top: 1px solid var(--color-divider);
}

.record-card__retry {
  margin-top: var(--space-3);
  padding-top: var(--space-2);
  border-top: 1px solid var(--color-divider);
}

.meta-label {
  font-size: var(--font-size-caption);
  color: var(--color-text-secondary);
  flex-shrink: 0;
}

.meta-value {
  font-size: var(--font-size-caption);
  color: var(--color-text-primary);
  text-align: right;
  flex: 1;
}

.record-meta-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--space-2);
}

.meta-hash {
  font-family: var(--font-mono);
  font-size: var(--font-size-overline);
  cursor: pointer;
  color: var(--color-info);
  text-decoration: underline dotted;
}

.meta-hash:hover {
  color: var(--color-brand-primary);
}

.meta-time {
  font-size: var(--font-size-overline);
  color: var(--color-text-tertiary);
}

.mock-badge {
  font-size: var(--font-size-overline);
  color: var(--color-warning);
  font-family: var(--font-body);
  margin-left: var(--space-1);
}

/* ========== Detail Drawer ========== */
.drawer-wrap {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.drawer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-4) var(--space-4) var(--space-3);
  border-bottom: 1px solid var(--color-divider);
  flex-shrink: 0;
}

.drawer-title {
  font-size: var(--font-size-body);
  font-weight: 600;
  color: var(--color-text-primary);
}

.drawer-close {
  color: var(--color-text-secondary);
  cursor: pointer;
  padding: var(--space-1);
}

.drawer-close:hover {
  color: var(--color-text-primary);
}

.drawer-body {
  flex: 1;
  overflow-y: auto;
  padding-bottom: var(--space-8);
}

.drawer-action {
  padding: var(--space-4);
}

.hash-block {
  margin: var(--space-3) var(--space-4);
  padding: var(--space-3);
  background: var(--color-surface-sunken);
  border-radius: var(--radius-base);
}

.hash-block__label {
  font-size: var(--font-size-caption);
  font-weight: 500;
  color: var(--color-text-secondary);
  margin-bottom: var(--space-1);
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.hash-block__value {
  font-size: var(--font-size-overline);
  font-family: var(--font-mono);
  color: var(--color-text-primary);
  word-break: break-all;
  line-height: 1.6;
}

.copy-icon {
  cursor: pointer;
  color: var(--color-info);
  font-size: var(--font-size-body-s);
  transition: color var(--motion-fast) var(--ease-out);
}

.copy-icon:hover {
  color: var(--color-brand-primary);
}

.copy-icon:focus-visible {
  outline: var(--outline-width) solid var(--color-brand-primary);
  outline-offset: 2px;
  border-radius: var(--radius-sm);
}

.snapshot-block {
  margin: var(--space-3) var(--space-4);
}

.snapshot-content {
  margin-top: var(--space-1);
  padding: var(--space-3);
  background: var(--color-surface-sunken);
  border-radius: var(--radius-base);
  font-size: var(--font-size-overline);
  font-family: var(--font-mono);
  color: var(--color-text-primary);
  white-space: pre-wrap;
  word-break: break-all;
  line-height: 1.5;
  max-height: 200px;
  overflow-y: auto;
}

.cell-title--error {
  color: var(--color-error);
}

.retry-max-hint {
  font-size: var(--font-size-overline);
  color: var(--color-error);
  margin-left: var(--space-2);
}

.retry-max-msg {
  font-size: var(--font-size-caption);
  color: var(--color-error);
  text-align: center;
  padding: var(--space-4);
  line-height: 1.6;
}

/* ========== Verify Tab ========== */
.verify-section {
  padding-bottom: var(--space-8);
}

.verify-search {
  background: var(--color-surface-raised);
}

.verify-result {
  padding: 0 var(--space-3) var(--space-4);
}

.verify-badge {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-4);
  margin: var(--space-3) 0;
  border-radius: var(--radius-md);
}

.verify-badge--success {
  background: var(--color-leaf-green-light);
}

.verify-badge--fail {
  background: var(--color-brand-light);
}

.verify-badge__text {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.verify-badge__title {
  font-size: var(--font-size-body);
  font-weight: 600;
  color: var(--color-text-primary);
}

.verify-badge--success .verify-badge__title {
  color: var(--color-success);
}

.verify-badge--fail .verify-badge__title {
  color: var(--color-error);
}

.verify-badge__sub {
  font-size: var(--font-size-caption);
  color: var(--color-text-secondary);
}

.verify-summary {
  margin-bottom: var(--space-3);
}

.verify-records-title {
  font-size: var(--font-size-caption);
  font-weight: 500;
  color: var(--color-text-secondary);
  padding: var(--space-2) var(--space-3);
}

.verify-record-card {
  background: var(--color-surface-raised);
  border-radius: var(--radius-base);
  padding: var(--space-3) var(--space-4);
  margin-bottom: var(--space-2);
  box-shadow: var(--shadow-1);
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.verify-record-card__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-1);
}

/* ========== Submit Tab ========== */
.submit-section {
  padding-bottom: var(--space-4);
}

.submit-action {
  padding: var(--space-4);
}

.submit-result {
  margin-top: var(--space-2);
  padding-bottom: var(--space-4);
}

/* ========== prefers-reduced-motion ========== */
@media (prefers-reduced-motion: reduce) {
  .record-card {
    transition: none;
  }
}
</style>
