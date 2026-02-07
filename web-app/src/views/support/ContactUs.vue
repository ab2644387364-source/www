<template>
  <div class="contact-page">
    <a-page-header title="联系我们" @back="() => $router.push('/support')">
    </a-page-header>

    <div class="contact-content">
      <a-row :gutter="24">
        <!-- 客服热线 -->
        <a-col :span="12">
          <a-card class="contact-card">
            <div class="contact-item">
              <div class="contact-icon phone">
                <a-icon type="phone" :style="{ fontSize: '32px' }" />
              </div>
              <div class="contact-info">
                <h3>客服热线</h3>
                <p class="primary-text">400-888-8888</p>
                <p class="secondary-text">周一至周日 08:00 - 20:00</p>
              </div>
            </div>
          </a-card>
        </a-col>

        <!-- 紧急热线 -->
        <a-col :span="12">
          <a-card class="contact-card">
            <div class="contact-item">
              <div class="contact-icon emergency">
                <a-icon type="alert" :style="{ fontSize: '32px' }" />
              </div>
              <div class="contact-info">
                <h3>紧急热线</h3>
                <p class="primary-text">400-999-9999</p>
                <p class="secondary-text">24小时服务，紧急情况专用</p>
              </div>
            </div>
          </a-card>
        </a-col>

        <!-- 电子邮箱 -->
        <a-col :span="12" style="margin-top: 24px">
          <a-card class="contact-card">
            <div class="contact-item">
              <div class="contact-icon email">
                <a-icon type="mail" :style="{ fontSize: '32px' }" />
              </div>
              <div class="contact-info">
                <h3>电子邮箱</h3>
                <p class="primary-text">2644387364@qq.com</p>
                <p class="secondary-text">一般1-2个工作日内回复</p>
              </div>
            </div>
          </a-card>
        </a-col>

        <!-- 公司地址 -->
        <a-col :span="12" style="margin-top: 24px">
          <a-card class="contact-card">
            <div class="contact-item">
              <div class="contact-icon address">
                <a-icon type="environment" :style="{ fontSize: '32px' }" />
              </div>
              <div class="contact-info">
                <h3>公司地址</h3>
                <p class="primary-text">河南省郑州市惠济区</p>
                <p class="secondary-text">河南牧业经济学院</p>
              </div>
            </div>
          </a-card>
        </a-col>
      </a-row>

      <!-- 服务承诺 -->
      <a-card style="margin-top: 24px" title="服务承诺">
        <a-row :gutter="48">
          <a-col :span="8">
            <div class="promise-item">
              <a-icon type="clock-circle" :style="{ fontSize: '40px', color: '#1890ff' }" />
              <h4>快速响应</h4>
              <p>客服热线1分钟内接通，问题咨询24小时内回复</p>
            </div>
          </a-col>
          <a-col :span="8">
            <div class="promise-item">
              <a-icon type="safety-certificate" :style="{ fontSize: '40px', color: '#52c41a' }" />
              <h4>安全保障</h4>
              <p>全程GPS跟踪，运输保险全覆盖，确保牲畜安全</p>
            </div>
          </a-col>
          <a-col :span="8">
            <div class="promise-item">
              <a-icon type="like" :style="{ fontSize: '40px', color: '#faad14' }" />
              <h4>专业服务</h4>
              <p>专业押运团队，科学运输方案，贴心售后服务</p>
            </div>
          </a-col>
        </a-row>
      </a-card>

      <!-- 工作时间 -->
      <a-card style="margin-top: 24px" title="服务时间">
        <a-descriptions :column="2" bordered>
          <a-descriptions-item label="普通客服">
            周一至周日 08:00 - 20:00
          </a-descriptions-item>
          <a-descriptions-item label="紧急热线">
            24小时全天候服务
          </a-descriptions-item>
          <a-descriptions-item label="在线反馈">
            随时提交，工作日处理
          </a-descriptions-item>
          <a-descriptions-item label="邮件回复">
            1-2个工作日
          </a-descriptions-item>
        </a-descriptions>
      </a-card>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ContactUs'
}
</script>

<style scoped>
.contact-page {
  padding: 24px;
  background: #f0f2f5;
  min-height: 100vh;
}

.contact-content {
  margin-top: 16px;
}

.contact-card {
  border-radius: 12px;
  transition: all 0.3s ease;
}

.contact-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.contact-item {
  display: flex;
  align-items: center;
  padding: 12px;
}

.contact-icon {
  width: 72px;
  height: 72px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20px;
  color: #fff;
}

.contact-icon.phone {
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
}

.contact-icon.emergency {
  background: linear-gradient(135deg, #ff4d4f 0%, #cf1322 100%);
}

.contact-icon.email {
  background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%);
}

.contact-icon.address {
  background: linear-gradient(135deg, #faad14 0%, #d48806 100%);
}

.contact-info h3 {
  margin: 0 0 8px;
  font-size: 16px;
  color: #262626;
}

.primary-text {
  font-size: 18px;
  font-weight: 600;
  color: #262626;
  margin: 0 0 4px;
}

.secondary-text {
  font-size: 13px;
  color: #8c8c8c;
  margin: 0;
}

.promise-item {
  text-align: center;
  padding: 24px 16px;
}

.promise-item h4 {
  margin: 16px 0 8px;
  font-size: 16px;
  color: #262626;
}

.promise-item p {
  color: #8c8c8c;
  margin: 0;
  line-height: 1.6;
}
</style>
