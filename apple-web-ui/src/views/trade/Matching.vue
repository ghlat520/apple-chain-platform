<template>
  <div class="matching-page">
    <van-tabs v-model:active="activeTab" sticky animated>
      <!-- ======== Tab 1: 撮合匹配 ======== -->
      <van-tab title="撮合匹配" name="match">
        <!-- 搜索栏: 输入 supplyId 计算撮合 -->
        <div class="search-bar">
          <van-field
            v-model.number="supplyIdInput"
            label="供货ID"
            type="number"
            placeholder="输入供货信息ID"
            input-align="right"
            style="flex: 1"
          />
          <van-button
            type="primary"
            size="small"
            :loading="computing"
            :disabled="!supplyIdInput"
            @click="handleCompute"
          >
            计算撮合
          </van-button>
          <van-button
            type="default"
            size="small"
            :loading="loadingMatch"
            :disabled="!supplyIdInput"
            @click="handleGetCached"
          >
            查缓存
          </van-button>
        </div>

        <!-- 撮合候选列表 -->
        <van-pull-refresh v-model="refreshing" @refresh="handleCompute">
          <div v-if="!computing && matchList.length === 0">
            <van-empty description="暂无撮合结果，请输入供货ID后计算" />
          </div>
          <van-cell-group
            v-for="item in matchList"
            :key="item.id"
            inset
            class="match-card"
          >
            <van-cell>
              <template #title>
                <span class="match-score nums-tabular">{{ item.matchScore ?? '-' }}分</span>
                <van-tag
                  :type="matchStatusType(item.status)"
                  size="small"
                  style="margin-left: var(--space-2)"
                >
                  {{ matchStatusLabel(item.status) }}
                </van-tag>
              </template>
              <template #label>
                <div class="match-info">
                  <span>供货: <span class="nums-tabular">{{ item.supplyId ?? '-' }}</span></span>
                  <span style="margin-left: var(--space-3)">需求: <span class="nums-tabular">{{ item.demandId ?? '-' }}</span></span>
                </div>
                <div v-if="item.matchTime" class="match-time">
                  {{ item.matchTime }}
                </div>
              </template>
              <template #value>
                <van-button
                  v-if="item.status !== 3 && item.status !== 4"
                  type="primary"
                  size="mini"
                  @click="openNegotiateDrawer(item)"
                >
                  发起议价
                </van-button>
              </template>
            </van-cell>
          </van-cell-group>
        </van-pull-refresh>
      </van-tab>

      <!-- ======== Tab 2: 议价聊天 ======== -->
      <van-tab title="议价聊天" name="chat">
        <!-- 议价会话列表 -->
        <template v-if="!currentSession">
          <van-pull-refresh v-model="refreshingSession" @refresh="loadNegotiations">
            <div v-if="!loadingSession && negotiationList.length === 0">
              <van-empty description="暂无议价会话" />
            </div>
            <van-cell-group
              v-for="nego in negotiationList"
              :key="nego.id"
              inset
              class="nego-card"
            >
              <van-cell is-link @click="openChat(nego)">
                <template #title>
                  <span>议价 #<span class="nums-tabular">{{ nego.id }}</span></span>
                  <van-tag
                    :type="negoStatusType(nego.status)"
                    size="small"
                    style="margin-left: var(--space-2)"
                  >
                    {{ negoStatusLabel(nego.status) }}
                  </van-tag>
                </template>
                <template #label>
                  <div class="nego-info">
                    <span>当前价格: <span class="nums-tabular price-highlight">{{ formatPrice(nego.currentPrice) }}</span></span>
                  </div>
                  <div class="nego-info">
                    <span>数量: <span class="nums-tabular">{{ nego.currentQuantity ?? '-' }}</span></span>
                    <span v-if="nego.lastOfferBy" style="margin-left: var(--space-3)">
                      最近报价方: {{ nego.lastOfferBy }}
                    </span>
                  </div>
                </template>
                <template #value>
                  <div class="nego-actions">
                    <van-button
                      v-if="nego.status === 0"
                      type="warning"
                      size="mini"
                      @click.stop="openOfferDrawer(nego)"
                    >
                      报价
                    </van-button>
                    <van-button
                      v-if="nego.status === 0"
                      type="success"
                      size="mini"
                      @click.stop="handleAccept(nego)"
                    >
                      接受
                    </van-button>
                    <van-button
                      v-if="nego.status === 0"
                      type="default"
                      size="mini"
                      @click.stop="handleCancel(nego)"
                    >
                      取消
                    </van-button>
                  </div>
                </template>
              </van-cell>
            </van-cell-group>
          </van-pull-refresh>
        </template>

        <!-- 聊天界面 -->
        <template v-else>
          <div class="chat-header">
            <van-icon name="arrow-left" size="20" @click="closeChat" />
            <span class="chat-title">议价 #<span class="nums-tabular">{{ currentSession.id }}</span></span>
            <van-tag
              :type="negoStatusType(currentSession.status)"
              size="small"
            >
              {{ negoStatusLabel(currentSession.status) }}
            </van-tag>
          </div>

          <!-- 消息列表 -->
          <div class="chat-messages" ref="messagesContainer">
            <div
              v-for="msg in chatMessages"
              :key="msg.id"
              :class="['chat-bubble-wrap', isOwnMessage(msg) ? 'own' : 'other']"
            >
              <div :class="['chat-bubble', msg.msgType === 'PRICE_OFFER' ? 'price-offer' : '']">
                <template v-if="msg.msgType === 'PRICE_OFFER'">
                  <div class="price-offer-tag">报价</div>
                  <div class="price-offer-content nums-tabular">{{ msg.content }}</div>
                </template>
                <template v-else>
                  {{ msg.content }}
                </template>
                <div class="chat-time">{{ formatTime(msg.sendTime) }}</div>
              </div>
            </div>
            <van-empty v-if="chatMessages.length === 0" description="暂无消息" />
          </div>

          <!-- 输入区域 -->
          <div v-if="currentSession.status === 0" class="chat-input-bar">
            <van-field
              v-model="chatInput"
              placeholder="输入消息..."
              type="textarea"
              rows="1"
              autosize
              maxlength="500"
              show-word-limit
              @keydown.enter.exact.prevent="handleSendText"
            />
            <van-button
              type="primary"
              size="small"
              :disabled="!chatInput.trim()"
              :loading="sendingChat"
              @click="handleSendText"
            >
              发送
            </van-button>
          </div>
          <div v-else class="chat-input-bar disabled">
            <span class="chat-disabled-text">议价已结束</span>
          </div>
        </template>
      </van-tab>
    </van-tabs>

    <!-- ======== 发起议价 Drawer ======== -->
    <van-popup
      v-model:show="showNegotiateDrawer"
      position="bottom"
      round
      :style="{ maxHeight: '80vh', overflowY: 'auto' }"
    >
      <div class="drawer-header">
        <span>发起议价</span>
        <van-icon name="cross" @click="showNegotiateDrawer = false" />
      </div>
      <van-form @submit="handleStartNegotiation" class="drawer-form">
        <van-cell-group inset>
          <van-field
            v-model.number="negoForm.matchId"
            label="撮合ID"
            type="number"
            placeholder="匹配ID"
            :rules="[{ required: true, message: '请填写撮合ID' }]"
          />
          <van-field
            v-model.number="negoForm.supplyUserId"
            label="供货方ID"
            type="number"
            placeholder="供货方用户ID"
            :rules="[{ required: true, message: '请填写供货方ID' }]"
          />
          <van-field
            v-model.number="negoForm.demandUserId"
            label="需求方ID"
            type="number"
            placeholder="需求方用户ID"
            :rules="[{ required: true, message: '请填写需求方ID' }]"
          />
          <van-field
            v-model.number="negoForm.price"
            label="初始报价"
            type="number"
            placeholder="请输入报价"
            :rules="[{ required: true, message: '请填写报价' }]"
          />
          <van-field
            v-model.number="negoForm.quantity"
            label="数量"
            type="number"
            placeholder="请输入数量"
            :rules="[{ required: true, message: '请填写数量' }]"
          />
        </van-cell-group>
        <div class="drawer-actions">
          <van-button block type="primary" native-type="submit" :loading="startingNego">
            确认发起
          </van-button>
        </div>
      </van-form>
    </van-popup>

    <!-- ======== 报价 Drawer ======== -->
    <van-popup
      v-model:show="showOfferDrawer"
      position="bottom"
      round
      :style="{ maxHeight: '60vh', overflowY: 'auto' }"
    >
      <div class="drawer-header">
        <span>报价</span>
        <van-icon name="cross" @click="showOfferDrawer = false" />
      </div>
      <van-form @submit="handleOffer" class="drawer-form">
        <van-cell-group inset>
          <van-field
            v-model.number="offerForm.price"
            label="报价"
            type="number"
            placeholder="请输入报价"
            :rules="[{ required: true, message: '请填写报价' }]"
          />
          <van-field
            v-model.number="offerForm.quantity"
            label="数量"
            type="number"
            placeholder="请输入数量"
            :rules="[{ required: true, message: '请填写数量' }]"
          />
        </van-cell-group>
        <div class="drawer-actions">
          <van-button block type="warning" native-type="submit" :loading="offering">
            确认报价
          </van-button>
        </div>
      </van-form>
    </van-popup>
  </div>
</template>

<script setup>
import { ref, reactive, nextTick, onMounted } from 'vue'
import { tradeApi } from '@/api/trade.js'
import { useAuthStore } from '@/store/auth.js'
import { showToast, showConfirmDialog } from 'vant'

const authStore = useAuthStore()
const currentUserId = authStore.user?.id || authStore.user?.userId

// ==================== Tab state ====================
const activeTab = ref('match')

// ==================== Tab 1: Match ====================
const supplyIdInput = ref(null)
const computing = ref(false)
const loadingMatch = ref(false)
const refreshing = ref(false)
const matchList = ref([])

async function handleCompute() {
  if (!supplyIdInput.value) return
  computing.value = true
  try {
    const res = await tradeApi.computeMatch(supplyIdInput.value)
    matchList.value = Array.isArray(res) ? res : (res?.records || [])
    showToast({ type: 'success', message: `找到 ${matchList.value.length} 条撮合` })
  } catch (e) {
    matchList.value = []
    showToast({ type: 'fail', message: '撮合计算失败' })
  } finally {
    computing.value = false
    refreshing.value = false
  }
}

async function handleGetCached() {
  if (!supplyIdInput.value) return
  loadingMatch.value = true
  try {
    const res = await tradeApi.getMatchTop(supplyIdInput.value)
    matchList.value = Array.isArray(res) ? res : (res?.records || [])
    showToast({ type: 'success', message: `缓存 ${matchList.value.length} 条` })
  } catch (e) {
    matchList.value = []
    showToast({ type: 'fail', message: '缓存查询失败' })
  } finally {
    loadingMatch.value = false
  }
}

// ==================== Tab 2: Negotiation list ====================
const loadingSession = ref(false)
const refreshingSession = ref(false)
const negotiationList = ref([])
const currentSession = ref(null)

async function loadNegotiations() {
  loadingSession.value = true
  try {
    // Use getMatchTop with a general query or the negotiation list endpoint.
    // Since there's no dedicated "list my negotiations" endpoint,
    // we rely on the cached match data. For now, show negotiations from matchList.
    // In a real scenario, there would be a GET /trade/negotiation/list endpoint.
    // We'll use computeMatch with supplyId as a proxy.
    negotiationList.value = []
    showToast({ type: 'fail', message: '暂无议价列表接口，请通过撮合后发起' })
  } catch (e) {
    negotiationList.value = []
  } finally {
    loadingSession.value = false
    refreshingSession.value = false
  }
}

// ==================== Start Negotiation ====================
const showNegotiateDrawer = ref(false)
const startingNego = ref(false)
const negoForm = reactive({
  matchId: null,
  supplyUserId: null,
  demandUserId: null,
  price: null,
  quantity: null
})

function openNegotiateDrawer(matchItem) {
  Object.assign(negoForm, {
    matchId: matchItem.id ?? null,
    supplyUserId: null,
    demandUserId: null,
    price: null,
    quantity: null
  })
  showNegotiateDrawer.value = true
}

async function handleStartNegotiation() {
  startingNego.value = true
  try {
    await tradeApi.startNegotiation(negoForm)
    showToast({ type: 'success', message: '议价已发起' })
    showNegotiateDrawer.value = false
    // Switch to chat tab and add to list
    activeTab.value = 'chat'
  } catch (e) {
    showToast({ type: 'fail', message: '发起议价失败' })
  } finally {
    startingNego.value = false
  }
}

// ==================== Offer / Accept / Cancel ====================
const showOfferDrawer = ref(false)
const offering = ref(false)
const offerForm = reactive({ price: null, quantity: null })
let targetNegoId = null

function openOfferDrawer(nego) {
  targetNegoId = nego.id
  Object.assign(offerForm, {
    price: nego.currentPrice ?? null,
    quantity: nego.currentQuantity ?? null
  })
  showOfferDrawer.value = true
}

async function handleOffer() {
  offering.value = true
  try {
    await tradeApi.offerNegotiation(targetNegoId, {
      byUserId: currentUserId,
      price: offerForm.price,
      quantity: offerForm.quantity
    })
    showToast({ type: 'success', message: '报价成功' })
    showOfferDrawer.value = false
    // Refresh current session if chatting
    if (currentSession.value && currentSession.value.id === targetNegoId) {
      await loadChatHistory(currentSession.value.id)
    }
  } catch (e) {
    showToast({ type: 'fail', message: '报价失败' })
  } finally {
    offering.value = false
  }
}

async function handleAccept(nego) {
  try {
    await showConfirmDialog({ title: '确认接受', message: '确认接受当前报价并自动建单？' })
    await tradeApi.acceptNegotiation(nego.id, { byUserId: currentUserId })
    showToast({ type: 'success', message: '已接受报价，订单已创建' })
  } catch (e) {
    if (e !== 'cancel' && e?.message !== 'cancel') {
      showToast({ type: 'fail', message: '操作失败' })
    }
  }
}

async function handleCancel(nego) {
  try {
    await showConfirmDialog({ title: '确认取消', message: '确认取消该议价？' })
    await tradeApi.cancelNegotiation(nego.id, { byUserId: currentUserId })
    showToast({ type: 'success', message: '议价已取消' })
  } catch (e) {
    if (e !== 'cancel' && e?.message !== 'cancel') {
      showToast({ type: 'fail', message: '取消失败' })
    }
  }
}

// ==================== Chat ====================
const chatMessages = ref([])
const chatInput = ref('')
const sendingChat = ref(false)
const messagesContainer = ref(null)

function openChat(nego) {
  currentSession.value = nego
  chatMessages.value = []
  chatInput.value = ''
  loadChatHistory(nego.id)
}

function closeChat() {
  currentSession.value = null
  chatMessages.value = []
  chatInput.value = ''
}

async function loadChatHistory(sessionId) {
  try {
    const res = await tradeApi.getChatHistory(sessionId)
    chatMessages.value = Array.isArray(res) ? res : (res?.records || [])
    await nextTick()
    scrollToBottom()
    // Mark as read
    if (currentUserId) {
      tradeApi.markChatRead(sessionId, currentUserId).catch(() => {})
    }
  } catch (e) {
    chatMessages.value = []
  }
}

async function handleSendText() {
  const content = chatInput.value.trim()
  if (!content || !currentSession.value || sendingChat.value) return
  sendingChat.value = true
  try {
    await tradeApi.sendChat({
      sessionId: currentSession.value.id,
      fromUserId: currentUserId,
      toUserId: currentSession.value.supplyUserId === currentUserId
        ? currentSession.value.demandUserId
        : currentSession.value.supplyUserId,
      msgType: 'TEXT',
      content
    })
    chatInput.value = ''
    await loadChatHistory(currentSession.value.id)
  } catch (e) {
    showToast({ type: 'fail', message: '发送失败' })
  } finally {
    sendingChat.value = false
  }
}

function isOwnMessage(msg) {
  return msg.fromUserId === currentUserId
}

function scrollToBottom() {
  const container = messagesContainer.value
  if (container) {
    container.scrollTop = container.scrollHeight
  }
}

// ==================== Helpers ====================
function matchStatusType(status) {
  const map = { 0: 'primary', 1: 'warning', 2: 'success', 3: 'success', 4: 'default' }
  return map[status] || 'default'
}

function matchStatusLabel(status) {
  const map = { 0: '候选', 1: '已联系', 2: '议价中', 3: '已成交', 4: '已拒绝' }
  return map[status] ?? String(status)
}

function negoStatusType(status) {
  const map = { 0: 'primary', 1: 'success', 2: 'default' }
  return map[status] || 'default'
}

function negoStatusLabel(status) {
  const map = { 0: '进行中', 1: '已成交', 2: '已取消' }
  return map[status] ?? String(status)
}

function formatPrice(price) {
  if (price == null) return '-'
  return '\u00A5' + Number(price).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function formatTime(time) {
  if (!time) return ''
  // Handle ISO string or yyyy-MM-dd HH:mm:ss format
  const d = new Date(time.replace(' ', 'T'))
  if (isNaN(d.getTime())) return String(time)
  const hh = String(d.getHours()).padStart(2, '0')
  const mm = String(d.getMinutes()).padStart(2, '0')
  return `${hh}:${mm}`
}
</script>

<style scoped>
.matching-page {
  min-height: 100vh;
  background: var(--color-surface-base);
}

/* ---- Search Bar (Tab 1) ---- */
.search-bar {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-3) var(--space-4);
  background: var(--color-surface-raised);
}

/* ---- Cards ---- */
.match-card,
.nego-card {
  margin-bottom: var(--space-3);
  border-radius: var(--radius-md);
  overflow: hidden;
}

.match-score {
  font-size: var(--font-size-body);
  font-weight: 600;
  color: var(--color-brand-primary);
}

.match-info,
.nego-info {
  display: flex;
  gap: var(--space-2);
  color: var(--color-text-secondary);
  font-size: var(--font-size-body);
  line-height: var(--line-height-body);
}

.match-time {
  color: var(--color-text-tertiary);
  font-size: 13px;
  margin-top: var(--space-1);
}

.price-highlight {
  color: var(--color-brand-primary);
  font-weight: 600;
}

.nego-actions {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
  align-items: flex-end;
}

/* ---- Chat ---- */
.chat-header {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3) var(--space-4);
  background: var(--color-surface-raised);
  border-bottom: 1px solid var(--color-border-default);
  position: sticky;
  top: 44px;
  z-index: 10;
}

.chat-title {
  flex: 1;
  font-size: var(--font-size-body);
  font-weight: 500;
}

.chat-messages {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
  padding: var(--space-4);
  min-height: calc(100vh - 260px);
  max-height: calc(100vh - 260px);
  overflow-y: auto;
  background: var(--color-surface-base);
}

.chat-bubble-wrap {
  display: flex;
}

.chat-bubble-wrap.own {
  justify-content: flex-end;
}

.chat-bubble-wrap.other {
  justify-content: flex-start;
}

.chat-bubble {
  max-width: 75%;
  padding: var(--space-3) var(--space-4);
  border-radius: var(--radius-md);
  font-size: var(--font-size-body);
  line-height: var(--line-height-body);
  position: relative;
}

.chat-bubble-wrap.own .chat-bubble {
  background: var(--color-brand-primary);
  color: var(--color-text-inverse);
  border-bottom-right-radius: var(--radius-sm);
}

.chat-bubble-wrap.other .chat-bubble {
  background: var(--color-surface-raised);
  color: var(--color-text-primary);
  border-bottom-left-radius: var(--radius-sm);
  box-shadow: var(--shadow-1);
}

.chat-bubble.price-offer {
  background: var(--color-warning);
  color: var(--color-text-inverse);
}

.chat-bubble-wrap.own .chat-bubble.price-offer {
  background: var(--color-warning);
}

.chat-bubble-wrap.other .chat-bubble.price-offer {
  background: var(--color-warning);
}

.price-offer-tag {
  font-size: 13px;
  font-weight: 500;
  opacity: 0.85;
  margin-bottom: var(--space-1);
}

.price-offer-content {
  font-size: var(--font-size-body);
  font-weight: 600;
}

.chat-time {
  font-size: 12px;
  opacity: 0.6;
  margin-top: var(--space-1);
  text-align: right;
}

/* ---- Chat Input ---- */
.chat-input-bar {
  display: flex;
  align-items: flex-end;
  gap: var(--space-2);
  padding: var(--space-3) var(--space-4);
  background: var(--color-surface-raised);
  border-top: 1px solid var(--color-border-default);
  position: sticky;
  bottom: 50px;
  z-index: 10;
}

.chat-input-bar.disabled {
  justify-content: center;
  padding: var(--space-5) var(--space-4);
}

.chat-disabled-text {
  color: var(--color-text-tertiary);
  font-size: var(--font-size-body);
}

.chat-input-bar :deep(.van-field) {
  flex: 1;
  background: var(--color-surface-sunken);
  border-radius: var(--radius-base);
  padding: var(--space-2) var(--space-3);
}

/* ---- Drawer ---- */
.drawer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-4);
  font-size: var(--font-size-body);
  font-weight: 600;
  border-bottom: 1px solid var(--color-border-default);
}

.drawer-form {
  padding-bottom: var(--space-5);
}

.drawer-actions {
  padding: var(--space-4);
}

/* ---- Loading / Empty states ---- */
.van-empty {
  padding: var(--space-12) 0;
}
</style>
