<template>
  <div class="page-container">
    <div class="section-header">
      <span class="section-title">果园地图</span>
      <van-button v-if="!drawing" size="small" type="primary" icon="edit" @click="startDraw">绘制边界</van-button>
      <van-button v-else size="small" type="warning" icon="cross" @click="cancelDraw">取消绘制</van-button>
    </div>

    <div ref="mapContainer" class="map-canvas"></div>

    <div v-if="loadError" class="hint hint-error">{{ loadError }}</div>
    <div v-else-if="!loaded" class="hint">地图加载中…</div>
    <div v-else-if="drawing" class="hint">在地图上点击至少 3 个点，双击结束绘制</div>
    <div v-else-if="visibleCount === 0" class="hint">当前视窗暂无果园数据</div>
    <div v-else class="hint">视窗内 {{ visibleCount }} 个果园</div>

    <van-action-sheet v-model:show="pickerVisible" title="选择要绑定边界的果园">
      <van-cell
        v-for="o in orchardChoices"
        :key="o.id"
        :title="o.orchardName || o.name || `果园 ${o.id}`"
        :label="o.location"
        is-link
        @click="bindBoundaryToOrchard(o.id)"
      />
      <van-empty v-if="orchardChoices.length === 0" description="没有可选果园" />
    </van-action-sheet>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import AMapLoader from '@amap/amap-jsapi-loader'
import { showToast, showSuccessToast, showFailToast } from 'vant'
import { orchardGeoApi, amapConfigApi } from '@/api/orchardGeo'
import { orchardApi } from '@/api/orchard'

const mapContainer = ref(null)
const loaded = ref(false)
const loadError = ref('')
const drawing = ref(false)
const visibleCount = ref(0)
const pickerVisible = ref(false)
const orchardChoices = ref([])

let map = null
let AMap = null
let mouseTool = null
let currentPolygon = null
const markers = []
const polygons = []

onMounted(async () => {
  try {
    const cfg = (await amapConfigApi.fetch())?.data || {}
    if (!cfg.key) {
      loadError.value = '高德地图未配置（请在 application-local.yml 设置 amap.web-jsapi-key）'
      return
    }
    // securityJsCode must be set BEFORE AMapLoader.load
    window._AMapSecurityConfig = { securityJsCode: cfg.securityJsCode || '' }

    AMap = await AMapLoader.load({
      key: cfg.key,
      version: '2.0',
      plugins: ['AMap.MouseTool', 'AMap.Polygon', 'AMap.Marker']
    })

    map = new AMap.Map(mapContainer.value, {
      zoom: 13,
      center: [121.4405, 37.4638], // 烟台默认中心
      viewMode: '2D'
    })

    map.on('moveend', loadViewport)
    map.on('zoomend', loadViewport)

    mouseTool = new AMap.MouseTool(map)
    mouseTool.on('draw', onPolygonDrawn)

    loaded.value = true
    loadViewport()
  } catch (e) {
    console.error(e)
    loadError.value = '地图加载失败: ' + (e?.message || e)
  }
})

onBeforeUnmount(() => {
  if (map) {
    map.destroy()
    map = null
  }
})

async function loadViewport() {
  if (!map) return
  const bounds = map.getBounds()
  const sw = bounds.getSouthWest()
  const ne = bounds.getNorthEast()
  try {
    const res = await orchardGeoApi.bbox({
      lng1: sw.getLng(), lat1: sw.getLat(),
      lng2: ne.getLng(), lat2: ne.getLat()
    })
    renderOrchards(res?.data || [])
  } catch (e) {
    // request interceptor already toasts
  }
}

function renderOrchards(list) {
  // Clear previous markers (but keep currentPolygon while drawing)
  markers.forEach(m => map.remove(m))
  markers.length = 0
  polygons.forEach(p => map.remove(p))
  polygons.length = 0

  visibleCount.value = list.length
  list.forEach(o => {
    if (o.centerLng == null || o.centerLat == null) return
    const marker = new AMap.Marker({
      position: [Number(o.centerLng), Number(o.centerLat)],
      title: `${o.orchardName} · ${o.areaMu || '?'}亩`,
      label: { content: o.orchardName, direction: 'top' }
    })
    map.add(marker)
    markers.push(marker)
  })
}

function startDraw() {
  if (!loaded.value) return
  drawing.value = true
  mouseTool.polygon({
    strokeColor: '#07c160',
    fillColor: '#07c160',
    fillOpacity: 0.25,
    strokeWeight: 2
  })
}

function cancelDraw() {
  drawing.value = false
  if (mouseTool) mouseTool.close(true)
  if (currentPolygon) {
    map.remove(currentPolygon)
    currentPolygon = null
  }
}

async function onPolygonDrawn(event) {
  drawing.value = false
  currentPolygon = event.obj

  // Convert AMap polygon path to GeoJSON
  const path = currentPolygon.getPath()
  if (!path || path.length < 3) {
    showFailToast('至少需要 3 个点')
    map.remove(currentPolygon)
    currentPolygon = null
    return
  }
  const ring = path.map(p => [p.getLng(), p.getLat()])
  // Close the ring (GeoJSON polygons must be closed)
  ring.push([ring[0][0], ring[0][1]])
  const geojson = JSON.stringify({ type: 'Polygon', coordinates: [ring] })

  // Open orchard picker so user can bind this boundary to one of their orchards
  await loadOrchardChoices()
  pendingGeojson = geojson
  pickerVisible.value = true
}

let pendingGeojson = null

async function loadOrchardChoices() {
  try {
    const res = await orchardApi.list({ page: 1, size: 50 })
    orchardChoices.value = res?.data?.records || []
  } catch (e) {
    orchardChoices.value = []
  }
}

async function bindBoundaryToOrchard(id) {
  if (!pendingGeojson) return
  try {
    await orchardGeoApi.saveBoundary(id, pendingGeojson)
    showSuccessToast('边界保存成功')
    pickerVisible.value = false
    if (currentPolygon) {
      map.remove(currentPolygon)
      currentPolygon = null
    }
    pendingGeojson = null
    loadViewport()
  } catch (e) {
    // interceptor toasts
  }
}
</script>

<style scoped>
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #fff;
}
.section-title {
  font-size: 16px;
  font-weight: 600;
}
.map-canvas {
  width: 100%;
  height: calc(100vh - 110px);
  background: #f7f8fa;
}
.hint {
  position: fixed;
  bottom: 16px;
  left: 50%;
  transform: translateX(-50%);
  background: rgba(0, 0, 0, 0.7);
  color: #fff;
  padding: 8px 16px;
  border-radius: 16px;
  font-size: 13px;
  z-index: 1000;
}
.hint-error {
  background: #ee0a24;
}
</style>
