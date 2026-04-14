/**
 * ECharts Theme — apple-chain
 *
 * DESIGN.md 要求所有 ECharts 必须通过此主题加载，禁止 inline 配色。
 * 使用方式：
 *   import { useChart } from '@/design/echarts-theme'
 *   const { chartRef, setOption, resize, dispose } = useChart()
 *
 * 或者直接注册主题：
 *   import { registerTheme } from '@/design/echarts-theme'
 *   registerTheme()
 *   echarts.init(dom, 'apple-chain')
 */

import * as echarts from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import {
  PieChart,
  BarChart,
  LineChart,
  ScatterChart,
  RadarChart,
} from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
  DataZoomComponent,
  ToolboxComponent,
} from 'echarts/components'
import { color } from './tokens'

// 按需注册 ECharts 组件
echarts.use([
  CanvasRenderer,
  PieChart,
  BarChart,
  LineChart,
  ScatterChart,
  RadarChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
  DataZoomComponent,
  ToolboxComponent,
])

const THEME_NAME = 'apple-chain'

/**
 * ECharts 主题配置
 */
const themeOption = {
  color: color.chartSeries,

  backgroundColor: 'transparent',

  textStyle: {
    fontFamily:
      '"PingFang SC", "Source Han Sans SC", "Microsoft YaHei", -apple-system, Inter, sans-serif',
    color: '#2C2E32',
  },

  title: {
    textStyle: {
      fontSize: 16,
      fontWeight: 600,
      color: '#2C2E32',
    },
    subtextStyle: {
      fontSize: 13,
      color: '#8A9099',
    },
  },

  legend: {
    textStyle: {
      fontSize: 13,
      color: '#5E6368',
    },
    icon: 'circle',
    itemWidth: 8,
    itemHeight: 8,
    itemGap: 16,
  },

  tooltip: {
    backgroundColor: 'rgba(255, 255, 255, 0.96)',
    borderColor: 'rgba(44, 46, 50, 0.08)',
    borderWidth: 1,
    textStyle: {
      fontSize: 13,
      color: '#2C2E32',
    },
    extraCssText:
      'border-radius: 8px; box-shadow: 0 4px 12px rgba(44,46,50,0.08);',
  },

  grid: {
    left: 16,
    right: 16,
    top: 32,
    bottom: 16,
    containLabel: true,
  },

  categoryAxis: {
    axisLine: { lineStyle: { color: 'rgba(44, 46, 50, 0.08)' } },
    axisTick: { show: false },
    axisLabel: { color: '#8A9099', fontSize: 12 },
    splitLine: { show: false },
  },

  valueAxis: {
    axisLine: { show: false },
    axisTick: { show: false },
    axisLabel: { color: '#8A9099', fontSize: 12 },
    splitLine: {
      lineStyle: { color: 'rgba(44, 46, 50, 0.06)', type: 'dashed' },
    },
  },

  pie: {
    itemStyle: {
      borderColor: '#FFFFFF',
      borderWidth: 2,
      borderRadius: 4,
    },
  },

  line: {
    smooth: false,
    symbol: 'circle',
    symbolSize: 6,
    lineStyle: { width: 2 },
    areaStyle: { opacity: 0.08 },
  },

  bar: {
    itemStyle: {
      borderRadius: [4, 4, 0, 0],
    },
    barMaxWidth: 32,
  },
}

let registered = false

/**
 * 注册 apple-chain 主题到 ECharts（幂等）
 */
export function registerTheme() {
  if (!registered) {
    echarts.registerTheme(THEME_NAME, themeOption)
    registered = true
  }
}

/**
 * Composable：封装 ECharts 实例的创建、主题绑定、resize、dispose
 *
 * 使用方式（在 <script setup> 中）：
 *   import { useChart } from '@/design/echarts-theme'
 *   const chartRef = ref(null)
 *   const { setOption } = useChart(chartRef)
 *   onMounted(() => setOption({ ... }))
 *
 * @param {import('vue').Ref<HTMLElement>} chartRef - template ref
 * @returns {{ setOption, resize, dispose, getInstance }}
 */
export function useChart(chartRef) {
  let instance = null

  function init() {
    if (!chartRef?.value) return null
    if (instance) return instance
    registerTheme()
    instance = echarts.init(chartRef.value, THEME_NAME)
    return instance
  }

  function getInstance() {
    return instance
  }

  function setOption(option, opts) {
    if (!instance) init()
    if (instance) {
      instance.setOption(option, opts)
    }
  }

  function resize() {
    instance?.resize()
  }

  function dispose() {
    if (instance) {
      instance.dispose()
      instance = null
    }
  }

  return { setOption, resize, dispose, getInstance }
}

export { echarts }
export default { registerTheme, useChart, echarts }
