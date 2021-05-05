<template>
  <div class="page">
    <a-card title="订单支付">
      <a-form-model :model="form" :label-col="{span: 6}" :wrapper-col="{span: 14}">
        <a-form-model-item label="选择订单">
          <a-select v-model="form.orderNo" placeholder="请选择订单">
            <a-select-option v-for="order in orders" :key="order.orderNo" :value="order.orderNo">
              {{ order.orderNo }} - {{ order.amount }} 元
            </a-select-option>
          </a-select>
        </a-form-model-item>
        <a-form-model-item label="支付方式">
          <a-radio-group v-model="form.method">
            <a-radio value="微信支付">微信支付</a-radio>
            <a-radio value="支付宝">支付宝</a-radio>
            <a-radio value="银行卡">银行卡</a-radio>
          </a-radio-group>
        </a-form-model-item>
        <a-form-model-item label="应付金额">
          <a-input :value="selectedAmount" disabled/>
        </a-form-model-item>
      </a-form-model>
      <div class="actions">
        <a-button type="primary" :loading="paying" @click="submitPayment">立即支付</a-button>
      </div>
    </a-card>

    <a-card title="待支付订单" style="margin-top: 16px">
      <a-table :columns="columns" :data-source="orders" rowKey="orderNo" :pagination="false"/>
    </a-card>
  </div>
</template>

<script>
import {FetchPayableOrders, SubmitPayment} from "@/api/portal";

export default {
  data() {
    return {
      paying: false,
      orders: [],
      form: {
        orderNo: "",
        method: "微信支付"
      },
      columns: [
        {title: "订单号", dataIndex: "orderNo", key: "orderNo"},
        {title: "金额(元)", dataIndex: "amount", key: "amount"},
        {title: "状态", dataIndex: "status", key: "status"},
      ]
    }
  },

  computed: {
    selectedAmount() {
      const order = this.orders.find(item => item.orderNo === this.form.orderNo)
      return order ? order.amount + " 元" : "-"
    }
  },

  mounted() {
    this.loadOrders()
  },

  methods: {
    loadOrders() {
      FetchPayableOrders().then((res) => {
        if (res.status) this.orders = res.data
      })
    },

    submitPayment() {
      if (!this.form.orderNo) {
        this.$message.warning("请选择订单")
        return
      }
      this.paying = true
      SubmitPayment(this.form).then((res) => {
        if (res.status) {
          this.$message.success("支付成功")
          this.loadOrders()
        }
        this.paying = false
      })
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
</style>
