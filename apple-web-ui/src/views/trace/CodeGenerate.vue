<template>
  <div class="code-generate-page">
    <van-nav-bar title="一果一码 三级编码" left-arrow @click-left="$router.back()" />

    <van-cell-group inset title="Step 1 · 选择批次">
      <van-field
        v-model="state.batchId"
        label="批次 ID"
        placeholder="输入 trace_batch.id"
        type="digit"
        clearable
      />
      <van-field
        v-model="state.boxCount"
        label="箱数"
        placeholder="1 ~ 10000"
        type="digit"
      />
      <div class="btn-row">
        <van-button
          type="primary"
          block
          :loading="state.loadingBox"
          @click="handleGenerateBox"
        >生成 BOX 编码</van-button>
      </div>
    </van-cell-group>

    <van-cell-group inset title="Step 2 · BOX 列表" v-if="state.boxes.length">
      <van-cell
        v-for="b in state.boxes"
        :key="b.code"
        :title="b.code"
        :label="`CRC ${b.crc16} · ${b.status}`"
        is-link
        @click="selectBox(b)"
      >
        <template #right-icon>
          <van-tag v-if="state.selectedBox === b.code" type="success">已选</van-tag>
        </template>
      </van-cell>
    </van-cell-group>

    <van-cell-group inset title="Step 3 · 生成 FRUIT" v-if="state.selectedBox">
      <van-field :model-value="state.selectedBox" label="父级 BOX" readonly />
      <van-field
        v-model="state.fruitCount"
        label="每箱果数"
        placeholder="1 ~ 1000"
        type="digit"
      />
      <div class="btn-row">
        <van-button
          type="primary"
          block
          :loading="state.loadingFruit"
          @click="handleGenerateFruit"
        >生成 FRUIT 编码</van-button>
      </div>
    </van-cell-group>

    <van-cell-group inset title="FRUIT 预览（前 20 条）" v-if="state.fruits.length">
      <van-cell
        v-for="f in state.fruits.slice(0, 20)"
        :key="f.code"
        :title="f.code"
        :label="`CRC ${f.crc16}`"
      />
      <van-cell v-if="state.fruits.length > 20"
                :title="`共 ${state.fruits.length} 条，仅显示前 20 条`" />
    </van-cell-group>

    <van-cell-group inset title="VDP 印刷文件导出" v-if="state.batchId">
      <div class="btn-row">
        <van-button type="default" block @click="handleExportVdp('csv')">导出 CSV（VDP）</van-button>
      </div>
      <div class="btn-row">
        <van-button type="default" block @click="handleExportVdp('txt')">导出 TXT（VDP）</van-button>
      </div>
    </van-cell-group>

    <!-- QR Preview -->
    <van-popup v-model:show="state.showQr" round :style="{ padding: '24px' }">
      <div class="qr-box">
        <canvas ref="qrCanvas" />
        <p class="qr-text">{{ state.qrCode }}</p>
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { reactive, ref, nextTick } from 'vue'
import { showToast, showNotify } from 'vant'
import { traceApi } from '@/api/trace.js'
import QRCode from 'qrcode'

const state = reactive({
  batchId: '',
  boxCount: '10',
  fruitCount: '50',
  boxes: [],
  fruits: [],
  selectedBox: '',
  loadingBox: false,
  loadingFruit: false,
  showQr: false,
  qrCode: ''
})

const qrCanvas = ref(null)

async function handleGenerateBox () {
  if (!state.batchId) return showToast('请输入批次 ID')
  const n = parseInt(state.boxCount, 10)
  if (!(n > 0 && n <= 10000)) return showToast('箱数 1~10000')
  state.loadingBox = true
  try {
    const res = await traceApi.generateBox(Number(state.batchId), n)
    state.boxes = res.data || []
    state.fruits = []
    state.selectedBox = ''
    showNotify({ type: 'success', message: `已生成 ${state.boxes.length} 个 BOX` })
  } catch (e) {
    showNotify({ type: 'danger', message: e?.message || 'BOX 生成失败' })
  } finally {
    state.loadingBox = false
  }
}

async function handleGenerateFruit () {
  if (!state.selectedBox) return showToast('请先选择 BOX')
  const n = parseInt(state.fruitCount, 10)
  if (!(n > 0 && n <= 1000)) return showToast('每箱果数 1~1000')
  state.loadingFruit = true
  try {
    const res = await traceApi.generateFruit(state.selectedBox, n)
    state.fruits = res.data || []
    showNotify({ type: 'success', message: `已生成 ${state.fruits.length} 个 FRUIT` })
  } catch (e) {
    showNotify({ type: 'danger', message: e?.message || 'FRUIT 生成失败' })
  } finally {
    state.loadingFruit = false
  }
}

async function selectBox (box) {
  state.selectedBox = box.code
  state.qrCode = box.code
  state.showQr = true
  await nextTick()
  try {
    await QRCode.toCanvas(qrCanvas.value, box.code, { width: 220, margin: 1 })
  } catch (e) {
    showToast('二维码渲染失败')
  }
}

function handleExportVdp (format) {
  if (!state.batchId) return showToast('请先输入批次 ID')
  traceApi.exportVdp(state.batchId, format)
}
</script>

<style scoped>
.code-generate-page {
  padding-bottom: 32px;
  background: #f7f8fa;
  min-height: 100vh;
}

.btn-row {
  padding: 12px 16px;
}

.qr-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 260px;
}

.qr-text {
  margin: 12px 0 0;
  font-size: 12px;
  color: #666;
  word-break: break-all;
  text-align: center;
}
</style>
