<template>
  <div class="orchard-map-page">
    <!-- ============ Header ============ -->
    <div class="map-header">
      <div class="map-header-row">
        <h1 class="page-title">果园地图</h1>
        <div class="map-header-actions">
          <van-button
            type="default"
            size="small"
            icon="aim"
            :disabled="mapLoading || !!configError"
            :loading="locating"
            @click="locateMe"
          >
            我的位置
          </van-button>
          <van-button
            type="primary"
            size="small"
            icon="edit"
            :loading="drawLoading"
            :disabled="mapLoading || !!configError"
            @click="openOrchardSheet"
          >
            绘制边界
          </van-button>
        </div>
      </div>

      <!-- Search bar -->
      <div class="map-search-row">
        <van-field
          v-model="searchKeyword"
          placeholder="搜索地名、地标或地址"
          left-icon="search"
          clearable
          :disabled="mapLoading || !!configError"
          @update:model-value="onSearchInput"
          @keyup.enter="onSearchEnter"
        />
        <ul
          v-if="searchTips.length"
          class="map-search-tips"
          role="listbox"
          aria-label="搜索建议"
        >
          <li
            v-for="tip in searchTips"
            :key="tip.id + tip.name"
            class="map-search-tip"
            tabindex="0"
            role="option"
            @click="onSelectTip(tip)"
            @keyup.enter="onSelectTip(tip)"
          >
            <span class="tip-name">{{ tip.name }}</span>
            <span class="tip-district">{{ tip.district || '' }}</span>
          </li>
        </ul>
      </div>
    </div>

    <!-- ============ Main: Map + List ============ -->
    <div class="map-main">
      <!-- Status overlays -->
      <div v-if="mapLoading" class="map-status" role="status" aria-live="polite">
        <van-loading type="spinner" size="36px" color="var(--color-brand-primary)">
          地图加载中...
        </van-loading>
      </div>

      <div v-else-if="configError" class="map-status">
        <van-empty image="error" :description="configError">
          <van-button type="primary" size="small" @click="retryInit">重试</van-button>
        </van-empty>
      </div>

      <!-- Map container (always rendered when no error; hidden during loading) -->
      <div
        ref="mapRef"
        class="map-container"
        :class="{ 'is-hidden': mapLoading || configError }"
        aria-label="果园分布地图"
      />

      <!-- List panel (desktop: left side / mobile: bottom sheet toggle) -->
      <aside
        v-if="!mapLoading && !configError"
        class="map-list-panel"
        :class="{ 'is-open': listOpen }"
        aria-label="果园列表"
      >
        <div class="list-panel-header">
          <span class="list-panel-title">
            果园列表
            <span class="list-panel-count nums-tabular">({{ orchardList.length }})</span>
          </span>
          <van-button
            class="list-toggle"
            size="mini"
            plain
            :icon="listOpen ? 'arrow-down' : 'arrow-up'"
            @click="listOpen = !listOpen"
            :aria-label="listOpen ? '收起列表' : '展开列表'"
          />
        </div>

        <div v-if="listLoading" class="list-panel-body list-panel-state">
          <van-loading size="24px">加载中...</van-loading>
        </div>
        <div v-else-if="listError" class="list-panel-body list-panel-state">
          <van-empty image="error" description="列表加载失败">
            <van-button size="small" @click="queryOrchards">重试</van-button>
          </van-empty>
        </div>
        <div v-else-if="orchardList.length === 0" class="list-panel-body list-panel-state">
          <van-empty description="当前视窗内无果园" />
        </div>
        <div v-else class="list-panel-body">
          <button
            v-for="o in orchardList"
            :key="o.id"
            class="list-item"
            :class="{ 'is-active': activeOrchardId === o.id }"
            type="button"
            @click="focusOrchard(o)"
          >
            <div class="list-item-main">
              <span class="list-item-name">{{ o.orchardName || '未命名果园' }}</span>
              <van-tag :type="statusTagType(o.status)" plain>
                {{ statusLabel(o.status) }}
              </van-tag>
            </div>
            <div class="list-item-meta">
              <span>{{ o.variety || '未设置品种' }}</span>
              <span class="nums-tabular">{{ formatArea(o.areaMu) }}</span>
            </div>
          </button>
        </div>
      </aside>
    </div>

    <!-- ============ Bottom hint ============ -->
    <div v-if="hintText" class="map-hint" role="status" aria-live="polite">
      <span class="hint-text">{{ hintText }}</span>
    </div>

    <!-- ============ Orchard picker for drawing ============ -->
    <van-action-sheet
      v-model:show="showSheet"
      title="选择果园绘制边界"
      :actions="orchardActions"
      cancel-text="取消"
      close-on-click-action
      @select="onOrchardSelect"
    />

    <!-- ============ Orchard info popup ============ -->
    <van-popup
      v-model:show="infoOpen"
      position="bottom"
      round
      :style="{ maxHeight: '70vh' }"
      safe-area-inset-bottom
    >
      <div v-if="activeOrchard" class="info-popup">
        <div class="info-popup-header">
          <h2 class="info-popup-title">{{ activeOrchard.orchardName || '未命名果园' }}</h2>
          <van-tag :type="statusTagType(activeOrchard.status)">
            {{ statusLabel(activeOrchard.status) }}
          </van-tag>
        </div>
        <van-cell-group inset>
          <van-cell title="品种" :value="activeOrchard.variety || '-'" />
          <van-cell title="登记面积">
            <template #value>
              <span class="nums-tabular">{{ formatArea(activeOrchard.areaMu) }}</span>
            </template>
          </van-cell>
          <van-cell v-if="computedArea != null" title="边界计算面积">
            <template #value>
              <span class="nums-tabular">{{ formatArea(computedArea) }}</span>
              <span v-if="areaDelta" class="area-delta" :class="areaDeltaClass">
                ({{ areaDelta }})
              </span>
            </template>
          </van-cell>
          <van-cell title="编号" :value="activeOrchard.orchardNo || '-'" />
        </van-cell-group>
        <div class="info-popup-actions">
          <van-button block type="primary" plain @click="goToDetail(activeOrchard)">
            查看详情
          </van-button>
        </div>
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { orchardGeoApi, amapConfigApi } from '@/api/orchardGeo.js'
import { farmApi } from '@/api/farm.js'
import { showToast } from 'vant'
import { color as designColor } from '@/design/tokens.js'

// AMap SDK only accepts hex colors (not CSS variables) — pull from design tokens
const BRAND_COLOR = designColor.brand.primary

/* ============ refs & state ============ */
const router = useRouter()
const mapRef = ref(null)

let map = null
let mouseTool = null
let AMapNS = null
let autoComplete = null
let placeSearch = null
let geolocation = null
let markers = []
let polygons = []
let locateMarker = null
let searchMarker = null
let currentDrawOverlay = null

const mapLoading = ref(true)
const configError = ref('')
const drawLoading = ref(false)
const showSheet = ref(false)
const hintText = ref('拖动地图查看果园位置')
const orchardActions = ref([])

const searchKeyword = ref('')
const searchTips = ref([])

const orchardList = ref([])
const listLoading = ref(false)
const listError = ref(false)
const listOpen = ref(true)

const activeOrchardId = ref(null)
const activeOrchard = ref(null)
const infoOpen = ref(false)
const computedArea = ref(null)
const locating = ref(false)

let selectedOrchardId = null
let queryTimer = null
let searchTimer = null

/* ============ constants ============ */
const DEFAULT_CENTER = [108.954, 34.266] // Xi'an-ish, center of apple regions
const REDUCED_MOTION = typeof window !== 'undefined'
  && window.matchMedia
  && window.matchMedia('(prefers-reduced-motion: reduce)').matches

/* ============ computed ============ */
const areaDelta = computed(() => {
  if (computedArea.value == null || !activeOrchard.value?.areaMu) return ''
  const registered = Number(activeOrchard.value.areaMu)
  const computed = Number(computedArea.value)
  if (!Number.isFinite(registered) || !Number.isFinite(computed) || registered === 0) return ''
  const diff = computed - registered
  const pct = (diff / registered) * 100
  const sign = diff > 0 ? '+' : ''
  return `${sign}${diff.toFixed(2)} 亩 / ${sign}${pct.toFixed(1)}%`
})

const areaDeltaClass = computed(() => {
  if (computedArea.value == null || !activeOrchard.value?.areaMu) return ''
  const registered = Number(activeOrchard.value.areaMu)
  const computed = Number(computedArea.value)
  if (!Number.isFinite(registered) || !Number.isFinite(computed) || registered === 0) return ''
  const pct = Math.abs((computed - registered) / registered) * 100
  if (pct < 5) return 'area-delta--ok'
  if (pct < 15) return 'area-delta--warn'
  return 'area-delta--critical'
})

/* ============ helpers ============ */
function statusTagType(status) {
  const s = (status || '').toUpperCase()
  const map = {
    NORMAL: 'success',
    DORMANT: 'default',
    HARVESTED: 'warning',
    ACTIVE: 'success',
    INACTIVE: 'default',
  }
  return map[s] || 'default'
}

function statusLabel(status) {
  const s = (status || '').toUpperCase()
  const map = {
    NORMAL: '正常',
    DORMANT: '休耕',
    HARVESTED: '已采收',
    ACTIVE: '正常',
    INACTIVE: '休耕',
  }
  return map[s] || status || '-'
}

function formatArea(mu) {
  if (mu == null || mu === '') return '-'
  const n = Number(mu)
  if (!Number.isFinite(n)) return '-'
  return `${n.toFixed(2)} 亩`
}

/* ============ map init ============ */
async function initMap() {
  mapLoading.value = true
  configError.value = ''

  let config
  try {
    const res = await amapConfigApi.fetch()
    config = res?.data || res || null
  } catch {
    config = null
  }

  if (!config?.key) {
    configError.value = '地图配置加载失败，请联系管理员'
    mapLoading.value = false
    return
  }

  if (config.securityJsCode) {
    window._AMapSecurityConfig = { securityJsCode: config.securityJsCode }
  }

  try {
    const AMapLoader = (await import('@amap/amap-jsapi-loader')).default
    AMapNS = await AMapLoader.load({
      key: config.key,
      version: '2.0',
      plugins: [
        'AMap.MouseTool',
        'AMap.Geocoder',
        'AMap.AutoComplete',
        'AMap.PlaceSearch',
        'AMap.Geolocation',
      ],
    })

    map = new AMapNS.Map(mapRef.value, {
      zoom: 11,
      center: DEFAULT_CENTER,
      resizeEnable: true,
      jogEnable: !REDUCED_MOTION,
      animateEnable: !REDUCED_MOTION,
    })

    mouseTool = new AMapNS.MouseTool(map)
    mouseTool.on('draw', onDrawComplete)

    autoComplete = new AMapNS.AutoComplete({ city: '全国' })
    placeSearch = new AMapNS.PlaceSearch({ map: null, pageSize: 10 })

    try {
      geolocation = new AMapNS.Geolocation({
        enableHighAccuracy: true,
        timeout: 10000,
        showButton: false,
        showMarker: false,
        showCircle: false,
      })
      map.addControl(geolocation)
    } catch {
      geolocation = null
    }

    map.on('moveend', debouncedQueryOrchards)
    map.on('zoomend', debouncedQueryOrchards)

    await nextTick()
    await queryOrchards()

    mapLoading.value = false
  } catch (e) {
    console.error('AMap init failed:', e)
    configError.value = '地图初始化失败，请检查网络后重试'
    mapLoading.value = false
  }
}

function retryInit() {
  configError.value = ''
  initMap()
}

/* ============ viewport bbox query ============ */
function debouncedQueryOrchards() {
  if (queryTimer) clearTimeout(queryTimer)
  queryTimer = setTimeout(queryOrchards, 400)
}

async function queryOrchards() {
  if (!map) return
  listLoading.value = true
  listError.value = false
  try {
    const bounds = map.getBounds()
    const bbox = {
      lng1: bounds.getSouthWest().getLng(),
      lat1: bounds.getSouthWest().getLat(),
      lng2: bounds.getNorthEast().getLng(),
      lat2: bounds.getNorthEast().getLat(),
    }
    const res = await orchardGeoApi.bbox(bbox)
    const orchards = res?.data ?? res ?? []
    orchardList.value = Array.isArray(orchards) ? orchards : []
    renderOrchardMarkers()

    if (orchardList.value.length === 0) {
      hintText.value = '当前视窗内无果园，拖动地图或切换区域'
    } else {
      hintText.value = ''
    }
  } catch (e) {
    listError.value = true
    hintText.value = '果园数据加载失败'
  } finally {
    listLoading.value = false
  }
}

function clearMarkers() {
  if (markers.length && map) map.remove(markers)
  markers = []
  if (polygons.length && map) map.remove(polygons)
  polygons = []
}

function renderOrchardMarkers() {
  clearMarkers()
  if (!AMapNS || !map) return

  orchardList.value.forEach((o) => {
    const lng = Number(o.centerLng)
    const lat = Number(o.centerLat)
    if (!Number.isFinite(lng) || !Number.isFinite(lat)) return

    const marker = new AMapNS.Marker({
      position: [lng, lat],
      title: o.orchardName || '果园',
      label: {
        content: `<span class="marker-label">${escapeHtml(o.orchardName || '果园')}</span>`,
        direction: 'top',
      },
      extData: { orchardId: o.id },
    })
    marker.on('click', () => focusOrchard(o))
    map.add(marker)
    markers.push(marker)
  })
}

function escapeHtml(s) {
  return String(s).replace(/[&<>"']/g, (c) => (
    { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]
  ))
}

/* ============ list <-> map linkage ============ */
async function focusOrchard(o) {
  if (!map || !AMapNS) return
  activeOrchardId.value = o.id
  activeOrchard.value = o
  computedArea.value = null

  const lng = Number(o.centerLng)
  const lat = Number(o.centerLat)
  if (Number.isFinite(lng) && Number.isFinite(lat)) {
    map.setZoomAndCenter(15, [lng, lat], !REDUCED_MOTION)
  }

  // Load boundary from detail endpoint and render polygon
  try {
    const detailRes = await farmApi.getOrchard(o.id)
    const detail = detailRes?.data ?? detailRes
    if (detail?.boundaryGeojson) {
      renderBoundary(detail.boundaryGeojson)
    }
    // Refresh activeOrchard with detail (includes areaMu if missing in VO)
    activeOrchard.value = { ...o, ...detail }
  } catch {
    // Non-fatal — popup still shows list data
  }

  infoOpen.value = true
}

function renderBoundary(geojsonRaw) {
  if (!AMapNS || !map) return
  let geo = geojsonRaw
  try {
    if (typeof geo === 'string') geo = JSON.parse(geo)
  } catch {
    return
  }

  // Accept Feature, Polygon, or GeometryCollection
  let coords = null
  if (geo?.type === 'Feature' && geo.geometry?.type === 'Polygon') {
    coords = geo.geometry.coordinates
  } else if (geo?.type === 'Polygon') {
    coords = geo.coordinates
  }
  if (!coords || !coords[0] || coords[0].length < 3) return

  const path = coords[0].map(([lng, lat]) => [lng, lat])
  const polygon = new AMapNS.Polygon({
    path,
    strokeColor: BRAND_COLOR,
    strokeOpacity: 0.85,
    strokeWeight: 2,
    fillColor: BRAND_COLOR,
    fillOpacity: 0.12,
  })
  map.add(polygon)
  polygons.push(polygon)

  // Compute polygon area via AMap GeometryUtil
  try {
    if (AMapNS.GeometryUtil?.ringArea) {
      const areaSqMeters = AMapNS.GeometryUtil.ringArea(path)
      // 1 亩 = 666.67 m²
      computedArea.value = Number((areaSqMeters / 666.67).toFixed(2))
    }
  } catch {
    computedArea.value = null
  }
}

function goToDetail(o) {
  if (!o?.id) return
  router.push(`/farm/orchard/${o.id}`)
}

/* ============ location search ============ */
function onSearchInput(val) {
  if (searchTimer) clearTimeout(searchTimer)
  if (!val || !autoComplete) {
    searchTips.value = []
    return
  }
  searchTimer = setTimeout(() => {
    autoComplete.search(val, (status, result) => {
      if (status === 'complete' && Array.isArray(result?.tips)) {
        searchTips.value = result.tips
          .filter((t) => t.location && typeof t.location.getLng === 'function')
          .slice(0, 8)
      } else {
        searchTips.value = []
      }
    })
  }, 250)
}

function onSearchEnter() {
  if (searchTips.value.length > 0) {
    onSelectTip(searchTips.value[0])
  }
}

function onSelectTip(tip) {
  if (!tip?.location || !map || !AMapNS) return
  const lng = tip.location.getLng()
  const lat = tip.location.getLat()
  map.setZoomAndCenter(15, [lng, lat], !REDUCED_MOTION)

  if (searchMarker) map.remove(searchMarker)
  searchMarker = new AMapNS.Marker({
    position: [lng, lat],
    title: tip.name,
    icon: new AMapNS.Icon({
      size: new AMapNS.Size(25, 34),
      image: 'https://webapi.amap.com/theme/v1.3/markers/n/mark_bs.png',
      imageSize: new AMapNS.Size(25, 34),
    }),
  })
  map.add(searchMarker)

  searchTips.value = []
  searchKeyword.value = tip.name
  hintText.value = `已定位到「${tip.name}」`
}

/* ============ current location ============ */
function locateMe() {
  if (!map || !AMapNS) return
  locating.value = true

  // Prefer AMap Geolocation (handles GCJ-02 conversion)
  if (geolocation) {
    geolocation.getCurrentPosition((status, result) => {
      locating.value = false
      if (status === 'complete' && result?.position) {
        const lng = result.position.getLng()
        const lat = result.position.getLat()
        placeLocateMarker(lng, lat)
        showToast({ type: 'success', message: '已定位到当前位置' })
      } else {
        fallbackGeolocation()
      }
    })
    return
  }
  fallbackGeolocation()
}

function fallbackGeolocation() {
  if (!navigator.geolocation) {
    locating.value = false
    showToast('当前设备不支持定位')
    return
  }
  navigator.geolocation.getCurrentPosition(
    (pos) => {
      locating.value = false
      placeLocateMarker(pos.coords.longitude, pos.coords.latitude)
      showToast({ type: 'success', message: '已定位到当前位置' })
    },
    () => {
      locating.value = false
      showToast('定位失败，请检查权限设置')
    },
    { enableHighAccuracy: true, timeout: 10000 },
  )
}

function placeLocateMarker(lng, lat) {
  if (!map || !AMapNS) return
  map.setZoomAndCenter(15, [lng, lat], !REDUCED_MOTION)
  if (locateMarker) map.remove(locateMarker)
  locateMarker = new AMapNS.Marker({
    position: [lng, lat],
    title: '我的位置',
    content: '<div class="locate-dot" aria-label="当前位置"></div>',
    offset: new AMapNS.Pixel(-10, -10),
  })
  map.add(locateMarker)
}

/* ============ boundary drawing ============ */
async function openOrchardSheet() {
  drawLoading.value = true
  try {
    const res = await farmApi.getOrchards({ page: 1, size: 999 })
    const payload = res?.data ?? res
    const list = payload?.records || payload?.list || payload || []
    orchardActions.value = (Array.isArray(list) ? list : []).map((o) => ({
      name: o.orchardName || '未知果园',
      id: o.id,
    }))
    if (orchardActions.value.length === 0) {
      showToast('暂无果园可选择')
      return
    }
    showSheet.value = true
  } catch {
    showToast('加载果园列表失败')
  } finally {
    drawLoading.value = false
  }
}

function onOrchardSelect(action) {
  if (!action?.id) return
  selectedOrchardId = action.id
  showSheet.value = false
  hintText.value = `正在绘制「${action.name}」边界，点击地图添加顶点，双击完成`
  showToast('请在地图上点击绘制多边形边界')

  if (mouseTool) {
    mouseTool.polygon({
      strokeColor: BRAND_COLOR,
      strokeOpacity: 0.85,
      strokeWeight: 2,
      fillColor: BRAND_COLOR,
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
    showToast('边界至少需要 3 个顶点')
    if (map) map.remove(overlay)
    return
  }

  const coordinates = path.map((p) => [p.getLng(), p.getLat()])
  // close ring
  if (coordinates[0][0] !== coordinates[coordinates.length - 1][0]
    || coordinates[0][1] !== coordinates[coordinates.length - 1][1]) {
    coordinates.push([coordinates[0][0], coordinates[0][1]])
  }

  const geojson = {
    type: 'Feature',
    properties: {},
    geometry: { type: 'Polygon', coordinates: [coordinates] },
  }

  if (mouseTool) mouseTool.close(true)
  currentDrawOverlay = overlay
  saveBoundary(selectedOrchardId, geojson, overlay)
}

async function saveBoundary(orchardId, geojson, overlay) {
  try {
    await orchardGeoApi.saveBoundary(orchardId, geojson)
    showToast({ type: 'success', message: '边界保存成功' })
    hintText.value = '边界已保存'
    // Refresh the viewport to pick up the saved orchard with new center/area
    await queryOrchards()
  } catch {
    showToast('边界保存失败，请重试')
    if (map && overlay) map.remove(overlay)
  } finally {
    selectedOrchardId = null
    currentDrawOverlay = null
  }
}

/* ============ lifecycle ============ */
onMounted(initMap)

onBeforeUnmount(() => {
  if (queryTimer) clearTimeout(queryTimer)
  if (searchTimer) clearTimeout(searchTimer)
  if (mouseTool) {
    try { mouseTool.close(true) } catch {}
    mouseTool = null
  }
  clearMarkers()
  if (locateMarker && map) map.remove(locateMarker)
  if (searchMarker && map) map.remove(searchMarker)
  if (map) {
    try { map.destroy() } catch {}
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

  /* Local layout tokens (derived from design scale) */
  --map-header-offset: calc(var(--space-8) * 3.75);  /* header + search height */
  --map-list-reserve:  calc(var(--space-8) * 5.625); /* header + bottom hint total */
  --list-item-accent: var(--space-1);                /* visible list-item left border accent */
}

/* ============ Header ============ */
.map-header {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  z-index: var(--z-sticky, 1100);
  padding: var(--space-3) var(--space-4) var(--space-2);
  background: linear-gradient(
    to bottom,
    var(--color-surface-raised) 0%,
    color-mix(in srgb, var(--color-surface-raised) 92%, transparent) 85%,
    transparent 100%
  );
}

.map-header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--space-3);
}

.page-title {
  font-size: var(--font-size-h2);
  font-weight: 600;
  color: var(--color-text-primary);
  margin: 0;
  line-height: 1.4;
}

.map-header-actions {
  display: flex;
  gap: var(--space-2);
}

.map-search-row {
  position: relative;
  margin-top: var(--space-3);
}

.map-search-row :deep(.van-field) {
  border-radius: var(--radius-base);
  background: var(--color-surface-sunken);
}

.map-search-tips {
  position: absolute;
  top: calc(100% + var(--space-1));
  left: 0;
  right: 0;
  max-height: 280px;
  overflow-y: auto;
  background: var(--color-surface-raised);
  border: 1px solid var(--color-border-default);
  border-radius: var(--radius-base);
  box-shadow: var(--shadow-2);
  list-style: none;
  padding: var(--space-1) 0;
  margin: 0;
  z-index: var(--z-sticky, 1100);
}

.map-search-tip {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
  padding: var(--space-3) var(--space-4);
  cursor: pointer;
  border-bottom: 1px solid var(--color-divider);
  transition: background-color var(--motion-fast) var(--ease-out);
}

.map-search-tip:last-child {
  border-bottom: none;
}

.map-search-tip:hover {
  background-color: var(--color-brand-light);
}

.map-search-tip:focus-visible {
  background-color: var(--color-brand-light);
  box-shadow: var(--shadow-focus);
}

.tip-name {
  font-size: var(--font-size-body);
  color: var(--color-text-primary);
}

.tip-district {
  font-size: var(--font-size-caption);
  color: var(--color-text-tertiary);
}

/* ============ Map + List main area ============ */
.map-main {
  position: absolute;
  inset: 0;
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
  padding: var(--space-6);
  background-color: var(--color-surface-base);
  z-index: 2;
}

/* ============ List panel ============ */
.map-list-panel {
  position: absolute;
  right: var(--space-4);
  top: var(--map-header-offset);
  width: min(var(--space-16) * 4.7, 100vw - var(--space-8));
  max-height: calc(100vh - var(--map-list-reserve));
  display: flex;
  flex-direction: column;
  background: var(--color-surface-raised);
  border: var(--border-width-default, 1px) solid var(--color-border-default);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-2);
  overflow: hidden;
  z-index: var(--z-sticky, 1100);
  transition: transform var(--motion-base) var(--ease-out),
              opacity var(--motion-base) var(--ease-out);
}

.list-panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-3) var(--space-4);
  border-bottom: 1px solid var(--color-divider);
  background: var(--color-surface-raised);
}

.list-panel-title {
  font-size: var(--font-size-h3);
  font-weight: 600;
  color: var(--color-text-primary);
  display: flex;
  gap: var(--space-2);
  align-items: baseline;
}

.list-panel-count {
  font-size: var(--font-size-caption);
  color: var(--color-text-tertiary);
  font-weight: 400;
}

.list-panel-body {
  flex: 1;
  overflow-y: auto;
  padding: var(--space-2) 0;
}

.list-panel-state {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-6) var(--space-4);
  min-height: 120px;
}

.list-item {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
  width: 100%;
  padding: var(--space-3) var(--space-4);
  background: transparent;
  border: none;
  border-left: var(--list-item-accent) solid transparent;
  text-align: left;
  cursor: pointer;
  transition: background-color var(--motion-fast) var(--ease-out),
              border-color var(--motion-fast) var(--ease-out);
}

.list-item:hover {
  background: var(--color-surface-sunken);
}

.list-item:focus-visible {
  outline: var(--outline-width) solid var(--color-brand-primary);
  outline-offset: calc(-1 * var(--outline-width));
}

.list-item.is-active {
  background: var(--color-brand-light);
  border-left-color: var(--color-brand-primary);
}

.list-item-main {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--space-2);
}

.list-item-name {
  font-size: var(--font-size-body);
  font-weight: 500;
  color: var(--color-text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.list-item-meta {
  display: flex;
  justify-content: space-between;
  gap: var(--space-2);
  font-size: var(--font-size-caption);
  color: var(--color-text-secondary);
}

.nums-tabular {
  font-family: var(--font-mono);
  font-variant-numeric: tabular-nums;
}

/* ============ Collapsed state (works on both mobile and desktop) ============ */
.map-list-panel:not(.is-open) {
  max-height: var(--density-cell-height);
}

.map-list-panel:not(.is-open) .list-panel-body {
  display: none;
}

/* ============ Hint ============ */
.map-hint {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: var(--z-sticky, 1100);
  padding: var(--space-3) var(--space-4);
  background: linear-gradient(
    to top,
    color-mix(in srgb, var(--color-text-primary) 75%, transparent) 0%,
    color-mix(in srgb, var(--color-text-primary) 40%, transparent) 80%,
    transparent 100%
  );
  pointer-events: none;
}

.hint-text {
  display: block;
  text-align: center;
  font-size: var(--font-size-body-s);
  color: var(--color-text-inverse);
  line-height: 1.5;
}

/* ============ Info popup ============ */
.info-popup {
  padding: var(--space-4) 0 var(--space-4);
}

.info-popup-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--space-3);
  padding: 0 var(--space-4) var(--space-3);
}

.info-popup-title {
  font-size: var(--font-size-h2);
  font-weight: 600;
  color: var(--color-text-primary);
  margin: 0;
}

.info-popup-actions {
  padding: var(--space-4);
}

.area-delta {
  margin-left: var(--space-2);
  font-size: var(--font-size-caption);
}

.area-delta--ok {
  color: var(--color-success);
}

.area-delta--warn {
  color: var(--color-warning);
}

.area-delta--critical {
  color: var(--color-error);
}

/* ============ AMap overlays (global scope since AMap renders outside Vue) ============ */
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

:deep(.locate-dot) {
  width: var(--space-5);
  height: var(--space-5);
  border-radius: 50%;
  background-color: var(--color-info);
  border: var(--list-item-accent) solid var(--color-surface-raised);
  box-shadow: 0 0 0 var(--space-1) color-mix(in srgb, var(--color-info) 30%, transparent);
}

/* ============ Reduced motion ============ */
@media (prefers-reduced-motion: reduce) {
  .map-list-panel,
  .list-item,
  .map-search-tip {
    transition: none;
  }
}
</style>
