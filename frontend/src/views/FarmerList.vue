<template>
  <div class="page-container">
    <div class="section-header">
      <span class="section-title">果农管理</span>
      <van-button type="primary" size="small" icon="plus" @click="showAddSheet = true">新增</van-button>
    </div>

    <van-search v-model="query.keyword" placeholder="搜索姓名/手机号" @search="loadList(true)" shape="round" />

    <van-pull-refresh v-model="refreshing" @refresh="() => loadList(true)">
      <van-list v-model:loading="loading" :finished="finished" finished-text="没有更多了" @load="loadList">
        <van-cell-group inset style="margin-bottom:8px;" v-for="item in list" :key="item.id">
          <van-cell is-link @click="showDetail(item)">
            <template #title>
              <span style="font-weight:600;">{{ item.name }}</span>
              <van-tag :type="item.status === 'ACTIVE' ? 'success' : 'default'" style="margin-left:6px;">
                {{ item.status === 'ACTIVE' ? '正常' : '注销' }}
              </van-tag>
            </template>
            <template #label>
              {{ item.phone }} · {{ item.location }} · {{ item.orchardCount || 0 }}个果园
            </template>
          </van-cell>
        </van-cell-group>
        <van-empty v-if="!loading && list.length === 0" description="暂无果农记录" />
      </van-list>
    </van-pull-refresh>

    <van-action-sheet v-model:show="showAddSheet" title="新增果农">
      <div style="padding:16px;">
        <van-form @submit="handleCreate">
          <van-field v-model="addForm.name" label="姓名" placeholder="请输入" :rules="[{required:true}]" />
          <van-field v-model="addForm.phone" label="手机号" type="tel" placeholder="请输入" :rules="[{required:true}]" />
          <van-field v-model="addForm.idCard" label="身份证号" placeholder="请输入" />
          <van-field v-model="addForm.location" label="所在地区" placeholder="请输入" :rules="[{required:true}]" />
          <van-button block type="primary" native-type="submit" style="margin-top:16px;" color="#07c160">提交</van-button>
        </van-form>
      </div>
    </van-action-sheet>

    <van-popup v-model:show="showDetailPopup" round position="bottom" style="height:70%;">
      <div style="padding:16px;" v-if="selectedFarmer">
        <div style="text-align:center;margin-bottom:16px;">
          <h3>{{ selectedFarmer.name }}</h3>
          <van-tag :type="selectedFarmer.status === 'ACTIVE' ? 'success' : 'default'">
            {{ selectedFarmer.status === 'ACTIVE' ? '正常' : '注销' }}
          </van-tag>
        </div>
        <van-cell-group>
          <van-cell title="手机号" :value="selectedFarmer.phone" />
          <van-cell title="身份证" :value="selectedFarmer.idCard || '-'" />
          <van-cell title="所在地区" :value="selectedFarmer.location" />
          <van-cell title="注册果园数" :value="`${selectedFarmer.orchardCount || 0} 个`" />
          <van-cell title="注册时间" :value="selectedFarmer.createdAt" />
          <van-cell title="信用评分" :value="selectedFarmer.creditScore || '-'" />
        </van-cell-group>
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { farmerApi } from '@/api/farmer'
import { showToast } from 'vant'

const list = ref([])
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const showAddSheet = ref(false)
const showDetailPopup = ref(false)
const selectedFarmer = ref(null)
const query = reactive({ keyword: '', page: 1, size: 10 })
const addForm = reactive({ name: '', phone: '', idCard: '', location: '' })

async function loadList(reset = false) {
  if (reset) { query.page = 1; list.value = []; finished.value = false }
  loading.value = true
  try {
    const res = await farmerApi.list({ ...query })
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

function showDetail(farmer) {
  selectedFarmer.value = farmer
  showDetailPopup.value = true
}

async function handleCreate() {
  await farmerApi.create(addForm)
  showToast('果农注册成功')
  showAddSheet.value = false
  Object.assign(addForm, { name: '', phone: '', idCard: '', location: '' })
  loadList(true)
}

loadList(true)
</script>
