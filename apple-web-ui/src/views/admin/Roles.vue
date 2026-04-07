<template>
  <div class="roles-page">
    <van-nav-bar title="角色与权限矩阵" left-arrow @click-left="$router.back()" />

    <van-tabs v-model:active="activeTab">
      <!-- ─── Tab 1: matrix ─────────────────────────────────────────────── -->
      <van-tab title="权限矩阵">
        <div v-if="loadingMatrix" class="loading-wrap">
          <van-loading size="24" vertical>加载中...</van-loading>
        </div>
        <div v-else>
          <van-cell-group inset v-for="(perms, roleCode) in matrix" :key="roleCode" style="margin-bottom:8px">
            <van-cell :title="roleLabel(roleCode)" :label="`${perms.length} 项权限`">
              <template #right-icon>
                <van-tag type="primary" plain>{{ roleCode }}</van-tag>
              </template>
            </van-cell>
            <van-cell>
              <template #title>
                <div class="perm-grid">
                  <van-tag
                    v-for="code in perms"
                    :key="code"
                    type="success"
                    plain
                    size="medium"
                    style="margin:2px 4px"
                  >
                    {{ code }}
                  </van-tag>
                  <van-empty v-if="perms.length === 0" description="未授权" :image-size="40" />
                </div>
              </template>
            </van-cell>
          </van-cell-group>
        </div>
      </van-tab>

      <!-- ─── Tab 2: assign ─────────────────────────────────────────────── -->
      <van-tab title="分配角色">
        <van-cell-group inset>
          <van-field
            v-model.number="assign.userId"
            label="用户 ID"
            type="digit"
            placeholder="例如 1002"
            clearable
          />
          <van-field
            label="角色"
            readonly
            is-link
            :value="assign.roleCodes.join(',')"
            placeholder="选择一个或多个角色"
            @click="showRolePicker = true"
          />
        </van-cell-group>
        <div class="action-bar">
          <van-button type="primary" block :loading="submitting" @click="submitAssign">
            提交分配
          </van-button>
        </div>
        <van-popup v-model:show="showRolePicker" position="bottom" round>
          <van-checkbox-group v-model="assign.roleCodes">
            <van-cell-group>
              <van-cell
                v-for="r in roles"
                :key="r.roleCode"
                :title="`${r.roleName} (${r.roleCode})`"
                clickable
                @click="toggleRole(r.roleCode)"
              >
                <template #right-icon>
                  <van-checkbox :name="r.roleCode" @click.stop />
                </template>
              </van-cell>
            </van-cell-group>
          </van-checkbox-group>
          <div style="padding:12px">
            <van-button block type="primary" @click="showRolePicker = false">完成</van-button>
          </div>
        </van-popup>
      </van-tab>
    </van-tabs>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { showToast, showSuccessToast } from 'vant'
import { rbacApi } from '@/api/auth.js'

const ROLE_LABELS = {
  ADMIN: '系统管理员',
  FARMER: '种植户/合作社',
  SUPPLIER: '供应商/加工方',
  BUYER: '收购商/经销商',
  LOGISTICS: '物流服务商',
  FINANCE: '金融机构',
  GOV: '监管部门'
}
const roleLabel = (code) => ROLE_LABELS[code] || code

const activeTab = ref(0)
const matrix = ref({})
const roles = ref([])
const loadingMatrix = ref(false)

const showRolePicker = ref(false)
const submitting = ref(false)
const assign = ref({ userId: null, roleCodes: [] })

async function loadMatrix() {
  loadingMatrix.value = true
  try {
    matrix.value = await rbacApi.getMatrix()
  } catch (e) {
    showToast({ type: 'fail', message: '加载矩阵失败' })
  } finally {
    loadingMatrix.value = false
  }
}

async function loadRoles() {
  try {
    roles.value = await rbacApi.listRoles()
  } catch (e) {
    showToast({ type: 'fail', message: '加载角色失败' })
  }
}

function toggleRole(code) {
  const i = assign.value.roleCodes.indexOf(code)
  if (i >= 0) {
    assign.value.roleCodes.splice(i, 1)
  } else {
    assign.value.roleCodes.push(code)
  }
}

async function submitAssign() {
  if (!assign.value.userId) {
    showToast('请输入用户 ID')
    return
  }
  if (assign.value.roleCodes.length === 0) {
    showToast('请至少选择一个角色')
    return
  }
  submitting.value = true
  try {
    await rbacApi.assignRoles(assign.value.userId, assign.value.roleCodes)
    showSuccessToast('角色已更新')
    assign.value = { userId: null, roleCodes: [] }
  } catch (e) {
    showToast({ type: 'fail', message: e?.message || '分配失败' })
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadMatrix()
  loadRoles()
})
</script>

<style scoped>
.roles-page {
  padding-bottom: 24px;
}
.loading-wrap {
  padding: 32px 0;
  text-align: center;
}
.perm-grid {
  display: flex;
  flex-wrap: wrap;
  padding: 8px 0;
}
.action-bar {
  padding: 16px;
}
</style>
