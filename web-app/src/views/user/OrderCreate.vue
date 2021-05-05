<template>
  <div class="page">
    <a-card title="创建订单" :loading="loading">
      <a-form-model :model="form" :label-col="{span: 6}" :wrapper-col="{span: 14}">
        <a-form-model-item label="起点">
          <a-input v-model="form.origin" placeholder="请输入起点"/>
        </a-form-model-item>
        <a-form-model-item label="终点">
          <a-input v-model="form.destination" placeholder="请输入终点"/>
        </a-form-model-item>
        <a-form-model-item label="货物类型">
          <a-select v-model="form.type" placeholder="请选择类型">
            <a-select-option value="牛">牛</a-select-option>
            <a-select-option value="羊">羊</a-select-option>
            <a-select-option value="猪">猪</a-select-option>
          </a-select>
        </a-form-model-item>
        <a-form-model-item label="数量">
          <a-input-number v-model="form.quantity" :min="1" style="width: 100%"/>
        </a-form-model-item>
        <a-form-model-item label="提货时间">
          <a-date-picker v-model="form.pickupDate" style="width: 100%"/>
        </a-form-model-item>
        <a-form-model-item label="备注">
          <a-textarea v-model="form.remark" :rows="3" placeholder="补充说明"/>
        </a-form-model-item>
      </a-form-model>
      <div class="actions">
        <a-button type="primary" :loading="creating" @click="createOrder">提交订单</a-button>
        <a-button style="margin-left: 8px" @click="resetForm">重置</a-button>
      </div>
    </a-card>

    <a-card title="订单预览" style="margin-top: 16px">
      <div v-if="orderResult">
        <p>订单号：{{ orderResult.orderNo }}</p>
        <p>预估费用：{{ orderResult.amount }} 元</p>
        <p>状态：{{ orderResult.status }}</p>
      </div>
      <div v-else class="empty-tip">提交订单后展示预览信息</div>
    </a-card>

    <a-card title="最近订单" style="margin-top: 16px">
      <a-table :columns="columns" :data-source="recentOrders" rowKey="orderNo" :pagination="false"/>
    </a-card>
  </div>
</template>

<script>
import {CreateOrder, FetchRecentOrders} from "@/api/portal";

export default {
  data() {
    return {
      loading: false,
      creating: false,
      form: {
        origin: "",
        destination: "",
        type: "",
        quantity: 1,
        pickupDate: null,
        remark: ""
      },
      orderResult: null,
      recentOrders: [],
      columns: [
        {title: "订单号", dataIndex: "orderNo", key: "orderNo"},
        {title: "起点", dataIndex: "origin", key: "origin"},
        {title: "终点", dataIndex: "destination", key: "destination"},
        {title: "状态", dataIndex: "status", key: "status"},
        {title: "金额(元)", dataIndex: "amount", key: "amount"},
      ]
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
      })
    },

    createOrder() {
      this.creating = true
      CreateOrder(this.form).then((res) => {
        if (res.status) {
          this.orderResult = res.data
          this.$message.success("订单创建成功")
          this.loadRecentOrders()
        }
        this.creating = false
      })
    },

    resetForm() {
      this.form = {
        origin: "",
        destination: "",
        type: "",
        quantity: 1,
        pickupDate: null,
        remark: ""
      }
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

.empty-tip {
  color: #8c8c8c;
}
</style>
