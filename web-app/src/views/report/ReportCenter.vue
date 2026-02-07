<template>
  <div class="report-center">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-content">
        <h2><a-icon type="file-excel" style="margin-right: 8px; color: #10b981"/> 报表中心</h2>
        <p>提供系统核心业务数据的查询、统计与 Excel 导出功能</p>
      </div>
      <div>
        <a-button @click="loadSummary" icon="sync">刷新数据</a-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <a-row :gutter="24" class="stats-row">
      <a-col :span="8">
        <div class="stat-card gradient-blue" @click="activeTab = '1'">
          <div class="stat-icon"><a-icon type="shopping-cart" /></div>
          <div class="stat-info">
            <div class="stat-value">{{ summary.totalOrders || 0 }}</div>
            <div class="stat-title">总订单数</div>
          </div>
        </div>
      </a-col>
      <a-col :span="8">
        <div class="stat-card gradient-green" @click="activeTab = '2'">
          <div class="stat-icon"><a-icon type="car" /></div>
          <div class="stat-info">
            <div class="stat-value">{{ summary.totalDistributions || 0 }}</div>
            <div class="stat-title">累计配送单</div>
          </div>
        </div>
      </a-col>
      <a-col :span="8">
        <div class="stat-card gradient-purple" @click="activeTab = '3'">
          <div class="stat-icon"><a-icon type="database" /></div>
          <div class="stat-info">
            <div class="stat-value">{{ summary.totalInventory || 0 }}</div>
            <div class="stat-title">库存总量</div>
          </div>
        </div>
      </a-col>
    </a-row>

    <!-- 报表内容区 -->
    <a-card :bordered="false" class="main-card">
      <a-tabs v-model="activeTab">
        <!-- 订单报表 -->
        <a-tab-pane key="1" tab="订单报表">
          <div class="toolbar">
            <div class="toolbar-left">
              <span class="label">日期范围：</span>
              <a-range-picker 
                v-model="orderDateRange" 
                format="YYYY-MM-DD"
                style="margin-right: 16px"
                @change="loadOrderReport"
              />
              <a-button icon="reload" @click="loadOrderReport">刷新</a-button>
            </div>
            <div class="toolbar-right">
              <download-excel
                :data="orderData"
                :fields="orderFields"
                name="订单报表.xls"
                :disabled="orderData.length === 0"
              >
                <a-button type="primary" icon="download" class="download-btn" :disabled="orderData.length === 0">
                  导出 Excel
                </a-button>
              </download-excel>
            </div>
          </div>
          <a-table
            :loading="orderLoading"
            :columns="orderColumns"
            :data-source="orderData"
            :pagination="{ pageSize: 10 }"
            rowKey="orderId"
            size="small"
            :scroll="{ x: 1500 }"
          />
        </a-tab-pane>

        <!-- 配送报表 -->
        <a-tab-pane key="2" tab="配送报表">
          <div class="toolbar">
            <div class="toolbar-left">
              <span class="label">日期范围：</span>
              <a-range-picker 
                v-model="distDateRange" 
                format="YYYY-MM-DD"
                class="filter-item"
                @change="loadDistributionReport"
              />
              <a-button icon="reload" @click="loadDistributionReport">刷新</a-button>
            </div>
            <div class="toolbar-right">
              <download-excel
                :data="distData"
                :fields="distFields"
                name="配送报表.xls"
                :disabled="distData.length === 0"
              >
                <a-button type="primary" icon="download" class="download-btn" :disabled="distData.length === 0">
                  导出 Excel
                </a-button>
              </download-excel>
            </div>
          </div>
          <a-table
            :loading="distLoading"
            :columns="distColumns"
            :data-source="distData"
            :pagination="{ pageSize: 10 }"
            rowKey="id"
            size="small"
            :scroll="{ x: 1200 }"
          />
        </a-tab-pane>

        <!-- 库存报表 -->
        <a-tab-pane key="3" tab="库存报表">
          <div class="toolbar">
            <div class="toolbar-left">
              <span class="label">选择仓库：</span>
              <a-select
                v-model="selectedWarehouse"
                placeholder="全部仓库"
                style="width: 200px" class="filter-item"
                allowClear
                @change="loadInventoryReport"
              >
                <a-select-option v-for="w in warehouses" :key="w.id" :value="w.id">
                  {{ w.name }}
                </a-select-option>
              </a-select>
              <a-button icon="reload" @click="loadInventoryReport">刷新</a-button>
            </div>
            <div class="toolbar-right">
              <download-excel
                :data="invData"
                :fields="invFields"
                name="库存报表.xls"
                :disabled="invData.length === 0"
              >
                <a-button type="primary" icon="download" class="download-btn" :disabled="invData.length === 0">
                  导出 Excel
                </a-button>
              </download-excel>
            </div>
          </div>
          <a-table
            :loading="invLoading"
            :columns="invColumns"
            :data-source="invData"
            :pagination="{ pageSize: 10 }"
            rowKey="id"
            size="small"
          >
            <template slot="isLowStock" slot-scope="text">
              <a-tag v-if="text === '是'" color="red">是</a-tag>
              <a-tag v-else color="green">否</a-tag>
            </template>
          </a-table>
        </a-tab-pane>
      </a-tabs>
    </a-card>
  </div>
</template>

<script>
import { getOrderReport, getDistributionReport, getInventoryReport, getReportSummary } from '@/api/report'
import { FindAllWarehouse } from '@/api/warehouse'

export default {
  name: 'ReportCenter',
  data: function() {
    return {
      activeTab: '1',
      summary: {},
      warehouses: [],
      selectedWarehouse: undefined,
      orderDateRange: [],
      distDateRange: [],
      orderLoading: false,
      distLoading: false,
      invLoading: false,
      orderData: [],
      distData: [],
      invData: [],
      // 订单报表字段映射
      orderFields: {
        '订单编号': 'orderNo',
        '发货人': 'fromContact',
        '发货人电话': 'fromPhone',
        '起点': 'origin',
        '收货人': 'toContact',
        '收货人电话': 'toPhone',
        '目的地': 'destination',
        '货物类型': 'type',
        '数量': 'quantity',
        '金额': 'amount',
        '状态': 'status',
        '支付方式': 'paymentMethod',
        '创建时间': 'createAt',
        '备注': 'remark'
      },
      orderColumns: [
        { title: '订单编号', dataIndex: 'orderNo', width: 220, fixed: 'left' },
        { title: '发货人', dataIndex: 'fromContact', width: 100 },
        { title: '发货人电话', dataIndex: 'fromPhone', width: 120 },
        { title: '收货人', dataIndex: 'toContact', width: 100 },
        { title: '收货人电话', dataIndex: 'toPhone', width: 120 },
        { title: '货物类型', dataIndex: 'type', width: 100 },
        { title: '金额', dataIndex: 'amount', width: 80 },
        { title: '状态', dataIndex: 'status', width: 80 },
        { title: '创建时间', dataIndex: 'createAt', width: 160 }
      ],
      // 配送报表字段映射
      distFields: {
        '配送单号': 'orderNo',
        '押运员': 'driver',
        '车牌号': 'number',
        '起点': 'origin',
        '终点': 'destination',
        '当前位置': 'currentNode',
        '状态': 'status',
        '异常等级': 'warningLevel',
        '联系电话': 'phone',
        '时间': 'time'
      },
      distColumns: [
        { title: '配送单号', dataIndex: 'orderNo', width: 220, fixed: 'left' },
        { title: '押运员', dataIndex: 'driver', width: 100 },
        { title: '车牌号', dataIndex: 'number', width: 100 },
        { title: '起点', dataIndex: 'origin', width: 120 },
        { title: '终点', dataIndex: 'destination', width: 120 },
        { title: '当前位置', dataIndex: 'currentNode', width: 120 },
        { title: '状态', dataIndex: 'status', width: 80 },
        { title: '异常', dataIndex: 'warningLevel', width: 80 },
        { title: '时间', dataIndex: 'time', width: 160 }
      ],
      // 库存报表字段映射
      invFields: {
        '商品名称': 'name',
        '存放位置': 'location',
        '库存数量': 'count',
        '预警阈值': 'minCount',
        '是否预警': 'isLowStock',
        '更新时间': 'updateAt'
      },
      invColumns: [
        { title: '商品名称', dataIndex: 'name', width: 150 },
        { title: '存放位置', dataIndex: 'location', width: 120 },
        { title: '库存数量', dataIndex: 'count', width: 100 },
        { title: '预警阈值', dataIndex: 'minCount', width: 100 },
        { title: '是否预警', dataIndex: 'isLowStock', scopedSlots: { customRender: 'isLowStock' }, width: 100 },
        { title: '更新时间', dataIndex: 'updateAt', width: 160 }
      ]
    }
  },
  mounted: function() {
    this.loadSummary()
    this.loadWarehouses()
    this.loadOrderReport()
    this.loadDistributionReport()
    this.loadInventoryReport()
  },
  methods: {
    loadSummary: function() {
      var self = this
      getReportSummary().then(function(res) {
        if (res.data) {
          self.summary = res.data
        }
      })
    },
    loadWarehouses: function() {
      var self = this
      FindAllWarehouse().then(function(res) {
        if (res.data) {
          self.warehouses = res.data
        }
      })
    },
    loadOrderReport: function() {
      var self = this
      self.orderLoading = true
      var params = {}
      if (self.orderDateRange && self.orderDateRange.length === 2) {
        params.startDate = self.orderDateRange[0].format('YYYY-MM-DD')
        params.endDate = self.orderDateRange[1].format('YYYY-MM-DD')
      }
      getOrderReport(params).then(function(res) {
        if (res.data) {
          self.orderData = res.data
        }
        self.orderLoading = false
      }).catch(function() {
        self.orderLoading = false
      })
    },
    loadDistributionReport: function() {
      var self = this
      self.distLoading = true
      var params = {}
      if (self.distDateRange && self.distDateRange.length === 2) {
        params.startDate = self.distDateRange[0].format('YYYY-MM-DD')
        params.endDate = self.distDateRange[1].format('YYYY-MM-DD')
      }
      getDistributionReport(params).then(function(res) {
        if (res.data) {
          self.distData = res.data
        }
        self.distLoading = false
      }).catch(function() {
        self.distLoading = false
      })
    },
    loadInventoryReport: function() {
      var self = this
      self.invLoading = true
      var params = {}
      if (self.selectedWarehouse) {
        params.wid = self.selectedWarehouse
      }
      getInventoryReport(params).then(function(res) {
        if (res.data) {
          self.invData = res.data
        }
        self.invLoading = false
      }).catch(function() {
        self.invLoading = false
      })
    }
  }
}
</script>

<style scoped>
.report-center {
  padding: 24px;
  background: #f0f2f5;
  min-height: 100vh;
}

.page-header {
  margin-bottom: 24px;
  background: #fff;
  padding: 20px 24px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-content h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #1f2d3d;
  display: flex;
  align-items: center;
}

.header-content p {
  margin: 6px 0 0 28px;
  color: #8c92a4;
  font-size: 13px;
}

/* 统计卡片 */
.stats-row {
  margin-bottom: 24px;
}

.stat-card {
  border-radius: 16px;
  padding: 24px;
  color: #fff;
  display: flex;
  align-items: center;
  height: 120px;
  position: relative;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid rgba(255,255,255,0.1);
}

.stat-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.15);
}

.stat-card::after {
  content: '';
  position: absolute;
  top: -50%;
  right: -20%;
  width: 200px;
  height: 200px;
  background: rgba(255,255,255,0.1);
  border-radius: 50%;
  pointer-events: none;
}

.gradient-blue {
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3);
}

.gradient-green {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  box-shadow: 0 4px 12px rgba(5, 150, 105, 0.3);
}

.gradient-purple {
  background: linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%);
  box-shadow: 0 4px 12px rgba(124, 58, 237, 0.3);
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  background: rgba(255,255,255,0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  margin-right: 20px;
  backdrop-filter: blur(4px);
}

.stat-info {
  flex: 1;
  z-index: 1;
}

.stat-value {
  font-size: 36px;
  font-weight: 700;
  line-height: 1.2;
  margin-bottom: 4px;
  text-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.stat-title {
  font-size: 14px;
  opacity: 0.9;
  letter-spacing: 0.5px;
}

/* 主卡片 */
.main-card {
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.05);
}

>>> .ant-tabs-nav .ant-tabs-tab {
  margin: 0 32px 0 0;
  padding: 12px 16px;
  font-size: 15px;
}

>>> .ant-tabs-nav .ant-tabs-tab-active {
  font-weight: 600;
}

.toolbar {
  background: #f9fafb;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border: 1px solid #e5e7eb;
}

.toolbar-left {
  display: flex;
  align-items: center;
}

.toolbar-left .label {
  margin-right: 12px;
  color: #4b5563;
  font-weight: 500;
  font-size: 14px;
}

.filter-item {
  margin-right: 16px;
}

.download-btn {
  background-color: #10b981;
  border-color: #10b981;
}

.download-btn:hover, .download-btn:focus {
  background-color: #059669;
  border-color: #059669;
}

>>> .ant-table-wrapper {
  background: #fff;
}

>>> .ant-table-thead > tr > th {
  background: #f9fafb;
  font-weight: 600;
  color: #374151;
}

>>> .ant-btn-primary {
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}
</style>
