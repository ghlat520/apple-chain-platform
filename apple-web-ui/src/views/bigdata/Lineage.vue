<template>
  <div class="page-container" data-density="compact">
    <van-nav-bar title="数据血缘" left-arrow @click-left="$router.back()" />

    <van-search v-model="keyword" placeholder="搜索节点名称" @search="onSearch" />

    <van-dropdown-menu>
      <van-dropdown-item v-model="filterRelation" :options="relationOptions" @change="onSearch" />
    </van-dropdown-menu>

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list v-model:loading="loading" :finished="finished" :finished-text="list.length ? '没有更多了' : ''" @load="onLoad">
        <van-cell-group v-for="item in list" :key="item.id" inset style="margin-bottom: var(--space-2, 8px)">
          <van-cell :title="`${item.upstreamName || item.upstreamId}`" :label="`${item.upstreamType?.desc || item.upstreamType}`">
            <template #value>
              <van-tag type="primary" plain>{{ item.relationType?.desc || item.relationType }}</van-tag>
            </template>
          </van-cell>
          <van-cell :title="`${item.downstreamName || item.downstreamId}`" :label="`${item.downstreamType?.desc || item.downstreamType}`">
            <template #right-icon>
              <van-icon name="arrow-down" style="color: var(--color-text-tertiary, #8A9099)" />
            </template>
          </van-cell>
          <van-cell v-if="item.description" :title="item.description" />
          <van-cell>
            <template #right-icon>
              <van-button size="mini" type="danger" plain @click.stop="deleteEdge(item.id)">删除</van-button>
            </template>
          </van-cell>
        </van-cell-group>
        <van-empty v-if="!loading && list.length === 0" description="暂无血缘关系" />
      </van-list>
    </van-pull-refresh>

    <van-button class="fab-btn" type="primary" icon="plus" round @click="showCreate" />

    <van-popup v-model:show="dialogVisible" position="bottom" round :style="{ maxHeight: '85%' }">
      <div class="popup-header">新增血缘边</div>
      <van-form @submit="onSubmit" class="popup-form">
        <van-field name="upstreamType" label="上游类型">
          <template #input>
            <van-radio-group v-model="form.upstreamType" direction="horizontal">
              <van-radio name="TABLE">表</van-radio>
              <van-radio name="FIELD">字段</van-radio>
              <van-radio name="JOB">任务</van-radio>
              <van-radio name="METRIC">指标</van-radio>
              <van-radio name="DASHBOARD">大屏</van-radio>
              <van-radio name="SYSTEM">系统</van-radio>
            </van-radio-group>
          </template>
        </van-field>
        <van-field v-model="form.upstreamId" label="上游ID" placeholder="节点ID" required />
        <van-field v-model="form.upstreamName" label="上游名称" placeholder="可选" />
        <van-field name="downstreamType" label="下游类型">
          <template #input>
            <van-radio-group v-model="form.downstreamType" direction="horizontal">
              <van-radio name="TABLE">表</van-radio>
              <van-radio name="FIELD">字段</van-radio>
              <van-radio name="JOB">任务</van-radio>
              <van-radio name="METRIC">指标</van-radio>
              <van-radio name="DASHBOARD">大屏</van-radio>
              <van-radio name="SYSTEM">系统</van-radio>
            </van-radio-group>
          </template>
        </van-field>
        <van-field v-model="form.downstreamId" label="下游ID" placeholder="节点ID" required />
        <van-field v-model="form.downstreamName" label="下游名称" placeholder="可选" />
        <van-field name="relationType" label="关系类型">
          <template #input>
            <van-radio-group v-model="form.relationType" direction="horizontal">
              <van-radio name="DERIVES_FROM">派生</van-radio>
              <van-radio name="TRANSFORMS">转换</van-radio>
              <van-radio name="AGGREGATES">聚合</van-radio>
              <van-radio name="REFERENCES">引用</van-radio>
            </van-radio-group>
          </template>
        </van-field>
        <van-field v-model.number="form.pipelineJobId" label="关联任务ID" placeholder="可选" type="digit" />
        <van-field v-model="form.description" label="描述" placeholder="可选" type="textarea" rows="2" />
        <div class="popup-actions">
          <van-button block type="primary" native-type="submit" :loading="submitting">创建</van-button>
        </div>
      </van-form>
    </van-popup>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { showToast, showSuccessToast, showConfirmDialog } from 'vant'
import { lineageApi } from '@/api/bigdata.js'

const keyword = ref('')
const filterRelation = ref('')
const relationOptions = [
  { text: '全部关系', value: '' },
  { text: '派生', value: 'DERIVES_FROM' },
  { text: '转换', value: 'TRANSFORMS' },
  { text: '聚合', value: 'AGGREGATES' },
  { text: '引用', value: 'REFERENCES' },
]

const list = ref([])
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const page = ref(1)

const dialogVisible = ref(false)
const submitting = ref(false)

const defaultForm = { upstreamType: 'TABLE', upstreamId: '', upstreamName: '', downstreamType: 'TABLE', downstreamId: '', downstreamName: '', relationType: 'DERIVES_FROM', pipelineJobId: null, description: '' }
const form = reactive({ ...defaultForm })

async function loadData() {
  try {
    const res = await lineageApi.list({ page: page.value, size: 20, relationType: filterRelation.value || undefined })
    const records = res.records || res
    if (page.value === 1) list.value = records
    else list.value.push(...records)
    finished.value = list.value.length >= (res.total ?? records.length)
  } catch { finished.value = true }
}

function onLoad() { loadData().finally(() => { loading.value = false; refreshing.value = false }) }
function onRefresh() { list.value = []; page.value = 1; finished.value = false; }
function onSearch() { onRefresh(); onLoad() }

async function deleteEdge(id) {
  try {
    await showConfirmDialog({ title: '确认删除', message: '删除此血缘关系？' })
  } catch { return }
  try {
    await lineageApi.delete(id)
    showSuccessToast('已删除'); onRefresh(); onLoad()
  } catch (e) { showToast({ type: 'fail', message: e.message || '删除失败' }) }
}

function showCreate() { Object.assign(form, { ...defaultForm }); dialogVisible.value = true }

async function onSubmit() {
  submitting.value = true
  try {
    await lineageApi.create(form)
    showSuccessToast('创建成功')
    dialogVisible.value = false; onRefresh(); onLoad()
  } catch (e) { showToast({ type: 'fail', message: e.message || '创建失败' }) } finally { submitting.value = false }
}
</script>

<style scoped>
.page-container { min-height: 100vh; background: var(--color-surface-base, #FAFAF8); }
.fab-btn { position: fixed; right: var(--space-5, 20px); bottom: calc(var(--space-6, 24px) + env(safe-area-inset-bottom)); z-index: var(--z-index-raised, 10); width: var(--density-button-height, 40px); height: var(--density-button-height, 40px); }
.popup-header { font-size: var(--typography-h3-size, 16px); font-weight: var(--typography-h3-weight, 500); padding: var(--space-4, 16px); text-align: center; }
.popup-form { padding: 0 var(--space-4, 16px) var(--space-6, 24px); }
.popup-actions { padding-top: var(--space-4, 16px); }
</style>
