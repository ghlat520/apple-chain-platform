<template>
  <div class="wxpay-page">
    <div class="wxpay-page__header">
      <h2 class="wxpay-page__title">微信支付管理</h2>
      <p class="wxpay-page__subtitle">演示原型 · 返回 Mock 预支付单号与状态</p>
    </div>

    <div class="wxpay-filter">
      <el-input
        v-model="query.keyword"
        placeholder="搜索订单号 / 交易号 / 描述"
        clearable
        class="wxpay-filter__input"
        @clear="loadData"
        @keyup.enter="loadData"
      />
      <el-select
        v-model="query.status"
        placeholder="支付状态"
        clearable
        class="wxpay-filter__select"
        @change="loadData"
      >
        <el-option label="待支付" value="PENDING" />
        <el-option label="支付成功" value="SUCCESS" />
        <el-option label="支付失败" value="FAILED" />
        <el-option label="已退款" value="REFUNDED" />
      </el-select>
      <el-button type="primary" :icon="Search" @click="loadData">查询</el-button>
      <el-button type="primary" plain :icon="Plus" @click="openCreateDialog">
        发起 Mock 支付
      </el-button>
    </div>

    <el-table
      :data="tableData"
      v-loading="loading"
      stripe
      border
      empty-text="暂无支付记录"
    >
      <el-table-column prop="outTradeNo" label="商户订单号" width="240" class-name="nums-tabular" />
      <el-table-column prop="orderId" label="业务订单ID" width="140" class-name="nums-tabular">
        <template v-slot:default="{ row }">{{ row.orderId || '-' }}</template>
      </el-table-column>
      <el-table-column label="金额" width="130" class-name="nums-tabular">
        <template v-slot:default="{ row }">
          <span class="wxpay-amount">¥{{ formatYuan(row.amountCents) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
      <el-table-column label="状态" width="130">
        <template v-slot:default="{ row }">
          <el-tag :type="statusTagType(row.status)" size="small" effect="light">
            {{ statusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="transactionId" label="微信交易号" width="220" class-name="nums-tabular">
        <template v-slot:default="{ row }">{{ row.transactionId || '-' }}</template>
      </el-table-column>
      <el-table-column prop="payTime" label="支付时间" width="180" />
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="160" fixed="right">
        <template v-slot:default="{ row }">
          <div class="wxpay-actions">
            <el-button link type="primary" size="small" @click="openDetail(row)">详情</el-button>
            <el-button
              v-if="statusName(row.status) === 'PENDING'"
              link
              type="warning"
              size="small"
              @click="refreshPayment(row)"
            >查询状态</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-if="total > 0"
      class="wxpay-pagination"
      background
      layout="total, prev, pager, next"
      :total="total"
      :page-size="query.size"
      :current-page="query.page"
      @current-change="(p) => { query.page = p; loadData() }"
    />

    <!-- 发起 Mock 支付对话框 -->
    <el-dialog
      v-model="createVisible"
      title="发起 Mock 微信支付"
      width="480px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="createFormRef"
        :model="createForm"
        :rules="createRules"
        label-width="110px"
      >
        <el-form-item label="业务订单ID" prop="orderId">
          <el-input-number
            v-model="createForm.orderId"
            :min="1"
            :controls="false"
            class="wxpay-form__full"
          />
        </el-form-item>
        <el-form-item label="支付金额（元）" prop="amountYuan">
          <el-input-number
            v-model="createForm.amountYuan"
            :min="0.01"
            :precision="2"
            :step="1"
            :controls="false"
            class="wxpay-form__full"
          />
          <div class="wxpay-form__hint">
            后端将换算为 <span class="nums-tabular">{{ Math.round((createForm.amountYuan || 0) * 100) }}</span> 分
          </div>
        </el-form-item>
        <el-form-item label="商品描述" prop="description">
          <el-input
            v-model="createForm.description"
            placeholder="例如：烟台红富士 100kg"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" :loading="createSubmitting" @click="submitCreate">
          发起支付
        </el-button>
      </template>
    </el-dialog>

    <!-- 支付详情抽屉 -->
    <el-drawer
      v-model="detailVisible"
      title="支付详情"
      direction="rtl"
      size="460px"
    >
      <div v-if="detail" class="wxpay-detail">
        <div
          v-if="statusName(detail.status) === 'SUCCESS' || statusName(detail.status) === 'PENDING'"
          class="wxpay-qr"
          role="img"
          aria-label="Mock 微信支付二维码占位图"
        >
          <div class="wxpay-qr__frame">
            <div class="wxpay-qr__mask">
              <svg viewBox="0 0 120 120" class="wxpay-qr__icon" aria-hidden="true">
                <rect x="8" y="8"   width="32" height="32" fill="currentColor" />
                <rect x="80" y="8"  width="32" height="32" fill="currentColor" />
                <rect x="8" y="80"  width="32" height="32" fill="currentColor" />
                <rect x="52" y="52" width="16" height="16" fill="currentColor" />
                <rect x="72" y="80" width="12" height="12" fill="currentColor" />
                <rect x="96" y="72" width="12" height="12" fill="currentColor" />
                <rect x="88" y="96" width="20" height="12" fill="currentColor" />
              </svg>
            </div>
            <div class="wxpay-qr__caption">Mock 二维码占位图（演示）</div>
          </div>
        </div>

        <dl class="wxpay-detail__list">
          <div class="wxpay-detail__row">
            <dt>商户订单号</dt>
            <dd class="nums-tabular">{{ detail.outTradeNo }}</dd>
          </div>
          <div class="wxpay-detail__row">
            <dt>业务订单ID</dt>
            <dd class="nums-tabular">{{ detail.orderId || '-' }}</dd>
          </div>
          <div class="wxpay-detail__row">
            <dt>金额</dt>
            <dd class="nums-tabular wxpay-amount">¥{{ formatYuan(detail.amountCents) }}</dd>
          </div>
          <div class="wxpay-detail__row">
            <dt>支付状态</dt>
            <dd>
              <el-tag :type="statusTagType(detail.status)" size="small" effect="light">
                {{ statusLabel(detail.status) }}
              </el-tag>
            </dd>
          </div>
          <div class="wxpay-detail__row">
            <dt>预支付会话</dt>
            <dd class="nums-tabular">{{ detail.prepayId || '-' }}</dd>
          </div>
          <div class="wxpay-detail__row">
            <dt>微信交易号</dt>
            <dd class="nums-tabular">{{ detail.transactionId || '-' }}</dd>
          </div>
          <div class="wxpay-detail__row">
            <dt>支付方式</dt>
            <dd>{{ detail.payMethod }}</dd>
          </div>
          <div class="wxpay-detail__row">
            <dt>商品描述</dt>
            <dd>{{ detail.description || '-' }}</dd>
          </div>
          <div class="wxpay-detail__row">
            <dt>支付时间</dt>
            <dd>{{ detail.payTime || '-' }}</dd>
          </div>
          <div class="wxpay-detail__row">
            <dt>创建时间</dt>
            <dd>{{ detail.createTime || '-' }}</dd>
          </div>
        </dl>
      </div>
      <el-empty v-else description="暂无详情" />
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Search, Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { wxPayApi } from '@/api/wxpay.js'

// ───── 状态映射（与后端 WxPaymentStatus 枚举对齐）─────
function statusName(status) {
  if (!status) return ''
  // BaseEnum 序列化为 {code, desc}；如果后端改为 name 字符串也兼容
  if (typeof status === 'string') return status
  if (status.code != null) {
    const byCode = { 1: 'PENDING', 2: 'SUCCESS', 3: 'FAILED', 4: 'REFUNDED' }
    return byCode[status.code] || ''
  }
  return ''
}

function statusLabel(status) {
  if (status && typeof status === 'object' && status.desc) return status.desc
  const map = { PENDING: '待支付', SUCCESS: '支付成功', FAILED: '支付失败', REFUNDED: '已退款' }
  return map[statusName(status)] || '-'
}

function statusTagType(status) {
  const map = { PENDING: 'warning', SUCCESS: 'success', FAILED: 'danger', REFUNDED: 'info' }
  return map[statusName(status)] || 'info'
}

function formatYuan(amountCents) {
  if (amountCents == null) return '0.00'
  return (Number(amountCents) / 100).toFixed(2)
}

// ───── 列表 ─────
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const query = reactive({ page: 1, size: 20, keyword: '', status: '' })

async function loadData() {
  loading.value = true
  try {
    const res = await wxPayApi.list(query)
    tableData.value = res?.records || []
    total.value = res?.total || 0
  } catch (e) {
    tableData.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

async function refreshPayment(row) {
  try {
    const fresh = await wxPayApi.get(row.id)
    Object.assign(row, fresh)
    ElMessage.success(`状态已更新: ${statusLabel(fresh.status)}`)
  } catch (e) {
    // interceptor toasts the error
  }
}

// ───── 详情抽屉 ─────
const detailVisible = ref(false)
const detail = ref(null)
async function openDetail(row) {
  detailVisible.value = true
  detail.value = row
  try {
    const fresh = await wxPayApi.get(row.id)
    detail.value = fresh
  } catch (e) {
    // keep row as fallback
  }
}

// ───── 创建对话框 ─────
const createVisible = ref(false)
const createSubmitting = ref(false)
const createFormRef = ref(null)
const createForm = reactive({ orderId: 1001, amountYuan: 128.00, description: '' })
const createRules = {
  orderId: [{ required: true, message: '请输入业务订单ID', trigger: 'blur' }],
  amountYuan: [
    { required: true, message: '请输入支付金额', trigger: 'blur' },
    { type: 'number', min: 0.01, message: '金额必须大于 0', trigger: 'blur' },
  ],
}

function openCreateDialog() {
  createForm.orderId = 1001
  createForm.amountYuan = 128.00
  createForm.description = ''
  createVisible.value = true
}

async function submitCreate() {
  const valid = await createFormRef.value.validate().catch(() => false)
  if (!valid) return
  createSubmitting.value = true
  try {
    const payload = {
      orderId: createForm.orderId,
      amountCents: Math.round(createForm.amountYuan * 100),
      description: createForm.description,
    }
    const created = await wxPayApi.create(payload)
    ElMessage.success('已发起 Mock 支付，可在列表中查询状态')
    createVisible.value = false
    await loadData()
    // 自动打开详情抽屉展示二维码占位
    openDetail(created)
  } catch (e) {
    // interceptor toasts the error
  } finally {
    createSubmitting.value = false
  }
}

onMounted(loadData)
</script>

<style scoped>
.wxpay-page {
  padding: var(--space-6);
  background-color: var(--color-surface-base);
  min-height: 100%;
  color: var(--color-text-primary);
  font-family: var(--font-body);
  font-size: var(--font-size-body);
  line-height: var(--line-height-body);
}

.wxpay-page__header {
  margin-bottom: var(--space-5);
}

.wxpay-page__title {
  margin: 0 0 var(--space-2);
  font-size: var(--font-size-h1);
  font-weight: 600;
  color: var(--color-text-primary);
}

.wxpay-page__subtitle {
  margin: 0;
  color: var(--color-text-secondary);
  font-size: var(--font-size-body-s);
}

.wxpay-filter {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-3);
  align-items: center;
  margin-bottom: var(--space-4);
  padding: var(--space-4);
  background-color: var(--color-surface-raised);
  border: 1px solid var(--color-border-default);
  border-radius: var(--radius-md);
}

.wxpay-filter__input {
  width: 260px;
}

.wxpay-filter__select {
  width: 160px;
}

.wxpay-actions {
  display: flex;
  gap: var(--space-2);
}

.wxpay-amount {
  font-family: var(--font-mono);
  font-weight: 500;
  color: var(--color-text-primary);
}

.wxpay-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: var(--space-4);
}

.wxpay-form__full {
  width: 100%;
}

.wxpay-form__hint {
  margin-top: var(--space-1);
  font-size: var(--font-size-caption);
  color: var(--color-text-tertiary);
}

/* ── 详情抽屉 ── */
.wxpay-detail {
  padding: var(--space-2);
}

.wxpay-qr {
  display: flex;
  justify-content: center;
  margin-bottom: var(--space-5);
}

.wxpay-qr__frame {
  padding: var(--space-4);
  background-color: var(--color-surface-raised);
  border: 1px solid var(--color-border-default);
  border-radius: var(--radius-md);
  text-align: center;
}

.wxpay-qr__mask {
  width: 180px;
  height: 180px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--color-surface-sunken);
  border-radius: var(--radius-base);
  color: var(--color-text-secondary);
}

.wxpay-qr__icon {
  width: 120px;
  height: 120px;
}

.wxpay-qr__caption {
  margin-top: var(--space-2);
  color: var(--color-text-tertiary);
  font-size: var(--font-size-caption);
}

.wxpay-detail__list {
  margin: 0;
}

.wxpay-detail__row {
  display: grid;
  grid-template-columns: calc(var(--space-8) * 3 + var(--space-3)) 1fr;
  gap: var(--space-3);
  padding: var(--space-3) 0;
  border-bottom: 1px solid var(--color-divider);
}

.wxpay-detail__row:last-child {
  border-bottom: none;
}

.wxpay-detail__row dt {
  color: var(--color-text-secondary);
  font-size: var(--font-size-body-s);
}

.wxpay-detail__row dd {
  margin: 0;
  color: var(--color-text-primary);
  word-break: break-all;
}

/* ── a11y ── */
:deep(.el-button:focus-visible),
:deep(.el-input__wrapper:focus-within) {
  box-shadow: var(--shadow-focus);
}

@media (prefers-reduced-motion: reduce) {
  .wxpay-page,
  .wxpay-page * {
    transition-duration: 0ms;
    animation-duration: 0ms;
  }
}
</style>
