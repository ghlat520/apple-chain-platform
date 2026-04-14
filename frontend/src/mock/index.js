/**
 * Mock API 入口 — 仅导入 data.js 触发加载
 *
 * 实际拦截逻辑在 request.js 的 error interceptor 中
 * 路由匹配函数在 routes.js 中
 */

import './data.js'

console.log('[Mock] Data layer loaded')
