<template>
  <div class="user-orders-page">
    <div class="page-header-wrapper">
      <div class="header-content">
        <div class="title-row">
          <h1>我的订单</h1>
          <div class="search-box">
             <a-input-search
              v-model="keyword"
              placeholder="搜索订单号、起点或终点"
              style="width: 300px"
              size="large"
              @search="onSearch"
              allowClear
            />
          </div>
        </div>
      </div>
    </div>

    <div class="page-content">
      <a-tabs v-model="activeTab" @change="onTabChange" class="custom-tabs" :animated="false">
        <a-tab-pane key="orders" tab="订单列表">
          
          <!-- 状态筛选 -->
          <div class="filter-bar">
            <span class="label">订单状态：</span>
            <a-radio-group v-model="statusFilter" button-style="solid" @change="onStatusChange" size="large">
              <a-radio-button value="all">全部</a-radio-button>
              <a-radio-button value="0">待支付</a-radio-button>
              <a-radio-button value="1">已支付</a-radio-button>
              <a-radio-button value="2">运输中</a-radio-button>
              <a-radio-button value="3">已完成</a-radio-button>
              <a-radio-button value="-1">已取消</a-radio-button>
            </a-radio-group>
          </div>

          <a-spin :spinning="loading">
            <div v-if="orders.length > 0">
              <a-row :gutter="[24, 24]">
                <a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="8" v-for="order in orders" :key="order.id">
                  <div class="order-card-new" @click="goDetail(order.id)">
                    <div class="card-header">
                      <div class="order-no">
                        <span class="label">NO.</span>
                        <span class="value">{{ order.orderNo }}</span>
                        <a-tooltip title="复制订单号">
                          <a-icon 
                            type="copy" 
                            class="copy-btn" 
                            @click.stop="copyOrderNo(order.orderNo)" 
                          />
                        </a-tooltip>
                      </div>
                      <a-tag :color="getStatusColor(order.status)" class="status-tag">
                        {{ getStatusText(order.status) }}
                      </a-tag>
                    </div>
                    
                    <div class="card-body">
                      <div class="route-info">
                        <div class="city from">
                           <div class="name">{{ order.origin }}</div>
                           <div class="icon"><a-icon type="environment" theme="filled" /></div>
                        </div>
                        <div class="arrow">
                          <div class="line"></div>
                          <a-icon type="right" />
                        </div>
                        <div class="city to">
                           <div class="name">{{ order.destination }}</div>
                           <div class="icon"><a-icon type="environment" theme="filled" /></div>
                        </div>
                      </div>
                      
                      <div class="goods-info">
                        <div class="info-item">
                          <span class="label">货物</span>
                          <span class="val">{{ order.type }}</span>
                        </div>
                        <div class="info-item">
                          <span class="label">数量</span>
                          <span class="val">{{ order.quantity }}</span>
                        </div>
                        <div class="info-item price">
                          <span class="label">金额</span>
                          <span class="val">¥{{ order.amount }}</span>
                        </div>
                      </div>
                    </div>

                    <div class="card-footer">
                      <div class="time">{{ order.createAt }}</div>
                      <div class="actions">
                        <a-button 
                          v-if="order.status !== -1"
                          type="primary" 
                          ghost 
                          size="small" 
                          icon="compass"
                          @click.stop="queryOrderRoute(order.orderNo)"
                        >
                          物流
                        </a-button>
                        <a-button 
                          size="small" 
                          @click.stop="goDetail(order.id)"
                        >
                          详情
                        </a-button>
                      </div>
                    </div>
                  </div>
                </a-col>
              </a-row>

              <div class="pagination">
                <a-pagination
                  :current="page + 1"
                  :total="total"
                  :page-size="size"
                  show-size-changer
                  :page-size-options="['6', '12', '24']"
                  @change="onPageChange"
                  @showSizeChange="onSizeChange"
                />
              </div>
            </div>
            
            <div v-else class="empty-state">
              <a-empty description="暂无相关订单" :image="simpleImage" />
            </div>
          </a-spin>
        </a-tab-pane>

        <a-tab-pane key="route" tab="物流查询">
          <div class="route-query-wrapper">
             <a-input-search
              v-model="routeForm.orderNo"
              placeholder="请输入要在地图上追踪的订单号"
              enter-button="查询物流"
              size="large"
              style="max-width: 600px; margin: 0 auto; display: block;"
              @search="queryRoute"
              :loading="routeLoading"
            />
          </div>

          <div v-if="distribution" class="route-result-container">
            <a-card :bordered="false" class="route-detail-card">
              <div class="route-header">
                <div class="route-nodes-visual">
                  <div class="node start">
                    <div class="dot"></div>
                    <div class="text">{{ distribution.origin }}</div>
                  </div>
                  <div class="line-process">
                    <transition name="fade">
                      <div class="truck-icon" :style="{left: getProgress(distribution.status) + '%'}">
                        <a-icon type="car" />
                      </div>
                    </transition>
                  </div>
                  <div class="node end">
                    <div class="dot"></div>
                    <div class="text">{{ distribution.destination }}</div>
                  </div>
                </div>
                <div class="current-status">
                   当前状态：<span :style="{color: getRouteStatusColor(distribution.status, true)}">{{ getRouteStatusText(distribution.status) }}</span>
                   <span class="location" v-if="distribution.currentNode">（{{ distribution.currentNode }}）</span>
                </div>
              </div>

              <a-divider />

              <a-row :gutter="24">
                <a-col :md="14" :xs="24">
                  <div class="sub-title">物流追踪</div>
                   <a-timeline mode="left" class="custom-timeline">
                    <a-timeline-item
                      v-for="record in orderedTrackRecords"
                      :key="record.id"
                      :color="getTimelineColor(record.status)"
                    >
                      <template #dot>
                         <a-icon :type="getTimelineIcon(record.status)" style="font-size: 16px" />
                      </template>
                      <div class="timeline-content">
                        <div class="time">{{ record.createTime }}</div>
                        <div class="status">{{ record.node }} - {{ record.status }}</div>
                        <div class="remark" v-if="record.remark">{{ record.remark }}</div>
                      </div>
                    </a-timeline-item>
                    <a-timeline-item v-if="orderedTrackRecords.length === 0" color="gray">
                       <p>暂无更新记录</p>
                    </a-timeline-item>
                  </a-timeline>
                </a-col>
                <a-col :md="10" :xs="24">
                   <div class="sub-title">订单信息</div>
                   <div class="info-list">
                     <div class="item">
                       <span class="label">订单编号</span>
                       <span class="val">{{ distribution.orderNo }}</span>
                     </div>
                     <div class="item">
                       <span class="label">异常等级</span>
                       <span class="val">
                         <a-tag :color="getWarningColor(distribution.warningLevel)">
                           {{ getWarningText(distribution.warningLevel) }}
                         </a-tag>
                       </span>
                     </div>
                     <div class="item" v-if="distribution.warningNote">
                       <span class="label">异常说明</span>
                       <span class="val text-red">{{ distribution.warningNote }}</span>
                     </div>
                     <div class="item">
                       <span class="label">预计路线</span>
                       <span class="val">{{ distribution.routeNodes || '-' }}</span>
                     </div>
                   </div>
                </a-col>
              </a-row>
            </a-card>
          </div>
          
           <div v-else-if="!routeLoading && routeForm.orderNo" class="empty-state">
              <a-empty description="未查询到物流信息" />
           </div>

        </a-tab-pane>
      </a-tabs>
    </div>
  </div>
</template>

<script>
import { FetchOrdersPage } from "@/api/order";
import { QueryRouteByOrder } from "@/api/distribution";
import { Empty } from 'ant-design-vue';

export default {
  name: "MyOrders",
  data() {
    return {
      simpleImage: Empty.PRESENTED_IMAGE_SIMPLE,
      loading: false,
      activeTab: "orders",
      statusFilter: "all",
      keyword: "",
      orders: [],
      page: 0,
      size: 12, // 增加每页数量以适应Grid
      total: 0,
      // 路线查询相关
      routeLoading: false,
      routeForm: {
        orderNo: ""
      },
      distribution: null,
      trackRecords: []
    };
  },

  computed: {
    orderedTrackRecords() {
      if (!this.trackRecords || this.trackRecords.length === 0) {
        return [];
      }
      return [...this.trackRecords].sort((a, b) => {
        const aTime = a.createTime || "";
        const bTime = b.createTime || "";
        return bTime.localeCompare(aTime); // 倒序，最新的在上面
      });
    }
  },

  mounted() {
    this.loadOrders();
    // 如果有查询参数，自动跳转
    if (this.$route.query.status) {
      this.statusFilter = String(this.$route.query.status);
      this.loadOrders();
    }
  },

  methods: {
    loadOrders() {
      this.loading = true;
      const params = {
        page: this.page,
        size: this.size
      };
      if (this.statusFilter !== "all") {
        params.status = parseInt(this.statusFilter);
      }
      if (this.keyword.trim()) {
        params.keyword = this.keyword.trim();
      }

      FetchOrdersPage(params)
        .then(res => {
          if (res.status && res.data) {
            this.orders = res.data.content || [];
            this.total = res.data.totalElements || 0;
          } else {
            this.orders = [];
            this.total = 0;
          }
          this.loading = false;
        })
        .catch(() => {
          this.orders = [];
          this.total = 0;
          this.loading = false;
        });
    },

    onTabChange(key) {
      if (key === 'orders' && this.orders.length === 0) {
        this.loadOrders();
      }
    },

    onStatusChange() {
      this.page = 0;
      this.loadOrders();
    },

    onSearch() {
      this.page = 0;
      this.loadOrders();
    },

    onPageChange(page) {
      this.page = page - 1;
      this.loadOrders();
    },

    onSizeChange(current, size) {
      this.page = 0;
      this.size = size;
      this.loadOrders();
    },

    goDetail(id) {
      this.$router.push(`/user/orders/${id}`);
    },

    // 复制订单号
    copyOrderNo(orderNo) {
      if (navigator.clipboard) {
        navigator.clipboard.writeText(orderNo).then(() => {
          this.$message.success('订单号已复制');
        }).catch(() => {
          this.$message.error('复制失败');
        });
      } else {
        // 兼容旧浏览器
        const textArea = document.createElement('textarea');
        textArea.value = orderNo;
        document.body.appendChild(textArea);
        textArea.select();
        try {
          document.execCommand('copy');
          this.$message.success('订单号已复制');
        } catch (err) {
          this.$message.error('复制失败');
        }
        document.body.removeChild(textArea);
      }
    },

    // 从订单列表快速查询物流
    queryOrderRoute(orderNo) {
      this.routeForm.orderNo = orderNo;
      this.activeTab = "route";
      this.$nextTick(() => {
        this.queryRoute();
      });
    },

    // 路线查询
    queryRoute() {
      if (!this.routeForm.orderNo) {
        this.$message.warning("请输入订单号");
        return;
      }
      this.routeLoading = true;
      // 模拟一点延迟感
      setTimeout(() => {
        QueryRouteByOrder(this.routeForm.orderNo.trim())
        .then(res => {
          if (res.status && res.data) {
            this.distribution = res.data.distribution || null;
            this.trackRecords = res.data.trackRecords || [];
          } else {
            this.distribution = null;
            this.trackRecords = [];
            this.$message.warning("未找到物流信息");
          }
          this.routeLoading = false;
        })
        .catch(() => {
          this.distribution = null;
          this.trackRecords = [];
          this.routeLoading = false;
        });
      }, 300);
    },

    resetRouteForm() {
      this.routeForm = { orderNo: "" };
      this.distribution = null;
      this.trackRecords = [];
    },

    // 辅助方法
    getStatusText(status) {
      const map = { 0: "待支付", 1: "已支付", 2: "运输中", 3: "已完成", [-1]: "已取消" };
      return map[status] || "未知";
    },

    getStatusColor(status) {
      const map = { 0: "orange", 1: "blue", 2: "cyan", 3: "green", [-1]: "red" };
      return map[status] || "default";
    },

    getRouteStatusText(status) {
      const map = { 0: "待调度", 1: "运输中", 2: "已完成" };
      return map[status] || "未知";
    },

    getRouteStatusColor(status, isText) {
      const map = { 0: "#faad14", 1: "#1890ff", 2: "#52c41a" };
      return map[status] || (isText ? "#333" : "default");
    },

    getWarningText(level) {
      const map = { 0: "正常", 1: "一般", 2: "严重" };
      return map[level] || "未知";
    },

    getWarningColor(level) {
      const map = { 0: "green", 1: "orange", 2: "red" };
      return map[level] || "default";
    },

    getTimelineColor(status) {
      const colors = { "到达": "green", "离开": "blue", "装货": "orange", "卸货": "purple", "异常": "red" };
      return colors[status] || "gray";
    },

    getTimelineIcon(status) {
      const icons = {
        "到达": "check-circle", "离开": "right-circle", "装货": "upload", "卸货": "download", "异常": "warning"
      };
      return icons[status] || "clock-circle";
    },

    getProgress(status) {
      if (status === 0) return 10;
      if (status === 1) return 50;
      if (status === 2) return 90;
      return 0;
    }
  }
};
</script>

<style scoped>
.user-orders-page {
  min-height: 100vh;
  background: #f0f2f5;
  padding-bottom: 40px;
}

/* 顶部标题栏 */
.page-header-wrapper {
  background: #fff;
  padding: 24px 32px;
  box-shadow: 0 1px 4px rgba(0,21,41,0.08);
  margin-bottom: 24px;
}

.header-content h1 {
  font-size: 24px;
  color: #333;
  margin: 0;
  font-weight: 600;
}

.title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  max-width: 1200px;
  margin: 0 auto;
}

.page-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}

/* 筛选栏 */
.filter-bar {
  background: #fff;
  padding: 16px 24px;
  border-radius: 8px;
  margin-bottom: 24px;
  display: flex;
  align-items: center;
  box-shadow: 0 1px 2px rgba(0,0,0,0.03);
}

.filter-bar .label {
  margin-right: 16px;
  font-weight: 500;
  color: #666;
}

/* 订单卡片新样式 */
.order-card-new {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s ease;
  border: 1px solid #f0f0f0;
  height: 100%;
  display: flex;
  flex-direction: column;
  cursor: pointer;
}

.order-card-new:hover {
  transform: translateY(-4px);
  box-shadow: 0 10px 20px rgba(0,0,0,0.08);
  border-color: transparent;
}

.card-header {
  padding: 16px;
  border-bottom: 1px solid #f5f5f5;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fafafa;
}

.order-no .label {
  color: #999;
  font-size: 12px;
  margin-right: 4px;
}

.order-no .value {
  font-weight: 600;
  color: #333;
  font-family: monospace;
  font-size: 16px;
}

.order-no .copy-btn {
  margin-left: 8px;
  color: #999;
  cursor: pointer;
  font-size: 14px;
  padding: 4px;
  border-radius: 4px;
  transition: all 0.2s ease;
}

.order-no .copy-btn:hover {
  color: #1890ff;
  background: rgba(24, 144, 255, 0.1);
  transform: scale(1.1);
}

.card-body {
  padding: 20px;
  flex: 1;
}

/* 路线显示 */
.route-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.city {
  text-align: center;
}

.city .name {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}

.city .icon {
  font-size: 12px;
}

.city.from .icon { color: #52c41a; }
.city.to .icon { color: #1890ff; }

.arrow {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  margin: 0 12px;
  color: #d9d9d9;
}

.arrow .line {
  height: 1px;
  background: #e8e8e8;
  width: 100%;
  position: absolute;
  top: 50%;
  left: 0;
}

.arrow .anticon {
  background: #fff;
  padding: 0 4px;
  position: relative;
  z-index: 1;
}

/* 货物简要信息 */
.goods-info {
  display: flex;
  justify-content: space-around;
  background: #f9f9f9;
  border-radius: 6px;
  padding: 12px;
}

.info-item {
  text-align: center;
}

.info-item .label {
  display: block;
  font-size: 12px;
  color: #999;
  margin-bottom: 2px;
}

.info-item .val {
  font-weight: 500;
  color: #666;
}

.info-item.price .val {
  color: #f5222d;
  font-weight: 700;
}

/* 底部 */
.card-footer {
  padding: 12px 16px;
  border-top: 1px solid #f5f5f5;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-footer .time {
  font-size: 12px;
  color: #aaa;
}

.card-footer .actions .ant-btn {
  margin-left: 8px;
}

/* 物流查询样式 */
.route-query-wrapper {
  padding: 40px 0;
  text-align: center;
}

.route-result-container {
  margin-top: 24px;
}

.route-detail-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
  padding: 24px;
}

.route-nodes-visual {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 40px 0;
  position: relative;
  padding: 0 10%;
}

.line-process {
  flex: 1;
  height: 4px;
  background: #e8e8e8;
  position: relative;
  margin: 0 20px;
  border-radius: 2px;
}

.truck-icon {
  position: absolute;
  top: -24px;
  transform: translateX(-50%);
  font-size: 24px;
  color: #1890ff;
}

.node {
  text-align: center;
  position: relative;
  z-index: 2;
}

.node .dot {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  margin: 0 auto 8px;
}

.node.start .dot { background: #52c41a; border: 3px solid #d9f7be; }
.node.end .dot { background: #1890ff; border: 3px solid #bae7ff; }

.node .text {
  font-weight: 600;
  color: #333;
  font-size: 16px;
}

.current-status {
  text-align: center;
  font-size: 16px;
  margin-top: 20px;
}

.sub-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 20px;
  padding-left: 10px;
  border-left: 4px solid #1890ff;
}

.info-list .item {
  display: flex;
  margin-bottom: 12px;
  border-bottom: 1px dashed #f0f0f0;
  padding-bottom: 12px;
}

.info-list .label {
  width: 80px;
  color: #999;
}

.info-list .val {
  color: #333;
  flex: 1;
}

.custom-timeline {
  margin-top: 10px;
}

.timeline-content .time {
  font-size: 12px;
  color: #999;
  margin-bottom: 4px;
}

.timeline-content .status {
  font-weight: 500;
  color: #333;
}

.empty-state {
  padding: 60px 0;
  text-align: center;
}

.pagination {
  margin-top: 32px;
  text-align: center;
}
</style>
