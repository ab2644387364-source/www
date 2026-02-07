<template>
  <div class="page">
    <a-page-header :title="order ? `订单 ${order.orderNo}` : '订单详情'" @back="goBack">
      <template slot="extra">
        <a-tag :color="getStatusColor(order.status)" v-if="order">
          {{ getStatusText(order.status) }}
        </a-tag>
      </template>
    </a-page-header>

    <a-spin :spinning="loading">
      <div v-if="order" class="content">
        <!-- 状态时间线 -->
        <a-card title="订单状态" class="section-card">
          <a-steps :current="getStatusStep(order.status)" size="small">
            <a-step title="待支付" :description="order.createAt" />
            <a-step title="已支付" :description="order.paidAt || '-'" />
            <a-step title="运输中" :description="distribution ? '已派车' : '-'" />
            <a-step title="已完成" />
          </a-steps>
        </a-card>

        <!-- 订单信息 -->
        <a-card title="订单信息" class="section-card">
          <a-descriptions :column="2" bordered size="small">
            <a-descriptions-item label="订单号">{{ order.orderNo }}</a-descriptions-item>
            <a-descriptions-item label="创建时间">{{ order.createAt }}</a-descriptions-item>
            <a-descriptions-item label="货物类型">{{ order.type }}</a-descriptions-item>
            <a-descriptions-item label="数量">{{ order.quantity }}</a-descriptions-item>
            <a-descriptions-item label="订单金额">
              <span class="amount">¥ {{ order.amount }}</span>
            </a-descriptions-item>
            <a-descriptions-item label="支付方式">{{ order.paymentMethod || '-' }}</a-descriptions-item>
            <a-descriptions-item label="提货时间">{{ order.pickupDate || '-' }}</a-descriptions-item>
            <a-descriptions-item label="备注">{{ order.remark || '-' }}</a-descriptions-item>
          </a-descriptions>
        </a-card>

        <!-- 收发信息 -->
        <a-card title="收发信息" class="section-card">
          <a-row :gutter="24">
            <a-col :span="12">
              <div class="address-card">
                <div class="address-title">
                  <a-icon type="environment" style="color: #52c41a" /> 发货地
                </div>
                <div class="address-content">{{ order.origin }}</div>
                <div class="address-contact" v-if="order.fromContact">
                  {{ order.fromContact }} {{ order.fromPhone }}
                </div>
              </div>
            </a-col>
            <a-col :span="12">
              <div class="address-card">
                <div class="address-title">
                  <a-icon type="environment" style="color: #1890ff" /> 收货地
                </div>
                <div class="address-content">{{ order.destination }}</div>
                <div class="address-contact" v-if="order.toContact">
                  {{ order.toContact }} {{ order.toPhone }}
                </div>
              </div>
            </a-col>
          </a-row>
        </a-card>

        <!-- 运输信息（已派车才显示） -->
        <a-card title="运输信息" class="section-card" v-if="distribution && (driver || vehicle)">
          <a-row :gutter="24">
            <a-col :span="12" v-if="driver">
              <div class="transport-item">
                <a-avatar icon="user" size="large" style="background: #1890ff" />
                <div class="transport-info">
                  <div class="transport-label">司机</div>
                  <div class="transport-value">{{ driver.name }}</div>
                  <div class="transport-sub">{{ driver.phone }}</div>
                </div>
              </div>
            </a-col>
            <a-col :span="12" v-if="vehicle">
              <div class="transport-item">
                <a-avatar icon="car" size="large" style="background: #52c41a" />
                <div class="transport-info">
                  <div class="transport-label">车辆</div>
                  <div class="transport-value">{{ vehicle.number }}</div>
                  <div class="transport-sub">{{ vehicle.type }}</div>
                </div>
              </div>
            </a-col>
          </a-row>
          <a-divider v-if="distribution.routePlan" />
          <div v-if="distribution.routePlan" class="route-plan">
            <strong>路线规划：</strong>{{ distribution.routePlan }}
          </div>
          <div v-if="distribution.currentNode" class="current-node">
            <strong>当前位置：</strong>
            <a-tag color="blue">{{ distribution.currentNode }}</a-tag>
          </div>
        </a-card>

        <!-- 物流轨迹 -->
        <a-card title="物流轨迹" class="section-card" v-if="trackRecords.length > 0">
          <a-timeline mode="left">
            <a-timeline-item
              v-for="record in trackRecords"
              :key="record.id"
              :color="getTimelineColor(record.status)"
            >
              <p class="timeline-title">{{ record.node }} - {{ record.status }}</p>
              <p class="timeline-desc">{{ record.remark || '-' }}</p>
              <p class="timeline-time">{{ record.createTime }}</p>
            </a-timeline-item>
          </a-timeline>
        </a-card>

        <!-- 暂无轨迹 -->
        <a-card title="物流轨迹" class="section-card" v-else-if="order.status >= 1">
          <a-empty description="暂无物流信息" />
        </a-card>

        <!-- 操作按钮 - 仅在可操作状态下显示 -->
        <a-card title="订单操作" class="section-card" v-if="canModifyOrder">
          <a-space>
            <a-button type="primary" @click="showRescheduleModal">
              <a-icon type="calendar" /> 改约提货时间
            </a-button>
            <a-button type="danger" @click="showCancelModal">
              <a-icon type="close-circle" /> 取消订单
            </a-button>
          </a-space>
        </a-card>

        <!-- 订单评价 - 仅对已完成订单显示 -->
        <a-card title="订单评价" class="section-card" v-if="order.status === 3">
          <!-- 已评价 -->
          <div v-if="review" class="review-display">
            <div class="review-rating">
              <a-rate :value="review.rating" disabled />
              <span class="rating-text">{{ review.rating }} 分</span>
            </div>
            <div class="review-content" v-if="review.content">
              {{ review.content }}
            </div>
            <div class="review-time">
              评价于 {{ review.createAt }}
            </div>
          </div>
          <!-- 未评价 -->
          <div v-else class="review-form">
            <div class="review-rating">
              <span class="rating-label">服务评分：</span>
              <a-rate v-model="reviewForm.rating" />
            </div>
            <a-textarea
              v-model="reviewForm.content"
              placeholder="请输入您的评价（可选）"
              :rows="3"
              style="margin-top: 12px"
            />
            <a-button
              type="primary"
              :loading="reviewLoading"
              @click="submitReview"
              style="margin-top: 12px"
            >
              提交评价
            </a-button>
          </div>
        </a-card>

        <!-- 取消订单对话框 -->
        <a-modal
          v-model="cancelModalVisible"
          title="取消订单"
          @ok="handleCancelOrder"
          :confirmLoading="cancelLoading"
        >
          <a-form>
            <a-form-item label="取消原因" required>
              <a-textarea
                v-model="cancelReason"
                placeholder="请输入取消原因"
                :rows="4"
              />
            </a-form-item>
          </a-form>
        </a-modal>

        <!-- 改约提货对话框 -->
        <a-modal
          v-model="rescheduleModalVisible"
          title="改约提货时间"
          @ok="handleReschedule"
          :confirmLoading="rescheduleLoading"
        >
          <a-form>
            <a-form-item label="新提货时间" required>
              <a-date-picker
                v-model="newPickupTime"
                show-time
                format="YYYY-MM-DD HH:mm"
                placeholder="选择新的提货时间"
                style="width: 100%"
              />
            </a-form-item>
            <a-form-item label="备注">
              <a-textarea
                v-model="rescheduleRemark"
                placeholder="请输入改约备注（可选）"
                :rows="3"
              />
            </a-form-item>
          </a-form>
        </a-modal>
      </div>

      <div v-else-if="!loading" class="error-state">
        <a-result status="404" title="订单不存在" sub-title="无法找到该订单信息">
          <template slot="extra">
            <a-button type="primary" @click="goBack">返回订单列表</a-button>
          </template>
        </a-result>
      </div>
    </a-spin>
  </div>
</template>

<script>
import { FetchOrderDetail, CancelOrderWithReason, RescheduleOrder } from "@/api/order";
import { getOrderReview, submitReview } from "@/api/review";
import moment from "moment";

export default {
  name: "OrderDetail",
  data() {
    return {
      loading: true,
      order: null,
      distribution: null,
      vehicle: null,
      driver: null,
      trackRecords: [],
      // 取消订单相关
      cancelModalVisible: false,
      cancelLoading: false,
      cancelReason: "",
      // 改约提货相关
      rescheduleModalVisible: false,
      rescheduleLoading: false,
      newPickupTime: null,
      rescheduleRemark: "",
      // 评价相关
      review: null,
      reviewLoading: false,
      reviewForm: {
        rating: 5,
        content: ""
      }
    };
  },

  computed: {
    // 判断订单是否可以修改（取消或改约）
    canModifyOrder() {
      if (!this.order) return false;
      // 只允许待支付(0)和已支付(1)状态
      if (this.order.status !== 0 && this.order.status !== 1) return false;
      // 已分配司机/车辆的订单不能修改
      if (this.distribution && this.isDistributed(this.distribution)) return false;
      return true;
    },
    // 判断配送单是否已分配司机/车辆
    isDistributed() {
      return (dist) => {
        if (!dist) return false;
        // 状态>=1 表示已开始处理
        if (dist.status !== null && dist.status >= 1) return true;
        // 已分配司机
        if (dist.did && dist.did.trim() !== '') return true;
        // 已分配车辆
        if (dist.vid && dist.vid.trim() !== '') return true;
        return false;
      };
    }
  },

  mounted() {
    this.loadDetail();
    this.loadReview();
  },

  methods: {
    loadDetail() {
      const id = this.$route.params.id;
      if (!id) {
        this.loading = false;
        return;
      }

      this.loading = true;
      FetchOrderDetail(id)
        .then(res => {
          if (res.status && res.data) {
            this.order = res.data.order;
            this.distribution = res.data.distribution;
            this.vehicle = res.data.vehicle;
            this.driver = res.data.driver;
            this.trackRecords = res.data.trackRecords || [];
          }
          this.loading = false;
        })
        .catch(() => {
          this.loading = false;
        });
    },

    goBack() {
      this.$router.push("/user/orders");
    },

    getStatusText(status) {
      const map = {
        0: "待支付",
        1: "已支付",
        2: "运输中",
        3: "已完成",
        [-1]: "已取消"
      };
      return map[status] || "未知";
    },

    getStatusColor(status) {
      const map = {
        0: "orange",
        1: "blue",
        2: "cyan",
        3: "green",
        [-1]: "default"
      };
      return map[status] || "default";
    },

    getStatusStep(status) {
      if (status === -1) return -1;
      if (status === 0) return 0;
      if (status === 1) return 1;
      if (status === 2) return 2;
      if (status === 3) return 3;
      return 0;
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

    // 显示取消订单对话框
    showCancelModal() {
      this.cancelReason = "";
      this.cancelModalVisible = true;
    },

    // 处理取消订单
    handleCancelOrder() {
      if (!this.cancelReason.trim()) {
        this.$message.warning("请输入取消原因");
        return;
      }

      this.cancelLoading = true;
      CancelOrderWithReason(this.order.id, this.cancelReason)
        .then(res => {
          if (res.status) {
            this.$message.success("订单已取消");
            this.cancelModalVisible = false;
            this.loadDetail(); // 重新加载订单详情
          } else {
            this.$message.error(res.message || "取消失败");
          }
          this.cancelLoading = false;
        })
        .catch(err => {
          this.$message.error((err.response && err.response.data && err.response.data.message) || "取消失败");
          this.cancelLoading = false;
        });
    },

    // 显示改约对话框
    showRescheduleModal() {
      this.newPickupTime = null;
      this.rescheduleRemark = "";
      this.rescheduleModalVisible = true;
    },

    // 处理改约提货
    handleReschedule() {
      if (!this.newPickupTime) {
        this.$message.warning("请选择新的提货时间");
        return;
      }

      const formattedTime = moment(this.newPickupTime).format("YYYY-MM-DD HH:mm");
      
      this.rescheduleLoading = true;
      RescheduleOrder(this.order.id, formattedTime, this.rescheduleRemark)
        .then(res => {
          if (res.status) {
            this.$message.success("提货时间已更新");
            this.rescheduleModalVisible = false;
            this.loadDetail(); // 重新加载订单详情
          } else {
            this.$message.error(res.message || "改约失败");
          }
          this.rescheduleLoading = false;
        })
        .catch(err => {
          this.$message.error((err.response && err.response.data && err.response.data.message) || "改约失败");
          this.rescheduleLoading = false;
        });
    },

    // 加载订单评价
    loadReview() {
      const id = this.$route.params.id;
      if (!id) return;
      getOrderReview(id).then(res => {
        if (res.data && res.data.hasReview) {
          this.review = res.data.review;
        }
      });
    },

    // 提交评价
    submitReview() {
      if (!this.reviewForm.rating) {
        this.$message.warning("请选择评分");
        return;
      }
      this.reviewLoading = true;
      submitReview({
        orderId: this.order.id,
        rating: this.reviewForm.rating,
        content: this.reviewForm.content
      }).then(res => {
        if (res.data) {
          this.$message.success("评价提交成功");
          this.review = res.data;
        }
        this.reviewLoading = false;
      }).catch(err => {
        this.$message.error((err.response && err.response.data && err.response.data.message) || "评价提交失败");
        this.reviewLoading = false;
      });
    }
  }
};
</script>

<style scoped>
.page {
  padding: 24px;
  background: #f0f2f5;
  min-height: 100vh;
}

.content {
  max-width: 900px;
  margin: 0 auto;
}

.section-card {
  margin-bottom: 16px;
}

.amount {
  font-size: 18px;
  font-weight: bold;
  color: #f5222d;
}

.address-card {
  background: #fafafa;
  padding: 16px;
  border-radius: 8px;
}

.address-title {
  font-weight: 600;
  margin-bottom: 8px;
}

.address-content {
  color: #333;
  font-size: 15px;
  margin-bottom: 4px;
}

.address-contact {
  color: #666;
  font-size: 13px;
}

.transport-item {
  display: flex;
  align-items: center;
  padding: 12px;
  background: #fafafa;
  border-radius: 8px;
}

.transport-info {
  margin-left: 16px;
}

.transport-label {
  color: #999;
  font-size: 12px;
}

.transport-value {
  font-weight: 600;
  font-size: 16px;
  color: #333;
}

.transport-sub {
  color: #666;
  font-size: 13px;
}

.route-plan,
.current-node {
  margin-top: 12px;
  color: #666;
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

.error-state {
  padding: 60px 0;
}

/* 评价样式 */
.review-display {
  background: #f9f9f9;
  padding: 16px;
  border-radius: 8px;
}

.review-rating {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.rating-text {
  margin-left: 12px;
  color: #faad14;
  font-weight: 600;
}

.rating-label {
  margin-right: 8px;
  color: #666;
}

.review-content {
  color: #333;
  line-height: 1.6;
  margin-bottom: 12px;
}

.review-time {
  color: #999;
  font-size: 12px;
}
</style>
