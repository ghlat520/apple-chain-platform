<template>
  <div class="risks-page">
    <van-search v-model="query.keyword" placeholder="搜索风险描述" @search="handleSearch" @clear="handleSearch" />
    <div class="filter-bar">
      <van-tabs v-model:active="query.status" @change="handleSearch" scrollable>
        <van-tab title="全部" name="" />
        <van-tab title="待处理" name="OPEN" />
        <van-tab title="处理中" name="HANDLING" />
        <van-tab title="已解决" name="RESOLVED" />
        <van-tab title="已关闭" name="CLOSED" />
      </van-tabs>
    </div>
    <div class="action-bar">
      <van-button type="primary" size="small" icon="plus" @click="openForm()">新增记录</van-button>
      <van-button type="default" size="small" icon="down" @click="handleExport">导出CSV</van-button>
    </div>

    <van-pull-refresh v-model="refreshing" @refresh="loadList">
      <van-list v-model:loading="loading" :finished="finished" finished-text="没有更多了" @load="loadMore">
        <van-cell-group inset style="margin-bottom:8px" v-for="item in list" :key="item.id">
          <van-cell :title="item.riskCode" :label="item.description" is-link @click="openDetail(item)">
            <template #value>
              <van-tag :type="levelTagType(item.riskLevel)">{{ levelLabel(item.riskLevel) }}</van-tag>
              <van-tag :type="statusTagType(item.status)" style="margin-left:4px">{{ statusLabel(item.status) }}</van-tag>
            </template>
          </van-cell>
        </van-cell-group>
        <van-empty v-if="!loading && list.length === 0" description="暂无风控记录" />
      </van-list>
    </van-pull-refresh>

    <van-popup v-model:show="showDetail" position="bottom" round :style="{ maxHeight: '85vh', overflowY: 'auto' }">
      <div class="drawer-header"><span>风控详情</span><van-icon name="cross" @click="showDetail = false" /></div>
      <van-cell-group inset v-if="currentItem">
        <van-cell title="风控编号" :value="currentItem.riskCode" />
        <van-cell title="关联类型" :value="relatedTypeLabel(currentItem.relatedType)" />
        <van-cell title="关联ID" :value="String(currentItem.relatedId || '-')" />
        <van-cell title="风险等级"><template #value><van-tag :type="levelTagType(currentItem.riskLevel)">{{ levelLabel(currentItem.riskLevel) }}</van-tag></template></van-cell>
        <van-cell title="风险类型" :value="riskTypeLabel(currentItem.riskType)" />
        <van-cell title="风险描述" :label="currentItem.description" />
        <van-cell title="处置措施" :label="currentItem.measure || '-'" />
        <van-cell title="处理人" :value="currentItem.handler || '-'" />
        <van-cell title="处理时间" :value="currentItem.handleTime ? currentItem.handleTime.replace('T',' ').substring(0,16) : '-'" />
        <van-cell title="状态"><template #value><van-tag :type="statusTagType(currentItem.status)">{{ statusLabel(currentItem.status) }}</van-tag></template></van-cell>
      </van-cell-group>
      <div class="drawer-actions">
        <van-button block type="primary" plain @click="openForm(currentItem)">编辑</van-button>
        <van-button block type="danger" plain style="margin-top:8px" @click="handleDelete(currentItem)">删除</van-button>
      </div>
    </van-popup>

    <van-popup v-model:show="showForm" position="bottom" round :style="{ maxHeight: '85vh', overflowY: 'auto' }">
      <div class="drawer-header"><span>{{ formData.id ? '编辑记录' : '新增记录' }}</span><van-icon name="cross" @click="showForm = false" /></div>
      <van-form @submit="handleSubmit">
        <van-cell-group inset>
          <van-field v-model="formData.relatedType" label="关联类型" is-link readonly @click="showRelatedPicker = true" :formatter="relatedTypeLabel" />
          <van-field v-model="formData.relatedId" label="关联ID" type="number" />
          <van-field v-model="formData.riskLevel" label="风险等级" is-link readonly @click="showLevelPicker = true" :formatter="levelLabel" />
          <van-field v-model="formData.riskType" label="风险类型" is-link readonly @click="showRiskTypePicker = true" :formatter="riskTypeLabel" />
          <van-field v-model="formData.description" label="风险描述" required type="textarea" rows="2" :rules="[{required:true,message:'请输入描述'}]" />
          <van-field v-model="formData.measure" label="处置措施" type="textarea" rows="2" />
          <van-field v-model="formData.handler" label="处理人" />
          <van-field v-model="formData.remark" label="备注" type="textarea" rows="2" />
        </van-cell-group>
        <div style="padding:16px"><van-button block type="primary" native-type="submit">保存</van-button></div>
      </van-form>
    </van-popup>

    <van-popup v-model:show="showRelatedPicker" position="bottom" round>
      <van-picker :columns="relatedOptions" @confirm="({selectedValues}) => { formData.relatedType = selectedValues[0]; showRelatedPicker = false }" @cancel="showRelatedPicker = false" />
    </van-popup>
    <van-popup v-model:show="showLevelPicker" position="bottom" round>
      <van-picker :columns="levelOptions" @confirm="({selectedValues}) => { formData.riskLevel = selectedValues[0]; showLevelPicker = false }" @cancel="showLevelPicker = false" />
    </van-popup>
    <van-popup v-model:show="showRiskTypePicker" position="bottom" round>
      <van-picker :columns="riskTypeOptions" @confirm="({selectedValues}) => { formData.riskType = selectedValues[0]; showRiskTypePicker = false }" @cancel="showRiskTypePicker = false" />
    </van-popup>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { financeApi } from '@/api/finance.js'
import { showToast, showConfirmDialog } from 'vant'

const list = ref([]); const loading = ref(false); const finished = ref(false); const refreshing = ref(false)
const showDetail = ref(false); const showForm = ref(false); const currentItem = ref(null)
const showRelatedPicker = ref(false); const showLevelPicker = ref(false); const showRiskTypePicker = ref(false)
const query = reactive({ keyword: '', status: '', page: 1, size: 10 })
const formData = reactive({ id: null, relatedType: 'LOAN', relatedId: null, riskLevel: 'MEDIUM', riskType: 'OVERDUE', description: '', measure: '', handler: '', remark: '' })
const relatedOptions = [{ text: '贷款', value: 'LOAN' }, { text: '质押', value: 'PLEDGE' }, { text: '信用', value: 'CREDIT' }]
const levelOptions = [{ text: '高', value: 'HIGH' }, { text: '中', value: 'MEDIUM' }, { text: '低', value: 'LOW' }]
const riskTypeOptions = [{ text: '逾期', value: 'OVERDUE' }, { text: '价格下跌', value: 'PRICE_DROP' }, { text: '质量风险', value: 'QUALITY' }, { text: '欺诈', value: 'FRAUD' }]

function levelTagType(l) { return { HIGH: 'danger', MEDIUM: 'warning', LOW: 'success' }[l] || 'default' }
function levelLabel(l) { return { HIGH: '高', MEDIUM: '中', LOW: '低' }[l] || l || '-' }
function statusTagType(s) { return { OPEN: 'danger', HANDLING: 'warning', RESOLVED: 'success', CLOSED: 'default' }[s] || 'default' }
function statusLabel(s) { return { OPEN: '待处理', HANDLING: '处理中', RESOLVED: '已解决', CLOSED: '已关闭' }[s] || s || '-' }
function relatedTypeLabel(t) { return { LOAN: '贷款', PLEDGE: '质押', CREDIT: '信用' }[t] || t || '-' }
function riskTypeLabel(t) { return { OVERDUE: '逾期', PRICE_DROP: '价格下跌', QUALITY: '质量风险', FRAUD: '欺诈' }[t] || t || '-' }

async function loadList() { loading.value = true; try { const res = await financeApi.getRisks({ ...query, page: 1 }); list.value = res?.records || []; finished.value = list.value.length >= (res?.total || 0); query.page = 1 } finally { loading.value = false; refreshing.value = false } }
async function loadMore() { if (finished.value) return; query.page += 1; try { const res = await financeApi.getRisks({ ...query }); const records = res?.records || []; list.value = [...list.value, ...records]; if (list.value.length >= (res?.total || 0) || records.length < query.size) finished.value = true } finally { loading.value = false } }
function handleSearch() { query.page = 1; finished.value = false; list.value = []; loadList() }
function openDetail(item) { currentItem.value = item; showDetail.value = true }
function openForm(item) { showDetail.value = false; if (item) Object.assign(formData, item); else { Object.keys(formData).forEach(k => formData[k] = k === 'id' || k === 'relatedId' ? null : ''); formData.relatedType = 'LOAN'; formData.riskLevel = 'MEDIUM'; formData.riskType = 'OVERDUE' }; showForm.value = true }
async function handleSubmit() { try { if (formData.id) await financeApi.updateRisk(formData.id, { ...formData }); else await financeApi.createRisk({ ...formData }); showToast('保存成功'); showForm.value = false; handleSearch() } catch (e) { showToast('保存失败') } }
async function handleDelete(item) { try { await showConfirmDialog({ title: '确认删除', message: `确定删除 ${item.riskCode}？` }); await financeApi.deleteRisk(item.id); showToast('删除成功'); showDetail.value = false; handleSearch() } catch (e) { if (e !== 'cancel') showToast('删除失败') } }
function handleExport() { financeApi.exportRisks() }
onMounted(loadList)
</script>

<style scoped>
.risks-page { padding-bottom: 20px; } .filter-bar { background: #fff; margin-bottom: 8px; } .action-bar { display: flex; gap: 8px; padding: 8px 16px; }
.drawer-header { display: flex; justify-content: space-between; align-items: center; padding: 16px; font-size: 16px; font-weight: 600; border-bottom: 1px solid #ebedf0; } .drawer-actions { padding: 16px; }
</style>
