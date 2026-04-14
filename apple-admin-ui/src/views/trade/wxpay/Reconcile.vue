<template>
  <div class="wxpay-page">
    <div class="wxpay-page__header">
      <h2 class="wxpay-page__title">退款与对账</h2>
      <p class="wxpay-page__subtitle">演示原型 · Mock 微信退款立即置成功，对账单按日聚合</p>
    </div>

    <div class="wxpay-filter">
      <span class="wxpay-filter__label">对账日期</span>
      <el-date-picker
        v-model="query.date"
        type="date"
        placeholder="选择日期"
        value-format="YYYY-MM-DD"
        class="wxpay-filter__datepicker"
        :clearable="false"
      />
      <el-button type="primary" :icon="Search" @click="loadReconcile">查询对账单</el-button>
    </div>

    <!-- ── Summary cards ── -->
    <div v-if="reconcile" class="wxpay-summary">
      <div class="wxpay-summary__card">
        <div class="wxpay-summary__label">支付笔数</div>
        <div class="wxpay-summary__value nums-tabular">{{ reconcile.paymentCount }}</div>
      </div>
      <div class="wxpay-summary__card">
        <div class="wxpay-summary__label">支付总额</div>
        <div class="wxpay-summary__value wxpay-summary__value--primary nums-tabular">
          ¥{{ formatYuan(reconcile.paymentTotalCents) }}
        </div>
      </div>
      <div class="wxpay-summary__card">
        <div class="wxpay-summary__label">退款笔数</div>
        <div class="wxpay-summary__value nums-tabular">{{ reconcile.refundCount }}</div>
      </div>
      <div class="wxpay-summary__card">
        <div class="wxpay-summary__label">退款总额</div>
        <div class="wxpay-summary__value wxpay-summary__value--warning nums-tabular">
          ¥{{ formatYuan(reconcile.refundTotalCents) }}
        </div>
      </div>
      <div class="wxpay-summary__card">
        <div class="wxpay-summary__label">净收入</div>
        <div class="wxpay-summary__value nums-tabular">
          ¥{{ formatYuan(reconcile.netAmountCents) }}
        </div>
      </div>
    </div>

    <!-- ── Payments 列表 + 退款入口 ── -->
    <div class="wxpay-section">
      <div class="wxpay-section__header">
        <h3 class="wxpay-section__title">当日成功支付</h3>
        <span class="wxpay-section__count">共 {{ paymentsRows.length }} 条</span>
      </div>

      <el-table
        :data="paymentsPageSlice"
        v-loading="loading"
        stripe
        border
        empty-text="当日暂无成功支付记录"
      >
        <el-table-column prop="outTradeNo" label="商户订单号" width="240" class-name="nums-tabular" />
        <el-table-column label="金额" width="130" class-name="nums-tabular">
          <template v-slot:default="{ row }">
            <span class="wxpay-amount">¥{{ formatYuan(row.amountCents) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="transactionId" label="微信交易号" width="220" class-name="nums-tabular" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="payTime" label="支付时间" width="180" />
        <el-table-column label="操作" width="140" fixed="right">
          <template v-slot:default="{ row }">
            <el-button
              link
              type="danger"
              size="small"
              @click="openRefundDialog(row)"
            >发起退款</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-if="paymentsRows.length > paymentsPageSize"
        class="wxpay-pagination"
        background
        layout="total, prev, pager, next"
        :total="paymentsRows.length"
        :page-size="paymentsPageSize"
        :current-page="paymentsPage"
        @current-change="(p) => paymentsPage = p"
      />
    </div>

    <!-- ── Refunds 列表 ── -->
    <div class="wxpay-section">
      <div class="wxpay-section__header">
        <h3 class="wxpay-section__title">当日成功退款</h3>
        <span class="wxpay-section__count">共 {{ refundsRows.length }} 条</span>
      </div>

      <el-table
        :data="refundsPageSlice"
        stripe
        border
        empty-text="当日暂无退款记录"
      >
        <el-table-column prop="outRefundNo" label="商户退款单号" width="240" class-name="nums-tabular" />
        <el-table-column prop="paymentId" label="支付ID" width="160" class-name="nums-tabular" />
        <el-table-column label="退款金额" width="130" class-name="nums-tabular">
          <template v-slot:default="{ row }">
            <span class="wxpay-amount">¥{{ formatYuan(row.refundAmountCents) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="退款原因" min-width="220" show-overflow-tooltip />
        <el-table-column prop="refundId" label="微信退款号" width="220" class-name="nums-tabular" />
        <el-table-column prop="refundTime" label="退款时间" width="180" />
      </el-table>

      <el-pagination
        v-if="refundsRows.length > refundsPageSize"
        class="wxpay-pagination"
        background
        layout="total, prev, pager, next"
        :total="refundsRows.length"
        :page-size="refundsPageSize"
        :current-page="refundsPage"
        @current-change="(p) => refundsPage = p"
      />
    </div>

    <!-- 退款对话框（带二次确认） -->
    <el-dialog
      v-model="refundVisible"
      title="发起 Mock 退款"
      width="480px"
      :close-on-click-modal="false"
    >
      <el-alert
        type="warning"
        :closable="false"
        show-icon
        title="演示原型：Mock 退款将立即置成功，原支付记录变为已退款状态"
        class="wxpay-dialog__alert"
      />
      <el-form
        v-if="refundForm.payment"
        ref="refundFormRef"
        :model="refundForm"
        :rules="refundRules"
        label-width="110px"
      >
        <el-form-item label="原支付单号">
          <span class="nums-tabular">{{ refundForm.payment.outTradeNo }}</span>
        </el-form-item>
        <el-form-item label="原支付金额">
          <span class="nums-tabular wxpay-amount">
            ¥{{ formatYuan(refundForm.payment.amountCents) }}
          </span>
        </el-form-item>
        <el-form-item label="退款金额（元）" prop="refundYuan">
          <el-input-number
            v-model="refundForm.refundYuan"
            :min="0.01"
            :max="(refundForm.payment.amountCents || 0) / 100"
            :precision="2"
            :step="1"
            :controls="false"
            class="wxpay-form__full"
          />
        </el-form-item>
        <el-form-item label="退款原因" prop="reason">
          <el-input
            v-model="refundForm.reason"
            type="textarea"
            :rows="3"
            placeholder="请填写退款原因（必填）"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="refundVisible = false">取消</el-button>
        <el-button
          type="danger"
          :loading="refundSubmitting"
          @click="confirmRefund"
        >确认退款</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { wxRefundApi, wxReconcileApi } from '@/api/wxpay.js'

// ───── Date helpers ─────
function todayIso() {
  const d = new Date()
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

function formatYuan(amountCents) {
  if (amountCents == null) return '0.00'
  return (Number(amountCents) / 100).toFixed(2)
}

// ───── Reconcile query ─────
const loading = ref(false)
const query = reactive({ date: todayIso() })
const reconcile = ref(null)

const paymentsRows = computed(() => reconcile.value?.payments || [])
const refundsRows = computed(() => reconcile.value?.refunds || [])

const paymentsPage = ref(1)
const paymentsPageSize = 10
const refundsPage = ref(1)
const refundsPageSize = 10

const paymentsPageSlice = computed(() =>
  paymentsRows.value.slice((paymentsPage.value - 1) * paymentsPageSize, paymentsPage.value * paymentsPageSize)
)
const refundsPageSlice = computed(() =>
  refundsRows.value.slice((refundsPage.value - 1) * refundsPageSize, refundsPage.value * refundsPageSize)
)

async function loadReconcile() {
  loading.value = true
  try {
    const res = await wxReconcileApi.byDate(query.date)
    reconcile.value = res
    paymentsPage.value = 1
    refundsPage.value = 1
  } catch (e) {
    reconcile.value = null
  } finally {
    loading.value = false
  }
}

// ───── Refund dialog ─────
const refundVisible = ref(false)
const refundSubmitting = ref(false)
const refundFormRef = ref(null)
const refundForm = reactive({
  payment: null,
  refundYuan: 0,
  reason: '',
})

const refundRules = {
  refundYuan: [
    { required: true, message: '请输入退款金额', trigger: 'blur' },
    { type: 'number', min: 0.01, message: '退款金额必须大于 0', trigger: 'blur' },
  ],
  reason: [{ required: true, message: '请填写退款原因', trigger: 'blur' }],
}

function openRefundDialog(row) {
  refundForm.payment = row
  refundForm.refundYuan = Number((row.amountCents / 100).toFixed(2))
  refundForm.reason = ''
  refundVisible.value = true
}

async function confirmRefund() {
  const valid = await refundFormRef.value.validate().catch(() => false)
  if (!valid) return

  // 二次确认（必需）
  try {
    await ElMessageBox.confirm(
      `确认对订单 ${refundForm.payment.outTradeNo} 退款 ¥${refundForm.refundYuan.toFixed(2)}？退款后原支付将置为"已退款"状态。`,
      '二次确认',
      {
        type: 'warning',
        confirmButtonText: '确认退款',
        cancelButtonText: '再想想',
      }
    )
  } catch {
    return
  }

  refundSubmitting.value = true
  try {
    await wxRefundApi.create({
      paymentId: refundForm.payment.id,
      refundAmountCents: Math.round(refundForm.refundYuan * 100),
      reason: refundForm.reason,
    })
    ElMessage.success('退款已完成')
    refundVisible.value = false
    await loadReconcile()
  } catch (e) {
    // interceptor toasts the error
  } finally {
    refundSubmitting.value = false
  }
}

onMounted(loadReconcile)
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

.wxpay-filter__label {
  color: var(--color-text-secondary);
  font-size: var(--font-size-body-s);
}

.wxpay-filter__datepicker {
  width: 200px;
}

.wxpay-summary {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(calc(var(--space-8) * 5 + var(--space-5)), 1fr));
  gap: var(--space-4);
  margin-bottom: var(--space-5);
}

.wxpay-summary__card {
  padding: var(--space-4);
  background-color: var(--color-surface-raised);
  border: 1px solid var(--color-border-default);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-1);
  transition: box-shadow var(--motion-fast) var(--ease-out), border-color var(--motion-fast) var(--ease-out);
}

.wxpay-summary__card:hover {
  border-color: var(--color-border-strong);
  box-shadow: var(--shadow-2);
}

.wxpay-summary__label {
  color: var(--color-text-secondary);
  font-size: var(--font-size-body-s);
  margin-bottom: var(--space-2);
}

.wxpay-summary__value {
  font-family: var(--font-mono);
  font-size: var(--font-size-number-l);
  font-weight: 500;
  color: var(--color-text-primary);
}

.wxpay-summary__value--primary {
  color: var(--color-brand-primary);
}

.wxpay-summary__value--warning {
  color: var(--color-warning);
}

.wxpay-section {
  margin-bottom: var(--space-6);
  padding: var(--space-4);
  background-color: var(--color-surface-raised);
  border: 1px solid var(--color-border-default);
  border-radius: var(--radius-md);
}

.wxpay-section__header {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-bottom: var(--space-3);
}

.wxpay-section__title {
  margin: 0;
  font-size: var(--font-size-h2);
  font-weight: 600;
  color: var(--color-text-primary);
}

.wxpay-section__count {
  color: var(--color-text-tertiary);
  font-size: var(--font-size-body-s);
}

.wxpay-amount {
  font-family: var(--font-mono);
  font-weight: 500;
}

.wxpay-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: var(--space-4);
}

.wxpay-dialog__alert {
  margin-bottom: var(--space-4);
}

.wxpay-form__full {
  width: 100%;
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
