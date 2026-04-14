<template>
  <div class="page-container">
    <div class="section-header">
      <span class="section-title">运输管理</span>
      <van-button type="primary" size="small" icon="plus" @click="openAddSheet()">新建运输单</van-button>
    </div>

    <van-search v-model="query.keyword" placeholder="搜索运输单号/车牌号/目的地" @search="loadList(true)" shape="round" />

    <van-tabs v-model:active="activeStatus" @change="handleTabChange" style="margin-bottom:12px;">
      <van-tab title="全部" name="" />
      <van-tab title="待发运" name="PENDING" />
      <van-tab title="运输中" name="IN_TRANSIT" />
      <van-tab title="已完成" name="COMPLETED" />
      <van-tab title="已取消" name="CANCELLED" />
    </van-tabs>

    <van-pull-refresh v-model="refreshing" @refresh="() => loadList(true)">
      <van-list v-model:loading="loading" :finished="finished" finished-text="没有更多了" @load="loadList">
        <van-swipe-cell v-for="item in list" :key="item.id" style="margin-bottom:8px;">
          <van-cell-group inset>
            <van-cell is-link @click="showDetailPopupFn(item)">
              <template #title>
                <span style="font-weight:600;">{{ item.orderNo }}</span>
                <van-tag :type="statusTagMap[item.status]?.type || 'default'" style="margin-left:6px;">
                  {{ statusTagMap[item.status]?.label || item.status }}
                </van-tag>
              </template>
              <template #label>
                {{ item.vehiclePlate || '-' }} → {{ item.destination || '-' }}
              </template>
            </van-cell>
            <van-cell title="收货人" :value="`${item.receiverName || '-'} ${item.receiverPhone || ''}`" />
            <van-cell title="总重量" :value="`${item.totalWeight || '-'}kg`" />
            <van-cell title="温度要求" :value="tempRange(item)" />
            <van-cell v-if="item.status === 'IN_TRANSIT' && item.actualTemp != null" title="当前温度" :value="`${item.actualTemp}°C`" />
          </van-cell-group>
          <template #right>
            <van-button
              v-if="item.status === 'PENDING'"
              square type="primary" text="发运" style="height:100%;"
              @click="handleAction(item.id, 'start')"
            />
            <van-button
              v-if="item.status === 'IN_TRANSIT'"
              square type="success" text="完成" style="height:100%;"
              @click="handleAction(item.id, 'complete')"
            />
            <van-button
              v-if="item.status === 'PENDING' || item.status === 'IN_TRANSIT'"
              square type="danger" text="取消" style="height:100%;"
              @click="handleAction(item.id, 'cancel')"
            />
          </template>
        </van-swipe-cell>
        <van-empty v-if="!loading && list.length === 0" description="暂无运输记录" />
      </van-list>
    </van-pull-refresh>

    <!-- 演示链路导航 -->
    <div style="padding:16px;" v-if="list.length > 0 && !showFormSheet && !showDetailPopup">
      <van-button
        block
        type="primary"
        color="#07c160"
        icon="guide-o"
        @click="router.push('/trades')"
      >
        下一步：收购交易
      </van-button>
    </div>

    <!-- 新建运输单弹窗 -->
    <van-action-sheet v-model:show="showFormSheet" title="新建运输单">
      <div style="padding:16px;">
        <van-form @submit="handleCreate">
          <van-field v-model="form.vehicleId" label="车辆ID" placeholder="请输入" :rules="[{required:true}]" />
          <van-field v-model="form.warehouseId" label="仓库ID" placeholder="请输入" :rules="[{required:true}]" />
          <van-field v-model="form.destination" label="目的地" placeholder="请输入" :rules="[{required:true}]" />
          <van-field v-model="form.receiverName" label="收货人" placeholder="请输入" :rules="[{required:true}]" />
          <van-field v-model="form.receiverPhone" label="收货电话" placeholder="请输入" />
          <van-field v-model="form.totalWeight" label="总重量(kg)" type="number" placeholder="请输入" />
          <van-field v-model="form.temperatureMin" label="最低温度(°C)" type="number" placeholder="-2" />
          <van-field v-model="form.temperatureMax" label="最高温度(°C)" type="number" placeholder="5" />
          <van-field v-model="form.remark" label="备注" type="textarea" placeholder="选填" rows="2" />

          <div style="margin-top:12px;margin-bottom:8px;font-size:14px;color:#666;">产品列表</div>
          <div v-for="(prod, idx) in form.products" :key="idx" style="display:flex;gap:8px;margin-bottom:8px;">
            <van-field v-model="prod.name" placeholder="名称" style="flex:1;" />
            <van-field v-model="prod.grade" placeholder="等级" style="flex:0.6;" />
            <van-field v-model="prod.quantity" placeholder="数量" type="number" style="flex:0.6;" />
            <van-field v-model="prod.unit" placeholder="单位" style="flex:0.5;" />
            <van-button size="small" type="danger" icon="minus" @click="removeProduct(idx)" />
          </div>
          <van-button size="small" type="primary" icon="plus" plain @click="addProduct" style="margin-bottom:12px;">添加产品</van-button>

          <van-button block type="primary" native-type="submit" style="margin-top:16px;" color="#07c160">提交</van-button>
        </van-form>
      </div>
    </van-action-sheet>

    <!-- 详情弹窗 -->
    <van-popup v-model:show="showDetailPopup" round position="bottom" style="height:70%;">
      <div style="padding:16px;" v-if="selectedItem">
        <h3 style="margin-bottom:12px;">{{ selectedItem.orderNo }}</h3>
        <van-cell-group>
          <van-cell title="车牌号" :value="selectedItem.vehiclePlate || '-'" />
          <van-cell title="仓库" :value="selectedItem.warehouseName || '-'" />
          <van-cell title="目的地" :value="selectedItem.destination || '-'" />
          <van-cell title="收货人" :value="selectedItem.receiverName || '-'" />
          <van-cell title="收货电话" :value="selectedItem.receiverPhone || '-'" />
          <van-cell title="总重量" :value="`${selectedItem.totalWeight || '-'}kg`" />
          <van-cell title="温度要求" :value="tempRange(selectedItem)" />
          <van-cell v-if="selectedItem.actualTemp != null" title="实际温度" :value="`${selectedItem.actualTemp}°C`" />
          <van-cell title="状态" :value="statusTagMap[selectedItem.status]?.label || '-'" />
          <van-cell title="开始时间" :value="selectedItem.startTime || '-'" />
          <van-cell title="完成时间" :value="selectedItem.endTime || '-'" />
          <van-cell title="备注" :value="selectedItem.remark || '-'" />
        </van-cell-group>

        <div v-if="selectedItem.products && selectedItem.products.length" style="margin-top:12px;">
          <div style="font-size:14px;color:#666;margin-bottom:8px;">产品明细</div>
          <van-cell-group inset>
            <van-cell
              v-for="(prod, idx) in selectedItem.products"
              :key="idx"
              :title="prod.name"
              :value="`${prod.quantity || '-'}${prod.unit || ''}`"
              :label="prod.grade ? `等级: ${prod.grade}` : ''"
            />
          </van-cell-group>
        </div>
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { coldchainApi } from '@/api/coldchain'
import { showToast, showConfirmDialog } from 'vant'

const list = ref([])
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const showFormSheet = ref(false)
const showDetailPopup = ref(false)
const selectedItem = ref(null)
const activeStatus = ref('')
const router = useRouter()

const query = reactive({ keyword: '', status: '', vehicleId: '', page: 1, size: 10 })
const form = reactive({
  vehicleId: '', warehouseId: '', destination: '', receiverName: '', receiverPhone: '',
  totalWeight: '', temperatureMin: '', temperatureMax: '', remark: '',
  products: [{ name: '', grade: '', quantity: '', unit: 'kg' }]
})

const statusTagMap = {
  PENDING: { label: '待发运', type: 'warning' },
  IN_TRANSIT: { label: '运输中', type: 'primary' },
  COMPLETED: { label: '已完成', type: 'success' },
  CANCELLED: { label: '已取消', type: 'default' }
}

function tempRange(item) {
  if (item.temperatureMin == null && item.temperatureMax == null) return '-'
  return `${item.temperatureMin ?? '-'}°C ~ ${item.temperatureMax ?? '-'}°C`
}

function addProduct() {
  form.products.push({ name: '', grade: '', quantity: '', unit: 'kg' })
}

function removeProduct(idx) {
  if (form.products.length > 1) form.products.splice(idx, 1)
}

function handleTabChange(val) {
  query.status = val
  loadList(true)
}

function openAddSheet() {
  Object.assign(form, {
    vehicleId: '', warehouseId: '', destination: '', receiverName: '', receiverPhone: '',
    totalWeight: '', temperatureMin: '', temperatureMax: '', remark: '',
    products: [{ name: '', grade: '', quantity: '', unit: 'kg' }]
  })
  showFormSheet.value = true
}

function showDetailPopupFn(item) {
  selectedItem.value = item
  showDetailPopup.value = true
}

async function loadList(reset = false) {
  if (reset) { query.page = 1; list.value = []; finished.value = false }
  loading.value = true
  try {
    const res = await coldchainApi.transportList({ ...query })
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

async function handleCreate() {
  const payload = { ...form }
  await coldchainApi.transportCreate(payload)
  showToast('运输单创建成功')
  showFormSheet.value = false
  loadList(true)
}

async function handleAction(id, action) {
  const actionLabels = { start: '发运', complete: '完成', cancel: '取消' }
  if (action === 'cancel') {
    await showConfirmDialog({ title: '确认取消', message: '确定要取消该运输单吗？' })
  }
  await coldchainApi[`transport${action.charAt(0).toUpperCase() + action.slice(1)}`](id)
  showToast(`运输单已${actionLabels[action]}`)
  loadList(true)
}

loadList(true)
</script>
