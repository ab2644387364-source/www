<template>
  <div class="page-container">
    <div class="inventory-content">
      <!-- 统计卡片区域 -->
      <div class="stats-row">
        <a-row :gutter="24">
          <a-col :span="8">
            <div class="stat-card blue-gradient">
              <div class="stat-icon">
                <a-icon type="database" />
              </div>
              <div class="stat-content">
                <div class="stat-title">库存记录总数</div>
                <div class="stat-value">{{ stats.total || 0 }}</div>
              </div>
            </div>
          </a-col>
          <a-col :span="8">
            <div class="stat-card green-gradient">
              <div class="stat-icon">
                <a-icon type="inbox" />
              </div>
              <div class="stat-content">
                <div class="stat-title">商品总库存量</div>
                <div class="stat-value">{{ stats.totalCount || 0 }}</div>
              </div>
            </div>
          </a-col>
          <a-col :span="8">
            <div class="stat-card red-gradient">
              <div class="stat-icon">
                <a-icon type="warning" />
              </div>
              <div class="stat-content">
                <div class="stat-title">库存预警商品</div>
                <div class="stat-value">{{ stats.lowStock || 0 }}</div>
              </div>
              <a-button type="link" class="stat-action" @click="showWarningList">
                查看详情 <a-icon type="right" />
              </a-button>
            </div>
          </a-col>
        </a-row>
      </div>

      <!-- 主内容区域 -->
      <a-card :bordered="false" class="main-card">
        <!-- 顶部工具栏 -->
        <div class="toolbar">
          <div class="toolbar-left">
            <span class="toolbar-label">仓库筛选：</span>
            <a-select
              v-model="selectedWarehouse"
              placeholder="全部仓库"
              style="width: 200px; margin-right: 24px"
              allowClear
              @change="loadData"
            >
              <a-select-option v-for="w in warehouses" :key="w.id" :value="w.id">
                {{ w.name }}
              </a-select-option>
            </a-select>
            <a-input-search
              v-model="keyword"
              placeholder="输入商品名称搜索..."
              style="width: 240px"
              @search="loadData"
              enter-button
            />
          </div>
          <div class="toolbar-right">
            <a-button icon="reload" @click="loadData">刷新列表</a-button>
            <a-button type="primary" icon="plus" style="margin-left: 10px" @click="$message.info('请在下方列表中选择商品进行入库操作')">
              快速入库
            </a-button>
          </div>
        </div>

        <!-- 库存表格 -->
        <a-table
          :loading="loading"
          :columns="columns"
          :data-source="inventoryList"
          :pagination="pagination"
          @change="onTableChange"
          rowKey="id"
          size="middle"
        >
          <!-- 商品名称列 -->
          <template slot="name" slot-scope="text, record">
            <span class="product-name">{{ text }}</span>
            <a-tooltip v-if="record.description" :title="record.description">
              <a-icon type="info-circle" style="color: #999; margin-left: 5px" />
            </a-tooltip>
          </template>

          <!-- 库存数量列 -->
          <template slot="count" slot-scope="text, record">
            <div class="stock-cell">
              <span class="stock-value" :class="{ 'warning-text': isLowStock(record) }">
                {{ text }}
              </span>
              <a-tag v-if="isLowStock(record)" color="#f50" style="margin-left: 8px">库存不足</a-tag>
            </div>
          </template>

          <!-- 预警阈值列 -->
          <template slot="minCount" slot-scope="text, record">
            <div class="threshold-cell">
              <span v-if="text !== null && text !== undefined">{{ text }}</span>
              <span v-else class="text-placeholder">未设置</span>
              <a-icon type="edit" class="edit-icon" @click="showSetThreshold(record)" />
            </div>
          </template>

          <!-- 操作列 -->
          <template slot="action" slot-scope="text, record">
            <div class="action-buttons">
              <a-tooltip title="入库">
                <a-button type="link" size="small" class="btn-in" @click="showInModal(record)">
                  <a-icon type="plus" />
                </a-button>
              </a-tooltip>
              <a-tooltip title="出库">
                <a-button type="link" size="small" class="btn-out" @click="showOutModal(record)">
                  <a-icon type="minus" />
                </a-button>
              </a-tooltip>
              <a-divider type="vertical" />
              <a-button type="link" size="small" @click="showCheckModal(record)">
                盘点
              </a-button>
            </div>
          </template>
        </a-table>
      </a-card>
    </div>

    <!-- 入库弹窗 -->
    <a-modal
      title="商品入库"
      :visible="inModalVisible"
      @ok="handleIn"
      @cancel="inModalVisible = false"
      :confirmLoading="actionLoading"
      okText="确认入库"
      cancelText="取消"
    >
      <div class="modal-product-info">
        <h3>{{ currentRecord ? currentRecord.name : '' }}</h3>
        <p>当前库存: <strong>{{ currentRecord ? currentRecord.count : 0 }}</strong></p>
      </div>
      <a-form-model :label-col="{ span: 5 }" :wrapper-col="{ span: 18 }">
        <a-form-model-item label="入库数量" required>
          <a-input-number v-model="inForm.count" :min="1" style="width: 100%" size="large" />
        </a-form-model-item>
        <a-form-model-item label="备注说明">
          <a-textarea v-model="inForm.description" :rows="3" placeholder="填写本次入库的备注信息..." />
        </a-form-model-item>
      </a-form-model>
    </a-modal>

    <!-- 出库弹窗 -->
    <a-modal
      title="商品出库"
      :visible="outModalVisible"
      @ok="handleOut"
      @cancel="outModalVisible = false"
      :confirmLoading="actionLoading"
      okText="确认出库"
      cancelText="取消"
      okType="danger"
    >
      <div class="modal-product-info">
        <h3>{{ currentRecord ? currentRecord.name : '' }}</h3>
        <p>当前库存: <strong>{{ currentRecord ? currentRecord.count : 0 }}</strong></p>
      </div>
      <a-form-model :label-col="{ span: 5 }" :wrapper-col="{ span: 18 }">
        <a-form-model-item label="出库数量" required>
          <a-input-number v-model="outForm.count" :min="1" :max="currentRecord ? currentRecord.count : 9999" style="width: 100%" size="large" />
        </a-form-model-item>
        <a-form-model-item label="备注说明">
          <a-textarea v-model="outForm.description" :rows="3" placeholder="填写本次出库的用途或去向..." />
        </a-form-model-item>
      </a-form-model>
    </a-modal>

    <!-- 盘点弹窗 -->
    <a-modal
      title="库存盘点"
      :visible="checkModalVisible"
      @ok="handleCheck"
      @cancel="checkModalVisible = false"
      :confirmLoading="actionLoading"
      okText="确认调整"
      cancelText="取消"
    >
      <a-alert message="请谨慎操作，系统将自动记录盘点差异" type="warning" show-icon style="margin-bottom: 20px" />
      <a-form-model :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <a-form-model-item label="商品名称">
          <span style="font-weight: bold">{{ currentRecord ? currentRecord.name : '' }}</span>
        </a-form-model-item>
        <a-form-model-item label="系统记录库存">
          <span>{{ currentRecord ? currentRecord.count : 0 }}</span>
        </a-form-model-item>
        <a-form-model-item label="实际盘点数量" required>
          <a-input-number v-model="checkForm.actualCount" :min="0" style="width: 100%" />
        </a-form-model-item>
        <a-form-model-item label="盘点情况说明">
          <a-textarea v-model="checkForm.remark" :rows="3" placeholder="如有差异，请输入原因..." />
        </a-form-model-item>
      </a-form-model>
    </a-modal>

    <!-- 设置阈值弹窗 -->
    <a-modal
      title="设置预警阈值"
      :visible="thresholdModalVisible"
      @ok="handleSetThreshold"
      @cancel="thresholdModalVisible = false"
      :confirmLoading="actionLoading"
      :width="400"
    >
      <div style="text-align: center; margin-bottom: 20px;">
        <p>当库存数量低于该值时，系统将触发预警提示。</p>
      </div>
      <a-form-model :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <a-form-model-item label="商品名称">
          <strong>{{ currentRecord ? currentRecord.name : '' }}</strong>
        </a-form-model-item>
        <a-form-model-item label="预警阈值" required>
          <a-input-number v-model="thresholdForm.minCount" :min="0" style="width: 100%" />
        </a-form-model-item>
      </a-form-model>
    </a-modal>

    <!-- 预警列表抽屉 -->
    <a-drawer
      title="⚠️ 库存预警清单"
      placement="right"
      :width="480"
      :visible="warningDrawerVisible"
      @close="warningDrawerVisible = false"
    >
      <div class="warning-list-container">
        <a-list
          :loading="warningLoading"
          :data-source="warningList"
          item-layout="vertical"
        >
          <a-list-item slot="renderItem" slot-scope="item" class="warning-item">
            <div class="warning-item-header">
              <span class="warning-name">{{ item.name }}</span>
              <a-tag color="red">缺货</a-tag>
            </div>
            <div class="warning-item-content">
              <div class="warning-info">
                <div>当前库存: <span class="text-red">{{ item.count }}</span></div>
                <div>预警阈值: <span class="text-gray">{{ item.minCount }}</span></div>
                <div>存放位置: <span class="text-gray">{{ item.location || '未知' }}</span></div>
              </div>
              <div class="warning-action">
                <a-button type="primary" size="small" @click="showInModalFromWarning(item)">补货</a-button>
              </div>
            </div>
          </a-list-item>
        </a-list>
        
        <div v-if="warningList.length === 0 && !warningLoading" class="empty-state">
           <a-icon type="check-circle" />
           <p>当前没有库存预警，太棒了！</p>
        </div>
      </div>
    </a-drawer>
  </div>
</template>

<script>
import { getInventoryPage, getWarningList, setMinCount, stockCheck, getInventoryStats, InAndOut } from '@/api/inventory'
import { FindAllWarehouse } from '@/api/warehouse'

export default {
  name: 'InventoryManage',
  data: function() {
    return {
      loading: false,
      actionLoading: false,
      warningLoading: false,
      inventoryList: [],
      warehouses: [],
      warningList: [],
      stats: {},
      selectedWarehouse: undefined,
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
        { title: '商品名称', dataIndex: 'name', scopedSlots: { customRender: 'name' } },
        { title: '库存数量', dataIndex: 'count', scopedSlots: { customRender: 'count' }, width: 140 },
        { title: '预警阈值', dataIndex: 'minCount', scopedSlots: { customRender: 'minCount' }, width: 120 },
        { title: '存放位置', dataIndex: 'location', width: 150 },
        { title: '最近更新', dataIndex: 'updateAt', width: 180 },
        { title: '操作', scopedSlots: { customRender: 'action' }, width: 160, fixed: 'right' }
      ],
      currentRecord: null,
      inModalVisible: false,
      outModalVisible: false,
      checkModalVisible: false,
      thresholdModalVisible: false,
      warningDrawerVisible: false,
      inForm: { count: 1, description: '' },
      outForm: { count: 1, description: '' },
      checkForm: { actualCount: 0, remark: '' },
      thresholdForm: { minCount: null }
    }
  },
  mounted: function() {
    this.loadWarehouses()
    this.loadData()
    this.loadStats()
  },
  methods: {
    loadWarehouses: function() {
      var self = this
      FindAllWarehouse().then(function(res) {
        if (res.data) {
          self.warehouses = res.data
        }
      })
    },
    loadData: function() {
      var self = this
      self.loading = true
      var params = {
        page: self.pagination.current - 1,
        size: self.pagination.pageSize
      }
      if (self.selectedWarehouse) {
        params.wid = self.selectedWarehouse
      }
      if (self.keyword) {
        params.keyword = self.keyword
      }
      getInventoryPage(params).then(function(res) {
        if (res.data) {
          self.inventoryList = res.data.content || []
          self.pagination.total = res.data.totalElements || 0
        }
        self.loading = false
      }).catch(function() {
        self.loading = false
      })
    },
    loadStats: function() {
      var self = this
      getInventoryStats().then(function(res) {
        if (res.data) {
          self.stats = res.data
        }
      })
    },
    onTableChange: function(pagination) {
      this.pagination.current = pagination.current
      this.pagination.pageSize = pagination.pageSize
      this.loadData()
    },
    isLowStock: function(record) {
      return record.minCount !== null && record.minCount !== undefined && record.count <= record.minCount
    },
    showInModal: function(record) {
      this.currentRecord = record
      this.inForm = { count: 1, description: '' }
      this.inModalVisible = true
    },
    showOutModal: function(record) {
      this.currentRecord = record
      this.outForm = { count: 1, description: '' }
      this.outModalVisible = true
    },
    showCheckModal: function(record) {
      this.currentRecord = record
      this.checkForm = { actualCount: record.count || 0, remark: '' }
      this.checkModalVisible = true
    },
    showSetThreshold: function(record) {
      this.currentRecord = record
      this.thresholdForm = { minCount: record.minCount }
      this.thresholdModalVisible = true
    },
    handleIn: function() {
      var self = this
      if (!self.inForm.count || self.inForm.count < 1) {
        self.$message.warning('请输入入库数量')
        return
      }
      self.actionLoading = true
      var data = {
        wid: self.currentRecord.wid,
        cid: self.currentRecord.cid,
        name: self.currentRecord.name,
        count: self.inForm.count,
        description: self.inForm.description
      }
      InAndOut('in', data).then(function(res) {
        if (res.data) {
          self.$message.success('入库成功')
          self.inModalVisible = false
          self.loadData()
          self.loadStats()
        }
        self.actionLoading = false
      }).catch(function() {
        self.actionLoading = false
      })
    },
    handleOut: function() {
      var self = this
      if (!self.outForm.count || self.outForm.count < 1) {
        self.$message.warning('请输入出库数量')
        return
      }
      if (self.outForm.count > self.currentRecord.count) {
        self.$message.warning('出库数量不能大于当前库存')
        return
      }
      self.actionLoading = true
      var data = {
        wid: self.currentRecord.wid,
        cid: self.currentRecord.cid,
        name: self.currentRecord.name,
        count: self.outForm.count,
        description: self.outForm.description
      }
      InAndOut('out', data).then(function(res) {
        if (res.data) {
          self.$message.success('出库成功')
          self.outModalVisible = false
          self.loadData()
          self.loadStats()
        }
        self.actionLoading = false
      }).catch(function() {
        self.actionLoading = false
      })
    },
    handleCheck: function() {
      var self = this
      if (self.checkForm.actualCount === null || self.checkForm.actualCount === undefined) {
        self.$message.warning('请输入实际数量')
        return
      }
      self.actionLoading = true
      var data = {
        id: self.currentRecord.id,
        actualCount: self.checkForm.actualCount,
        remark: self.checkForm.remark
      }
      stockCheck(data).then(function(res) {
        if (res.data) {
          self.$message.success('盘点完成')
          self.checkModalVisible = false
          self.loadData()
          self.loadStats()
        } else {
          self.$message.error(res.message || '盘点失败')
        }
        self.actionLoading = false
      }).catch(function() {
        self.actionLoading = false
      })
    },
    handleSetThreshold: function() {
      var self = this
      self.actionLoading = true
      setMinCount(self.currentRecord.id, self.thresholdForm.minCount).then(function(res) {
        if (res.data) {
          self.$message.success('阈值设置成功')
          self.thresholdModalVisible = false
          self.loadData()
          self.loadStats()
        } else {
          self.$message.error(res.message || '设置失败')
        }
        self.actionLoading = false
      }).catch(function() {
        self.actionLoading = false
      })
    },
    showWarningList: function() {
      var self = this
      self.warningDrawerVisible = true
      self.warningLoading = true
      getWarningList().then(function(res) {
        if (res.data) {
          self.warningList = res.data
        }
        self.warningLoading = false
      }).catch(function() {
        self.warningLoading = false
      })
    },
    showInModalFromWarning: function(item) {
      this.warningDrawerVisible = false
      this.showInModal(item)
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

/* 统计卡片样式 */
.stats-row {
  margin-bottom: 24px;
}
.stat-card {
  height: 120px;
  border-radius: 12px;
  padding: 24px;
  color: #fff;
  position: relative;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0,0,0,0.1);
  transition: transform 0.3s;
  display: flex;
  align-items: center;
}
.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0,0,0,0.15);
}
.blue-gradient {
  background: linear-gradient(135deg, #1890ff 0%, #36cfc9 100%);
}
.green-gradient {
  background: linear-gradient(135deg, #52c41a 0%, #95de64 100%);
}
.red-gradient {
  background: linear-gradient(135deg, #ff4d4f 0%, #ff7875 100%);
}
.stat-icon {
  font-size: 48px;
  opacity: 0.3;
  margin-right: 24px;
}
.stat-content {
  flex: 1;
}
.stat-title {
  font-size: 14px;
  opacity: 0.9;
  margin-bottom: 4px;
}
.stat-value {
  font-size: 32px;
  font-weight: bold;
}
.stat-action {
  color: rgba(255,255,255,0.9);
  align-self: flex-end;
  padding: 0;
}
.stat-action:hover {
  color: #fff;
  text-decoration: underline;
}

/* 主内容样式 */
.main-card {
  border-radius: 8px;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
}
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}
.toolbar-left {
  display: flex;
  align-items: center;
}
.toolbar-label {
  margin-right: 8px;
  font-weight: 500;
  color: #333;
}

/* 表格样式优化 */
.product-name {
  font-weight: 500;
  color: #1890ff;
}
.stock-value {
  font-family: 'Arial', sans-serif;
  font-weight: bold;
  font-size: 16px;
}
.text-placeholder {
  color: #ccc;
  font-size: 12px;
}
.edit-icon {
  visibility: hidden;
  color: #1890ff;
  cursor: pointer;
  margin-left: 8px;
  opacity: 0.7;
}
.threshold-cell:hover .edit-icon {
  visibility: visible;
}
.warning-text {
  color: #ff4d4f;
}

/* 按钮样式 */
.btn-in {
  color: #52c41a;
}
.btn-in:hover {
  color: #73d13d;
}
.btn-out {
  color: #fa8c16;
}
.btn-out:hover {
  color: #ffa940;
}

/* 弹窗部分样式 */
.modal-product-info {
  background: #f7f7f7;
  padding: 16px;
  border-radius: 4px;
  margin-bottom: 24px;
  border-left: 4px solid #1890ff;
}
.modal-product-info h3 {
  margin: 0;
  font-size: 16px;
  color: #333;
}
.modal-product-info p {
  margin: 8px 0 0 0;
  color: #666;
}

/* 预警列表样式 */
.warning-item {
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
  transition: background 0.3s;
}
.warning-item:hover {
  background: #fafafa;
}
.warning-item-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}
.warning-name {
  font-weight: bold;
  font-size: 16px;
  color: #333;
}
.warning-item-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.warning-info {
  font-size: 13px;
  line-height: 1.6;
}
.text-red {
  color: #ff4d4f;
  font-weight: bold;
}
.text-gray {
  color: #999;
}
.empty-state {
  text-align: center;
  padding: 40px 0;
  color: #ccc;
}
.empty-state i {
  font-size: 48px;
  margin-bottom: 16px;
  color: #52c41a;
}
</style>
