<template>
  <div class="expense-analysis">
    <a-page-header title="消费分析" sub-title="查看您的运输消费数据统计">
      <template slot="extra">
        <a-radio-group v-model="timeRange" button-style="solid" @change="loadData">
          <a-radio-button value="month">本月</a-radio-button>
          <a-radio-button value="quarter">本季度</a-radio-button>
          <a-radio-button value="year">本年</a-radio-button>
          <a-radio-button value="all">全部</a-radio-button>
        </a-radio-group>
      </template>
    </a-page-header>

    <!-- 消费概览 -->
    <a-row :gutter="24" class="stats-row">
      <a-col :span="8">
        <a-card :bordered="false" class="overview-card gradient-blue">
          <div class="overview-content">
            <div class="overview-value">¥{{ formatNumber(stats.totalExpense) }}</div>
            <div class="overview-title">累计消费</div>
            <div class="overview-trend">
              共 {{ stats.orderCount }} 个已支付订单
            </div>
          </div>
          <div class="overview-icon">
            <a-icon type="wallet" />
          </div>
        </a-card>
      </a-col>
      <a-col :span="8">
        <a-card :bordered="false" class="overview-card gradient-green">
          <div class="overview-content">
            <div class="overview-value">¥{{ formatNumber(stats.avgOrderAmount) }}</div>
            <div class="overview-title">平均订单金额</div>
            <div class="overview-trend">
              共 {{ stats.orderCount }} 个订单
            </div>
          </div>
          <div class="overview-icon">
            <a-icon type="shopping" />
          </div>
        </a-card>
      </a-col>
      <a-col :span="8">
        <a-card :bordered="false" class="overview-card gradient-purple">
          <div class="overview-content">
            <div class="overview-value">¥{{ formatNumber(stats.savings) }}</div>
            <div class="overview-title">累计节省</div>
            <div class="overview-trend">
              使用优惠券 {{ stats.couponUsed }} 张
            </div>
          </div>
          <div class="overview-icon">
            <a-icon type="gift" />
          </div>
        </a-card>
      </a-col>
    </a-row>

    <a-row :gutter="24">
      <!-- 消费趋势图 -->
      <a-col :span="16">
        <a-card title="消费趋势" :bordered="false" class="chart-card">
          <a-spin :spinning="loading">
            <div ref="trendChart" class="chart-container"></div>
          </a-spin>
        </a-card>
      </a-col>

      <!-- 消费构成 -->
      <a-col :span="8">
        <a-card title="费用构成" :bordered="false" class="chart-card">
          <a-spin :spinning="loading">
            <div class="expense-breakdown">
              <div v-for="item in expenseBreakdown" :key="item.name" class="breakdown-item">
                <div class="breakdown-header">
                  <span class="breakdown-name">{{ item.name }}</span>
                  <span class="breakdown-percent">{{ item.percent }}%</span>
                </div>
                <a-progress :percent="item.percent" :strokeColor="item.color" :showInfo="false" />
                <div class="breakdown-amount">¥{{ formatNumber(item.amount) }}</div>
              </div>
            </div>
          </a-spin>
        </a-card>
      </a-col>
    </a-row>

    <a-row :gutter="24" style="margin-top: 24px">
      <!-- 消费排行 -->
      <a-col :span="12">
        <a-card title="消费排行（按路线）" :bordered="false" class="chart-card">
          <a-spin :spinning="loading">
            <a-empty v-if="topExpenseRoutes.length === 0" description="暂无消费记录" />
            <a-list v-else :data-source="topExpenseRoutes" size="small">
              <a-list-item slot="renderItem" slot-scope="item, index">
                <a-list-item-meta>
                  <template #avatar>
                    <a-avatar :style="{ background: getAvatarColor(index) }">
                      {{ index + 1 }}
                    </a-avatar>
                  </template>
                  <template #title>{{ item.route }}</template>
                  <template #description>{{ item.count }} 次运输</template>
                </a-list-item-meta>
                <div class="expense-amount">¥{{ formatNumber(item.amount) }}</div>
              </a-list-item>
            </a-list>
          </a-spin>
        </a-card>
      </a-col>

      <!-- 消费账单 -->
      <a-col :span="12">
        <a-card title="消费账单" :bordered="false" class="chart-card">
          <a-spin :spinning="loading">
            <a-empty v-if="recentBills.length === 0" description="暂无账单记录" />
            <a-list v-else :data-source="recentBills" size="small">
              <a-list-item slot="renderItem" slot-scope="item">
                <a-list-item-meta>
                  <template #avatar>
                    <a-avatar :style="{ background: '#1890ff' }">
                      <a-icon type="file-text" />
                    </a-avatar>
                  </template>
                  <template #title>{{ item.orderNo }}</template>
                  <template #description>{{ item.route }} · {{ item.date }}</template>
                </a-list-item-meta>
                <div class="bill-amount">-¥{{ item.amount }}</div>
              </a-list-item>
            </a-list>
          </a-spin>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script>
import { FetchExpenseAnalysis, FetchRecentBills } from '@/api/order'

export default {
  name: 'ExpenseAnalysis',
  data() {
    return {
      timeRange: 'all',
      loading: false,
      stats: {
        totalExpense: 0,
        expenseTrend: 0,
        avgOrderAmount: 0,
        orderCount: 0,
        savings: 0,
        couponUsed: 0
      },
      expenseBreakdown: [],
      topExpenseRoutes: [],
      recentBills: []
    }
  },
  mounted() {
    this.loadData()
  },
  methods: {
    loadData() {
      var self = this
      self.loading = true

      // 并行加载数据
      Promise.all([
        FetchExpenseAnalysis(),
        FetchRecentBills()
      ]).then(function(results) {
        var analysisRes = results[0]
        var billsRes = results[1]

        if (analysisRes.status && analysisRes.data) {
          var data = analysisRes.data
          self.stats = {
            totalExpense: data.totalExpense || 0,
            expenseTrend: data.expenseTrend || 0,
            avgOrderAmount: data.avgOrderAmount || 0,
            orderCount: data.orderCount || 0,
            savings: data.savings || 0,
            couponUsed: data.couponUsed || 0
          }
          self.expenseBreakdown = data.expenseBreakdown || []
          self.topExpenseRoutes = data.topExpenseRoutes || []
        }

        if (billsRes.status && billsRes.data) {
          self.recentBills = billsRes.data
        }

        self.loading = false
        self.$nextTick(function() {
          self.initTrendChart()
        })
      }).catch(function(err) {
        console.error('加载消费分析数据失败', err)
        self.loading = false
        self.$message.error('加载数据失败，请稍后重试')
      })
    },

    initTrendChart() {
      var container = this.$refs.trendChart
      if (!container) return
      
      // 简单的消费趋势图示意
      var chartHtml = '<div style="height: 280px; display: flex; align-items: center; justify-content: center;">'
      if (this.stats.orderCount === 0) {
        chartHtml += '<span style="color: #999;">暂无消费数据</span>'
      } else {
        chartHtml += '<div style="text-align: center;">' +
          '<div style="font-size: 48px; color: #1890ff; font-weight: bold;">¥' + this.formatNumber(this.stats.totalExpense) + '</div>' +
          '<div style="color: #999; margin-top: 12px;">累计消费金额</div>' +
          '<div style="margin-top: 20px; color: #666;">' +
          '<span>共 <strong>' + this.stats.orderCount + '</strong> 个已支付订单</span>' +
          '</div>' +
          '</div>'
      }
      chartHtml += '</div>'
      container.innerHTML = chartHtml
    },

    formatNumber(num) {
      if (!num) return '0'
      return Number(num).toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',')
    },

    getAvatarColor(index) {
      var colors = ['#1890ff', '#52c41a', '#faad14', '#722ed1', '#eb2f96']
      return colors[index] || '#1890ff'
    }
  }
}
</script>

<style scoped>
.expense-analysis {
  padding: 24px;
  background: #f0f2f5;
  min-height: 100vh;
}

.stats-row {
  margin-bottom: 24px;
}

.overview-card {
  border-radius: 16px;
  padding: 24px;
  color: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  overflow: hidden;
}

.gradient-blue {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.gradient-green {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
}

.gradient-purple {
  background: linear-gradient(135deg, #8E2DE2 0%, #4A00E0 100%);
}

.overview-value {
  font-size: 32px;
  font-weight: 700;
}

.overview-title {
  font-size: 14px;
  opacity: 0.9;
  margin-top: 4px;
}

.overview-trend {
  font-size: 12px;
  margin-top: 12px;
  opacity: 0.8;
}

.overview-icon {
  font-size: 64px;
  opacity: 0.3;
}

.chart-card {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}

.chart-container {
  height: 300px;
}

.expense-breakdown {
  padding: 12px 0;
}

.breakdown-item {
  margin-bottom: 20px;
}

.breakdown-item:last-child {
  margin-bottom: 0;
}

.breakdown-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.breakdown-name {
  font-weight: 500;
}

.breakdown-percent {
  color: #1890ff;
  font-weight: 600;
}

.breakdown-amount {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.expense-amount {
  font-weight: 600;
  color: #1890ff;
  font-size: 16px;
}

.bill-amount {
  font-weight: 600;
  color: #f5222d;
  font-size: 14px;
}
</style>
