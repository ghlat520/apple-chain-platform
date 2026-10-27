<template>
  <div class="page">
    <van-nav-bar title="产品溯源" left-arrow @click-left="goBack" />

    <div v-if="loading" class="page__state">
      <van-loading size="24" vertical>加载中…</van-loading>
    </div>

    <div v-else-if="error" class="page__state">
      <van-empty :description="error">
        <van-button round type="primary" size="small" @click="load">
          重试
        </van-button>
      </van-empty>
    </div>

    <template v-else-if="chain">
      <van-cell-group inset title="批次信息">
        <van-cell
          title="溯源码"
          :value="chain.traceCode || code"
          class="nums-tabular"
        />
        <van-cell
          v-if="chain.batch?.batchCode"
          title="批次码"
          :value="chain.batch.batchCode"
        />
        <van-cell
          v-if="chain.batch?.variety"
          title="品种"
          :value="chain.batch.variety"
        />
        <van-cell
          v-if="chain.batch?.harvestDate"
          title="采收日期"
          :value="formatDate(chain.batch.harvestDate)"
        />
        <van-cell
          v-if="chain.batch?.grade"
          title="等级"
          :value="`${chain.batch.grade} 级`"
        />
        <van-cell
          v-if="chain.batch?.weight != null"
          title="重量"
          :value="`${chain.batch.weight} kg`"
          class="nums-tabular"
        />
        <van-cell
          v-if="chain.batch?.status"
          title="当前状态"
          :value="statusLabel(chain.batch.status)"
        />
      </van-cell-group>

      <van-cell-group inset title="全链路节点">
        <div v-if="chain.nodes && chain.nodes.length" class="page__timeline">
          <div
            v-for="(node, idx) in chain.nodes"
            :key="node.id ?? idx"
            class="node"
          >
            <div class="node__dot" :class="{ 'node__dot--last': idx === chain.nodes.length - 1 }" />
            <div class="node__body">
              <div class="node__title">
                {{ node.nodeName || node.nodeType || '未命名节点' }}
              </div>
              <div class="node__meta">
                {{ formatDateTime(node.operateTime) }}
                <span v-if="node.operator"> · {{ node.operator }}</span>
              </div>
              <div v-if="node.location" class="node__extra">
                📍 {{ node.location }}
              </div>
              <div v-if="node.remark" class="node__extra">
                {{ node.remark }}
              </div>
            </div>
          </div>
        </div>
        <van-empty v-else description="暂无节点数据" />
      </van-cell-group>

      <div
        v-if="chain.batch?.blockchainHash"
        class="page__hash nums-tabular"
      >
        区块链哈希：{{ chain.batch.blockchainHash }}
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import {
  NavBar as VanNavBar,
  CellGroup as VanCellGroup,
  Cell as VanCell,
  Empty as VanEmpty,
  Loading as VanLoading,
  Button as VanButton,
} from 'vant';
import {
  traceApi,
  formatDate,
  formatDateTime,
  TRACE_STATUS_LABEL,
  type TraceBatchStatus,
  type TraceFullChain,
} from '@apple/shared-core';

const route = useRoute();
const router = useRouter();
const code = ref<string>(String(route.params.code || ''));

const chain = ref<TraceFullChain | null>(null);
const loading = ref(false);
const error = ref<string | null>(null);

function goBack(): void {
  if (window.history.length > 1) router.back();
  else void router.replace('/m/planting/record');
}

function statusLabel(s: string): string {
  return (TRACE_STATUS_LABEL as Record<string, string>)[s] ?? s;
}

async function load(): Promise<void> {
  if (!code.value) {
    error.value = '溯源码为空';
    return;
  }
  loading.value = true;
  error.value = null;
  try {
    chain.value = await traceApi.findTraceFullChain(code.value);
  } catch (e) {
    error.value = (e as Error).message || '查询失败';
  } finally {
    loading.value = false;
  }
}

onMounted(load);
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: var(--color-surface-base);
  padding-bottom: var(--space-8);
}
.page__state {
  padding: var(--space-8) var(--space-4);
  text-align: center;
}
.page__timeline {
  padding: var(--space-4);
}
.node {
  position: relative;
  padding-left: var(--space-6);
  padding-bottom: var(--space-5);
}
.node:last-child {
  padding-bottom: 0;
}
.node__dot {
  position: absolute;
  left: 4px;
  top: 6px;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: var(--color-brand-primary);
  box-shadow: 0 0 0 3px var(--color-brand-primary-light);
}
.node:not(:last-child)::before {
  content: '';
  position: absolute;
  left: 8px;
  top: 18px;
  bottom: 0;
  width: 2px;
  background: var(--color-border-default);
}
.node__title {
  font-size: 16px;
  font-weight: 500;
  color: var(--color-text-primary);
  margin-bottom: var(--space-1);
}
.node__meta {
  font-size: 13px;
  color: var(--color-text-secondary);
}
.node__extra {
  margin-top: var(--space-1);
  font-size: 14px;
  color: var(--color-text-secondary);
  line-height: 1.5;
}
.page__hash {
  padding: var(--space-4);
  color: var(--color-text-tertiary);
  font-size: 12px;
  word-break: break-all;
  text-align: center;
}
:deep(.van-cell-group__title) {
  padding: var(--space-4) var(--space-4) var(--space-2);
  font-size: 14px;
  color: var(--color-text-secondary);
}
</style>
