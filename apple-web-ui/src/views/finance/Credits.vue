<template>
  <div class="credits-page">
    <van-search v-model="query.keyword" placeholder="搜索主体名称" @search="handleSearch" @clear="handleSearch" />
    <div class="filter-bar">
      <van-tabs v-model:active="query.entityType" @change="handleSearch" scrollable>
        <van-tab title="全部" name="" />
        <van-tab title="农户" name="FARMER" />
        <van-tab title="企业" name="ENTERPRISE" />
        <van-tab title="合作社" name="COOPERATIVE" />
      </van-tabs>
    </div>
    <div class="action-bar">
      <van-button type="primary" size="small" icon="plus" @click="openForm()">新增评级</van-button>
      <van-button type="default" size="small" icon="down" @click="handleExport">导出CSV</van-button>
    </div>

    <van-pull-refresh v-model="refreshing" @refresh="loadList">
      <van-list v-model:loading="loading" :finished="finished" finished-text="没有更多了" @load="loadMore">
        <van-cell-group inset style="margin-bottom:8px" v-for="item in list" :key="item.id">
          <van-cell :title="item.entityName" :label="`${entityTypeLabel(item.entityType)} · 评分: ${item.creditScore} · 评定: ${item.assessmentDate}`" is-link @click="openDetail(item)">
            <template #value>
              <van-tag :type="levelTagType(item.creditLevel)" size="large">{{ item.creditLevel }}</van-tag>
            </template>
          </van-cell>
        </van-cell-group>
        <van-empty v-if="!loading && list.length === 0" description="暂无信用评级" />
      </van-list>
    </van-pull-refresh>

    <van-popup v-model:show="showDetail" position="bottom" round :style="{ maxHeight: '85vh', overflowY: 'auto' }">
      <div class="drawer-header"><span>评级详情</span><van-icon name="cross" @click="showDetail = false" /></div>
      <van-cell-group inset v-if="currentItem">
        <van-cell title="主体类型" :value="entityTypeLabel(currentItem.entityType)" />
        <van-cell title="主体名称" :value="currentItem.entityName" />
        <van-cell title="信用评分" :value="String(currentItem.creditScore)" />
        <van-cell title="信用等级"><template #value><van-tag :type="levelTagType(currentItem.creditLevel)" size="large">{{ currentItem.creditLevel }}</van-tag></template></van-cell>
        <van-cell title="交易信用分" :value="String(currentItem.tradeScore)" />
        <van-cell title="生产信用分" :value="String(currentItem.productionScore)" />
        <van-cell title="财务信用分" :value="String(currentItem.financialScore)" />
        <van-cell title="评定日期" :value="currentItem.assessmentDate || '-'" />
        <van-cell title="有效期至" :value="currentItem.validUntil || '-'" />
        <van-cell title="评定人" :value="currentItem.assessor || '-'" />
      </van-cell-group>
      <div class="drawer-actions">
        <van-button block type="primary" plain @click="openForm(currentItem)">编辑</van-button>
        <van-button block type="danger" plain style="margin-top:8px" @click="handleDelete(currentItem)">删除</van-button>
      </div>
    </van-popup>

    <van-popup v-model:show="showForm" position="bottom" round :style="{ maxHeight: '85vh', overflowY: 'auto' }">
      <div class="drawer-header"><span>{{ formData.id ? '编辑评级' : '新增评级' }}</span><van-icon name="cross" @click="showForm = false" /></div>
      <van-form @submit="handleSubmit">
        <van-cell-group inset>
          <van-field v-model="formData.entityName" label="主体名称" required :rules="[{required:true,message:'请输入名称'}]" />
          <van-field v-model="formData.entityType" label="主体类型" required is-link readonly @click="showTypePicker = true" :formatter="entityTypeLabel" />
          <van-field v-model="formData.creditScore" label="信用评分" type="number" required :rules="[{required:true,message:'请输入评分'}]" />
          <van-field v-model="formData.creditLevel" label="信用等级" required :rules="[{required:true,message:'请输入等级'}]" />
          <van-field v-model="formData.tradeScore" label="交易信用分" type="number" />
          <van-field v-model="formData.productionScore" label="生产信用分" type="number" />
          <van-field v-model="formData.financialScore" label="财务信用分" type="number" />
          <van-field v-model="formData.assessor" label="评定人" />
          <van-field v-model="formData.remark" label="备注" type="textarea" rows="2" />
        </van-cell-group>
        <div style="padding:16px"><van-button block type="primary" native-type="submit">保存</van-button></div>
      </van-form>
    </van-popup>

    <van-popup v-model:show="showTypePicker" position="bottom" round>
      <van-picker :columns="typeOptions" @confirm="onTypePick" @cancel="showTypePicker = false" />
    </van-popup>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { financeApi } from '@/api/finance.js'
import { showToast, showConfirmDialog } from 'vant'

const list = ref([]); const loading = ref(false); const finished = ref(false); const refreshing = ref(false)
const showDetail = ref(false); const showForm = ref(false); const showTypePicker = ref(false); const currentItem = ref(null)
const query = reactive({ keyword: '', entityType: '', page: 1, size: 10 })
const formData = reactive({ id: null, entityName: '', entityType: '', creditScore: '', creditLevel: '', tradeScore: '', productionScore: '', financialScore: '', assessor: '', remark: '' })
const typeOptions = [{ text: '农户', value: 'FARMER' }, { text: '企业', value: 'ENTERPRISE' }, { text: '合作社', value: 'COOPERATIVE' }]

function entityTypeLabel(t) { return { FARMER: '农户', ENTERPRISE: '企业', COOPERATIVE: '合作社' }[t] || t || '-' }
function levelTagType(l) { return l?.startsWith('AAA') ? 'success' : l?.startsWith('AA') || l === 'A' ? 'primary' : l?.startsWith('B') ? 'warning' : 'danger' }

async function loadList() {
  loading.value = true
  try { const res = await financeApi.getCredits({ ...query, page: 1 }); list.value = res?.records || []; finished.value = list.value.length >= (res?.total || 0); query.page = 1 }
  finally { loading.value = false; refreshing.value = false }
}
async function loadMore() {
  if (finished.value) return; query.page += 1
  try { const res = await financeApi.getCredits({ ...query }); const records = res?.records || []; list.value = [...list.value, ...records]; if (list.value.length >= (res?.total || 0) || records.length < query.size) finished.value = true }
  finally { loading.value = false }
}
function handleSearch() { query.page = 1; finished.value = false; list.value = []; loadList() }
function openDetail(item) { currentItem.value = item; showDetail.value = true }
function openForm(item) { showDetail.value = false; if (item) Object.assign(formData, item); else Object.keys(formData).forEach(k => formData[k] = k === 'id' ? null : ''); showForm.value = true }
function onTypePick({ selectedValues }) { formData.entityType = selectedValues[0]; showTypePicker.value = false }
async function handleSubmit() {
  try { if (formData.id) await financeApi.updateCredit(formData.id, { ...formData }); else await financeApi.createCredit({ ...formData, assessmentDate: new Date().toISOString().substring(0,10) }); showToast('保存成功'); showForm.value = false; handleSearch() }
  catch (e) { showToast('保存失败') }
}
async function handleDelete(item) {
  try { await showConfirmDialog({ title: '确认删除', message: `确定删除 ${item.entityName} 的评级？` }); await financeApi.deleteCredit(item.id); showToast('删除成功'); showDetail.value = false; handleSearch() }
  catch (e) { if (e !== 'cancel') showToast('删除失败') }
}
function handleExport() { financeApi.exportCredits() }
onMounted(loadList)
</script>

<style scoped>
.credits-page { padding-bottom: 20px; }
.filter-bar { background: #fff; margin-bottom: 8px; }
.action-bar { display: flex; gap: 8px; padding: 8px 16px; }
.drawer-header { display: flex; justify-content: space-between; align-items: center; padding: 16px; font-size: 16px; font-weight: 600; border-bottom: 1px solid #ebedf0; }
.drawer-actions { padding: 16px; }
</style>
