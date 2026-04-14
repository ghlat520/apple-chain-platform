<template>
  <div class="page">
    <van-nav-bar title="上传种植记录" />

    <van-form class="page__form" @submit="onSubmit">
      <van-cell-group inset title="基本信息">
        <van-field
          v-model="orchardDisplay"
          label="果园"
          is-link
          readonly
          placeholder="选择果园"
          :rules="[{ required: true, message: '请选择果园' }]"
          @click="showOrchardPicker = true"
        />

        <van-field
          v-model="recordTypeLabel"
          label="作业类型"
          is-link
          readonly
          placeholder="选择作业类型"
          :rules="[{ required: true, message: '请选择作业类型' }]"
          @click="showTypePicker = true"
        />

        <van-field
          v-model="form.operateDate"
          label="作业日期"
          is-link
          readonly
          placeholder="选择日期"
          :rules="[{ required: true, message: '请选择作业日期' }]"
          @click="showDatePicker = true"
        />

        <van-field
          v-model="form.operator"
          label="作业人"
          placeholder="姓名"
          maxlength="20"
        />

        <van-field
          v-model="form.weather"
          label="天气"
          placeholder="如 晴 / 阴 / 小雨"
          maxlength="20"
        />
      </van-cell-group>

      <van-cell-group inset title="现场照片（可选）">
        <van-uploader
          v-model="photos"
          class="page__uploader"
          :max-count="4"
          :max-size="5 * 1024 * 1024"
          @oversize="onOversize"
        />
      </van-cell-group>

      <van-cell-group inset title="GPS 定位">
        <van-cell>
          <template #title>
            <span v-if="gpsState === 'ok'">
              经度 {{ gps.lng.toFixed(6) }} · 纬度 {{ gps.lat.toFixed(6) }}
            </span>
            <span v-else-if="gpsState === 'loading'">定位中…</span>
            <span v-else>未获取到位置，请手动填写</span>
          </template>
          <template #right-icon>
            <van-button size="small" plain @click="locate">重新定位</van-button>
          </template>
        </van-cell>
        <van-field
          v-if="gpsState !== 'ok'"
          v-model="form.location"
          label="地点"
          placeholder="请手动输入作业地点"
        />
      </van-cell-group>

      <van-cell-group inset title="备注">
        <van-field
          v-model="form.notes"
          type="textarea"
          rows="3"
          autosize
          placeholder="施药品种 / 用量 / 病虫害观察等"
          maxlength="300"
          show-word-limit
        />
      </van-cell-group>

      <div class="page__actions">
        <van-button
          block
          round
          type="primary"
          native-type="submit"
          :loading="submitting"
        >
          提交记录
        </van-button>
      </div>
    </van-form>

    <!-- Pickers -->
    <van-popup v-model:show="showOrchardPicker" position="bottom" round>
      <van-picker
        title="选择果园"
        :columns="orchardColumns"
        @confirm="onOrchardConfirm"
        @cancel="showOrchardPicker = false"
      />
    </van-popup>

    <van-popup v-model:show="showTypePicker" position="bottom" round>
      <van-picker
        title="作业类型"
        :columns="typeColumns"
        @confirm="onTypeConfirm"
        @cancel="showTypePicker = false"
      />
    </van-popup>

    <van-popup v-model:show="showDatePicker" position="bottom" round>
      <van-date-picker
        v-model="dateValue"
        :min-date="minDate"
        :max-date="maxDate"
        title="选择作业日期"
        @confirm="onDateConfirm"
        @cancel="showDatePicker = false"
      />
    </van-popup>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, computed } from 'vue';
import {
  NavBar as VanNavBar,
  Form as VanForm,
  Field as VanField,
  CellGroup as VanCellGroup,
  Cell as VanCell,
  Button as VanButton,
  Uploader as VanUploader,
  Popup as VanPopup,
  Picker as VanPicker,
  DatePicker as VanDatePicker,
  showToast,
  showSuccessToast,
  type UploaderFileListItem,
} from 'vant';
import {
  plantingApi,
  GROWTH_RECORD_TYPE_LABEL,
  type GrowthRecord,
  type GrowthRecordType,
  type Orchard,
} from '@apple/shared-core';

// ---------- State ----------
const form = reactive<GrowthRecord & { location?: string }>({
  orchardId: undefined,
  recordType: undefined,
  operateDate: '',
  operator: '',
  weather: '',
  notes: '',
  location: '',
});

const orchardDisplay = ref('');
const recordTypeLabel = ref('');
const photos = ref<UploaderFileListItem[]>([]);
const submitting = ref(false);

const showOrchardPicker = ref(false);
const showTypePicker = ref(false);
const showDatePicker = ref(false);

const orchards = ref<Orchard[]>([]);
const orchardColumns = computed(() =>
  orchards.value.map((o) => ({
    text: `${o.orchardName || '(未命名)'} · ${o.variety || ''}`,
    value: o.id,
  }))
);

const typeColumns = (
  Object.entries(GROWTH_RECORD_TYPE_LABEL) as [GrowthRecordType, string][]
).map(([value, text]) => ({ text, value }));

const now = new Date();
const minDate = new Date(now.getFullYear() - 1, 0, 1);
const maxDate = new Date(now.getFullYear(), now.getMonth(), now.getDate());
const dateValue = ref<string[]>([
  String(now.getFullYear()),
  String(now.getMonth() + 1).padStart(2, '0'),
  String(now.getDate()).padStart(2, '0'),
]);

type GpsState = 'idle' | 'loading' | 'ok' | 'fail';
const gpsState = ref<GpsState>('idle');
const gps = reactive({ lng: 0, lat: 0 });

// ---------- Load orchards ----------
onMounted(async () => {
  try {
    const page = await plantingApi.findOrchards({ page: 1, size: 50 });
    orchards.value = page.records ?? [];
  } catch {
    // toast already shown via configureApi.onError
  }
  locate();
});

// ---------- Handlers ----------
function onOrchardConfirm(e: { selectedOptions: Array<{ text: string; value: number }> }): void {
  const sel = e.selectedOptions[0];
  if (sel) {
    form.orchardId = sel.value;
    orchardDisplay.value = sel.text;
  }
  showOrchardPicker.value = false;
}

function onTypeConfirm(e: {
  selectedOptions: Array<{ text: string; value: GrowthRecordType }>;
}): void {
  const sel = e.selectedOptions[0];
  if (sel) {
    form.recordType = sel.value;
    recordTypeLabel.value = sel.text;
  }
  showTypePicker.value = false;
}

function onDateConfirm(): void {
  form.operateDate = dateValue.value.join('-');
  showDatePicker.value = false;
}

function onOversize(): void {
  showToast('单张图片不能超过 5 MB');
}

function locate(): void {
  if (!navigator.geolocation) {
    gpsState.value = 'fail';
    showToast('当前设备不支持定位，请手动填写');
    return;
  }
  gpsState.value = 'loading';
  navigator.geolocation.getCurrentPosition(
    (pos) => {
      gps.lng = pos.coords.longitude;
      gps.lat = pos.coords.latitude;
      gpsState.value = 'ok';
    },
    () => {
      gpsState.value = 'fail';
      showToast('定位失败，请手动填写地点');
    },
    { timeout: 8000, enableHighAccuracy: true }
  );
}

async function onSubmit(): Promise<void> {
  if (!form.orchardId || !form.recordType || !form.operateDate) {
    showToast('请补全必填项');
    return;
  }
  submitting.value = true;
  try {
    const locationText =
      gpsState.value === 'ok'
        ? `${gps.lng.toFixed(6)},${gps.lat.toFixed(6)}`
        : form.location || '';
    const payload: GrowthRecord = {
      orchardId: form.orchardId,
      recordType: form.recordType,
      operateDate: form.operateDate,
      operator: form.operator,
      weather: form.weather,
      // notes carries photos count + location until a dedicated file-upload endpoint exists
      notes: [
        form.notes,
        locationText ? `[GPS]${locationText}` : '',
        photos.value.length ? `[photos]${photos.value.length}` : '',
      ]
        .filter(Boolean)
        .join(' | '),
    };
    await plantingApi.uploadGrowthRecord(payload);
    showSuccessToast('提交成功');
    // Reset (keep orchard selection for rapid consecutive entries)
    form.notes = '';
    photos.value = [];
  } catch {
    // onError already toasted
  } finally {
    submitting.value = false;
  }
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: var(--color-surface-base);
  padding-bottom: var(--space-8);
}
.page__form {
  padding-top: var(--space-2);
}
.page__uploader {
  padding: var(--space-3) var(--space-4);
}
.page__actions {
  padding: var(--space-6) var(--space-4) var(--space-4);
}
:deep(.van-cell-group__title) {
  padding: var(--space-4) var(--space-4) var(--space-2);
  font-size: 14px;
  color: var(--color-text-secondary);
}
</style>
