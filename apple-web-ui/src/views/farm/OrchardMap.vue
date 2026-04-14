<template>
  <div class="orchard-map-page">
    <!-- Header -->
    <div class="map-header">
      <h1 class="page-title">果园地图</h1>
      <van-button
        type="primary"
        size="small"
        icon="edit"
        :loading="drawLoading"
        :disabled="mapLoading"
        @click="openOrchardSheet"
      >
        绘制边界
      </van-button>
    </div>

    <!-- Map Loading -->
    <div v-if="mapLoading || configError" class="map-status">
      <van-loading v-if="mapLoading" type="spinner" size="36px" color="var(--color-brand-primary)">
        地图加载中...
      </van-loading>
      <van-empty v-else-if="configError" :description="configError" />
    </div>

    <!-- Map Container -->
    <div ref="mapRef" class="map-container" :class="{ 'is-hidden': mapLoading || configError }" />

    <!-- Bottom Hint -->
    <div v-if="hintText" class="map-hint">
      <span class="hint-text">{{ hintText }}</span>
    </div>

    <!-- Orchard Selection Action Sheet -->
    <van-action-sheet
      v-model:show="showSheet"
      title="选择果园绘制边界"
      :actions="orchardActions"
      cancel-text="取消"
      close-on-click-action
      @select="onOrchardSelect"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { orchardGeoApi, amapConfigApi } from '@/api/orchardGeo.js'
import { farmApi } from '@/api/farm.js'
import { showToast } from 'vant'
import { color as designColor } from '@/design/tokens'

/* ---------- refs ---------- */
const mapRef = ref(null)

/* ---------- state ---------- */
let map = null
let mouseTool = null
let markers = []
const mapLoading = ref(true)
const configError = ref('')
const drawLoading = ref(false)
const showSheet = ref(false)
const hintText = ref('拖动地图查看果园位置')

const orchardActions = ref([])

/* ---------- constants ---------- */
const DEFAULT_CENTER = [105.735, 36.07] // approximate center for apple-growing regions

/* ---------- map lifecycle ---------- */
async function initMap() {
  // 1. Fetch AMap config from backend
  let config
  try {
    const res = await amapConfigApi.fetch()
    config = res?.data || res || null
  } catch {
    config = null
  }

  if (!config?.key) {
    configError.value = '地图配置加载失败，无法获取高德地图密钥'
    mapLoading.value = false
    return
  }

  // 2. Set security config
  if (config.securityJsCode) {
    window._AMapSecurityConfig = { securityJsCode: config.securityJsCode }
  }

  try {
    // 3. Dynamic import AMap loader
    const AMapLoader = (await import('@amap/amap-jsapi-loader')).default
    const AMap = await AMapLoader.load({
      key: config.key,
      version: '2.0',
      plugins: ['AMap.MouseTool', 'AMap.Geocoder'],
    })

    // 4. Create map instance
    map = new AMap.Map(mapRef.value, {
      zoom: 12,
      center: DEFAULT_CENTER,
      resizeEnable: true,
    })

    // 5. Initialize MouseTool for boundary drawing
    mouseTool = new AMap.MouseTool(map)
    mouseTool.on('draw', onDrawComplete)

    // 6. Listen for viewport changes to query orchards
    map.on('moveend', debouncedQueryOrchards)
    map.on('zoomend', debouncedQueryOrchards)

    // 7. Initial orchard query
    await queryOrchards()

    mapLoading.value = false
  } catch (e) {
    console.error('AMap init failed:', e)
    configError.value = '地图初始化失败，请刷新重试'
    mapLoading.value = false
  }
}

/* ---------- orchard viewport query ---------- */
let queryTimer = null

function debouncedQueryOrchards() {
  if (queryTimer) clearTimeout(queryTimer)
  queryTimer = setTimeout(queryOrchards, 500)
}

async function queryOrchards() {
  if (!map) return

  try {
    const bounds = map.getBounds()
    const bbox = {
      lng1: bounds.southwest.getLng(),
      lat1: bounds.southwest.getLat(),
      lng2: bounds.northeast.getLng(),
      lat2: bounds.northeast.getLat(),
    }

    const res = await orchardGeoApi.bbox(bbox)
    const orchards = res?.data || res || []

    // Clear existing markers
    clearMarkers()

    if (Array.isArray(orchards) && orchards.length > 0) {
      hintText.value = ''
      const AMap = window.AMap

      orchards.forEach((o) => {
        if (!o.lng || !o.lat) return

        const marker = new AMap.Marker({
          position: [Number(o.lng), Number(o.lat)],
          title: o.orchardName || o.name || '果园',
          label: {
            content: `<span class="marker-label">${o.orchardName || o.name || '果园'}</span>`,
            direction: 'top',
          },
        })
        marker._orchardData = o
        map.add(marker)
        markers.push(marker)
      })
    } else {
      hintText.value = '当前视窗内无果园'
    }
  } catch {
    // silent — viewport query is supplementary
  }
}

function clearMarkers() {
  if (markers.length > 0) {
    map.remove(markers)
    markers = []
  }
}

/* ---------- boundary drawing ---------- */
async function openOrchardSheet() {
  drawLoading.value = true
  try {
    const res = await farmApi.getOrchards({ page: 1, pageSize: 999 })
    const list = res?.records || res || []
    orchardActions.value = list.map((o) => ({
      name: o.orchardName || o.name || '未知果园',
      id: o.id,
    }))

    if (list.length === 0) {
      showToast('暂无果园可选择')
      drawLoading.value = false
      return
    }

    showSheet.value = true
  } catch {
    showToast('加载果园列表失败')
  } finally {
    drawLoading.value = false
  }
}

let selectedOrchardId = null

function onOrchardSelect(action) {
  if (!action?.id) return
  selectedOrchardId = action.id
  showSheet.value = false

  hintText.value = `正在绘制「${action.name}」边界，请在地图上点击绘制多边形`
  showToast('请在地图上点击绘制多边形边界')

  // Start drawing polygon with MouseTool
  if (mouseTool) {
    mouseTool.polygon({
      strokeColor: designColor.brand.primary,
      strokeOpacity: 0.8,
      strokeWeight: 2,
      fillColor: designColor.brand.primary,
      fillOpacity: 0.15,
      strokeStyle: 'solid',
    })
  }
}

function onDrawComplete(event) {
  if (!event?.obj || !selectedOrchardId) return

  const overlay = event.obj
  const path = overlay.getPath()

  if (!path || path.length < 3) {
    showToast('边界至少需要3个点')
    map.remove(overlay)
    return
  }

  // Convert to GeoJSON format: [[lng, lat], [lng, lat], ...]
  const coordinates = path.map((p) => [p.getLng(), p.getLat()])
  // Close the ring
  coordinates.push([coordinates[0][0], coordinates[0][1]])

  const geojson = {
    type: 'Feature',
    properties: {},
    geometry: {
      type: 'Polygon',
      coordinates: [coordinates],
    },
  }

  // Close drawing tool
  if (mouseTool) {
    mouseTool.close(true)
  }

  // Save boundary to backend
  saveBoundary(selectedOrchardId, geojson, overlay)
}

async function saveBoundary(orchardId, geojson, overlay) {
  try {
    await orchardGeoApi.saveBoundary(orchardId, geojson)
    showToast({ type: 'success', message: '边界保存成功' })
    hintText.value = '边界已保存'
  } catch {
    showToast('边界保存失败')
    map.remove(overlay)
  } finally {
    selectedOrchardId = null
  }
}

/* ---------- cleanup ---------- */
onMounted(() => {
  initMap()
})

onBeforeUnmount(() => {
  if (queryTimer) clearTimeout(queryTimer)
  if (mouseTool) {
    mouseTool.close(true)
    mouseTool = null
  }
  clearMarkers()
  if (map) {
    map.destroy()
    map = null
  }
  delete window._AMapSecurityConfig
})
</script>

<style scoped>
.orchard-map-page {
  position: relative;
  width: 100%;
  height: 100vh;
  overflow: hidden;
  background-color: var(--color-surface-base);
}

.map-header {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  z-index: var(--z-index-sticky, 1100);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-4);
  background: linear-gradient(
    to bottom,
    var(--color-surface-raised) 0%,
    color-mix(in srgb, var(--color-surface-raised) 80%, transparent) 80%,
    transparent 100%
  );
}

.page-title {
  font-size: var(--font-size-h2);
  font-weight: 600;
  color: var(--color-text-primary);
  margin: 0;
  line-height: 1.4;
}

.map-container {
  width: 100%;
  height: 100%;
}

.map-container.is-hidden {
  visibility: hidden;
}

.map-status {
  position: absolute;
  inset: 0;
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: var(--z-index-base, 0);
}

.map-hint {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: var(--z-index-sticky, 1100);
  padding: var(--space-3) var(--space-4);
  background: linear-gradient(
    to top,
    color-mix(in srgb, var(--color-text-primary) 80%, transparent) 0%,
    color-mix(in srgb, var(--color-text-primary) 50%, transparent) 80%,
    transparent 100%
  );
  pointer-events: none;
}

.hint-text {
  display: block;
  text-align: center;
  font-size: var(--font-size-body-s);
  line-height: 1.5;
}

/* AMap marker label styling (global since AMap creates labels outside Vue scope) */
:deep(.marker-label) {
  background-color: var(--color-surface-raised);
  color: var(--color-text-primary);
  padding: var(--space-1) var(--space-2);
  border-radius: var(--radius-sm);
  font-size: var(--font-size-caption);
  border: 1px solid var(--color-border-default);
  box-shadow: var(--shadow-1);
  white-space: nowrap;
}
</style>
