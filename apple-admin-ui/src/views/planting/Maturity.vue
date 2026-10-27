<template>
  <div class="page-container">
    <div class="page-header">
      <h2>成熟度管理</h2>
      <el-button type="primary" @click="openRecordDialog" icon="Plus">录入成熟度</el-button>
    </div>

    <!-- Standards Section -->
    <div class="section-title" style="margin:0 0 12px;font-weight:600;font-size:15px">成熟度标准</div>
    <div class="filter-bar">
      <el-input v-model="stdQuery.keyword" placeholder="搜索品种" clearable style="width:200px" @clear="loadStandards" @keyup.enter="loadStandards" />
      <el-button type="primary" @click="loadStandards" icon="Search">查询</el-button>
    </div>
    <el-table :data="standardsData" stripe v-loading="stdLoading" border style="margin-bottom:32px">
      <el-table-column label="品种" width="160">
        <template #default="{ row }">{{ varietyMap[row.variety] || row.variety }}</template>
      </el-table-column>
      <el-table-column label="糖度范围(°Bx)" width="160" class-name="nums-tabular">
        <template #default="{ row }">{{ row.brixMin }} ~ {{ row.brixMax }}</template>
      </el-table-column>
      <el-table-column label="硬度范围" width="140" class-name="nums-tabular">
        <template #default="{ row }">{{ row.firmnessMin }} ~ {{ row.firmnessMax }}</template>
      </el-table-column>
      <el-table-column label="目标色值" width="150">
        <template #default="{ row }">
          <span v-if="row.colorTarget" style="display:inline-flex;align-items:center;gap:6px">
            <span :style="{ display:'inline-block', width:'14px', height:'14px', borderRadius:'2px', backgroundColor:'#' + row.colorTarget }"></span>
            #{{ row.colorTarget }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="description" label="说明" min-width="200" show-overflow-tooltip />
    </el-table>

    <!-- Records Section -->
    <div class="section-title" style="margin:0 0 12px;font-weight:600;font-size:15px">成熟度记录</div>
    <div class="filter-bar">
      <el-select v-model="recQuery.orchardId" placeholder="选择果园" clearable style="width:240px" @change="loadRecords" @clear="clearRecords">
        <el-option v-for="o in orchardList" :key="o.id" :label="o.orchardName" :value="o.id" />
      </el-select>
      <el-button type="primary" @click="loadRecords" icon="Search">查询记录</el-button>
    </div>
    <el-table :data="recordsData" stripe v-loading="recLoading" border>
      <el-table-column prop="orchardId" label="果园ID" width="100" />
      <el-table-column prop="variety" label="品种" width="120" />
      <el-table-column prop="sampleDate" label="采样日期" width="130" />
      <el-table-column prop="brix" label="糖度(°Bx)" width="120" class-name="nums-tabular" />
      <el-table-column prop="firmness" label="硬度(kg/cm²)" width="130" class-name="nums-tabular" />
      <el-table-column label="色值" width="110">
        <template #default="{ row }">
          <span v-if="row.colorRgb" style="display:inline-flex;align-items:center;gap:6px">
            <span :style="{ display:'inline-block', width:'14px', height:'14px', borderRadius:'2px', backgroundColor:'#' + row.colorRgb }"></span>
            #{{ row.colorRgb }}
          </span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="accumulateTemp" label="积温" width="90" class-name="nums-tabular" />
      <el-table-column prop="maturityScore" label="成熟度评分" width="120" class-name="nums-tabular" />
      <el-table-column label="推荐" width="100">
        <template #default="{ row }">
          <el-tag :type="row.recommendation === 'OPTIMAL' ? 'success' : row.recommendation === 'OVERRIPE' ? 'danger' : 'warning'" size="small" v-if="row.recommendation">
            {{ { UNRIPE: '未熟', OPTIMAL: '最佳', OVERRIPE: '过熟' }[row.recommendation] || row.recommendation }}
          </el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="operator" label="操作人" width="120" />
      <el-table-column prop="remark" label="备注" min-width="160" show-overflow-tooltip />
    </el-table>
    <div v-if="recTotal > 0" style="margin-top:12px;text-align:right;color:#909399;font-size:13px">
      共 {{ recTotal }} 条记录
    </div>

    <!-- Record Dialog -->
    <el-dialog v-model="recordDialogVisible" title="录入成熟度" width="480px">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="110px">
        <el-form-item label="果园" prop="orchardId">
          <el-select v-model="form.orchardId" placeholder="选择果园" style="width:100%">
            <el-option v-for="o in orchardList" :key="o.id" :label="o.orchardName" :value="o.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="品种">
          <el-input v-model="form.variety" placeholder="如: 红富士/嘎拉/黄元帅/秦冠" />
        </el-form-item>
        <el-form-item label="采样日期" prop="sampleDate">
          <el-date-picker v-model="form.sampleDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
        <el-form-item label="糖度(°Bx)" prop="brix">
          <el-input-number v-model="form.brix" :min="0" :precision="1" style="width:100%" />
        </el-form-item>
        <el-form-item label="硬度(kg/cm²)">
          <el-input-number v-model="form.firmness" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="色值(RGB)">
          <el-input v-model="form.colorRgb" placeholder="6位RGB hex 如 C8281E" maxlength="6" />
        </el-form-item>
        <el-form-item label="积温">
          <el-input-number v-model="form.accumulateTemp" :min="0" style="width:100%" />
        </el-form-item>
        <el-form-item label="操作人">
          <el-input v-model="form.operator" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="recordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { maturityApi, orchardApi } from '@/api/planting.js'
import { ElMessage } from 'element-plus'

const varietyMap = {
  red_fuji: '红富士',
  gala: '嘎拉',
  golden_delicious: '黄元帅(金冠)',
  qinguan: '秦冠'
}

// Standards
const stdLoading = ref(false)
const standardsData = ref([])
const stdQuery = reactive({ keyword: '' })

// Orchards for dropdown
const orchardList = ref([])

// Records
const recLoading = ref(false)
const recordsData = ref([])
const recTotal = ref(0)
const recQuery = reactive({ orchardId: null, limit: 50 })

// Record form
const recordDialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref()
const form = reactive({
  orchardId: null,
  variety: '',
  sampleDate: '',
  brix: 0,
  firmness: 0,
  colorRgb: '',
  accumulateTemp: null,
  operator: '',
  remark: ''
})
const formRules = {
  orchardId: [{ required: true, message: '请输入果园ID', trigger: 'blur' }],
  brix: [{ required: true, message: '请输入糖度', trigger: 'blur' }],
  sampleDate: [{ required: true, message: '请选择采样日期', trigger: 'change' }]
}

async function loadStandards() {
  stdLoading.value = true
  try {
    const res = await maturityApi.standards(stdQuery)
    standardsData.value = res?.records || res?.list || res || []
  } catch (e) {
    console.error(e)
  } finally {
    stdLoading.value = false
  }
}

async function loadRecords() {
  if (!recQuery.orchardId) return
  recLoading.value = true
  try {
    const res = await maturityApi.records(recQuery.orchardId, { limit: recQuery.limit })
    const list = Array.isArray(res) ? res : (res?.records || res?.list || [])
    recordsData.value = list
    recTotal.value = list.length
  } catch (e) {
    console.error(e)
  } finally {
    recLoading.value = false
  }
}

function clearRecords() {
  recordsData.value = []
  recTotal.value = 0
}

function openRecordDialog() {
  Object.assign(form, { orchardId: null, variety: '', sampleDate: '', brix: 0, firmness: 0, colorRgb: '', accumulateTemp: null, operator: '', remark: '' })
  recordDialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    await maturityApi.record(form)
    ElMessage.success('成熟度记录成功')
    recordDialogVisible.value = false
    if (recQuery.orchardId) loadRecords()
  } catch (e) {
    // error shown by interceptor
  } finally {
    submitting.value = false
  }
}

async function loadOrchards() {
  try {
    const res = await orchardApi.list({ page: 1, size: 200 })
    orchardList.value = res?.records || res?.list || []
    // 自动选中第一个果园并加载记录
    if (orchardList.value.length > 0 && !recQuery.orchardId) {
      recQuery.orchardId = orchardList.value[0].id
      loadRecords()
    }
  } catch (e) { console.error(e) }
}

onMounted(() => {
  loadStandards()
  loadOrchards()
})
</script>
