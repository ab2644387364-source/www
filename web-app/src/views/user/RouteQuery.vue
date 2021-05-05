<template>
  <div class="page">
    <a-card title="路线查询">
      <a-form-model :model="form" :label-col="{span: 6}" :wrapper-col="{span: 14}">
        <a-form-model-item label="订单号">
          <a-input v-model="form.orderNo" placeholder="输入订单号查询"/>
        </a-form-model-item>
        <a-form-model-item label="起点">
          <a-input v-model="form.origin" placeholder="输入起点"/>
        </a-form-model-item>
        <a-form-model-item label="终点">
          <a-input v-model="form.destination" placeholder="输入终点"/>
        </a-form-model-item>
      </a-form-model>
      <div class="actions">
        <a-button type="primary" :loading="loading" @click="queryRoute">查询路线</a-button>
        <a-button style="margin-left: 8px" @click="resetForm">重置</a-button>
      </div>
    </a-card>

    <a-card title="路线结果" style="margin-top: 16px">
      <a-table :columns="columns" :data-source="routes" rowKey="node" :pagination="false"/>
    </a-card>
  </div>
</template>

<script>
import {QueryRoutes} from "@/api/portal";

export default {
  data() {
    return {
      loading: false,
      form: {
        orderNo: "",
        origin: "",
        destination: ""
      },
      routes: [],
      columns: [
        {title: "节点", dataIndex: "node", key: "node"},
        {title: "城市", dataIndex: "city", key: "city"},
        {title: "时间", dataIndex: "time", key: "time"},
        {title: "状态", dataIndex: "status", key: "status"},
      ]
    }
  },

  methods: {
    queryRoute() {
      this.loading = true
      QueryRoutes(this.form).then((res) => {
        if (res.status) this.routes = res.data
        this.loading = false
      })
    },

    resetForm() {
      this.form = {orderNo: "", origin: "", destination: ""}
      this.routes = []
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
