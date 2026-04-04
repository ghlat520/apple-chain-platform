<template>
  <div class="loans-page">
    <van-search v-model="query.keyword" placeholder="搜索贷款编号/借款人" @search="handleSearch" @clear="handleSearch" />
    <div class="filter-bar">
      <van-tabs v-model:active="query.status" @change="handleSearch" scrollable>
        <van-tab title="全部" name="" />
        <van-tab title="待审批" name="PENDING" />
        <van-tab title="已审批" name="APPROVED" />
        <van-tab title="已放款" name="DISBURSED" />
        <van-tab title="已还清" name="REPAID" />
        <van-tab title="已拒绝" name="REJECTED" />
      </van-tabs>
    </div>
    <div class="action-bar">
      <van-button type="primary" size="small" icon="plus" @click="openForm()">新增贷款</van-button>
      <van-button type="default" size="small" icon="down" @click="handleExport">导出CSV</van-button>
    </div>

    <van-pull-refresh v-model="refreshing" @refresh="loadList">
      <van-list v-model:loading="loading" :finished="finished" finished-text="没有更多了" @load="loadMore">
        <van-cell-group inset style="margin-bottom:8px" v-for="item in list" :key="item.id">
          <van-cell :title="item.loanCode" :label="`${item.borrowerName} · ${loanTypeLabel(item.loanType)} · ¥${item.amount}`" is-link @click="openDetail(item)">
            <template #value><van-tag :type="statusTagType(item.status)">{{ statusLabel(item.status) }}</van-tag></template>
          </van-cell>
        </van-cell-group>
        <van-empty v-if="!loading && list.length === 0" description="暂无贷款记录" />
      </van-list>
    </van-pull-refresh>

    <van-popup v-model:show="showDetail" position="bottom" round :style="{ maxHeight: '85vh', overflowY: 'auto' }">
      <div class="drawer-header"><span>贷款详情</span><van-icon name="cross" @click="showDetail = false" /></div>
      <van-cell-group inset v-if="currentItem">
        <van-cell title="贷款编号" :value="currentItem.loanCode" />
        <van-cell title="借款人" :value="currentItem.borrowerName" />
        <van-cell title="借款人类型" :value="entityTypeLabel(currentItem.borrowerType)" />
        <van-cell title="贷款类型" :value="loanTypeLabel(currentItem.loanType)" />
        <van-cell title="金额(元)" :value="`¥${currentItem.amount}`" />
        <van-cell title="年利率" :value="`${(currentItem.interestRate * 100).toFixed(2)}%`" />
        <van-cell title="期限(月)" :value="String(currentItem.termMonths)" />
        <van-cell title="申请日期" :value="currentItem.applyDate || '-'" />
        <van-cell title="审批日期" :value="currentItem.approveDate || '-'" />
        <van-cell title="放款日期" :value="currentItem.disburseDate || '-'" />
        <van-cell title="到期日期" :value="currentItem.dueDate || '-'" />
        <van-cell title="已还金额" :value="`¥${currentItem.repaidAmount || 0}`" />
        <van-cell title="状态"><template #value><van-tag :type="statusTagType(currentItem.status)">{{ statusLabel(currentItem.status) }}</van-tag></template></van-cell>
      </van-cell-group>
      <div class="drawer-actions">
        <van-button block type="success" plain v-if="currentItem?.status === 'PENDING'" @click="handleApprove">审批通过</van-button>
        <van-button block type="danger" plain v-if="currentItem?.status === 'PENDING'" style="margin-top:8px" @click="handleReject">审批拒绝</van-button>
        <van-button block type="primary" plain v-if="currentItem?.status === 'APPROVED'" style="margin-top:8px" @click="handleDisburse">放款</van-button>
        <van-button block type="primary" plain style="margin-top:8px" @click="openForm(currentItem)">编辑</van-button>
        <van-button block type="danger" plain style="margin-top:8px" @click="handleDelete(currentItem)">删除</van-button>
      </div>
    </van-popup>

    <van-popup v-model:show="showForm" position="bottom" round :style="{ maxHeight: '85vh', overflowY: 'auto' }">
      <div class="drawer-header"><span>{{ formData.id ? '编辑贷款' : '新增贷款' }}</span><van-icon name="cross" @click="showForm = false" /></div>
      <van-form @submit="handleSubmit">
        <van-cell-group inset>
          <van-field v-model="formData.borrowerName" label="借款人" required :rules="[{required:true,message:'请输入借款人'}]" />
          <van-field v-model="formData.borrowerType" label="借款人类型" is-link readonly @click="showBorrowerPicker = true" :formatter="entityTypeLabel" />
          <van-field v-model="formData.loanType" label="贷款类型" is-link readonly @click="showLoanTypePicker = true" :formatter="loanTypeLabel" />
          <van-field v-model="formData.amount" label="金额(元)" type="number" required :rules="[{required:true,message:'请输入金额'}]" />
          <van-field v-model="formData.interestRate" label="年利率" type="number" required :rules="[{required:true,message:'请输入利率'}]" />
          <van-field v-model="formData.termMonths" label="期限(月)" type="number" required :rules="[{required:true,message:'请输入期限'}]" />
          <van-field v-model="formData.remark" label="备注" type="textarea" rows="2" />
        </van-cell-group>
        <div style="padding:16px"><van-button block type="primary" native-type="submit">保存</van-button></div>
      </van-form>
    </van-popup>

    <van-popup v-model:show="showBorrowerPicker" position="bottom" round>
      <van-picker :columns="borrowerOptions" @confirm="({selectedValues}) => { formData.borrowerType = selectedValues[0]; showBorrowerPicker = false }" @cancel="showBorrowerPicker = false" />
    </van-popup>
    <van-popup v-model:show="showLoanTypePicker" position="bottom" round>
      <van-picker :columns="loanTypeOptions" @confirm="({selectedValues}) => { formData.loanType = selectedValues[0]; showLoanTypePicker = false }" @cancel="showLoanTypePicker = false" />
    </van-popup>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { financeApi } from '@/api/finance.js'
import { showToast, showConfirmDialog } from 'vant'

const list = ref([]); const loading = ref(false); const finished = ref(false); const refreshing = ref(false)
const showDetail = ref(false); const showForm = ref(false); const showBorrowerPicker = ref(false); const showLoanTypePicker = ref(false)
const currentItem = ref(null)
const query = reactive({ keyword: '', status: '', page: 1, size: 10 })
const formData = reactive({ id: null, borrowerName: '', borrowerType: 'FARMER', loanType: 'CREDIT', amount: '', interestRate: '', termMonths: '', remark: '' })
const borrowerOptions = [{ text: '农户', value: 'FARMER' }, { text: '企业', value: 'ENTERPRISE' }, { text: '合作社', value: 'COOPERATIVE' }]
const loanTypeOptions = [{ text: '仓单质押', value: 'PLEDGE' }, { text: '应收账款', value: 'RECEIVABLE' }, { text: '信用贷', value: 'CREDIT' }]

function entityTypeLabel(t) { return { FARMER: '农户', ENTERPRISE: '企业', COOPERATIVE: '合作社' }[t] || t || '-' }
function loanTypeLabel(t) { return { PLEDGE: '仓单质押', RECEIVABLE: '应收账款', CREDIT: '信用贷' }[t] || t || '-' }
function statusTagType(s) { return { PENDING: 'warning', APPROVED: 'primary', REJECTED: 'danger', DISBURSED: 'success', REPAID: 'default', OVERDUE: 'danger' }[s] || 'default' }
function statusLabel(s) { return { PENDING: '待审批', APPROVED: '已审批', REJECTED: '已拒绝', DISBURSED: '已放款', REPAID: '已还清', OVERDUE: '已逾期' }[s] || s || '-' }

async function loadList() { loading.value = true; try { const res = await financeApi.getLoans({ ...query, page: 1 }); list.value = res?.records || []; finished.value = list.value.length >= (res?.total || 0); query.page = 1 } finally { loading.value = false; refreshing.value = false } }
async function loadMore() { if (finished.value) return; query.page += 1; try { const res = await financeApi.getLoans({ ...query }); const records = res?.records || []; list.value = [...list.value, ...records]; if (list.value.length >= (res?.total || 0) || records.length < query.size) finished.value = true } finally { loading.value = false } }
function handleSearch() { query.page = 1; finished.value = false; list.value = []; loadList() }
function openDetail(item) { currentItem.value = item; showDetail.value = true }
function openForm(item) { showDetail.value = false; if (item) Object.assign(formData, item); else { Object.keys(formData).forEach(k => formData[k] = k === 'id' ? null : ''); formData.borrowerType = 'FARMER'; formData.loanType = 'CREDIT' }; showForm.value = true }

async function handleSubmit() {
  try { if (formData.id) await financeApi.updateLoan(formData.id, { ...formData }); else await financeApi.createLoan({ ...formData, applyDate: new Date().toISOString().substring(0,10) }); showToast('保存成功'); showForm.value = false; handleSearch() }
  catch (e) { showToast('保存失败') }
}
async function handleApprove() { try { await showConfirmDialog({ title: '审批确认', message: '确定通过该贷款申请？' }); await financeApi.approveLoan(currentItem.value.id); showToast('审批通过'); showDetail.value = false; handleSearch() } catch (e) { if (e !== 'cancel') showToast('操作失败') } }
async function handleReject() { try { await showConfirmDialog({ title: '审批拒绝', message: '确定拒绝该贷款申请？' }); await financeApi.rejectLoan(currentItem.value.id); showToast('已拒绝'); showDetail.value = false; handleSearch() } catch (e) { if (e !== 'cancel') showToast('操作失败') } }
async function handleDisburse() { try { await showConfirmDialog({ title: '放款确认', message: '确定放款？' }); await financeApi.disburseLoan(currentItem.value.id); showToast('放款成功'); showDetail.value = false; handleSearch() } catch (e) { if (e !== 'cancel') showToast('操作失败') } }
async function handleDelete(item) { try { await showConfirmDialog({ title: '确认删除', message: `确定删除贷款 ${item.loanCode}？` }); await financeApi.deleteLoan(item.id); showToast('删除成功'); showDetail.value = false; handleSearch() } catch (e) { if (e !== 'cancel') showToast('删除失败') } }
function handleExport() { financeApi.exportLoans() }
onMounted(loadList)
</script>

<style scoped>
.loans-page { padding-bottom: 20px; } .filter-bar { background: #fff; margin-bottom: 8px; } .action-bar { display: flex; gap: 8px; padding: 8px 16px; }
.drawer-header { display: flex; justify-content: space-between; align-items: center; padding: 16px; font-size: 16px; font-weight: 600; border-bottom: 1px solid #ebedf0; } .drawer-actions { padding: 16px; }
</style>
