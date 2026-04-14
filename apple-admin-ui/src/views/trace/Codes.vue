<template>
  <div class="page-container">
    <div class="page-header">
      <h2>溯源码管理</h2>
    </div>

    <el-row :gutter="20" style="margin-bottom:20px">
      <!-- Generate BOX Code -->
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <span style="font-weight:600">生成 BOX 码</span>
          </template>
          <el-form ref="boxFormRef" :model="boxForm" :rules="boxRules" label-width="100px">
            <el-form-item label="批次 ID" prop="batchId">
              <el-input-number v-model="boxForm.batchId" :min="1" class="nums-tabular" style="width:100%" placeholder="请输入批次 ID" />
            </el-form-item>
            <el-form-item label="生成数量" prop="boxCount">
              <el-input-number v-model="boxForm.boxCount" :min="1" :max="1000" class="nums-tabular" style="width:160px" />
            </el-form-item>
          </el-form>
          <div style="text-align:right">
            <el-button type="primary" @click="handleGenerateBox" :loading="boxLoading">生成 BOX 码</el-button>
          </div>
          <div v-if="boxResult" style="margin-top:16px">
            <el-alert type="success" :closable="false">
              <template #title>
                生成成功，共 <span class="nums-tabular">{{ boxResultCodes.length }}</span> 个 BOX 码
              </template>
              <template v-if="boxResultCodes.length">
                <div style="margin-top:8px;font-size:13px;word-break:break-all;color:var(--el-text-color-secondary)">
                  {{ boxResultCodes.slice(0, 5).join(', ') }}{{ boxResultCodes.length > 5 ? ' ...' : '' }}
                </div>
              </template>
            </el-alert>
          </div>
        </el-card>
      </el-col>

      <!-- Generate FRUIT Code -->
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <span style="font-weight:600">生成 FRUIT 码</span>
          </template>
          <el-form ref="fruitFormRef" :model="fruitForm" :rules="fruitRules" label-width="100px">
            <el-form-item label="BOX 码" prop="boxCode">
              <el-input v-model="fruitForm.boxCode" placeholder="请输入 BOX 码" />
            </el-form-item>
            <el-form-item label="生成数量" prop="fruitCount">
              <el-input-number v-model="fruitForm.fruitCount" :min="1" :max="10000" class="nums-tabular" style="width:160px" />
            </el-form-item>
          </el-form>
          <div style="text-align:right">
            <el-button type="primary" @click="handleGenerateFruit" :loading="fruitLoading">生成 FRUIT 码</el-button>
          </div>
          <div v-if="fruitResult" style="margin-top:16px">
            <el-alert type="success" :closable="false">
              <template #title>
                生成成功，共 <span class="nums-tabular">{{ fruitResultCodes.length }}</span> 个 FRUIT 码
              </template>
              <template v-if="fruitResultCodes.length">
                <div style="margin-top:8px;font-size:13px;word-break:break-all;color:var(--el-text-color-secondary)">
                  {{ fruitResultCodes.slice(0, 5).join(', ') }}{{ fruitResultCodes.length > 5 ? ' ...' : '' }}
                </div>
              </template>
            </el-alert>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Verify Code -->
    <el-card shadow="never">
      <template #header>
        <span style="font-weight:600">验证溯源码</span>
      </template>
      <div style="display:flex;gap:12px;align-items:flex-start;max-width:560px">
        <el-input
          v-model="verifyCode"
          placeholder="请输入溯源码"
          clearable
          @keyup.enter="handleVerify"
          style="flex:1"
        />
        <el-button type="primary" @click="handleVerify" :loading="verifyLoading">验证</el-button>
      </div>
      <div v-if="verifyResult !== null" style="margin-top:20px">
        <el-result
          v-if="verifyResult.valid"
          icon="success"
          :title="verifyResult.code || verifyCode"
          sub-title="溯源码有效"
        >
          <template #extra>
            <el-descriptions :column="2" border size="small" style="min-width:400px">
              <el-descriptions-item label="层级">{{ granularityLabel(verifyResult.granularity) }}</el-descriptions-item>
              <el-descriptions-item label="状态">{{ verifyResult.status || '-' }}</el-descriptions-item>
              <el-descriptions-item label="父级码">{{ verifyResult.parentCode || '-' }}</el-descriptions-item>
              <el-descriptions-item label="批次 ID" class-name="nums-tabular">{{ verifyResult.batchId || '-' }}</el-descriptions-item>
            </el-descriptions>
          </template>
        </el-result>
        <el-result
          v-else
          icon="error"
          :title="verifyCode"
          :sub-title="verifyResult.message || '溯源码无效或不存在'"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { codeApi } from '@/api/trace.js'
import { ElMessage } from 'element-plus'

// BOX form
const boxFormRef = ref()
const boxLoading = ref(false)
const boxResult = ref(null)
const boxResultCodes = ref([])
const boxForm = reactive({ batchId: null, boxCount: 10 })
const boxRules = {
  batchId: [{ required: true, message: '请输入批次 ID', trigger: 'blur' }],
  boxCount: [{ required: true, message: '请输入生成数量', trigger: 'blur' }],
}

// FRUIT form
const fruitFormRef = ref()
const fruitLoading = ref(false)
const fruitResult = ref(null)
const fruitResultCodes = ref([])
const fruitForm = reactive({ boxCode: '', fruitCount: 100 })
const fruitRules = {
  boxCode: [{ required: true, message: '请输入 BOX 码', trigger: 'blur' }],
  fruitCount: [{ required: true, message: '请输入生成数量', trigger: 'blur' }],
}

// Verify
const verifyCode = ref('')
const verifyLoading = ref(false)
const verifyResult = ref(null)

function granularityLabel(granularity) {
  const map = { BATCH: '批次级', BOX: '箱级', FRUIT: '果级' }
  return map[granularity] || granularity || '-'
}

async function handleGenerateBox() {
  const valid = await boxFormRef.value.validate().catch(() => false)
  if (!valid) return
  boxLoading.value = true
  boxResult.value = null
  boxResultCodes.value = []
  try {
    const res = await codeApi.generateBox(boxForm.batchId, boxForm.boxCount)
    boxResult.value = res
    boxResultCodes.value = (Array.isArray(res) ? res : []).map(tc => tc.code)
    ElMessage.success(`BOX 码生成成功，共 ${boxResultCodes.value.length} 个`)
  } catch (e) {
    // error shown by interceptor
  } finally {
    boxLoading.value = false
  }
}

async function handleGenerateFruit() {
  const valid = await fruitFormRef.value.validate().catch(() => false)
  if (!valid) return
  fruitLoading.value = true
  fruitResult.value = null
  fruitResultCodes.value = []
  try {
    const res = await codeApi.generateFruit(fruitForm.boxCode, fruitForm.fruitCount)
    fruitResult.value = res
    fruitResultCodes.value = (Array.isArray(res) ? res : []).map(tc => tc.code)
    ElMessage.success(`FRUIT 码生成成功，共 ${fruitResultCodes.value.length} 个`)
  } catch (e) {
    // error shown by interceptor
  } finally {
    fruitLoading.value = false
  }
}

async function handleVerify() {
  if (!verifyCode.value.trim()) {
    ElMessage.warning('请输入溯源码')
    return
  }
  verifyLoading.value = true
  verifyResult.value = null
  try {
    const res = await codeApi.verify(verifyCode.value.trim())
    verifyResult.value = res || { valid: false }
  } catch (e) {
    verifyResult.value = { valid: false }
  } finally {
    verifyLoading.value = false
  }
}
</script>
