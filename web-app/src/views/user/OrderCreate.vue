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
          <a-date-picker v-model="form.pickupDate" style="width: 100%" valueFormat="YYYY-MM-DD"/>
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
      <div v-if="orderResult" class="order-result">
        <a-descriptions :column="1" bordered size="small">
          <a-descriptions-item label="订单号">{{ orderResult.orderNo }}</a-descriptions-item>
          <a-descriptions-item label="预估费用">
            <span class="amount">¥ {{ orderResult.amount }}</span>
          </a-descriptions-item>
          <a-descriptions-item label="状态">
            <a-tag color="orange">{{ getStatusText(orderResult.status) }}</a-tag>
          </a-descriptions-item>
        </a-descriptions>
        <div class="order-actions">
          <a-button type="primary" @click="goToPayment">立即支付</a-button>
        </div>
      </div>
      <div v-else class="empty-tip">提交订单后展示预览信息</div>
    </a-card>

    <a-card title="最近订单" style="margin-top: 16px">
      <a-table :columns="columns" :data-source="recentOrders" rowKey="id" :pagination="false">
        <template slot="status" slot-scope="status">
          <a-tag :color="getStatusColor(status)">{{ getStatusText(status) }}</a-tag>
        </template>
        <template slot="amount" slot-scope="amount">
          ¥ {{ amount }}
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script>
import {CreateOrder, FetchRecentOrders} from "@/api/order";

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
        {title: "状态", dataIndex: "status", key: "status", scopedSlots: {customRender: "status"}},
        {title: "金额", dataIndex: "amount", key: "amount", scopedSlots: {customRender: "amount"}},
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
      }).catch(() => {
        this.loading = false
      })
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
.page {
  padding: 24px;
}

.actions {
  text-align: right;
}

.empty-tip {
  color: #8c8c8c;
}

.order-result {
  padding: 8px 0;
}

.amount {
  font-size: 18px;
  font-weight: bold;
  color: #f5222d;
}

.order-actions {
  margin-top: 16px;
  text-align: right;
}
</style>
