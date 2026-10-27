/**
 * Mock 模式判定
 *
 * 读取 VITE_ENABLE_MOCK 环境变量：
 *   - 'on' / 'true'   → 强制 Mock（后端不启动也能跑）
 *   - 'off' / 'false' → 禁用 Mock（生产必须）
 *   - 'fallback' 或其他 → 真实 API 失败时降级（开发默认）
 */

/** @returns {'on' | 'off' | 'fallback'} */
export function getMockMode() {
  const mode = import.meta.env.VITE_ENABLE_MOCK
  if (mode === 'true' || mode === 'on') return 'on'
  if (mode === 'false' || mode === 'off') return 'off'
  return 'fallback'
}

export function shouldMockBeforeRequest() {
  return getMockMode() === 'on'
}

export function shouldMockOnFailure() {
  return getMockMode() === 'fallback'
}

export function isMockDisabled() {
  return getMockMode() === 'off'
}
