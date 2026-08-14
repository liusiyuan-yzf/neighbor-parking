<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import {
  Building2,
  Check,
  ClipboardList,
  LayoutDashboard,
  LogOut,
  ParkingSquare,
  Plus,
  RefreshCw,
  ScrollText,
  ShieldAlert,
  X,
} from '@lucide/vue'
import { api } from './api'
import type { AuditLog, Booking, Community, Complaint, ParkingSpace, User } from './types'

type PageKey = 'dashboard' | 'communities' | 'spaces' | 'bookings' | 'complaints' | 'audits'

const navigation = [
  { key: 'dashboard' as PageKey, label: '运营概览', icon: LayoutDashboard },
  { key: 'communities' as PageKey, label: '小区管理', icon: Building2 },
  { key: 'spaces' as PageKey, label: '车位审核', icon: ParkingSquare },
  { key: 'bookings' as PageKey, label: '预约订单', icon: ClipboardList },
  { key: 'complaints' as PageKey, label: '投诉处理', icon: ShieldAlert },
  { key: 'audits' as PageKey, label: '审计日志', icon: ScrollText },
]

const currentPage = ref<PageKey>('dashboard')
const user = ref<User>()
const loading = ref(false)
const loginLoading = ref(false)
const errorMessage = ref('')
const communities = ref<Community[]>([])
const spaces = ref<ParkingSpace[]>([])
const bookings = ref<Booking[]>([])
const complaints = ref<Complaint[]>([])
const audits = ref<AuditLog[]>([])
const showCommunityForm = ref(false)
const communityForm = reactive({ name: '', address: '', latitude: 31.2304, longitude: 121.4737, active: true })

const currentNav = computed(() => navigation.find((item) => item.key === currentPage.value)!)
const activeBookings = computed(
  () => bookings.value.filter((item) => ['CONFIRMED', 'IN_USE'].includes(item.status)).length,
)
const openComplaints = computed(
  () => complaints.value.filter((item) => ['OPEN', 'PROCESSING'].includes(item.status)).length,
)

onMounted(async () => {
  if (api.hasToken()) await refreshAll()
})

async function login() {
  loginLoading.value = true
  errorMessage.value = ''
  try {
    user.value = (await api.login()).user
    await refreshAll()
  } catch (error) {
    setError(error)
  } finally {
    loginLoading.value = false
  }
}

async function refreshAll() {
  loading.value = true
  errorMessage.value = ''
  try {
    const result = await Promise.all([
      api.communities(),
      api.pendingSpaces(),
      api.bookings(),
      api.complaints(),
      api.audits(),
    ])
    ;[communities.value, spaces.value, bookings.value, complaints.value, audits.value] = result
    user.value ||= {
      id: 3,
      nickname: '物业管理员',
      phoneMasked: '138****0003',
      roles: ['PROPERTY_ADMIN', 'PLATFORM_ADMIN'],
    }
  } catch (error) {
    setError(error)
  } finally {
    loading.value = false
  }
}

function logout() {
  api.logout()
  user.value = undefined
}

async function createCommunity() {
  if (!communityForm.name.trim() || !communityForm.address.trim()) {
    errorMessage.value = '请完整填写小区名称和地址'
    return
  }
  try {
    await api.createCommunity({ ...communityForm })
    Object.assign(communityForm, { name: '', address: '', latitude: 31.2304, longitude: 121.4737, active: true })
    showCommunityForm.value = false
    await refreshAll()
  } catch (error) {
    setError(error)
  }
}

async function toggleCommunity(community: Community) {
  try {
    await api.updateCommunity(community.id, {
      name: community.name,
      address: community.address,
      latitude: community.latitude,
      longitude: community.longitude,
      active: !community.active,
    })
    await refreshAll()
  } catch (error) {
    setError(error)
  }
}

async function reviewSpace(space: ParkingSpace, approved: boolean) {
  const note = window.prompt(
    approved ? '请输入通过说明' : '请输入驳回原因',
    approved ? '权属材料与现场信息核验通过' : '',
  )
  if (!note?.trim()) return
  try {
    await api.reviewSpace(space.id, approved, note.trim())
    await refreshAll()
  } catch (error) {
    setError(error)
  }
}

async function resolveComplaint(complaint: Complaint, resolved: boolean) {
  const note = window.prompt(resolved ? '请输入处理结论' : '请输入驳回说明', '')
  if (!note?.trim()) return
  try {
    await api.resolveComplaint(complaint.id, resolved, note.trim())
    await refreshAll()
  } catch (error) {
    setError(error)
  }
}

function setError(error: unknown) {
  errorMessage.value = error instanceof Error ? error.message : '操作失败'
}

function formatTime(value: string) {
  return new Intl.DateTimeFormat('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  }).format(new Date(value))
}

const statusText = (status: string) =>
  ({
    CONFIRMED: '待使用',
    IN_USE: '使用中',
    COMPLETED: '已完成',
    CANCELLED: '已取消',
    DISPUTED: '争议中',
    OPEN: '待处理',
    PROCESSING: '处理中',
    RESOLVED: '已解决',
    REJECTED: '已驳回',
  })[status] || status
</script>

<template>
  <main v-if="!user" class="login-screen">
    <section class="login-panel">
      <div class="brand-mark"><ParkingSquare :size="34" /></div>
      <p class="eyebrow">NEIGHBOR PARKING</p>
      <h1>邻里车位管理后台</h1>
      <p class="login-description">面向物业与平台运营人员的小区、车位、订单和投诉管理工作台。</p>
      <div v-if="errorMessage" class="error-banner">{{ errorMessage }}</div>
      <button class="button primary wide" :disabled="loginLoading" @click="login">
        {{ loginLoading ? '正在登录…' : '进入本地演示后台' }}
      </button>
      <p class="login-note">本地演示使用管理员 ID 3；生产环境将关闭开发登录。</p>
    </section>
    <section class="login-context">
      <div>
        <span>阶段 2 · 免费共享 MVP</span>
        <h2>让物业审核与邻里共享<br />形成可追溯的闭环</h2>
        <p>所有敏感操作都会写入审计日志。</p>
      </div>
    </section>
  </main>

  <div v-else class="app-shell">
    <aside class="sidebar">
      <div class="brand">
        <div class="brand-mark small"><ParkingSquare :size="24" /></div>
        <div><strong>邻里车位</strong><span>管理工作台</span></div>
      </div>
      <nav>
        <button
          v-for="item in navigation"
          :key="item.key"
          :class="['nav-item', { active: currentPage === item.key }]"
          @click="currentPage = item.key"
        >
          <component :is="item.icon" :size="19" /><span>{{ item.label }}</span>
          <span v-if="item.key === 'spaces' && spaces.length" class="count">{{ spaces.length }}</span>
          <span v-if="item.key === 'complaints' && openComplaints" class="count danger">{{ openComplaints }}</span>
        </button>
      </nav>
      <div class="sidebar-user">
        <div class="user-avatar">物</div>
        <div>
          <strong>{{ user.nickname }}</strong
          ><span>{{ user.phoneMasked }}</span>
        </div>
        <button title="退出" @click="logout"><LogOut :size="18" /></button>
      </div>
    </aside>

    <main class="content">
      <header class="topbar">
        <div>
          <p class="breadcrumb">邻里车位 / {{ currentNav.label }}</p>
          <h1>{{ currentNav.label }}</h1>
        </div>
        <button class="button secondary" :disabled="loading" @click="refreshAll">
          <RefreshCw :size="17" :class="{ spinning: loading }" />刷新数据
        </button>
      </header>
      <div v-if="errorMessage" class="error-banner">
        <span>{{ errorMessage }}</span
        ><button @click="errorMessage = ''"><X :size="16" /></button>
      </div>

      <template v-if="currentPage === 'dashboard'">
        <section class="metric-grid">
          <article class="metric-card">
            <span>已接入小区</span><strong>{{ communities.length }}</strong
            ><small>当前全部小区</small>
          </article>
          <article class="metric-card warning">
            <span>待审核车位</span><strong>{{ spaces.length }}</strong
            ><small>需物业核验权属</small>
          </article>
          <article class="metric-card">
            <span>进行中预约</span><strong>{{ activeBookings }}</strong
            ><small>待使用与使用中</small>
          </article>
          <article class="metric-card danger-card">
            <span>待处理投诉</span><strong>{{ openComplaints }}</strong
            ><small>需要及时跟进</small>
          </article>
        </section>
        <section class="panel">
          <div class="panel-heading">
            <div>
              <h2>最近预约</h2>
              <p>阶段 2 免费共享订单</p>
            </div>
            <button class="text-link" @click="currentPage = 'bookings'">查看全部</button>
          </div>
          <div class="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>预约号</th>
                  <th>车位</th>
                  <th>租用人</th>
                  <th>时间</th>
                  <th>状态</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="item in bookings.slice(0, 6)" :key="item.id">
                  <td class="mono">{{ item.bookingNo }}</td>
                  <td>#{{ item.spaceId }}</td>
                  <td>用户 #{{ item.renterId }}</td>
                  <td>{{ formatTime(item.startAt) }}</td>
                  <td>
                    <span :class="['status-pill', item.status.toLowerCase()]">{{ statusText(item.status) }}</span>
                  </td>
                </tr>
                <tr v-if="!bookings.length">
                  <td colspan="5" class="empty-cell">暂无预约数据</td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>
      </template>

      <section v-else-if="currentPage === 'communities'" class="panel">
        <div class="panel-heading">
          <div>
            <h2>运营小区</h2>
            <p>配置阶段 2 试点范围与地图坐标</p>
          </div>
          <button class="button primary" @click="showCommunityForm = !showCommunityForm">
            <Plus :size="17" />新增小区
          </button>
        </div>
        <form v-if="showCommunityForm" class="inline-form" @submit.prevent="createCommunity">
          <label>小区名称<input v-model="communityForm.name" placeholder="例如：阳光花园" /></label
          ><label class="wide-field"
            >详细地址<input v-model="communityForm.address" placeholder="省市区与门牌号" /></label
          ><label>纬度<input v-model.number="communityForm.latitude" type="number" step="0.000001" /></label
          ><label>经度<input v-model.number="communityForm.longitude" type="number" step="0.000001" /></label>
          <div class="form-actions">
            <button type="button" class="button ghost" @click="showCommunityForm = false">取消</button
            ><button class="button primary">保存小区</button>
          </div>
        </form>
        <div class="table-wrap">
          <table>
            <thead>
              <tr>
                <th>小区</th>
                <th>地址</th>
                <th>坐标</th>
                <th>状态</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in communities" :key="item.id">
                <td>
                  <strong>{{ item.name }}</strong>
                </td>
                <td>{{ item.address }}</td>
                <td class="mono">{{ item.latitude.toFixed(4) }}, {{ item.longitude.toFixed(4) }}</td>
                <td>
                  <span :class="['status-pill', item.active ? 'approved' : '']">{{
                    item.active ? '运营中' : '已停用'
                  }}</span>
                </td>
                <td>
                  <button class="text-link" @click="toggleCommunity(item)">{{ item.active ? '停用' : '启用' }}</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <section v-else-if="currentPage === 'spaces'" class="panel">
        <div class="panel-heading">
          <div>
            <h2>待审核车位</h2>
            <p>核验车位权属、编号和车辆限制</p>
          </div>
          <span class="summary-count">{{ spaces.length }} 项待办</span>
        </div>
        <div class="review-grid">
          <article v-for="space in spaces" :key="space.id" class="review-card">
            <div class="parking-icon"><ParkingSquare :size="26" /></div>
            <div class="review-body">
              <div class="review-title">
                <div>
                  <h3>{{ space.title }}</h3>
                  <p>{{ space.spaceCode }} · 小区 #{{ space.communityId }}</p>
                </div>
                <span class="status-pill warning">待审核</span>
              </div>
              <dl>
                <div>
                  <dt>车位主</dt>
                  <dd>用户 #{{ space.ownerId }}</dd>
                </div>
                <div>
                  <dt>车辆限制</dt>
                  <dd>{{ space.vehicleLimit }}</dd>
                </div>
                <div>
                  <dt>提交时间</dt>
                  <dd>{{ formatTime(space.createdAt) }}</dd>
                </div>
              </dl>
              <div class="review-actions">
                <button class="button reject" @click="reviewSpace(space, false)"><X :size="16" />驳回</button
                ><button class="button primary" @click="reviewSpace(space, true)"><Check :size="16" />通过审核</button>
              </div>
            </div>
          </article>
          <div v-if="!spaces.length" class="empty-state">
            <Check :size="36" />
            <h3>审核队列已清空</h3>
            <p>没有待处理的车位申请。</p>
          </div>
        </div>
      </section>

      <section v-else-if="currentPage === 'bookings'" class="panel">
        <div class="panel-heading">
          <div>
            <h2>预约订单</h2>
            <p>免费共享订单全量视图</p>
          </div>
          <span class="summary-count">{{ bookings.length }} 笔</span>
        </div>
        <div class="table-wrap">
          <table>
            <thead>
              <tr>
                <th>预约号</th>
                <th>车位 / 时段</th>
                <th>参与方</th>
                <th>状态</th>
                <th>创建时间</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in bookings" :key="item.id">
                <td class="mono">{{ item.bookingNo }}</td>
                <td>
                  车位 #{{ item.spaceId }}<small>{{ formatTime(item.startAt) }} — {{ formatTime(item.endAt) }}</small>
                </td>
                <td>
                  车主 #{{ item.ownerId }}<small>租用人 #{{ item.renterId }}</small>
                </td>
                <td>
                  <span :class="['status-pill', item.status.toLowerCase()]">{{ statusText(item.status) }}</span>
                </td>
                <td>{{ formatTime(item.createdAt) }}</td>
              </tr>
              <tr v-if="!bookings.length">
                <td colspan="5" class="empty-cell">暂无预约数据</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <section v-else-if="currentPage === 'complaints'" class="panel">
        <div class="panel-heading">
          <div>
            <h2>投诉处理</h2>
            <p>按订单核实并沉淀处理结论</p>
          </div>
          <span class="summary-count">{{ openComplaints }} 项待办</span>
        </div>
        <div class="complaint-list">
          <article v-for="item in complaints" :key="item.id" class="complaint-item">
            <div>
              <div class="complaint-meta">
                <span :class="['status-pill', item.status.toLowerCase()]">{{ statusText(item.status) }}</span
                ><span>订单 #{{ item.bookingId }}</span
                ><span>{{ formatTime(item.createdAt) }}</span>
              </div>
              <p>{{ item.content }}</p>
              <small v-if="item.resolutionNote">处理结论：{{ item.resolutionNote }}</small>
            </div>
            <div v-if="['OPEN', 'PROCESSING'].includes(item.status)" class="complaint-actions">
              <button class="button ghost" @click="resolveComplaint(item, false)">驳回</button
              ><button class="button primary" @click="resolveComplaint(item, true)">标记已解决</button>
            </div>
          </article>
          <div v-if="!complaints.length" class="empty-state">
            <Check :size="36" />
            <h3>暂无投诉</h3>
            <p>当前没有需要处理的问题。</p>
          </div>
        </div>
      </section>

      <section v-else class="panel">
        <div class="panel-heading">
          <div>
            <h2>敏感操作审计</h2>
            <p>保留最近 100 条管理员操作记录</p>
          </div>
          <span class="summary-count">{{ audits.length }} 条</span>
        </div>
        <div class="table-wrap">
          <table>
            <thead>
              <tr>
                <th>时间</th>
                <th>操作人</th>
                <th>动作</th>
                <th>对象</th>
                <th>说明</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in audits" :key="item.id">
                <td>{{ formatTime(item.createdAt) }}</td>
                <td>管理员 #{{ item.operatorId }}</td>
                <td>
                  <span class="action-code">{{ item.action }}</span>
                </td>
                <td>{{ item.targetType }} #{{ item.targetId }}</td>
                <td>{{ item.detail || '—' }}</td>
              </tr>
              <tr v-if="!audits.length">
                <td colspan="5" class="empty-cell">暂无审计记录</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>
    </main>
  </div>
</template>
