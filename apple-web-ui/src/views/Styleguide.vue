<template>
  <div class="styleguide">
    <!-- 页头 + 密度切换 -->
    <header class="sg-header">
      <div>
        <h1 class="sg-title">Styleguide</h1>
        <p class="sg-subtitle">apple-chain-platform · UI 视觉 QA 唯一真源</p>
      </div>
      <div class="sg-controls">
        <select v-model="density" class="sg-select" @change="applyDensity">
          <option value="spacious">Spacious · 果农/司机</option>
          <option value="comfortable">Comfortable · 默认</option>
          <option value="compact">Compact · 管理员</option>
        </select>
      </div>
    </header>

    <!-- 1. 色板 -->
    <section class="sg-section">
      <h2 class="sg-h2">1. Color Palette</h2>

      <h3 class="sg-h3">Brand</h3>
      <div class="sg-swatches">
        <div v-for="(val, key) in color.brand" :key="key" class="sg-swatch">
          <div class="sg-swatch-box" :style="{ backgroundColor: val }" />
          <div>
            <div class="sg-swatch-name">{{ key }}</div>
            <code class="sg-swatch-val">{{ val }}</code>
          </div>
        </div>
      </div>

      <h3 class="sg-h3">Accent</h3>
      <div class="sg-swatches">
        <div v-for="(val, key) in color.accent" :key="key" class="sg-swatch">
          <div class="sg-swatch-box" :style="{ backgroundColor: val }" />
          <div>
            <div class="sg-swatch-name">{{ key }}</div>
            <code class="sg-swatch-val">{{ val }}</code>
          </div>
        </div>
      </div>

      <h3 class="sg-h3">Text</h3>
      <div class="sg-swatches">
        <div v-for="(val, key) in color.text" :key="key" class="sg-swatch">
          <div class="sg-swatch-box" :style="{ backgroundColor: val }" />
          <div>
            <div class="sg-swatch-name">{{ key }}</div>
            <code class="sg-swatch-val">{{ val }}</code>
          </div>
        </div>
      </div>

      <h3 class="sg-h3">Semantic</h3>
      <div class="sg-swatches">
        <div v-for="(val, key) in color.semantic" :key="key" class="sg-swatch">
          <div class="sg-swatch-box" :style="{ backgroundColor: val }" />
          <div>
            <div class="sg-swatch-name">{{ key }}</div>
            <code class="sg-swatch-val">{{ val }}</code>
          </div>
        </div>
      </div>

      <h3 class="sg-h3">IoT · 冷链数据</h3>
      <div class="sg-swatches">
        <div v-for="(val, key) in color.iot" :key="key" class="sg-swatch">
          <div class="sg-swatch-box" :style="{ backgroundColor: val }" />
          <div>
            <div class="sg-swatch-name">{{ key }}</div>
            <code class="sg-swatch-val">{{ val }}</code>
          </div>
        </div>
      </div>
    </section>

    <!-- 2. Typography -->
    <section class="sg-section">
      <h2 class="sg-h2">2. Typography</h2>
      <div style="font-size: 28px; line-height: 1.3; font-weight: 600;">Display L · 苹果产业链平台</div>
      <div style="font-size: 22px; line-height: 1.35; font-weight: 600;">Heading 1 · 果园溯源档案</div>
      <div style="font-size: 18px; line-height: 1.4; font-weight: 600;">Heading 2 · 冷链运输记录</div>
      <div style="font-size: 16px; line-height: 1.5; font-weight: 500;">Heading 3 · 订单详情</div>
      <div style="font-size: 16px; line-height: 1.6; font-weight: 400;">Body · 默认正文，照顾 35–60 岁用户视力。陕西洛川县某果园 2026 年秋季采摘记录。</div>
      <div style="font-size: 14px; line-height: 1.5; font-weight: 400;">Body S · 次要正文</div>
      <div style="font-size: 13px; line-height: 1.4; color: var(--color-text-secondary);">Caption · 2026-04-11 14:30 · 创建时间</div>
      <div class="nums-tabular" style="font-size: 24px; font-weight: 500;">Number L · 2.5°C · 1,234 kg · ¥12.50</div>
    </section>

    <!-- 3. Buttons -->
    <section class="sg-section">
      <h2 class="sg-h2">3. Buttons</h2>
      <div class="sg-row">
        <button class="sg-btn sg-btn-primary">主按钮 · 创建果园</button>
        <button class="sg-btn sg-btn-primary" disabled>禁用态</button>
        <button class="sg-btn sg-btn-secondary">次按钮</button>
        <button class="sg-btn sg-btn-ghost">幽灵按钮</button>
        <button class="sg-btn sg-btn-success">溯源通过</button>
      </div>
    </section>

    <!-- 4. Cell 列表 -->
    <section class="sg-section">
      <h2 class="sg-h2">4. Cell · 列表原子</h2>
      <div class="sg-card">
        <div class="sg-cell">
          <div class="sg-cell-title">洛川果园 A 区</div>
          <div class="sg-cell-value">120 亩</div>
        </div>
        <div class="sg-cell">
          <div class="sg-cell-title">今日温度</div>
          <div class="sg-cell-value nums-tabular">2.5°C</div>
        </div>
        <div class="sg-cell">
          <div class="sg-cell-title">本季预估产量</div>
          <div class="sg-cell-value nums-tabular">45,000 kg</div>
        </div>
      </div>
    </section>

    <!-- 5. System States -->
    <section class="sg-section">
      <h2 class="sg-h2">5. System States</h2>
      <div class="sg-states">
        <div class="sg-state-box">
          <h3 class="sg-h3">Empty</h3>
          <div class="sg-empty">
            <div class="sg-empty-icon">📭</div>
            <div class="sg-empty-text">暂无果园记录</div>
            <button class="sg-btn sg-btn-primary" style="margin-top: 12px;">新建果园</button>
          </div>
        </div>

        <div class="sg-state-box">
          <h3 class="sg-h3">Loading · Skeleton</h3>
          <div class="sg-skeleton">
            <div class="sg-skel-line" style="width: 100%;" />
            <div class="sg-skel-line" style="width: 75%;" />
            <div class="sg-skel-line" style="width: 50%;" />
          </div>
        </div>

        <div class="sg-state-box sg-state-error">
          <h3 class="sg-h3">Error</h3>
          <p style="color: var(--color-error);">网络中断，请检查连接</p>
          <button class="sg-btn sg-btn-primary" style="margin-top: 12px;">重试</button>
        </div>

        <div class="sg-state-box sg-state-success">
          <h3 class="sg-h3">Success</h3>
          <p style="color: var(--color-success);">✓ 溯源通过</p>
        </div>
      </div>
    </section>

    <!-- 6. Tags -->
    <section class="sg-section">
      <h2 class="sg-h2">6. Tags · 状态标签</h2>
      <div class="sg-row">
        <span class="sg-tag sg-tag-success">已认证</span>
        <span class="sg-tag sg-tag-warning">待审核</span>
        <span class="sg-tag sg-tag-error">已拒绝</span>
        <span class="sg-tag sg-tag-info">冷链中</span>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { color } from '@/design/tokens'

const density = ref('comfortable')

function applyDensity() {
  document.body.setAttribute('data-density', density.value)
}

onMounted(applyDensity)
</script>

<style scoped>
.styleguide {
  min-height: 100vh;
  background-color: var(--color-surface-base);
  color: var(--color-text-primary);
  padding: var(--space-6);
  font-family: var(--font-body);
}

.sg-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-8);
  padding-bottom: var(--space-4);
  border-bottom: 1px solid var(--color-border-default);
}

.sg-title {
  font-size: 28px;
  font-weight: 600;
  margin: 0;
}

.sg-subtitle {
  color: var(--color-text-secondary);
  font-size: 14px;
  margin: 4px 0 0;
}

.sg-select {
  height: var(--density-button-height);
  padding: 0 var(--space-3);
  border: 1px solid var(--color-border-default);
  border-radius: var(--radius-base);
  background: var(--color-surface-raised);
  font-size: 14px;
  font-family: inherit;
  color: var(--color-text-primary);
}

.sg-section {
  margin-bottom: var(--space-12);
}

.sg-h2 {
  font-size: 22px;
  font-weight: 600;
  margin: 0 0 var(--space-4);
  padding-bottom: var(--space-2);
  border-bottom: 1px solid var(--color-border-default);
}

.sg-h3 {
  font-size: 16px;
  font-weight: 600;
  margin: var(--space-5) 0 var(--space-3);
  color: var(--color-text-secondary);
}

.sg-swatches {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: var(--space-3);
  margin-bottom: var(--space-4);
}

.sg-swatch {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3);
  border: 1px solid var(--color-border-default);
  border-radius: var(--radius-base);
  background: var(--color-surface-raised);
}

.sg-swatch-box {
  width: 44px;
  height: 44px;
  border-radius: var(--radius-base);
  border: 1px solid var(--color-border-default);
  flex-shrink: 0;
}

.sg-swatch-name {
  font-size: 14px;
  font-weight: 500;
}

.sg-swatch-val {
  font-size: 12px;
  color: var(--color-text-tertiary);
  font-family: var(--font-mono);
}

.sg-row {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-3);
}

/* Button */
.sg-btn {
  height: var(--density-button-height);
  padding: 0 var(--space-5);
  border-radius: var(--radius-base);
  border: none;
  font-size: 16px;
  font-weight: 500;
  font-family: inherit;
  cursor: pointer;
  transition: background-color var(--motion-fast) var(--ease-out),
              transform var(--motion-fast) var(--ease-out);
}

.sg-btn-primary {
  background: var(--color-brand-primary);
  color: var(--color-text-inverse);
}

.sg-btn-primary:hover:not(:disabled) {
  background: var(--color-brand-hover);
}

.sg-btn-primary:active:not(:disabled) {
  background: var(--color-brand-active);
  transform: scale(0.98);
}

.sg-btn-primary:disabled {
  background: var(--color-brand-light);
  color: var(--color-text-tertiary);
  cursor: not-allowed;
}

.sg-btn-secondary {
  background: var(--color-surface-raised);
  color: var(--color-text-primary);
  border: 1px solid var(--color-border-default);
}

.sg-btn-ghost {
  background: transparent;
  color: var(--color-brand-primary);
}

.sg-btn-ghost:hover {
  background: var(--color-brand-light);
}

.sg-btn-success {
  background: var(--color-success);
  color: var(--color-text-inverse);
}

/* Card + Cell */
.sg-card {
  background: var(--color-surface-raised);
  border: 1px solid var(--color-border-default);
  border-radius: var(--radius-md);
  overflow: hidden;
}

.sg-cell {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--density-cell-padding-y) var(--density-cell-padding-x);
  min-height: var(--density-cell-height);
  border-bottom: 0.5px solid var(--color-divider);
}

.sg-cell:last-child {
  border-bottom: none;
}

.sg-cell-title {
  font-size: 16px;
  color: var(--color-text-primary);
}

.sg-cell-value {
  font-size: 16px;
  color: var(--color-text-secondary);
}

/* State boxes */
.sg-states {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: var(--space-4);
}

.sg-state-box {
  padding: var(--space-5);
  border: 1px solid var(--color-border-default);
  border-radius: var(--radius-md);
  background: var(--color-surface-raised);
}

.sg-state-error {
  border-color: var(--color-error);
}

.sg-state-success {
  border-color: var(--color-success);
}

.sg-empty {
  text-align: center;
  padding: var(--space-6) 0;
}

.sg-empty-icon {
  font-size: 48px;
  margin-bottom: var(--space-2);
  filter: grayscale(1);
}

.sg-empty-text {
  color: var(--color-text-secondary);
}

.sg-skeleton {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.sg-skel-line {
  height: 16px;
  background: var(--color-surface-sunken);
  border-radius: var(--radius-sm);
  animation: sg-pulse 1.2s linear infinite;
}

@keyframes sg-pulse {
  0%, 100% { opacity: 0.5; }
  50% { opacity: 1; }
}

/* Tags */
.sg-tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: var(--radius-sm);
  font-size: 12px;
  font-weight: 500;
}

.sg-tag-success {
  background: var(--color-leaf-green-light);
  color: var(--color-leaf-green);
}

.sg-tag-warning {
  background: #FFF3E0;
  color: var(--color-warning);
}

.sg-tag-error {
  background: var(--color-brand-light);
  color: var(--color-brand-primary);
}

.sg-tag-info {
  background: #E3F2FD;
  color: var(--color-info);
}
</style>
