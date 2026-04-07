<template>
  <div class="maturity-record-page">
    <van-nav-bar title="成熟度采样录入" left-arrow @click-left="$router.back()" />

    <van-form @submit="handleSubmit">
      <van-cell-group inset>
        <van-field
          v-model.number="form.orchardId"
          label="果园ID"
          type="number"
          placeholder="请输入果园ID"
          :rules="[{ required: true, message: '请填写果园ID' }]"
        />

        <van-field
          v-model="varietyLabel"
          label="苹果品种"
          is-link
          readonly
          placeholder="请选择品种"
          @click="showVarietyPicker = true"
          :rules="[{ required: true, message: '请选择品种' }]"
        />
        <van-popup v-model:show="showVarietyPicker" position="bottom" round>
          <van-picker
            :columns="varietyOptions"
            @confirm="onPickVariety"
            @cancel="showVarietyPicker = false"
            show-toolbar
          />
        </van-popup>

        <van-field
          v-model="form.sampleDate"
          label="采样日期"
          is-link
          readonly
          @click="showDatePicker = true"
          placeholder="选择采样日期"
        />
        <van-popup v-model:show="showDatePicker" position="bottom" round>
          <van-date-picker
            v-model="datePickerValue"
            title="选择采样日期"
            @confirm="onPickDate"
            @cancel="showDatePicker = false"
          />
        </van-popup>

        <van-field
          v-model.number="form.brix"
          label="糖度 (Brix)"
          type="number"
          placeholder="例: 14.5"
          :rules="[{ required: true, message: '请填写糖度' }]"
        />

        <van-field
          v-model.number="form.firmness"
          label="硬度 (kg/cm²)"
          type="number"
          placeholder="例: 7.5"
          :rules="[{ required: true, message: '请填写硬度' }]"
        />

        <van-field
          v-model="form.colorRgb"
          label="色泽 RGB"
          placeholder="6位 HEX，例如 C8281E"
          maxlength="6"
          :rules="[{ required: true, message: '请填写色泽' }]"
        />

        <van-field
          v-model.number="form.accumulateTemp"
          label="积温 (°C·d)"
          type="number"
          placeholder="例: 3200"
          :rules="[{ required: true, message: '请填写积温' }]"
        />

        <van-field
          v-model="form.operator"
          label="采样人"
          placeholder="请输入采样人姓名"
        />

        <van-field
          v-model="form.remark"
          label="备注"
          placeholder="选填"
          type="textarea"
          autosize
        />
      </van-cell-group>

      <div class="actions">
        <van-button block round type="primary" native-type="submit" :loading="submitting">
          提交采样
        </van-button>
        <van-button
          block
          round
          plain
          type="primary"
          style="margin-top: 12px"
          @click="goRecommend"
          :disabled="!form.orchardId"
        >
          查看采收推荐
        </van-button>
      </div>
    </van-form>

    <van-cell-group v-if="lastResult" inset title="最近一次评分结果" style="margin-top: 16px">
      <van-cell title="评分" :value="lastResult.maturityScore" />
      <van-cell title="状态">
        <template #value>
          <van-tag :type="statusTag(lastResult.recommendation)">{{ statusLabel(lastResult.recommendation) }}</van-tag>
        </template>
      </van-cell>
      <van-cell title="提交时间" :value="lastResult.createTime || '-'" />
    </van-cell-group>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showFailToast } from 'vant'
import { maturityApi } from '@/api/maturity.js'

const router = useRouter()

const VARIETY_LABELS = {
  red_fuji: '红富士',
  gala: '嘎拉',
  golden_delicious: '黄元帅'
}

const varietyOptions = ref([
  { text: '红富士', value: 'red_fuji' },
  { text: '嘎拉', value: 'gala' },
  { text: '黄元帅', value: 'golden_delicious' }
])

const showVarietyPicker = ref(false)
const showDatePicker = ref(false)
const submitting = ref(false)
const lastResult = ref(null)
const datePickerValue = ref([])

const today = new Date().toISOString().slice(0, 10)
const form = reactive({
  orchardId: '',
  variety: '',
  sampleDate: today,
  brix: '',
  firmness: '',
  colorRgb: '',
  accumulateTemp: '',
  operator: '',
  remark: ''
})

const varietyLabel = computed(() => VARIETY_LABELS[form.variety] || '')

function onPickVariety({ selectedOptions }) {
  form.variety = selectedOptions[0].value
  showVarietyPicker.value = false
}

function onPickDate({ selectedValues }) {
  form.sampleDate = selectedValues.join('-')
  showDatePicker.value = false
}

function statusTag(s) {
  return { OPTIMAL: 'success', UNRIPE: 'warning', OVERRIPE: 'danger' }[s] || 'default'
}

function statusLabel(s) {
  return { OPTIMAL: '最佳采收', UNRIPE: '尚未成熟', OVERRIPE: '已过熟' }[s] || s || '-'
}

async function handleSubmit() {
  if (!form.variety) {
    showFailToast('请先选择品种')
    return
  }
  submitting.value = true
  try {
    const res = await maturityApi.recordMeasurement({ ...form })
    lastResult.value = res
    showToast({ type: 'success', message: `采样已记录，评分 ${res.maturityScore}` })
  } catch (e) {
    showFailToast(e?.message || '提交失败')
  } finally {
    submitting.value = false
  }
}

function goRecommend() {
  router.push(`/planting/harvest-recommend?orchardId=${form.orchardId}`)
}

onMounted(() => {
  // initialise date picker default
  const [y, m, d] = today.split('-')
  datePickerValue.value = [y, m, d]
})
</script>

<style scoped>
.maturity-record-page { padding-bottom: 32px; background: #f7f8fa; min-height: 100vh; }
.actions { padding: 16px; }
</style>
