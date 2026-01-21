<template>
  <div class="page">
    <a-card title="订单支付" :loading="loading">
      <a-form-model :model="form" :label-col="{span: 6}" :wrapper-col="{span: 14}">
        <a-form-model-item label="选择订单">
          <a-select v-model="form.orderId" placeholder="请选择订单" @change="onOrderChange">
            <a-select-option v-for="order in orders" :key="order.id" :value="order.id">
              {{ order.orderNo }} - ¥{{ order.amount }}
            </a-select-option>
          </a-select>
        </a-form-model-item>
        <a-form-model-item label="订单详情" v-if="selectedOrder">
          <a-descriptions :column="1" size="small">
            <a-descriptions-item label="起点">{{ selectedOrder.origin }}</a-descriptions-item>
            <a-descriptions-item label="终点">{{ selectedOrder.destination }}</a-descriptions-item>
            <a-descriptions-item label="货物类型">{{ selectedOrder.type }}</a-descriptions-item>
            <a-descriptions-item label="数量">{{ selectedOrder.quantity }}</a-descriptions-item>
            <a-descriptions-item label="创建时间">{{ selectedOrder.createAt }}</a-descriptions-item>
          </a-descriptions>
        </a-form-model-item>
        <a-form-model-item label="支付方式">
          <a-radio-group v-model="form.method">
            <a-radio value="微信支付">
              <a-icon type="wechat" style="color: #07c160"/> 微信支付
            </a-radio>
            <a-radio value="支付宝">
              <a-icon type="alipay-circle" style="color: #1677ff"/> 支付宝
            </a-radio>
            <a-radio value="银行卡">
              <a-icon type="credit-card" style="color: #faad14"/> 银行卡
            </a-radio>
          </a-radio-group>
        </a-form-model-item>
        <a-form-model-item label="应付金额">
          <span class="amount">¥ {{ selectedAmount }}</span>
        </a-form-model-item>
      </a-form-model>
      <div class="actions">
        <a-button type="primary" size="large" :loading="paying" @click="submitPayment">
          <a-icon type="pay-circle"/> 立即支付
        </a-button>
      </div>
    </a-card>

    <a-card title="待支付订单" style="margin-top: 16px">
      <a-table :columns="columns" :data-source="orders" rowKey="id" :pagination="false">
        <template slot="amount" slot-scope="amount">
          <span class="table-amount">¥ {{ amount }}</span>
        </template>
        <template slot="status" slot-scope="status">
          <a-tag color="orange">{{ getStatusText(status) }}</a-tag>
        </template>
        <template slot="action" slot-scope="text, record">
          <a-button type="link" size="small" @click="selectOrder(record)">选择支付</a-button>
          <a-popconfirm title="确定取消此订单？" @confirm="cancelOrder(record)">
            <a-button type="link" size="small" style="color: #ff4d4f">取消</a-button>
          </a-popconfirm>
        </template>
      </a-table>
      <div v-if="orders.length === 0" class="empty-tip">
        暂无待支付订单
      </div>
    </a-card>
  </div>
</template>

<script>
import {FetchPayableOrders, PayOrder, CancelOrder} from "@/api/order";

export default {
  data() {
    return {
      loading: false,
      paying: false,
      orders: [],
      form: {
        orderId: "",
        method: "微信支付"
      },
      columns: [
        {title: "订单号", dataIndex: "orderNo", key: "orderNo"},
        {title: "金额", dataIndex: "amount", key: "amount", scopedSlots: {customRender: "amount"}},
        {title: "起点", dataIndex: "origin", key: "origin"},
        {title: "终点", dataIndex: "destination", key: "destination"},
        {title: "状态", dataIndex: "status", key: "status", scopedSlots: {customRender: "status"}},
        {title: "操作", key: "action", scopedSlots: {customRender: "action"}},
      ]
    }
  },

  computed: {
    selectedOrder() {
      return this.orders.find(item => item.id === this.form.orderId)
    },
    selectedAmount() {
      return this.selectedOrder ? this.selectedOrder.amount : "-"
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

    onOrderChange(orderId) {
      this.form.orderId = orderId
    },

    selectOrder(record) {
      this.form.orderId = record.id
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
          this.loadOrders()
        }
      })
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
.page {
  padding: 24px;
}

.actions {
  text-align: right;
}

.amount {
  font-size: 24px;
  font-weight: bold;
  color: #f5222d;
}

.table-amount {
  font-weight: bold;
  color: #f5222d;
}

.empty-tip {
  text-align: center;
  color: #8c8c8c;
  padding: 16px;
}
</style>
