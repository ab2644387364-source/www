<template>
  <div class="page">
    <a-card title="路线查询">
      <a-form-model :model="form" :label-col="{span: 6}" :wrapper-col="{span: 14}">
        <a-form-model-item label="订单号">
          <a-input v-model="form.orderNo" placeholder="请输入订单号"/>
        </a-form-model-item>
      </a-form-model>
      <div class="actions">
        <a-button type="primary" :loading="loading" @click="queryRoute">查询路线</a-button>
        <a-button style="margin-left: 8px" @click="resetForm">重置</a-button>
      </div>
    </a-card>

    <a-card v-if="distribution" title="路线概览" style="margin-top: 16px">
      <a-steps :current="distribution.status || 0" size="small">
        <a-step title="待调度"/>
        <a-step title="运输中"/>
        <a-step title="已完成"/>
      </a-steps>
      <a-descriptions :column="1" bordered size="small" style="margin-top: 16px">
        <a-descriptions-item label="订单号">{{ distribution.orderNo }}</a-descriptions-item>
        <a-descriptions-item label="起点">{{ distribution.origin }}</a-descriptions-item>
        <a-descriptions-item label="目的地">{{ distribution.destination }}</a-descriptions-item>
        <a-descriptions-item label="当前位置">{{ distribution.currentNode || '-' }}</a-descriptions-item>
        <a-descriptions-item label="状态">
          <a-tag :color="getStatusColor(distribution.status)">{{ getStatusText(distribution.status) }}</a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="异常等级">
          <a-tag :color="getWarningColor(distribution.warningLevel)">{{ getWarningText(distribution.warningLevel) }}</a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="异常说明">{{ distribution.warningNote || '-' }}</a-descriptions-item>
        <a-descriptions-item label="路线规划">{{ distribution.routePlan || '-' }}</a-descriptions-item>
        <a-descriptions-item label="途经节点">{{ distribution.routeNodes || '-' }}</a-descriptions-item>
      </a-descriptions>
    </a-card>

    <a-card v-if="distribution" title="路线时间线" style="margin-top: 16px">
      <a-timeline mode="left" v-if="orderedTrackRecords.length > 0">
        <a-timeline-item
          v-for="record in orderedTrackRecords"
          :key="record.id"
          :color="getTimelineColor(record.status)"
        >
          <p class="timeline-title">{{ record.node }} - {{ record.status }}</p>
          <p class="timeline-desc">{{ record.remark || '-' }}</p>
          <p class="timeline-time">{{ record.createTime }}</p>
        </a-timeline-item>
      </a-timeline>
      <a-timeline mode="left" v-else-if="plannedNodes.length > 0">
        <a-timeline-item
          v-for="(node, index) in plannedNodes"
          :key="node + index"
          :color="getPlannedNodeColor(node)"
        >
          <p class="timeline-title">{{ node }}</p>
          <p class="timeline-desc">计划节点</p>
        </a-timeline-item>
      </a-timeline>
      <a-empty v-else description="暂无路线数据"/>
    </a-card>
  </div>
</template>

<script>
import {QueryRouteByOrder} from "@/api/distribution";

export default {
  data() {
    return {
      loading: false,
      form: {
        orderNo: ""
      },
      distribution: null,
      trackRecords: []
    }
  },

  computed: {
    orderedTrackRecords() {
      if (!this.trackRecords || this.trackRecords.length === 0) {
        return [];
      }
      return [...this.trackRecords].sort((a, b) => {
        const aTime = a.createTime || "";
        const bTime = b.createTime || "";
        return aTime.localeCompare(bTime);
      });
    },
    plannedNodes() {
      if (!this.distribution || !this.distribution.routeNodes) {
        return [];
      }
      return this.distribution.routeNodes.split("-").map(node => node.trim()).filter(Boolean);
    }
  },

  methods: {
    queryRoute() {
      if (!this.form.orderNo) {
        this.$message.warning("请输入订单号");
        return;
      }
      this.loading = true;
      QueryRouteByOrder(this.form.orderNo.trim()).then((res) => {
        if (res.status && res.data) {
          this.distribution = res.data.distribution || null;
          this.trackRecords = res.data.trackRecords || [];
        } else {
          this.distribution = null;
          this.trackRecords = [];
        }
        this.loading = false;
      }).catch(() => {
        this.distribution = null;
        this.trackRecords = [];
        this.loading = false;
      });
    },

    resetForm() {
      this.form = { orderNo: "" };
      this.distribution = null;
      this.trackRecords = [];
    },

    getStatusText(status) {
      const map = {
        0: "待调度",
        1: "运输中",
        2: "已完成"
      };
      return map[status] || "未知";
    },

    getStatusColor(status) {
      const map = {
        0: "orange",
        1: "blue",
        2: "green"
      };
      return map[status] || "default";
    },

    getWarningText(level) {
      const map = {
        0: "正常",
        1: "一般",
        2: "严重"
      };
      return map[level] || "未知";
    },

    getWarningColor(level) {
      const map = {
        0: "green",
        1: "orange",
        2: "red"
      };
      return map[level] || "default";
    },

    getTimelineColor(status) {
      const colors = {
        "到达": "green",
        "离开": "blue",
        "装货": "orange",
        "卸货": "purple",
        "异常": "red"
      };
      return colors[status] || "gray";
    },

    getPlannedNodeColor(node) {
      if (!this.distribution) {
        return "gray";
      }
      if (this.distribution.currentNode && node === this.distribution.currentNode) {
        return "green";
      }
      return "blue";
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

.timeline-title {
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}

.timeline-desc {
  color: #666;
  font-size: 13px;
  margin-bottom: 4px;
}

.timeline-time {
  color: #999;
  font-size: 12px;
}
</style>
