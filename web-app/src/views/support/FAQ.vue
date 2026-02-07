<template>
  <div class="faq-page">
    <a-page-header title="常见问题" @back="() => $router.push('/support')">
    </a-page-header>

    <div class="faq-content">
      <!-- 分类标签 -->
      <a-radio-group v-model="activeCategory" button-style="solid" style="margin-bottom: 24px">
        <a-radio-button v-for="cat in categories" :key="cat.key" :value="cat.key">
          <a-icon :type="cat.icon" /> {{ cat.name }}
        </a-radio-button>
      </a-radio-group>

      <!-- 问题列表 -->
      <a-collapse v-model="activeKey" accordion>
        <a-collapse-panel 
          v-for="(item, index) in filteredFAQs" 
          :key="String(index)"
          :header="item.question"
        >
          <p class="answer-text">{{ item.answer }}</p>
        </a-collapse-panel>
      </a-collapse>

      <a-empty v-if="filteredFAQs.length === 0" description="暂无相关问题" />
    </div>

    <!-- 没找到答案提示 -->
    <a-card class="help-card" style="margin-top: 24px">
      <div class="help-content">
        <a-icon type="customer-service" :style="{ fontSize: '32px', color: '#1890ff' }" />
        <div class="help-text">
          <h4>没有找到您需要的答案？</h4>
          <p>您可以联系我们的客服团队，或者直接提交反馈</p>
        </div>
        <div class="help-actions">
          <a-button type="primary" @click="$router.push('/support/feedback')">提交反馈</a-button>
          <a-button style="margin-left: 12px" @click="$router.push('/support/contact')">联系客服</a-button>
        </div>
      </div>
    </a-card>
  </div>
</template>

<script>
export default {
  name: 'FAQ',
  data() {
    return {
      activeCategory: 'all',
      activeKey: [],
      categories: [
        { key: 'all', name: '全部', icon: 'appstore' },
        { key: 'transport', name: '运输流程', icon: 'car' },
        { key: 'fee', name: '费用说明', icon: 'pay-circle' },
        { key: 'order', name: '订单问题', icon: 'shopping-cart' },
        { key: 'livestock', name: '牲畜须知', icon: 'home' }
      ],
      faqList: [
        // 运输流程
        { category: 'transport', question: '如何创建运输订单？', answer: '登录系统后，进入"订单服务" > "创建订单"，填写发货地、目的地、牲畜信息等资料，提交后等待审核即可。' },
        { category: 'transport', question: '订单审核需要多长时间？', answer: '一般情况下，订单会在提交后2-4小时内完成审核。如遇高峰期可能会稍有延迟，您可以在"我的订单"中查看审核状态。' },
        { category: 'transport', question: '如何查看运输进度？', answer: '在"我的订单"页面点击订单详情，可以查看实时物流轨迹和预计到达时间。' },
        { category: 'transport', question: '运输途中可以更改目的地吗？', answer: '运输途中一般不支持更改目的地。如有特殊情况，请及时联系客服热线协商处理。' },
        // 费用说明
        { category: 'fee', question: '运费是如何计算的？', answer: '运费根据运输距离、牲畜数量、牲畜类型和重量综合计算。您可以使用"运费估算"功能提前了解预估费用。' },
        { category: 'fee', question: '支持哪些支付方式？', answer: '目前支持微信支付、支付宝和银行转账等多种支付方式。' },
        { category: 'fee', question: '订单取消后如何退款？', answer: '未发车的订单取消后，费用将在1-3个工作日内原路退回。已发车订单需要另行协商处理。' },
        // 订单问题
        { category: 'order', question: '如何取消订单？', answer: '在订单详情页面点击"取消订单"按钮，填写取消原因后提交即可。注意：只有待审核和已审核（未发车）状态的订单可以取消。' },
        { category: 'order', question: '如何修改订单信息？', answer: '订单创建后，只有在待审核状态下可以修改。已审核或已发车的订单无法直接修改，请联系客服处理。' },
        { category: 'order', question: '订单支付失败怎么办？', answer: '请检查账户余额是否充足，网络是否正常。如问题持续，可以尝试更换支付方式或联系客服。' },
        // 牲畜须知
        { category: 'livestock', question: '运输牲畜需要准备什么证件？', answer: '需要准备动物检疫合格证明、产地检疫证明等相关证件。具体要求请咨询当地畜牧部门。' },
        { category: 'livestock', question: '运输过程中如何保障牲畜安全？', answer: '我们配备专业的运输车辆，车内设有通风、饮水设施，并有专业押运员全程监护。运输途中会在指定中转站进行休息和检查。' },
        { category: 'livestock', question: '牲畜在运输中受伤如何处理？', answer: '我们提供运输保险服务。如在运输过程中发生意外，请立即联系客服并保留相关证据，我们将按照保险条款进行理赔。' }
      ]
    }
  },
  computed: {
    filteredFAQs() {
      if (this.activeCategory === 'all') {
        return this.faqList
      }
      return this.faqList.filter(item => item.category === this.activeCategory)
    }
  }
}
</script>

<style scoped>
.faq-page {
  padding: 24px;
  background: #fff;
  min-height: 100vh;
}

.faq-content {
  margin-top: 16px;
}

.answer-text {
  color: #595959;
  line-height: 1.8;
  margin: 0;
}

.help-card {
  background: linear-gradient(135deg, #e6f7ff 0%, #f0f5ff 100%);
  border: none;
}

.help-content {
  display: flex;
  align-items: center;
  gap: 20px;
}

.help-text h4 {
  margin: 0 0 4px;
  font-size: 16px;
  color: #262626;
}

.help-text p {
  margin: 0;
  color: #8c8c8c;
}

.help-actions {
  margin-left: auto;
}
</style>
