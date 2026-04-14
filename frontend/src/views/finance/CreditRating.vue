<template>
  <div class="page-container">
    <div class="section-header">
      <span class="section-title">信用评级</span>
      <van-button type="primary" size="small" icon="plus" @click="openCreateSheet">新增评级</van-button>
    </div>

    <van-search v-model="query.keyword" placeholder="搜索农户姓名" @search="loadList(true)" shape="round" />

    <van-dropdown-menu active-color="#07c160">
      <van-dropdown-item v-model="query.level" :options="levelOptions" @change="loadList(true)" />
    </van-dropdown-menu>

    <van-pull-refresh v-model="refreshing" @refresh="() => loadList(true)">
      <van-list v-model:loading="loading" :finished="finished" finished-text="没有更多了" @load="loadList">
        <van-cell-group inset style="margin-top:8px;margin-bottom:8px;" v-for="item in list" :key="item.id">
          <van-cell is-link @click="showDetail(item)">
            <template #title>
              <span style="font-weight:600;">{{ item.farmerName }}</span>
              <van-tag :type="levelColor(item.level)" style="margin-left:6px;">{{ item.level }}</van-tag>
            </template>
            <template #label>
              种植规模: {{ item.plantingScale }}亩 · 经验: {{ item.yearsExperience }}年
            </template>
          </van-cell>
          <van-cell>
            <template #title>
              <div class="score-row">
                <span style="font-size:13px;color:#666;width:32px;">评分</span>
                <div class="score-bar-wrap">
                  <div class="score-bar" :style="{ width: scorePercent(item.score) + '%', background: scoreBarColor(item.score) }"></div>
                </div>
                <span class="score-value" :style="{ color: scoreBarColor(item.score) }">{{ item.score }}</span>
              </div>
            </template>
            <template #value>
              <span style="font-size:13px;color:#666;">优果率 {{ item.qualityRate }}%</span>
            </template>
          </van-cell>
        </van-cell-group>
        <van-empty v-if="!loading && list.length === 0" description="暂无评级数据" />
      </van-list>
    </van-pull-refresh>

    <!-- New rating form -->
    <van-action-sheet v-model:show="showCreateSheet" title="新增评级">
      <div style="padding:16px;">
        <van-form @submit="handleCreate">
          <van-field v-model="createForm.farmerId" label="农户ID" placeholder="请输入" :rules="[{ required: true }]" />
          <van-field v-model="createForm.farmerName" label="农户姓名" placeholder="请输入" :rules="[{ required: true }]" />
          <van-field v-model="createForm.plantingScale" label="种植规模(亩)" type="number" placeholder="请输入" :rules="[{ required: true }]" />
          <van-field v-model="createForm.yearsExperience" label="经验年限" type="digit" placeholder="请输入" :rules="[{ required: true }]" />
          <van-field v-model="createForm.yieldRate" label="产量率(%)" type="number" placeholder="请输入" :rules="[{ required: true }]" />
          <van-field v-model="createForm.qualityRate" label="优果率(%)" type="number" placeholder="请输入" :rules="[{ required: true }]" />
          <van-field v-model="createForm.repaymentRate" label="还款率(%)" type="number" placeholder="请输入" :rules="[{ required: true }]" />
          <van-field v-model="createForm.remark" label="备注" type="textarea" rows="2" placeholder="选填" />
          <van-button block type="primary" native-type="submit" style="margin-top:16px;" color="#07c160">提交评级</van-button>
        </van-form>
      </div>
    </van-action-sheet>

    <!-- Detail popup -->
    <van-popup v-model:show="showDetailPopup" round position="bottom" style="height:70%;">
      <div style="padding:16px;" v-if="selectedItem">
        <h3 style="margin-bottom:4px;">{{ selectedItem.farmerName }}</h3>
        <div style="margin-bottom:12px;">
          <van-tag :type="levelColor(selectedItem.level)" size="medium">{{ selectedItem.level }}</van-tag>
          <span style="margin-left:8px;font-size:20px;font-weight:700;" :style="{ color: scoreBarColor(selectedItem.score) }">{{ selectedItem.score }}</span>
        </div>
        <van-cell-group>
          <van-cell title="种植规模" :value="`${selectedItem.plantingScale} 亩`" />
          <van-cell title="种植经验" :value="`${selectedItem.yearsExperience} 年`" />
          <van-cell title="产量率" :value="`${selectedItem.yieldRate}%`" />
          <van-cell title="优果率" :value="`${selectedItem.qualityRate}%`" />
          <van-cell title="还款率" :value="`${selectedItem.repaymentRate}%`" />
          <van-cell title="备注" :value="selectedItem.remark || '-'" />
        </van-cell-group>
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { financeApi } from '@/api/finance'
import { showToast } from 'vant'

const list = ref([])
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const showCreateSheet = ref(false)
const showDetailPopup = ref(false)
const selectedItem = ref(null)
const query = reactive({ keyword: '', level: '', page: 1, size: 10 })
const createForm = reactive({
  farmerId: '', farmerName: '', plantingScale: '',
  yearsExperience: '', yieldRate: '', qualityRate: '',
  repaymentRate: '', remark: ''
})

const LEVEL_COLOR_MAP = {
  AAA: 'success', AA: 'success',
  A: 'primary', BBB: 'primary',
  BB: 'warning', B: 'warning',
  C: 'danger'
}
const levelOptions = [
  { text: '全部等级', value: '' },
  { text: 'AAA', value: 'AAA' }, { text: 'AA', value: 'AA' },
  { text: 'A', value: 'A' }, { text: 'BBB', value: 'BBB' },
  { text: 'BB', value: 'BB' }, { text: 'B', value: 'B' },
  { text: 'C', value: 'C' }
]

function levelColor(l) { return LEVEL_COLOR_MAP[l] || 'default' }
function scorePercent(s) { return Math.min(100, Math.max(0, ((s - 300) / 550) * 100)) }
function scoreBarColor(s) {
  if (s >= 750) return '#07c160'
  if (s >= 650) return '#1989fa'
  if (s >= 550) return '#ff976a'
  return '#ee0a24'
}

async function loadList(reset = false) {
  if (reset) { query.page = 1; list.value = []; finished.value = false }
  loading.value = true
  try {
    const res = await financeApi.ratingList({ ...query })
    const records = res.data?.records || []
    if (reset) list.value = records
    else list.value.push(...records)
    finished.value = list.value.length >= (res.data?.total || 0)
    query.page++
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

function showDetail(item) {
  selectedItem.value = item
  showDetailPopup.value = true
}

function openCreateSheet() {
  Object.assign(createForm, {
    farmerId: '', farmerName: '', plantingScale: '',
    yearsExperience: '', yieldRate: '', qualityRate: '',
    repaymentRate: '', remark: ''
  })
  showCreateSheet.value = true
}

async function handleCreate() {
  await financeApi.ratingCreate({
    ...createForm,
    plantingScale: Number(createForm.plantingScale),
    yearsExperience: Number(createForm.yearsExperience),
    yieldRate: Number(createForm.yieldRate),
    qualityRate: Number(createForm.qualityRate),
    repaymentRate: Number(createForm.repaymentRate)
  })
  showToast('评级创建成功')
  showCreateSheet.value = false
  loadList(true)
}

loadList(true)
</script>

<style scoped>
.score-row {
  display: flex;
  align-items: center;
  gap: 6px;
  width: 100%;
}
.score-bar-wrap {
  flex: 1;
  height: 8px;
  background: #f0f0f0;
  border-radius: 4px;
  overflow: hidden;
}
.score-bar {
  height: 100%;
  border-radius: 4px;
  transition: width 0.3s ease;
}
.score-value {
  width: 32px;
  font-weight: 700;
  font-size: 14px;
  text-align: right;
}
</style>
