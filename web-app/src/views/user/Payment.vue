<template>
  <div class="payment-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <h1><a-icon type="pay-circle" /> 订单支付</h1>
        <p class="subtitle">安全快捷的支付服务</p>
      </div>
    </div>

    <div class="page-body">
      <a-row :gutter="24">
        <!-- 左侧：支付表单 -->
        <a-col :xs="24" :lg="14">
          <div class="payment-card">
            <div class="card-title">
              <a-icon type="credit-card" />
              <span>选择支付订单</span>
            </div>

            <a-spin :spinning="loading">
              <!-- 订单选择 -->
              <div v-if="orders.length > 0" class="order-select-section">
                <div class="order-grid">
                  <div 
                    v-for="order in orders" 
                    :key="order.id" 
                    class="order-option"
                    :class="{ 'selected': form.orderId === order.id }"
                    @click="selectOrder(order)"
                  >
                    <div class="option-header">
                      <div class="order-no">
                        <a-icon type="file-text" />
                        {{ order.orderNo }}
                      </div>
                      <a-tag color="orange" size="small">待支付</a-tag>
                    </div>
                    <div class="option-body">
                      <div class="route">
                        <span class="from">{{ order.origin }}</span>
                        <a-icon type="arrow-right" class="arrow" />
                        <span class="to">{{ order.destination }}</span>
                      </div>
                      <div class="details">
                        <span>{{ order.type }}</span>
                        <span>×{{ order.quantity }}</span>
                      </div>
                    </div>
                    <div class="option-footer">
                      <div class="amount">¥{{ order.amount }}</div>
                      <a-popconfirm title="确定取消此订单？" @confirm="cancelOrder(order)" placement="top">
                        <a-button type="link" size="small" class="cancel-btn" @click.stop>取消订单</a-button>
                      </a-popconfirm>
                    </div>
                    <div class="check-icon" v-if="form.orderId === order.id">
                      <a-icon type="check-circle" theme="filled" />
                    </div>
                  </div>
                </div>
              </div>

              <div v-else class="empty-orders">
                <a-empty description="暂无待支付订单" :image="simpleImage" />
              </div>
            </a-spin>
          </div>
        </a-col>

        <!-- 右侧：支付详情 -->
        <a-col :xs="24" :lg="10">
          <div class="checkout-card" :class="{ 'has-order': selectedOrder }">
            <div class="card-title">
              <a-icon type="shopping-cart" />
              <span>支付详情</span>
            </div>

            <div v-if="selectedOrder" class="checkout-content">
              <!-- 订单信息摘要 -->
              <div class="order-summary">
                <div class="summary-row">
                  <span class="label">订单编号</span>
                  <span class="value mono">{{ selectedOrder.orderNo }}</span>
                </div>
                <div class="summary-row">
                  <span class="label">运输路线</span>
                  <span class="value">{{ selectedOrder.origin }} → {{ selectedOrder.destination }}</span>
                </div>
                <div class="summary-row">
                  <span class="label">货物信息</span>
                  <span class="value">{{ selectedOrder.type }} × {{ selectedOrder.quantity }}</span>
                </div>
                <div class="summary-row">
                  <span class="label">创建时间</span>
                  <span class="value">{{ selectedOrder.createAt }}</span>
                </div>
              </div>

              <!-- 支付方式选择 -->
              <div class="payment-methods">
                <div class="methods-title">选择支付方式</div>
                <div class="methods-grid">
                  <div 
                    class="method-item" 
                    :class="{ 'active': form.method === '微信支付' }"
                    @click="form.method = '微信支付'"
                  >
                    <a-icon type="wechat" class="icon wechat" />
                    <span>微信支付</span>
                  </div>
                  <div 
                    class="method-item" 
                    :class="{ 'active': form.method === '支付宝' }"
                    @click="form.method = '支付宝'"
                  >
                    <a-icon type="alipay-circle" class="icon alipay" />
                    <span>支付宝</span>
                  </div>
                  <div 
                    class="method-item" 
                    :class="{ 'active': form.method === '银行卡' }"
                    @click="form.method = '银行卡'"
                  >
                    <a-icon type="credit-card" class="icon bank" />
                    <span>银行卡</span>
                  </div>
                </div>
              </div>

              <!-- 支付金额 -->
              <div class="total-section">
                <div class="total-label">应付金额</div>
                <div class="total-amount">¥{{ selectedOrder.amount }}</div>
              </div>

              <!-- 支付按钮 -->
              <a-button 
                type="primary" 
                block 
                size="large" 
                class="pay-btn"
                :loading="paying" 
                @click="submitPayment"
              >
                <a-icon type="safety-certificate" />
                确认支付
              </a-button>

              <div class="security-tip">
                <a-icon type="lock" /> 安全支付由平台保障
              </div>
            </div>

            <div v-else class="empty-checkout">
              <div class="empty-icon">
                <a-icon type="shopping-cart" />
              </div>
              <p>请从左侧选择要支付的订单</p>
            </div>
          </div>
        </a-col>
      </a-row>
    </div>
  </div>
</template>

<script>
import {FetchPayableOrders, PayOrder, CancelOrder} from "@/api/order";
import { Empty } from 'ant-design-vue';

export default {
  data() {
    return {
      simpleImage: Empty.PRESENTED_IMAGE_SIMPLE,
      loading: false,
      paying: false,
      orders: [],
      form: {
        orderId: "",
        method: "微信支付"
      }
    }
  },

  computed: {
    selectedOrder() {
      return this.orders.find(item => item.id === this.form.orderId)
    }
  },

  mounted() {
    this.loadOrders()
  },

  methods: {
    loadOrders() {
      this.loading = true
      FetchPayableOrders().then((res) => {
        if (res.status) this.orders = res.data
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },

    selectOrder(order) {
      this.form.orderId = order.id
    },

    submitPayment() {
      if (!this.form.orderId) {
        this.$message.warning("请选择订单")
        return
      }
      this.paying = true
      PayOrder(this.form.orderId, this.form.method).then((res) => {
        if (res.status) {
          this.$message.success("支付成功")
          this.form.orderId = ""
          this.loadOrders()
        }
        this.paying = false
      }).catch(() => {
        this.paying = false
      })
    },

    cancelOrder(record) {
      CancelOrder(record.id).then((res) => {
        if (res.status) {
          this.$message.success("订单已取消")
          if (this.form.orderId === record.id) {
            this.form.orderId = ""
          }
          this.loadOrders()
        }
      })
    },

    goCreateOrder() {
      this.$router.push("/user/order-create")
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
    }
  }
}
</script>

<style scoped>
.payment-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8ec 100%);
}

/* 页面头部 */
.page-header {
  background: linear-gradient(135deg, #722ed1 0%, #531dab 100%);
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

/* 卡片通用样式 */
.payment-card,
.checkout-card {
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
  color: #722ed1;
}

/* 订单选择网格 */
.order-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.order-option {
  position: relative;
  border: 2px solid #f0f0f0;
  border-radius: 12px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.3s;
  background: #fafafa;
}

.order-option:hover {
  border-color: #d3adf7;
  box-shadow: 0 4px 12px rgba(114, 46, 209, 0.1);
}

.order-option.selected {
  border-color: #722ed1;
  background: #f9f0ff;
}

.option-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.order-no {
  font-size: 13px;
  color: #666;
  font-family: monospace;
  display: flex;
  align-items: center;
  gap: 4px;
}

.option-body {
  margin-bottom: 12px;
}

.route {
  font-size: 15px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.route .from { color: #52c41a; }
.route .to { color: #1890ff; }
.route .arrow { color: #bfbfbf; font-size: 12px; }

.details {
  font-size: 13px;
  color: #999;
  display: flex;
  gap: 8px;
}

.option-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px dashed #e8e8e8;
}

.option-footer .amount {
  font-size: 20px;
  font-weight: 700;
  color: #f5222d;
}

.cancel-btn {
  color: #999;
  padding: 0;
}

.cancel-btn:hover {
  color: #f5222d;
}

.check-icon {
  position: absolute;
  top: 12px;
  right: 12px;
  font-size: 20px;
  color: #722ed1;
}

/* 空订单状态 */
.empty-orders {
  padding: 40px 0;
  text-align: center;
}

/* 结算卡片 */
.checkout-card.has-order {
  background: linear-gradient(135deg, #f9f0ff 0%, #efdbff 100%);
  border: 1px solid #d3adf7;
}

.checkout-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 订单摘要 */
.order-summary {
  background: rgba(255, 255, 255, 0.8);
  border-radius: 8px;
  padding: 16px;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px dashed #e8e8e8;
}

.summary-row:last-child {
  border-bottom: none;
}

.summary-row .label {
  color: #666;
  font-size: 13px;
}

.summary-row .value {
  color: #333;
  font-weight: 500;
}

.summary-row .value.mono {
  font-family: monospace;
}

/* 支付方式 */
.payment-methods {
  background: rgba(255, 255, 255, 0.8);
  border-radius: 8px;
  padding: 16px;
}

.methods-title {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 12px;
}

.methods-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.method-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16px 8px;
  border: 2px solid #e8e8e8;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  background: #fff;
}

.method-item:hover {
  border-color: #d3adf7;
}

.method-item.active {
  border-color: #722ed1;
  background: #f9f0ff;
}

.method-item .icon {
  font-size: 28px;
  margin-bottom: 6px;
}

.method-item .icon.wechat { color: #07c160; }
.method-item .icon.alipay { color: #1677ff; }
.method-item .icon.bank { color: #faad14; }

.method-item span {
  font-size: 12px;
  color: #666;
}

/* 支付金额 */
.total-section {
  text-align: center;
  padding: 20px;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 8px;
}

.total-label {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.total-amount {
  font-size: 36px;
  font-weight: 700;
  color: #f5222d;
}

/* 支付按钮 */
.pay-btn {
  height: 50px;
  font-size: 18px;
  background: linear-gradient(135deg, #722ed1 0%, #531dab 100%);
  border: none;
}

.pay-btn:hover {
  background: linear-gradient(135deg, #9254de 0%, #722ed1 100%);
}

.security-tip {
  text-align: center;
  font-size: 12px;
  color: #999;
  margin-top: 12px;
}

.security-tip .anticon {
  color: #52c41a;
  margin-right: 4px;
}

/* 空结算状态 */
.empty-checkout {
  text-align: center;
  padding: 60px 20px;
  color: #999;
}

.empty-checkout .empty-icon {
  font-size: 48px;
  color: #d9d9d9;
  margin-bottom: 16px;
}

.empty-checkout p {
  margin: 0;
}

/* 响应式适配 */
@media (max-width: 992px) {
  .page-body {
    padding: 0 16px 24px;
  }
  
  .payment-card,
  .checkout-card {
    margin-top: 16px;
  }
  
  .methods-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 576px) {
  .order-grid {
    grid-template-columns: 1fr;
  }
}
</style>
</CodeContent>
<parameter name="EmptyFile">false
