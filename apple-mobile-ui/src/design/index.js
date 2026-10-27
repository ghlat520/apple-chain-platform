/**
 * Design System 统一导出
 *
 * main.js 中：
 *   import '@/design'  // 自动加载 CSS 变量 + Vant 主题覆盖
 *   import { tokens } from '@/design'  // 获取 JS token 对象
 */

import './vant-theme.css'

export { tokens, color, typography, spacing, radius, elevation, motion, density, breakpoints, zIndex } from './tokens'
export { default } from './tokens'
