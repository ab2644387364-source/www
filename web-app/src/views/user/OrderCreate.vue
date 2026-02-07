<template>
  <div class="order-create-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <h1><a-icon type="form" /> 创建运输订单</h1>
        <p class="subtitle">填写订单信息，快速发起物流服务</p>
      </div>
    </div>

    <div class="page-body">
      <a-row :gutter="24">
        <!-- 左侧：订单表单 -->
        <a-col :xs="24" :lg="14">
          <div class="form-card">
            <div class="card-title">
              <a-icon type="edit" />
              <span>订单信息</span>
            </div>
            
            <a-form-model :model="form" :label-col="{span: 24}" :wrapper-col="{span: 24}" class="order-form">
              <!-- 路线信息 -->
              <div class="form-section">
                <div class="section-title">
                  <a-icon type="environment" />
                  <span>路线信息</span>
                </div>
                <a-row :gutter="16">
                  <a-col :span="12">
                    <a-form-model-item label="起点" required>
                      <a-input v-model="form.origin" placeholder="请输入起点城市" size="large">
                        <a-icon slot="prefix" type="environment" style="color: #52c41a" />
                      </a-input>
                    </a-form-model-item>
                  </a-col>
                  <a-col :span="12">
                    <a-form-model-item label="终点" required>
                      <a-input v-model="form.destination" placeholder="请输入终点城市" size="large">
                        <a-icon slot="prefix" type="environment" style="color: #1890ff" />
                      </a-input>
                    </a-form-model-item>
                  </a-col>
                </a-row>
              </div>

              <!-- 货物信息 -->
              <div class="form-section">
                <div class="section-title">
                  <a-icon type="inbox" />
                  <span>货物信息</span>
                </div>
                <a-row :gutter="16">
                  <a-col :span="12">
                    <a-form-model-item label="货物类型" required>
                      <a-select v-model="form.type" placeholder="请选择类型" size="large">
                        <a-select-option value="牛">🐂 牛</a-select-option>
                        <a-select-option value="羊">🐑 羊</a-select-option>
                        <a-select-option value="猪">🐷 猪</a-select-option>
                      </a-select>
                    </a-form-model-item>
                  </a-col>
                  <a-col :span="12">
                    <a-form-model-item label="数量" required>
                      <a-input-number v-model="form.quantity" :min="1" style="width: 100%" size="large" placeholder="输入数量" />
                    </a-form-model-item>
                  </a-col>
                </a-row>
              </div>

              <!-- 联系信息 -->
              <div class="form-section">
                <div class="section-title">
                  <a-icon type="phone" />
                  <span>联系信息</span>
                </div>
                <a-form-model-item label="联系手机" required>
                  <a-input v-model="form.fromPhone" placeholder="请输入11位手机号" :maxLength="11" size="large">
                    <a-icon slot="prefix" type="phone" />
                  </a-input>
                  <div v-if="form.fromPhone && !isValidPhone(form.fromPhone)" class="field-error">
                    <a-icon type="exclamation-circle" /> 请输入正确的11位手机号
                  </div>
                </a-form-model-item>
              </div>

              <!-- 附加信息 -->
              <div class="form-section">
                <div class="section-title">
                  <a-icon type="calendar" />
                  <span>附加信息</span>
                </div>
                <a-row :gutter="16">
                  <a-col :span="12">
                    <a-form-model-item label="期望提货时间">
                      <a-date-picker v-model="form.pickupDate" style="width: 100%" valueFormat="YYYY-MM-DD" size="large" placeholder="选择日期" />
                    </a-form-model-item>
                  </a-col>
                  <a-col :span="12">
                    <a-form-model-item label="备注">
                      <a-input v-model="form.remark" placeholder="补充说明（选填）" size="large" />
                    </a-form-model-item>
                  </a-col>
                </a-row>
              </div>

              <!-- 操作按钮 -->
              <div class="form-actions">
                <a-button size="large" @click="resetForm">
                  <a-icon type="reload" /> 重置
                </a-button>
                <a-button type="primary" size="large" :loading="creating" @click="createOrder">
                  <a-icon type="check" /> 提交订单
                </a-button>
              </div>
            </a-form-model>
          </div>
        </a-col>

        <!-- 右侧：订单预览 & 最近订单 -->
        <a-col :xs="24" :lg="10">
          <!-- 订单预览卡片 -->
          <div class="preview-card" :class="{ 'has-result': orderResult }">
            <div class="card-title">
              <a-icon type="eye" />
              <span>订单预览</span>
            </div>
            
            <div v-if="orderResult" class="result-content">
              <div class="success-icon">
                <a-icon type="check-circle" theme="filled" />
              </div>
              <div class="success-text">订单创建成功！</div>
              
              <div class="order-info">
                <div class="info-row">
                  <span class="label">订单号</span>
                  <span class="value order-no">{{ orderResult.orderNo }}</span>
                </div>
                <div class="info-row">
                  <span class="label">预估费用</span>
                  <span class="value amount">¥{{ orderResult.amount }}</span>
                </div>
                <div class="info-row">
                  <span class="label">订单状态</span>
                  <a-tag :color="getStatusColor(orderResult.status)">{{ getStatusText(orderResult.status) }}</a-tag>
                </div>
              </div>
              
              <a-button type="primary" block size="large" class="pay-btn" @click="goToPayment">
                <a-icon type="pay-circle" /> 立即支付
              </a-button>
            </div>
            
            <div v-else class="empty-preview">
              <a-icon type="file-text" class="empty-icon" />
              <p>提交订单后将在这里显示预览信息</p>
            </div>
          </div>

          <!-- 最近订单卡片 -->
          <div class="recent-card">
            <div class="card-title">
              <a-icon type="history" />
              <span>最近订单</span>
            </div>
            
            <a-spin :spinning="loading">
              <div v-if="recentOrders.length > 0" class="order-list">
                <div v-for="order in recentOrders" :key="order.id" class="order-item">
                  <div class="order-main">
                    <div class="order-route">
                      <span class="from">{{ order.origin }}</span>
                      <a-icon type="arrow-right" class="arrow" />
                      <span class="to">{{ order.destination }}</span>
                    </div>
                    <div class="order-no">{{ order.orderNo }}</div>
                  </div>
                  <div class="order-side">
                    <div class="order-amount">¥{{ order.amount }}</div>
                    <a-tag :color="getStatusColor(order.status)" size="small">{{ getStatusText(order.status) }}</a-tag>
                  </div>
                </div>
              </div>
              <div v-else class="empty-list">
                <a-empty description="暂无订单" :image="simpleImage" />
              </div>
            </a-spin>
          </div>
        </a-col>
      </a-row>
    </div>
  </div>
</template>

<script>
import {CreateOrder, FetchRecentOrders} from "@/api/order";
import { Empty } from 'ant-design-vue';

export default {
  data() {
    return {
      simpleImage: Empty.PRESENTED_IMAGE_SIMPLE,
      loading: false,
      creating: false,
      form: {
        origin: "",
        destination: "",
        fromPhone: "",
        type: "",
        quantity: 1,
        pickupDate: null,
        remark: ""
      },
      orderResult: null,
      recentOrders: []
    }
  },

  mounted() {
    this.loadRecentOrders()
  },

  methods: {
    loadRecentOrders() {
      this.loading = true
      FetchRecentOrders().then((res) => {
        if (res.status) this.recentOrders = res.data
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },

    isValidPhone(phone) {
      return /^1[3-9]\d{9}$/.test(phone)
    },

    createOrder() {
      if (!this.form.origin) {
        this.$message.warning("请输入起点")
        return
      }
      if (!this.form.destination) {
        this.$message.warning("请输入终点")
        return
      }
      if (!this.form.fromPhone || !this.isValidPhone(this.form.fromPhone)) {
        this.$message.warning("请输入正确的11位手机号")
        return
      }
      if (!this.form.type) {
        this.$message.warning("请选择货物类型")
        return
      }
      this.creating = true
      CreateOrder(this.form).then((res) => {
        if (res.status) {
          this.orderResult = res.data
          this.$message.success("订单创建成功")
          this.loadRecentOrders()
        }
        this.creating = false
      }).catch(() => {
        this.creating = false
      })
    },

    resetForm() {
      this.form = {
        origin: "",
        destination: "",
        fromPhone: "",
        type: "",
        quantity: 1,
        pickupDate: null,
        remark: ""
      }
    },

    goToPayment() {
      this.$router.push("/user/pay")
    },

    getStatusText(status) {
      const map = {
        0: "待支付",
        1: "已支付",
        2: "运输中",
        3: "已完成",
        [-1]: "已取消"
      }
      return map[status] || "未知"
    },

    getStatusColor(status) {
      const map = {
        0: "orange",
        1: "blue",
        2: "cyan",
        3: "green",
        [-1]: "red"
      }
      return map[status] || "default"
    }
  }
}
</script>

<style scoped>
.order-create-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8ec 100%);
}

/* 页面头部 */
.page-header {
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
  padding: 32px;
  color: #fff;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
}

.header-content h1 {
  color: #fff;
  font-size: 28px;
  margin: 0 0 8px 0;
  font-weight: 600;
}

.header-content .subtitle {
  color: rgba(255, 255, 255, 0.85);
  font-size: 14px;
  margin: 0;
}

/* 页面主体 */
.page-body {
  max-width: 1200px;
  margin: -24px auto 0;
  padding: 0 24px 40px;
  position: relative;
}

/* 表单卡片 */
.form-card,
.preview-card,
.recent-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  padding: 24px;
  margin-bottom: 24px;
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin-bottom: 24px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.card-title .anticon {
  color: #1890ff;
}

/* 表单分区 */
.form-section {
  margin-bottom: 24px;
  padding-bottom: 24px;
  border-bottom: 1px dashed #f0f0f0;
}

.form-section:last-of-type {
  border-bottom: none;
  margin-bottom: 16px;
  padding-bottom: 0;
}

.section-title {
  font-size: 14px;
  font-weight: 500;
  color: #666;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.section-title .anticon {
  color: #1890ff;
}

/* 表单样式 */
.order-form .ant-form-item {
  margin-bottom: 16px;
}

.order-form .ant-form-item-label > label {
  font-weight: 500;
  color: #333;
}

.field-error {
  color: #f5222d;
  font-size: 12px;
  margin-top: 4px;
}

/* 操作按钮 */
.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

/* 预览卡片 */
.preview-card.has-result {
  background: linear-gradient(135deg, #f6ffed 0%, #d9f7be 100%);
  border: 1px solid #b7eb8f;
}

.result-content {
  text-align: center;
}

.success-icon {
  font-size: 48px;
  color: #52c41a;
  margin-bottom: 12px;
}

.success-text {
  font-size: 18px;
  font-weight: 600;
  color: #52c41a;
  margin-bottom: 24px;
}

.order-info {
  background: rgba(255, 255, 255, 0.8);
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 20px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px dashed #e8e8e8;
}

.info-row:last-child {
  border-bottom: none;
}

.info-row .label {
  color: #666;
}

.info-row .value {
  font-weight: 600;
  color: #333;
}

.info-row .order-no {
  font-family: monospace;
  font-size: 15px;
}

.info-row .amount {
  font-size: 20px;
  color: #f5222d;
}

.pay-btn {
  height: 44px;
  font-size: 16px;
}

/* 空预览状态 */
.empty-preview {
  text-align: center;
  padding: 40px 0;
  color: #999;
}

.empty-preview .empty-icon {
  font-size: 48px;
  color: #d9d9d9;
  margin-bottom: 12px;
}

.empty-preview p {
  margin: 0;
}

/* 最近订单列表 */
.order-list {
  max-height: 300px;
  overflow-y: auto;
}

.order-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background: #fafafa;
  border-radius: 8px;
  margin-bottom: 8px;
  transition: all 0.2s;
}

.order-item:hover {
  background: #f0f5ff;
}

.order-item:last-child {
  margin-bottom: 0;
}

.order-main {
  flex: 1;
}

.order-route {
  font-size: 14px;
  color: #333;
  margin-bottom: 4px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.order-route .arrow {
  color: #bfbfbf;
  font-size: 12px;
}

.order-route .from {
  color: #52c41a;
  font-weight: 500;
}

.order-route .to {
  color: #1890ff;
  font-weight: 500;
}

.order-main .order-no {
  font-size: 12px;
  color: #999;
  font-family: monospace;
}

.order-side {
  text-align: right;
}

.order-amount {
  font-size: 16px;
  font-weight: 600;
  color: #f5222d;
  margin-bottom: 4px;
}

.empty-list {
  padding: 24px 0;
}

/* 响应式适配 */
@media (max-width: 992px) {
  .page-body {
    padding: 0 16px 24px;
  }
  
  .form-card,
  .preview-card,
  .recent-card {
    margin-top: 16px;
  }
}
</style>
</CodeContent>
<parameter name="EmptyFile">false
