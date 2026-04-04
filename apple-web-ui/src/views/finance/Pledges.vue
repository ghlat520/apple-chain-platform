<template>
  <div class="pledges-page">
    <van-search v-model="query.keyword" placeholder="搜索质押编号/出质人" @search="handleSearch" @clear="handleSearch" />
    <div class="filter-bar">
      <van-tabs v-model:active="query.status" @change="handleSearch" scrollable>
        <van-tab title="全部" name="" />
        <van-tab title="待生效" name="PENDING" />
        <van-tab title="生效中" name="ACTIVE" />
        <van-tab title="已解除" name="RELEASED" />
        <van-tab title="已违约" name="DEFAULTED" />
      </van-tabs>
    </div>
    <div class="action-bar">
      <van-button type="primary" size="small" icon="plus" @click="openForm()">新增质押</van-button>
      <van-button type="default" size="small" icon="down" @click="handleExport">导出CSV</van-button>
    </div>

    <van-pull-refresh v-model="refreshing" @refresh="loadList">
      <van-list v-model:loading="loading" :finished="finished" finished-text="没有更多了" @load="loadMore">
        <van-cell-group inset style="margin-bottom:8px" v-for="item in list" :key="item.id">
          <van-cell :title="item.pledgeCode" :label="`${item.pledgorName} → ${item.pledgeeName} · ${item.commodity || '-'}`" is-link @click="openDetail(item)">
            <template #value><van-tag :type="statusTagType(item.status)">{{ statusLabel(item.status) }}</van-tag></template>
          </van-cell>
        </van-cell-group>
        <van-empty v-if="!loading && list.length === 0" description="暂无质押记录" />
      </van-list>
    </van-pull-refresh>

    <van-popup v-model:show="showDetail" position="bottom" round :style="{ maxHeight: '85vh', overflowY: 'auto' }">
      <div class="drawer-header"><span>质押详情</span><van-icon name="cross" @click="showDetail = false" /></div>
      <van-cell-group inset v-if="currentItem">
        <van-cell title="质押编号" :value="currentItem.pledgeCode" />
        <van-cell title="仓单编码" :value="currentItem.receiptCode || '-'" />
        <van-cell title="出质人" :value="currentItem.pledgorName" />
        <van-cell title="质权人" :value="currentItem.pledgeeName" />
        <van-cell title="质押物" :value="currentItem.commodity || '-'" />
        <van-cell title="数量" :value="`${currentItem.quantity || '-'} ${currentItem.unit || ''}`" />
        <van-cell title="评估价值" :value="currentItem.appraisedValue ? `¥${currentItem.appraisedValue}` : '-'" />
        <van-cell title="质押率" :value="currentItem.pledgeRate ? `${(currentItem.pledgeRate * 100).toFixed(1)}%` : '-'" />
        <van-cell title="可贷金额" :value="currentItem.loanAmount ? `¥${currentItem.loanAmount}` : '-'" />
        <van-cell title="起始日" :value="currentItem.startDate || '-'" />
        <van-cell title="到期日" :value="currentItem.endDate || '-'" />
        <van-cell title="状态"><template #value><van-tag :type="statusTagType(currentItem.status)">{{ statusLabel(currentItem.status) }}</van-tag></template></van-cell>
      </van-cell-group>
      <div class="drawer-actions">
        <van-button block type="success" plain v-if="currentItem?.status === 'PENDING'" @click="handleActivate">激活质押</van-button>
        <van-button block type="warning" plain v-if="currentItem?.status === 'ACTIVE'" style="margin-top:8px" @click="handleRelease">解除质押</van-button>
        <van-button block type="primary" plain style="margin-top:8px" @click="openForm(currentItem)">编辑</van-button>
        <van-button block type="danger" plain style="margin-top:8px" @click="handleDelete(currentItem)">删除</van-button>
      </div>
    </van-popup>

    <van-popup v-model:show="showForm" position="bottom" round :style="{ maxHeight: '85vh', overflowY: 'auto' }">
      <div class="drawer-header"><span>{{ formData.id ? '编辑质押' : '新增质押' }}</span><van-icon name="cross" @click="showForm = false" /></div>
      <van-form @submit="handleSubmit">
        <van-cell-group inset>
          <van-field v-model="formData.pledgorName" label="出质人" required :rules="[{required:true,message:'请输入出质人'}]" />
          <van-field v-model="formData.pledgeeName" label="质权人" required :rules="[{required:true,message:'请输入质权人'}]" />
          <van-field v-model="formData.receiptCode" label="仓单编码" />
          <van-field v-model="formData.commodity" label="质押物品" />
          <van-field v-model="formData.quantity" label="数量" type="number" />
          <van-field v-model="formData.unit" label="单位" />
          <van-field v-model="formData.appraisedValue" label="评估价值(元)" type="number" />
          <van-field v-model="formData.pledgeRate" label="质押率" type="number" />
          <van-field v-model="formData.remark" label="备注" type="textarea" rows="2" />
        </van-cell-group>
        <div style="padding:16px"><van-button block type="primary" native-type="submit">保存</van-button></div>
      </van-form>
    </van-popup>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { financeApi } from '@/api/finance.js'
import { showToast, showConfirmDialog } from 'vant'

const list = ref([]); const loading = ref(false); const finished = ref(false); const refreshing = ref(false)
const showDetail = ref(false); const showForm = ref(false); const currentItem = ref(null)
const query = reactive({ keyword: '', status: '', page: 1, size: 10 })
const formData = reactive({ id: null, pledgorName: '', pledgeeName: '', receiptCode: '', commodity: '', quantity: '', unit: '', appraisedValue: '', pledgeRate: '', remark: '' })

function statusTagType(s) { return { PENDING: 'warning', ACTIVE: 'success', RELEASED: 'default', DEFAULTED: 'danger' }[s] || 'default' }
function statusLabel(s) { return { PENDING: '待生效', ACTIVE: '生效中', RELEASED: '已解除', DEFAULTED: '已违约' }[s] || s || '-' }

async function loadList() { loading.value = true; try { const res = await financeApi.getPledges({ ...query, page: 1 }); list.value = res?.records || []; finished.value = list.value.length >= (res?.total || 0); query.page = 1 } finally { loading.value = false; refreshing.value = false } }
async function loadMore() { if (finished.value) return; query.page += 1; try { const res = await financeApi.getPledges({ ...query }); const records = res?.records || []; list.value = [...list.value, ...records]; if (list.value.length >= (res?.total || 0) || records.length < query.size) finished.value = true } finally { loading.value = false } }
function handleSearch() { query.page = 1; finished.value = false; list.value = []; loadList() }
function openDetail(item) { currentItem.value = item; showDetail.value = true }
function openForm(item) { showDetail.value = false; if (item) Object.assign(formData, item); else Object.keys(formData).forEach(k => formData[k] = k === 'id' ? null : ''); showForm.value = true }

async function handleSubmit() { try { if (formData.id) await financeApi.updatePledge(formData.id, { ...formData }); else await financeApi.createPledge({ ...formData }); showToast('保存成功'); showForm.value = false; handleSearch() } catch (e) { showToast('保存失败') } }
async function handleActivate() { try { await showConfirmDialog({ title: '激活质押', message: '确定激活该质押？' }); await financeApi.activatePledge(currentItem.value.id); showToast('激活成功'); showDetail.value = false; handleSearch() } catch (e) { if (e !== 'cancel') showToast('操作失败') } }
async function handleRelease() { try { await showConfirmDialog({ title: '解除质押', message: '确定解除该质押？' }); await financeApi.releasePledge(currentItem.value.id); showToast('已解除'); showDetail.value = false; handleSearch() } catch (e) { if (e !== 'cancel') showToast('操作失败') } }
async function handleDelete(item) { try { await showConfirmDialog({ title: '确认删除', message: `确定删除质押 ${item.pledgeCode}？` }); await financeApi.deletePledge(item.id); showToast('删除成功'); showDetail.value = false; handleSearch() } catch (e) { if (e !== 'cancel') showToast('删除失败') } }
function handleExport() { financeApi.exportPledges() }
onMounted(loadList)
</script>

<style scoped>
.pledges-page { padding-bottom: 20px; } .filter-bar { background: #fff; margin-bottom: 8px; } .action-bar { display: flex; gap: 8px; padding: 8px 16px; }
.drawer-header { display: flex; justify-content: space-between; align-items: center; padding: 16px; font-size: 16px; font-weight: 600; border-bottom: 1px solid #ebedf0; } .drawer-actions { padding: 16px; }
</style>
