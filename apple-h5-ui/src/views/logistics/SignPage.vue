<template>
  <div class="page">
    <van-nav-bar title="物流签收" />

    <van-cell-group inset title="配送单号">
      <van-field
        v-model="code"
        placeholder="请输入或扫描配送单号（如 DLV...）"
        clearable
        :border="false"
      >
        <template #button>
          <van-button
            size="small"
            type="primary"
            :loading="loading"
            @click="onLookup"
          >
            查询
          </van-button>
        </template>
      </van-field>
    </van-cell-group>

    <div v-if="delivery" class="page__delivery">
      <van-cell-group inset title="配送信息">
        <van-cell
          title="配送单号"
          :value="delivery.deliveryCode || '-'"
          class="nums-tabular"
        />
        <van-cell title="当前状态">
          <template #value>
            <van-tag :type="statusTagType" round>
              {{ statusLabel }}
            </van-tag>
          </template>
        </van-cell>
        <van-cell title="收货人" :value="delivery.receiverName || '-'" />
        <van-cell title="联系电话" :value="delivery.receiverPhone || '-'" />
        <van-cell title="收货地址" :value="delivery.receiverAddr || '-'" />
      </van-cell-group>

      <van-cell-group inset title="签收确认">
        <van-field
          v-model="form.qualityRemark"
          label="质检备注"
          placeholder="包装/温度/外观情况"
          maxlength="100"
        />
        <van-field label="质检结论">
          <template #input>
            <van-radio-group v-model="form.qualityCheck" direction="horizontal">
              <van-radio name="PASSED">合格</van-radio>
              <van-radio name="REJECTED">不合格</van-radio>
            </van-radio-group>
          </template>
        </van-field>
        <van-field
          v-model="form.remark"
          type="textarea"
          rows="2"
          autosize
          label="签收备注"
          placeholder="可选"
          maxlength="200"
        />
      </van-cell-group>

      <div class="page__actions">
        <van-button
          block
          round
          type="primary"
          :loading="signing"
          :disabled="isFinalState"
          @click="onSign"
        >
          {{ isFinalState ? '已完结' : '确认签收' }}
        </van-button>
      </div>
    </div>

    <van-empty v-else-if="!loading && searched" description="未找到该配送单" />
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue';
import {
  NavBar as VanNavBar,
  CellGroup as VanCellGroup,
  Cell as VanCell,
  Field as VanField,
  Button as VanButton,
  Tag as VanTag,
  RadioGroup as VanRadioGroup,
  Radio as VanRadio,
  Empty as VanEmpty,
  showToast,
  showSuccessToast,
} from 'vant';
import {
  logisticsApi,
  DELIVERY_STATUS_LABEL,
  type Delivery,
  type DeliveryStatus,
  type DeliveryQualityCheck,
} from '@apple/shared-core';

const code = ref('');
const loading = ref(false);
const searched = ref(false);
const signing = ref(false);
const delivery = ref<Delivery | null>(null);

const form = reactive<{
  qualityCheck: DeliveryQualityCheck;
  qualityRemark: string;
  remark: string;
}>({
  qualityCheck: 'PASSED',
  qualityRemark: '',
  remark: '',
});

const statusLabel = computed<string>(() => {
  const s = delivery.value?.status as DeliveryStatus | undefined;
  return s ? DELIVERY_STATUS_LABEL[s] : '-';
});
const statusTagType = computed<'primary' | 'success' | 'warning' | 'danger'>(() => {
  switch (delivery.value?.status) {
    case 'SIGNED':
      return 'success';
    case 'REJECTED':
      return 'danger';
    case 'DELIVERING':
      return 'primary';
    default:
      return 'warning';
  }
});
const isFinalState = computed(
  () =>
    delivery.value?.status === 'SIGNED' || delivery.value?.status === 'REJECTED'
);

async function onLookup(): Promise<void> {
  if (!code.value.trim()) {
    showToast('请输入配送单号');
    return;
  }
  loading.value = true;
  searched.value = true;
  delivery.value = null;
  try {
    delivery.value = await logisticsApi.findDeliveryByCode(code.value.trim());
  } catch {
    // onError already toasted
  } finally {
    loading.value = false;
  }
}

async function onSign(): Promise<void> {
  if (!delivery.value?.id) return;
  signing.value = true;
  try {
    // Backend DeliveryController.sign(@PathVariable id, @RequestBody Delivery)
    const updated = await logisticsApi.acknowledgeDelivery(delivery.value.id, {
      qualityCheck: form.qualityCheck,
      qualityRemark: form.qualityRemark,
      remark: form.remark,
      // signTime: backend fills server-side per contract; we don't spoof it
    });
    delivery.value = updated;
    showSuccessToast('签收成功');
  } catch {
    // toasted
  } finally {
    signing.value = false;
  }
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: var(--color-surface-base);
  padding-bottom: var(--space-8);
}
.page__delivery {
  margin-top: var(--space-2);
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
