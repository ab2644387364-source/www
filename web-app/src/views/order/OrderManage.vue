<template>
  <div class="page-container">
    <a-card :bordered="false">
      <!-- 顶部操作栏 -->
      <div class="table-header">
        <div class="header-left">
          <a-tabs v-model="activeStatus" @change="onStatusChange">
            <a-tab-pane key="all" tab="全部"></a-tab-pane>
            <a-tab-pane key="1" tab="待审核"></a-tab-pane>
            <a-tab-pane key="2" tab="已审核"></a-tab-pane>
            <a-tab-pane key="3" tab="运输中"></a-tab-pane>
            <a-tab-pane key="4" tab="已完成"></a-tab-pane>
            <a-tab-pane key="-1" tab="已取消"></a-tab-pane>
          </a-tabs>
        </div>
        <div class="header-right">
          <a-input-search
            v-model="keyword"
            placeholder="搜索订单号/起点/终点/联系人"
            style="width: 280px"
            @search="loadData"
          />
          <a-button type="primary" icon="reload" @click="loadData" style="margin-left: 10px">刷新</a-button>
        </div>
      </div>

      <!-- 状态统计卡片 -->
      <div class="stats-row">
        <a-row :gutter="16">
          <a-col :span="4">
            <a-statistic title="全部订单" :value="stats.total || 0" :value-style="{ color: '#1890ff' }">
              <a-icon slot="prefix" type="profile" />
            </a-statistic>
          </a-col>
          <a-col :span="4">
            <a-statistic title="待审核" :value="stats.paid || 0" :value-style="{ color: '#faad14' }">
              <a-icon slot="prefix" type="clock-circle" />
            </a-statistic>
          </a-col>
          <a-col :span="4">
            <a-statistic title="已审核" :value="stats.approved || 0" :value-style="{ color: '#52c41a' }">
              <a-icon slot="prefix" type="check-circle" />
            </a-statistic>
          </a-col>
          <a-col :span="4">
            <a-statistic title="运输中" :value="stats.shipping || 0" :value-style="{ color: '#13c2c2' }">
              <a-icon slot="prefix" type="car" />
            </a-statistic>
          </a-col>
          <a-col :span="4">
            <a-statistic title="已完成" :value="stats.completed || 0" :value-style="{ color: '#52c41a' }">
              <a-icon slot="prefix" type="trophy" />
            </a-statistic>
          </a-col>
          <a-col :span="4">
            <a-statistic title="已取消" :value="stats.cancelled || 0" :value-style="{ color: '#ff4d4f' }">
              <a-icon slot="prefix" type="close-circle" />
            </a-statistic>
          </a-col>
        </a-row>
      </div>

      <!-- 订单表格 -->
      <a-table
        :loading="loading"
        :columns="columns"
        :data-source="orders"
        :pagination="pagination"
        @change="onTableChange"
        rowKey="id"
        size="middle"
      >
        <!-- 状态列 -->
        <template slot="status" slot-scope="status">
          <a-tag :color="getStatusColor(status)">{{ getStatusText(status) }}</a-tag>
        </template>

        <!-- 金额列 -->
        <template slot="amount" slot-scope="amount">
          <span style="color: #f5222d; font-weight: 500">¥{{ formatAmount(amount) }}</span>
        </template>

        <!-- 联系人列 -->
        <template slot="contact" slot-scope="text, record">
          <div>{{ record.fromContact }} → {{ record.toContact }}</div>
          <div style="color: #999; font-size: 12px">{{ record.fromPhone }}</div>
        </template>

        <!-- 路线列 -->
        <template slot="route" slot-scope="text, record">
          <div>{{ record.origin }}</div>
          <div style="color: #1890ff">↓</div>
          <div>{{ record.destination }}</div>
        </template>

        <!-- 操作列 -->
        <template slot="action" slot-scope="text, record">
          <a-button type="link" size="small" @click="showDetail(record)">
            <a-icon type="eye" /> 详情
          </a-button>
          <template v-if="record.status === 1">
            <a-button type="link" size="small" style="color: #52c41a" @click="handleApprove(record)">
              <a-icon type="check" /> 通过
            </a-button>
            <a-button type="link" size="small" style="color: #ff4d4f" @click="showRejectModal(record)">
              <a-icon type="close" /> 驳回
            </a-button>
          </template>
          <template v-if="record.status === 2">
            <a-button type="link" size="small" style="color: #13c2c2" @click="handleDispatch(record)">
              <a-icon type="car" /> 发货
            </a-button>
          </template>
          <template v-if="record.status === 3">
            <a-button type="link" size="small" style="color: #52c41a" @click="handleComplete(record)">
              <a-icon type="check-circle" /> 送达
            </a-button>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 订单详情抽屉 -->
    <a-drawer
      title="订单详情"
      placement="right"
      :width="600"
      :visible="detailVisible"
      @close="detailVisible = false"
    >
      <template v-if="currentOrder">
        <a-descriptions title="基本信息" bordered size="small" :column="2">
          <a-descriptions-item label="订单号">{{ currentOrder.orderNo }}</a-descriptions-item>
          <a-descriptions-item label="状态">
            <a-tag :color="getStatusColor(currentOrder.status)">{{ getStatusText(currentOrder.status) }}</a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="金额">¥{{ formatAmount(currentOrder.amount) }}</a-descriptions-item>
          <a-descriptions-item label="支付方式">{{ currentOrder.paymentMethod || '-' }}</a-descriptions-item>
          <a-descriptions-item label="货物类型">{{ currentOrder.type }}</a-descriptions-item>
          <a-descriptions-item label="数量">{{ currentOrder.quantity }}</a-descriptions-item>
          <a-descriptions-item label="创建时间" :span="2">{{ currentOrder.createAt }}</a-descriptions-item>
        </a-descriptions>

        <a-divider />

        <a-descriptions title="发货信息" bordered size="small" :column="2">
          <a-descriptions-item label="发货地">{{ currentOrder.origin }}</a-descriptions-item>
          <a-descriptions-item label="收货地">{{ currentOrder.destination }}</a-descriptions-item>
          <a-descriptions-item label="发货人">{{ currentOrder.fromContact }}</a-descriptions-item>
          <a-descriptions-item label="发货电话">{{ currentOrder.fromPhone }}</a-descriptions-item>
          <a-descriptions-item label="收货人">{{ currentOrder.toContact }}</a-descriptions-item>
          <a-descriptions-item label="收货电话">{{ currentOrder.toPhone }}</a-descriptions-item>
          <a-descriptions-item label="提货日期" :span="2">{{ currentOrder.pickupDate }}</a-descriptions-item>
          <a-descriptions-item label="备注" :span="2">{{ currentOrder.remark || '无' }}</a-descriptions-item>
        </a-descriptions>

        <a-divider />

        <!-- 操作按钮 -->
        <div class="drawer-actions" v-if="currentOrder.status === 1">
          <a-button type="primary" @click="handleApprove(currentOrder)" :loading="actionLoading">
            <a-icon type="check" /> 审核通过
          </a-button>
          <a-button type="danger" @click="showRejectModal(currentOrder)" style="margin-left: 10px">
            <a-icon type="close" /> 驳回订单
          </a-button>
        </div>
      </template>
    </a-drawer>

    <!-- 驳回原因弹窗 -->
    <a-modal
      title="驳回订单"
      :visible="rejectVisible"
      @ok="handleReject"
      @cancel="rejectVisible = false"
      :confirmLoading="actionLoading"
    >
      <a-form-model :model="rejectForm" :label-col="{ span: 4 }" :wrapper-col="{ span: 18 }">
        <a-form-model-item label="订单号">
          <span>{{ rejectOrderNo }}</span>
        </a-form-model-item>
        <a-form-model-item label="驳回原因" required>
          <a-textarea
            v-model="rejectForm.reason"
            :rows="4"
            placeholder="请输入驳回原因，将通知用户"
          />
        </a-form-model-item>
      </a-form-model>
    </a-modal>
  </div>
</template>

<script>
import { getOrderList, approveOrder, rejectOrder, updateOrderStatus, getOrderStats } from '@/api/adminOrder'

export default {
  name: 'OrderManage',
  data() {
    return {
      loading: false,
      actionLoading: false,
      orders: [],
      stats: {},
      activeStatus: 'all',
      keyword: '',
      pagination: {
        current: 1,
        pageSize: 10,
        total: 0,
        showSizeChanger: true,
        showQuickJumper: true,
        showTotal: function(total) { return '共 ' + total + ' 条' }
      },
      columns: [
        { title: '订单号', dataIndex: 'orderNo', width: 180 },
        { title: '状态', dataIndex: 'status', scopedSlots: { customRender: 'status' }, width: 100 },
        { title: '路线', scopedSlots: { customRender: 'route' }, width: 150 },
        { title: '联系人', scopedSlots: { customRender: 'contact' }, width: 150 },
        { title: '货物', dataIndex: 'type', width: 80 },
        { title: '数量', dataIndex: 'quantity', width: 60 },
        { title: '金额', dataIndex: 'amount', scopedSlots: { customRender: 'amount' }, width: 100 },
        { title: '创建时间', dataIndex: 'createAt', width: 160 },
        { title: '操作', scopedSlots: { customRender: 'action' }, width: 200, fixed: 'right' }
      ],
      detailVisible: false,
      currentOrder: null,
      rejectVisible: false,
      rejectOrderData: null,
      rejectForm: {
        reason: ''
      }
    }
  },
  computed: {
    rejectOrderNo: function() {
      return this.rejectOrderData ? this.rejectOrderData.orderNo : ''
    }
  },
  mounted() {
    this.loadData()
    this.loadStats()
  },
  methods: {
    formatAmount: function(amount) {
      if (amount === null || amount === undefined) return '0.00'
      return amount.toFixed(2)
    },
    loadData: function() {
      var self = this
      self.loading = true
      var params = {
        page: self.pagination.current - 1,
        size: self.pagination.pageSize
      }
      if (self.activeStatus !== 'all') {
        params.status = parseInt(self.activeStatus)
      }
      if (self.keyword) {
        params.keyword = self.keyword
      }

      getOrderList(params).then(function(res) {
        if (res.status && res.data) {
          self.orders = res.data.content || []
          self.pagination.total = res.data.totalElements || 0
        }
        self.loading = false
      }).catch(function() {
        self.loading = false
      })
    },
    loadStats: function() {
      var self = this
      getOrderStats().then(function(res) {
        if (res.status && res.data) {
          self.stats = res.data
        }
      })
    },
    onStatusChange: function(key) {
      this.activeStatus = key
      this.pagination.current = 1
      this.loadData()
    },
    onTableChange: function(pagination) {
      this.pagination.current = pagination.current
      this.pagination.pageSize = pagination.pageSize
      this.loadData()
    },
    showDetail: function(record) {
      this.currentOrder = record
      this.detailVisible = true
    },
    handleApprove: function(record) {
      var self = this
      self.$confirm({
        title: '确认审核通过',
        content: '确定要审核通过订单 ' + record.orderNo + ' 吗？',
        onOk: function() {
          self.actionLoading = true
          approveOrder(record.id).then(function(res) {
            if (res.status) {
              self.$message.success('审核通过成功')
              self.loadData()
              self.loadStats()
              self.detailVisible = false
            } else {
              self.$message.error(res.message || '操作失败')
            }
            self.actionLoading = false
          }).catch(function() {
            self.actionLoading = false
          })
        }
      })
    },
    showRejectModal: function(record) {
      this.rejectOrderData = record
      this.rejectForm.reason = ''
      this.rejectVisible = true
    },
    handleReject: function() {
      var self = this
      if (!self.rejectForm.reason) {
        self.$message.warning('请输入驳回原因')
        return
      }
      self.actionLoading = true
      rejectOrder(self.rejectOrderData.id, self.rejectForm.reason).then(function(res) {
        if (res.status) {
          self.$message.success('订单已驳回')
          self.rejectVisible = false
          self.detailVisible = false
          self.loadData()
          self.loadStats()
        } else {
          self.$message.error(res.message || '操作失败')
        }
        self.actionLoading = false
      }).catch(function() {
        self.actionLoading = false
      })
    },
    handleDispatch: function(record) {
      var self = this
      self.$confirm({
        title: '确认发货',
        content: '确定要将订单 ' + record.orderNo + ' 标记为发货吗？',
        onOk: function() {
          updateOrderStatus(record.id, 3, '订单已发货').then(function(res) {
            if (res.status) {
              self.$message.success('订单已发货')
              self.loadData()
              self.loadStats()
            } else {
              self.$message.error(res.message || '操作失败')
            }
          })
        }
      })
    },
    handleComplete: function(record) {
      var self = this
      self.$confirm({
        title: '确认送达',
        content: '确定要将订单 ' + record.orderNo + ' 标记为已送达吗？',
        onOk: function() {
          updateOrderStatus(record.id, 4, '订单已送达').then(function(res) {
            if (res.status) {
              self.$message.success('订单已完成')
              self.loadData()
              self.loadStats()
            } else {
              self.$message.error(res.message || '操作失败')
            }
          })
        }
      })
    },
    getStatusText: function(status) {
      var map = {
        0: '待支付',
        1: '待审核',
        2: '已审核',
        3: '运输中',
        4: '已完成',
        '-1': '已取消'
      }
      return map[status] || '未知'
    },
    getStatusColor: function(status) {
      var map = {
        0: 'default',
        1: 'orange',
        2: 'blue',
        3: 'cyan',
        4: 'green',
        '-1': 'red'
      }
      return map[status] || 'default'
    }
  }
}
</script>

<style scoped>
.page-container {
  padding: 24px;
  background-color: #f0f2f5;
  min-height: 100%;
}
.table-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}
.header-left {
  flex: 1;
}
.header-right {
  display: flex;
  align-items: center;
}
.stats-row {
  margin-bottom: 24px;
  padding: 16px;
  background: #fff;
  border-radius: 4px;
}
.drawer-actions {
  margin-top: 24px;
  text-align: center;
}
</style>
