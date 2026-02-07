<template>
  <div class="customer-service-page">
    <a-page-header title="客服中心" sub-title="我们随时为您提供帮助">
    </a-page-header>

    <div class="service-cards">
      <a-row :gutter="24">
        <!-- FAQ 卡片 -->
        <a-col :span="12">
          <a-card hoverable class="service-card faq-card" @click="goTo('/support/faq')">
            <div class="card-content">
              <div class="card-icon">
                <a-icon type="question-circle" :style="{ fontSize: '48px', color: '#1890ff' }" />
              </div>
              <div class="card-info">
                <h3>常见问题</h3>
                <p>查看运输流程、费用计算、订单状态等常见问题解答</p>
              </div>
            </div>
          </a-card>
        </a-col>

        <!-- 投诉建议 卡片 -->
        <a-col :span="12">
          <a-card hoverable class="service-card feedback-card" @click="goTo('/support/feedback')">
            <div class="card-content">
              <div class="card-icon">
                <a-icon type="edit" :style="{ fontSize: '48px', color: '#52c41a' }" />
              </div>
              <div class="card-info">
                <h3>投诉建议</h3>
                <p>提交您的意见反馈，我们会尽快处理并回复</p>
              </div>
            </div>
          </a-card>
        </a-col>

        <!-- 联系我们 卡片 -->
        <a-col :span="12" style="margin-top: 24px">
          <a-card hoverable class="service-card contact-card" @click="goTo('/support/contact')">
            <div class="card-content">
              <div class="card-icon">
                <a-icon type="phone" :style="{ fontSize: '48px', color: '#faad14' }" />
              </div>
              <div class="card-info">
                <h3>联系我们</h3>
                <p>获取客服热线、服务时间和公司联系方式</p>
              </div>
            </div>
          </a-card>
        </a-col>

        <!-- 快速查询 卡片 -->
        <a-col :span="12" style="margin-top: 24px">
          <a-card class="service-card query-card">
            <div class="card-content">
              <div class="card-icon">
                <a-icon type="search" :style="{ fontSize: '48px', color: '#722ed1' }" />
              </div>
              <div class="card-info">
                <h3>快速查询</h3>
                <a-input-search
                  placeholder="输入订单号查询"
                  enter-button="查询"
                  size="large"
                  v-model="orderNumber"
                  @search="onSearch"
                  style="margin-top: 12px"
                />
              </div>
            </div>
          </a-card>
        </a-col>
      </a-row>
    </div>

    <!-- 服务时间提示 -->
    <a-alert
      message="在线客服时间"
      description="周一至周日 08:00 - 20:00，节假日可能有所调整。紧急情况请拨打24小时热线：400-888-8888"
      type="info"
      show-icon
      style="margin-top: 24px"
    />
  </div>
</template>

<script>
export default {
  name: 'CustomerService',
  data() {
    return {
      orderNumber: ''
    }
  },
  methods: {
    goTo(path) {
      this.$router.push(path)
    },
    onSearch(value) {
      if (value) {
        this.$router.push('/user/orders')
        this.$message.info('跳转到订单列表，请在列表中查找')
      } else {
        this.$message.warning('请输入订单号')
      }
    }
  }
}
</script>

<style scoped>
.customer-service-page {
  padding: 24px;
  background: #f0f2f5;
  min-height: 100vh;
}

.service-cards {
  margin-top: 24px;
}

.service-card {
  border-radius: 12px;
  transition: all 0.3s ease;
  cursor: pointer;
}

.service-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.card-content {
  display: flex;
  align-items: flex-start;
  padding: 16px;
}

.card-icon {
  flex-shrink: 0;
  width: 80px;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
  border-radius: 12px;
  margin-right: 20px;
}

.card-info h3 {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 8px;
  color: #262626;
}

.card-info p {
  color: #8c8c8c;
  margin: 0;
  line-height: 1.6;
}

.query-card {
  cursor: default;
}

.query-card:hover {
  transform: none;
}
</style>
