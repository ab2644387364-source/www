<template>
  <div class="user-dashboard">
    <!-- 欢迎区域 -->
    <div class="welcome-banner">
      <div class="welcome-content">
        <h1>您好，{{ userName || '用户' }} 👋</h1>
        <p>{{ currentTime }} · 欢迎使用物流管理系统</p>
      </div>
      <div class="quick-actions">
        <a-button type="primary" icon="plus" @click="$router.push('/user/order/create')">
          创建订单
        </a-button>
        <a-button icon="search" @click="$router.push('/user/route')">
          物流查询
        </a-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <a-row :gutter="24" class="stats-row">
      <a-col :span="6">
        <div class="stat-card gradient-blue" @click="$router.push('/user/orders')">
          <div class="stat-icon"><a-icon type="file-text" /></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.totalOrders || 0 }}</div>
            <div class="stat-title">总订单数</div>
          </div>
        </div>
      </a-col>
      <a-col :span="6">
        <div class="stat-card gradient-orange" @click="goToOrders(0)">
          <div class="stat-icon"><a-icon type="pay-circle" /></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.pendingPayment || 0 }}</div>
            <div class="stat-title">待支付</div>
          </div>
        </div>
      </a-col>
      <a-col :span="6">
        <div class="stat-card gradient-green" @click="goToOrders(2)">
          <div class="stat-icon"><a-icon type="car" /></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.inTransit || 0 }}</div>
            <div class="stat-title">运输中</div>
          </div>
        </div>
      </a-col>
      <a-col :span="6">
        <div class="stat-card gradient-purple">
          <div class="stat-icon"><a-icon type="dollar" /></div>
          <div class="stat-info">
            <div class="stat-value">¥{{ formatAmount(stats.totalAmount) }}</div>
            <div class="stat-title">累计消费</div>
          </div>
        </div>
      </a-col>
    </a-row>

    <!-- 主内容区 -->
    <a-row :gutter="24">
      <!-- 待办事项 -->
      <a-col :span="12">
        <a-card title="待办事项" :bordered="false" class="content-card">
          <template #extra>
            <a-button type="link" size="small" @click="$router.push('/user/orders')">
              查看全部
            </a-button>
          </template>
          <div v-if="payableOrders.length > 0" class="todo-list">
            <div v-for="order in payableOrders" :key="order.id" class="todo-item">
              <div class="todo-info">
                <span class="order-no">{{ order.orderNo }}</span>
                <span class="order-route">{{ order.origin }} → {{ order.destination }}</span>
              </div>
              <div class="todo-actions">
                <span class="order-amount">¥{{ order.amount }}</span>
                <a-button type="primary" size="small" @click="goPay(order.id)">
                  去支付
                </a-button>
              </div>
            </div>
          </div>
          <a-empty v-else description="暂无待办" :image="simpleImage" />
        </a-card>
      </a-col>

      <!-- 运输中订单 -->
      <a-col :span="12">
        <a-card title="物流追踪" :bordered="false" class="content-card">
          <template #extra>
            <a-button type="link" size="small" @click="$router.push('/user/route')">
              查询物流
            </a-button>
          </template>
          <div v-if="inTransitOrders.length > 0" class="transit-list">
            <div v-for="order in inTransitOrders" :key="order.orderId" class="transit-item">
              <div class="transit-header">
                <span class="order-no">{{ order.orderNo }}</span>
                <a-tag :color="getStatusColor(order.status)">{{ getStatusText(order.status) }}</a-tag>
              </div>
              <div class="transit-route">
                <a-icon type="environment" style="color: #52c41a" />
                <span>{{ order.origin }}</span>
                <a-icon type="arrow-right" style="margin: 0 8px; color: #999" />
                <a-icon type="environment" style="color: #1890ff" />
                <span>{{ order.destination }}</span>
              </div>
              <div class="transit-status">
                <a-icon type="compass" style="margin-right: 4px" />
                当前位置：<span class="current-node">{{ order.currentNode || '等待调度' }}</span>
              </div>
            </div>
          </div>
          <a-empty v-else description="暂无运输中订单" :image="simpleImage" />
        </a-card>
      </a-col>
    </a-row>

    <!-- 最近订单 -->
    <a-card title="最近订单" :bordered="false" class="content-card" style="margin-top: 24px">
      <template #extra>
        <a-button type="link" size="small" @click="$router.push('/user/orders')">
          全部订单
        </a-button>
      </template>
      <a-table
        :loading="recentLoading"
        :columns="recentColumns"
        :data-source="recentOrders"
        :pagination="false"
        rowKey="id"
        size="small"
      >
        <template slot="status" slot-scope="text">
          <a-tag :color="getStatusColor(text)">{{ getStatusText(text) }}</a-tag>
        </template>
        <template slot="action" slot-scope="_, record">
          <a-button type="link" size="small" @click="$router.push('/user/orders/' + record.id)">
            详情
          </a-button>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script>
import { getUserStats, getInTransitOrders } from '@/api/userDashboard'
import { FetchPayableOrders, FetchRecentOrders } from '@/api/order'
import { Empty } from 'ant-design-vue'

export default {
  name: 'UserDashboard',
  data: function() {
    return {
      simpleImage: Empty.PRESENTED_IMAGE_SIMPLE,
      currentTime: '',
      stats: {},
      payableOrders: [],
      inTransitOrders: [],
      recentOrders: [],
      recentLoading: false,
      recentColumns: [
        { title: '订单编号', dataIndex: 'orderNo', width: 180 },
        { title: '起点', dataIndex: 'origin', width: 120 },
        { title: '目的地', dataIndex: 'destination', width: 120 },
        { title: '货物', dataIndex: 'type', width: 80 },
        { title: '金额', dataIndex: 'amount', width: 80 },
        { title: '状态', dataIndex: 'status', scopedSlots: { customRender: 'status' }, width: 90 },
        { title: '操作', scopedSlots: { customRender: 'action' }, width: 80 }
      ]
    }
  },
  computed: {
    userName: function() {
      var user = this.$store.state.user.loginUser
      return user ? user.name : ''
    }
  },
  mounted: function() {
    this.updateTime()
    setInterval(this.updateTime, 60000)
    this.loadData()
  },
  methods: {
    updateTime: function() {
      var now = new Date()
      var options = { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' }
      this.currentTime = now.toLocaleDateString('zh-CN', options)
    },
    loadData: function() {
      this.loadStats()
      this.loadPayableOrders()
      this.loadInTransitOrders()
      this.loadRecentOrders()
    },
    loadStats: function() {
      var self = this
      getUserStats().then(function(res) {
        if (res.data) {
          self.stats = res.data
        }
      })
    },
    loadPayableOrders: function() {
      var self = this
      FetchPayableOrders().then(function(res) {
        if (res.status && res.data) {
          self.payableOrders = res.data.slice(0, 3)
        }
      })
    },
    loadInTransitOrders: function() {
      var self = this
      getInTransitOrders().then(function(res) {
        if (res.data) {
          self.inTransitOrders = res.data.slice(0, 3)
        }
      })
    },
    loadRecentOrders: function() {
      var self = this
      self.recentLoading = true
      FetchRecentOrders().then(function(res) {
        if (res.status && res.data) {
          self.recentOrders = res.data.slice(0, 5)
        }
        self.recentLoading = false
      }).catch(function() {
        self.recentLoading = false
      })
    },
    formatAmount: function(amount) {
      if (!amount) return '0.00'
      return parseFloat(amount).toFixed(2)
    },
    goToOrders: function(status) {
      this.$router.push({ path: '/user/orders', query: { status: status } })
    },
    goPay: function(orderId) {
      this.$router.push('/user/payment?orderId=' + orderId)
    },
    getStatusText: function(status) {
      var map = {
        0: '待支付',
        1: '已支付',
        2: '待配送',
        3: '运输中',
        4: '已完成',
        [-1]: '已取消'
      }
      return map[status] || '未知'
    },
    getStatusColor: function(status) {
      var map = {
        0: 'orange',
        1: 'blue',
        2: 'cyan',
        3: 'geekblue',
        4: 'green',
        [-1]: 'red'
      }
      return map[status] || 'default'
    }
  }
}
</script>

<style scoped>
.user-dashboard {
  padding: 24px;
  background: #f0f2f5;
  min-height: 100vh;
}

/* 欢迎横幅 */
.welcome-banner {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 32px;
  margin-bottom: 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #fff;
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.4);
}

.welcome-content h1 {
  margin: 0;
  font-size: 28px;
  font-weight: 600;
}

.welcome-content p {
  margin: 8px 0 0 0;
  opacity: 0.9;
}

.quick-actions .ant-btn {
  margin-left: 12px;
}

.quick-actions .ant-btn-primary {
  background: rgba(255,255,255,0.2);
  border-color: rgba(255,255,255,0.3);
}

.quick-actions .ant-btn-primary:hover {
  background: rgba(255,255,255,0.3);
}

/* 统计卡片 */
.stats-row {
  margin-bottom: 24px;
}

.stat-card {
  border-radius: 12px;
  padding: 20px;
  color: #fff;
  display: flex;
  align-items: center;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(0,0,0,0.15);
}

.gradient-blue {
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
}

.gradient-orange {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
}

.gradient-green {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
}

.gradient-purple {
  background: linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%);
}

.stat-icon {
  font-size: 32px;
  opacity: 0.8;
  margin-right: 16px;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
}

.stat-title {
  font-size: 13px;
  opacity: 0.9;
}

/* 内容卡片 */
.content-card {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}

/* 待办列表 */
.todo-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.todo-item:last-child {
  border-bottom: none;
}

.todo-info {
  display: flex;
  flex-direction: column;
}

.todo-info .order-no {
  font-weight: 600;
  color: #333;
}

.todo-info .order-route {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.todo-actions {
  display: flex;
  align-items: center;
}

.todo-actions .order-amount {
  font-size: 16px;
  font-weight: 600;
  color: #f5222d;
  margin-right: 12px;
}

/* 运输中列表 */
.transit-item {
  padding: 12px;
  background: #fafafa;
  border-radius: 8px;
  margin-bottom: 12px;
}

.transit-item:last-child {
  margin-bottom: 0;
}

.transit-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.transit-header .order-no {
  font-weight: 600;
  color: #333;
}

.transit-route {
  display: flex;
  align-items: center;
  font-size: 13px;
  color: #666;
  margin-bottom: 8px;
}

.transit-status {
  font-size: 12px;
  color: #999;
}

.transit-status .current-node {
  color: #1890ff;
  font-weight: 500;
}
</style>
