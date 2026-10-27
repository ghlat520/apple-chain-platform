/**
 * apple-chain-platform Design Tokens — Single Source of Truth
 *
 * ⚠️ 本文件由 DESIGN.md 派生。禁止直接修改，任何修改先改 DESIGN.md 再同步。
 *
 * 使用方式：
 *   import { tokens } from '@/design/tokens'
 *   <div :style="{ color: tokens.color.text.primary }" />
 *
 * 或通过 CSS 变量（推荐 — 在 vant-theme.css 已注入 :root）：
 *   <div class="text-primary bg-surface-raised" />
 *
 * Vant 组件主题覆盖：
 *   见 src/design/vant-theme.css（CSS 变量方式，无需 JS）
 */

// ---------- Color ----------
export const color = {
  brand: {
    primary: '#D32F2F',        // 苹果红
    primaryHover: '#C62828',
    primaryActive: '#B71C1C',
    primaryLight: '#FFEBEE',
    primaryDisabled: 'rgba(211, 47, 47, 0.35)',
  },

  accent: {
    leafGreen: '#43A047',       // 果叶绿，语义正向专用
    leafGreenLight: '#E8F5E9',
  },

  text: {
    primary: '#2C2E32',          // 偏暖的黑
    secondary: '#5E6368',
    tertiary: '#8A9099',
    disabled: '#BDC1C7',
    inverse: '#FFFFFF',
    link: '#D32F2F',            // 和品牌色一致
  },

  surface: {
    base: '#FAFAF8',            // 暖米白，非冷白
    raised: '#FFFFFF',
    overlay: '#FFFFFF',
    sunken: '#F5F5F2',
  },

  border: {
    default: 'rgba(44, 46, 50, 0.08)',
    strong: 'rgba(44, 46, 50, 0.16)',
    divider: 'rgba(44, 46, 50, 0.06)',
  },

  semantic: {
    success: '#43A047',
    successBg: '#E8F5E9',
    warning: '#F57C00',
    warningBg: '#FFF3E0',
    error: '#D32F2F',
    errorBg: '#FFEBEE',
    info: '#1976D2',
    infoBg: '#E3F2FD',
  },

  // 🌡️ IoT / 冷链专属
  iot: {
    tempNormal: '#1976D2',
    tempWarning: '#F57C00',
    tempCritical: '#D32F2F',
    humidity: '#00838F',
    gpsTrack: '#43A047',
  },

  // 图表序列（色盲友好，8 色）
  chartSeries: [
    '#D32F2F',  // 苹果红 (品牌)
    '#43A047',  // 果叶绿
    '#1976D2',  // 政务蓝
    '#F57C00',  // 橙
    '#00838F',  // 青
    '#6A1B9A',  // 紫
    '#FBC02D',  // 黄
    '#546E7A',  // 灰蓝
  ],
}

// ---------- Typography ----------
export const typography = {
  fontFamily: {
    body: '"PingFang SC", "Source Han Sans SC", "Microsoft YaHei", -apple-system, Inter, sans-serif',
    display: 'Inter, "PingFang SC", sans-serif',
    mono: '"JetBrains Mono", "SF Mono", Consolas, monospace',
  },

  fontSize: {
    'display-l':  { size: '28px', lineHeight: 1.3,  weight: 600 },
    'h1':         { size: '22px', lineHeight: 1.35, weight: 600 },
    'h2':         { size: '18px', lineHeight: 1.4,  weight: 600 },
    'h3':         { size: '16px', lineHeight: 1.5,  weight: 500 },
    'body-l':     { size: '17px', lineHeight: 1.6,  weight: 400 },
    'body':       { size: '16px', lineHeight: 1.6,  weight: 400 }, // 默认
    'body-s':     { size: '14px', lineHeight: 1.5,  weight: 400 },
    'caption':    { size: '13px', lineHeight: 1.4,  weight: 400 },
    'overline':   { size: '12px', lineHeight: 1.3,  weight: 500 },
    'number-l':   { size: '24px', lineHeight: 1.1,  weight: 500 },
    'number':     { size: '16px', lineHeight: 1.2,  weight: 500 },
  },

  fontWeight: {
    regular: 400,
    medium: 500,
    semibold: 600,
  },
}

// ---------- Spacing ----------
export const spacing = {
  0: '0',
  1: '4px',
  2: '8px',
  3: '12px',
  4: '16px',  // 默认
  5: '20px',
  6: '24px',
  8: '32px',
  12: '48px',
  16: '64px',
}

// ---------- Radius ----------
export const radius = {
  none: '0',
  sm: '4px',
  base: '8px',    // 按钮 / 输入框（覆盖 Vant 默认 2px）
  md: '12px',     // 卡片
  lg: '16px',     // 弹窗
  full: '9999px',
}

// ---------- Shadow / Elevation ----------
export const elevation = {
  0: 'none',
  1: '0 1px 2px rgba(44,46,50,0.04)',
  2: '0 4px 12px rgba(44,46,50,0.08)',
  3: '0 16px 48px rgba(0,0,0,0.15)',
  4: '0 8px 24px rgba(0,0,0,0.12)',
}

// ---------- Motion ----------
export const motion = {
  duration: {
    instant: '0ms',
    fast: '150ms',
    base: '250ms',
    slow: '400ms',
  },
  easing: {
    out: 'cubic-bezier(0.16, 1, 0.3, 1)',       // 默认入场
    in: 'cubic-bezier(0.7, 0, 0.84, 0)',         // 出场
    spring: 'cubic-bezier(0.34, 1.56, 0.64, 1)', // 勾选反馈
  },
}

// ---------- Density ----------
export const density = {
  spacious: {      // 果农 / 冷链司机
    buttonHeight: '56px',
    inputHeight: '56px',
    cellHeight: '64px',
    cellPaddingY: '16px',
    cellPaddingX: '20px',
    fontSizeOffset: 2,
  },
  comfortable: {   // 默认
    buttonHeight: '48px',
    inputHeight: '48px',
    cellHeight: '56px',
    cellPaddingY: '14px',
    cellPaddingX: '16px',
    fontSizeOffset: 0,
  },
  compact: {       // 管理员 / 金融机构
    buttonHeight: '40px',
    inputHeight: '40px',
    cellHeight: '44px',
    cellPaddingY: '10px',
    cellPaddingX: '14px',
    fontSizeOffset: -1,
  },
}

// ---------- Breakpoints ----------
export const breakpoints = {
  xs: '0',
  sm: '576px',
  md: '768px',
  lg: '1024px',
  xl: '1280px',
}

// ---------- Z-Index ----------
export const zIndex = {
  base: 0,
  raised: 10,
  dropdown: 1000,
  sticky: 1100,
  overlay: 1200,
  modal: 1300,
  popover: 1400,
  toast: 1500,
  tooltip: 1600,
}

// ---------- Unified export ----------
export const tokens = {
  color,
  typography,
  spacing,
  radius,
  elevation,
  motion,
  density,
  breakpoints,
  zIndex,
}

export default tokens
