<template>
  <div class="page-container" data-density="compact">
    <van-nav-bar title="大屏配置" left-arrow @click-left="$router.back()" />

    <van-search v-model="keyword" placeholder="搜索大屏名称" @search="onSearch" />

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list v-model:loading="loading" :finished="finished" :finished-text="list.length ? '没有更多了' : ''" @load="onLoad">
        <van-cell v-for="item in list" :key="item.id" :title="item.dashboardName"
          :label="`${item.dashboardCode} · ${item.category?.desc || item.category} · 刷新 ${item.refreshSeconds ?? '-'}s`" is-link @click="showDetail(item)">
          <template #value>
            <div style="display:flex;flex-direction:column;align-items:flex-end;gap:2px">
              <van-tag :type="item.published === 1 ? 'success' : 'default'">
                {{ item.published === 1 ? '已发布' : '草稿' }}
              </van-tag>
              <span style="font-size:var(--typography-caption-size,13px);color:var(--color-text-tertiary, #8A9099)">
                {{ item.updatedAt }}
              </span>
            </div>
          </template>
        </van-cell>
        <van-empty v-if="!loading && list.length === 0" description="暂无大屏" />
      </van-list>
    </van-pull-refresh>

    <van-button class="fab-btn" type="primary" icon="plus" round @click="showCreate" />

    <!-- 新建/编辑弹窗 -->
    <van-popup v-model:show="dialogVisible" position="bottom" round :style="{ maxHeight: '90%' }">
      <div class="popup-header">{{ dialogTitle }}</div>
      <van-form @submit="onSubmit" class="popup-form">
        <van-field v-model="form.dashboardCode" label="大屏编码" placeholder="唯一编码" required :readonly="isEdit" />
        <van-field v-model="form.dashboardName" label="大屏名称" placeholder="名称" required />
        <van-field name="category" label="分类">
          <template #input>
            <van-radio-group v-model="form.category" direction="horizontal">
              <van-radio name="GOV">政务</van-radio>
              <van-radio name="OPS">运营</van-radio>
              <van-radio name="INDUSTRY">产业</van-radio>
              <van-radio name="CUSTOM">自定义</van-radio>
            </van-radio-group>
          </template>
        </van-field>
        <van-field v-model="form.layoutJson" label="布局 JSON" placeholder='{"rows":[...]}' type="textarea" rows="4"
          class="json-editor" />
        <van-field v-model="form.theme" label="主题" placeholder="light / dark" />
        <van-field name="visibility" label="可见性">
          <template #input>
            <van-radio-group v-model="form.visibility" direction="horizontal">
              <van-radio name="PUBLIC">公开</van-radio>
              <van-radio name="ROLE">按角色</van-radio>
              <van-radio name="PRIVATE">私有</van-radio>
            </van-radio-group>
          </template>
        </van-field>
        <van-field v-model="form.visibleRoles" label="可见角色" placeholder="逗号分隔，如 ADMIN,FARMER" />
        <van-field v-model.number="form.refreshSeconds" label="刷新间隔(s)" placeholder="如 30" type="digit" />
        <van-field v-model="form.owner" label="负责人" placeholder="可选" />
        <van-field v-model="form.remark" label="备注" placeholder="可选" type="textarea" rows="2" />
        <div class="popup-actions">
          <van-button block type="primary" native-type="submit" :loading="submitting">{{ isEdit ? '更新' : '创建' }}</van-button>
        </div>
      </van-form>
    </van-popup>

    <!-- 发布确认 -->
    <van-dialog v-model:show="publishVisible" title="发布大屏" show-cancel-button @confirm="confirmPublish">
      <div style="padding:var(--space-4, 16px);color:var(--color-text-secondary, #5E6368)">
        发布后将对授权角色可见，确认发布？
      </div>
    </van-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { showToast, showSuccessToast } from 'vant'
import { screenApi } from '@/api/bigdata.js'

const keyword = ref('')
const list = ref([])
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const page = ref(1)

const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const submitting = ref(false)
const dialogTitle = ref('新增大屏')

const publishVisible = ref(false)
const publishId = ref(null)

const defaultForm = { dashboardCode: '', dashboardName: '', category: 'GOV', layoutJson: '', theme: '', visibility: 'PUBLIC', visibleRoles: '', refreshSeconds: null, owner: '', remark: '' }
const form = reactive({ ...defaultForm })

async function loadData() {
  try {
    const res = await screenApi.list({ page: page.value, size: 20 })
    const records = res.records || res
    if (page.value === 1) list.value = records
    else list.value.push(...records)
    finished.value = list.value.length >= (res.total ?? records.length)
  } catch (e) { showToast({ type: 'fail', message: e.message || '加载失败' }); finished.value = true }
}

function onLoad() { loadData().finally(() => { loading.value = false; refreshing.value = false }) }
function onRefresh() { list.value = []; page.value = 1; finished.value = false; }
function onSearch() { onRefresh(); onLoad() }

function resetForm() { Object.assign(form, { ...defaultForm }) }
function showCreate() { isEdit.value = false; editId.value = null; dialogTitle.value = '新增大屏'; resetForm(); dialogVisible.value = true }
function showDetail(item) {
  isEdit.value = true; editId.value = item.id; dialogTitle.value = '编辑大屏'
  Object.assign(form, {
    dashboardCode: item.dashboardCode, dashboardName: item.dashboardName,
    category: item.category?.code || 'GOV', layoutJson: item.layoutJson || '',
    theme: item.theme || '', visibility: item.visibility?.code || 'PUBLIC',
    visibleRoles: item.visibleRoles || '', refreshSeconds: item.refreshSeconds,
    owner: item.owner || '', remark: item.remark || ''
  })
  dialogVisible.value = true
}

async function confirmPublish() {
  if (!publishId.value) return
  try { await screenApi.publish(publishId.value); showSuccessToast('已发布'); onRefresh(); onLoad() }
  catch (e) { showToast({ type: 'fail', message: e.message || '发布失败' }) }
}

async function onSubmit() {
  submitting.value = true
  try {
    if (isEdit.value) await screenApi.update(editId.value, form)
    else await screenApi.create(form)
    showSuccessToast(isEdit.value ? '更新成功' : '创建成功')
    dialogVisible.value = false; onRefresh(); onLoad()
  } catch (e) { showToast({ type: 'fail', message: e.message || '操作失败' }) } finally { submitting.value = false }
}
</script>

<style scoped>
.page-container { min-height: 100vh; background: var(--color-surface-base, #FAFAF8); }
.fab-btn { position: fixed; right: var(--space-5, 20px); bottom: calc(var(--space-6, 24px) + env(safe-area-inset-bottom)); z-index: var(--z-index-raised, 10); width: var(--density-button-height, 40px); height: var(--density-button-height, 40px); }
.popup-header { font-size: var(--typography-h3-size, 16px); font-weight: var(--typography-h3-weight, 500); padding: var(--space-4, 16px); text-align: center; }
.popup-form { padding: 0 var(--space-4, 16px) var(--space-6, 24px); }
.popup-actions { padding-top: var(--space-4, 16px); }
.json-editor :deep(textarea) { font-family: var(--font-mono, monospace); font-size: var(--typography-caption-size, 13px); }
</style>
