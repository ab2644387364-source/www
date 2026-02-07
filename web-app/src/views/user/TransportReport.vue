<template>
  <div class="transport-report">
    <a-page-header title="运输报告" sub-title="查看您的历史运输数据分析">
      <template slot="extra">
        <a-range-picker v-model="dateRange" style="width: 250px" @change="loadData" />
        <a-button type="primary" icon="download" style="margin-left: 12px" @click="exportReport">
          导出报告
        </a-button>
      </template>
    </a-page-header>

    <!-- 统计概览 -->
    <a-row :gutter="24" class="stats-row">
      <a-col :span="6">
        <a-card :bordered="false" class="stat-card">
          <a-statistic title="运输订单总数" :value="stats.totalOrders" suffix="单">
            <template #prefix><a-icon type="file-done" /></template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card :bordered="false" class="stat-card">
          <a-statistic title="运输总里程" :value="stats.totalDistance" suffix="公里">
            <template #prefix><a-icon type="car" /></template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card :bordered="false" class="stat-card">
          <a-statistic title="运输牲畜头数" :value="stats.totalLivestock" suffix="头">
            <template #prefix><a-icon type="heart" /></template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card :bordered="false" class="stat-card">
          <a-statistic title="累计消费" :value="stats.totalAmount" prefix="¥">
            <template #prefix><a-icon type="dollar" /></template>
          </a-statistic>
        </a-card>
      </a-col>
    </a-row>

    <a-row :gutter="24">
      <!-- 月度订单趋势 -->
      <a-col :span="12">
        <a-card title="月度订单趋势" :bordered="false" class="chart-card">
          <a-spin :spinning="loading">
            <div ref="orderChart" class="chart-container"></div>
          </a-spin>
        </a-card>
      </a-col>

      <!-- 常用路线 TOP5 -->
      <a-col :span="12">
        <a-card title="常用运输路线 TOP5" :bordered="false" class="chart-card">
          <a-spin :spinning="loading">
            <a-empty v-if="topRoutes.length === 0" description="暂无运输记录" />
            <div v-else class="route-list">
              <div v-for="(route, index) in topRoutes" :key="index" class="route-item">
                <div class="route-rank" :class="'rank-' + (index + 1)">{{ index + 1 }}</div>
                <div class="route-info">
                  <div class="route-name">{{ route.origin }} → {{ route.destination }}</div>
                  <a-progress :percent="route.percent" :showInfo="false" size="small" />
                </div>
                <div class="route-count">{{ route.count }} 单</div>
              </div>
            </div>
          </a-spin>
        </a-card>
      </a-col>
    </a-row>

    <a-row :gutter="24" style="margin-top: 24px">
      <!-- 货物类型分布 -->
      <a-col :span="12">
        <a-card title="货物类型分布" :bordered="false" class="chart-card">
          <a-spin :spinning="loading">
            <a-empty v-if="typeDistribution.length === 0" description="暂无货物数据" />
            <div v-else ref="typeChart" class="chart-container"></div>
          </a-spin>
        </a-card>
      </a-col>

      <!-- 月度消费趋势 -->
      <a-col :span="12">
        <a-card title="月度消费趋势" :bordered="false" class="chart-card">
          <a-spin :spinning="loading">
            <div ref="amountChart" class="chart-container"></div>
          </a-spin>
        </a-card>
      </a-col>
    </a-row>

    <!-- 运输记录表格 -->
    <a-card title="运输记录明细" :bordered="false" style="margin-top: 24px">
      <a-table
        :columns="columns"
        :data-source="records"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
        rowKey="id"
      >
        <template slot="status" slot-scope="text">
          <a-tag :color="getStatusColor(text)">{{ getStatusText(text) }}</a-tag>
        </template>
        <template slot="amount" slot-scope="text">
          ¥{{ text }}
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script>
import { FetchOrders, FetchTransportReport } from '@/api/order'
import * as echarts from 'echarts'

export default {
  name: 'TransportReport',
  data() {
    return {
      dateRange: [],
      loading: false,
      stats: {
        totalOrders: 0,
        totalDistance: 0,
        totalLivestock: 0,
        totalAmount: 0
      },
      topRoutes: [],
      typeDistribution: [],
      monthlyTrend: [],
      records: [],
      pagination: {
        current: 1,
        pageSize: 10,
        total: 0
      },
      amountChart: null,
      orderChart: null,
      columns: [
        { title: '订单编号', dataIndex: 'orderNo', width: 160 },
        { title: '起点', dataIndex: 'origin', width: 100 },
        { title: '目的地', dataIndex: 'destination', width: 100 },
        { title: '货物类型', dataIndex: 'type', width: 80 },
        { title: '数量', dataIndex: 'quantity', width: 80 },
        { title: '金额', dataIndex: 'amount', scopedSlots: { customRender: 'amount' }, width: 100 },
        { title: '状态', dataIndex: 'status', scopedSlots: { customRender: 'status' }, width: 90 },
        { title: '创建时间', dataIndex: 'createAt', width: 150 }
      ]
    }
  },
  mounted() {
    this.loadData()
    window.addEventListener('resize', this.handleResize)
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.handleResize)
    if (this.amountChart) {
      this.amountChart.dispose()
      this.amountChart = null
    }
    if (this.orderChart) {
      this.orderChart.dispose()
      this.orderChart = null
    }
  },
  methods: {
    loadData() {
      var self = this
      self.loading = true

      // 并行加载数据
      Promise.all([
        FetchTransportReport(),
        FetchOrders()
      ]).then(function(results) {
        var reportRes = results[0]
        var ordersRes = results[1]

        if (reportRes.status && reportRes.data) {
          var data = reportRes.data
          self.stats = {
            totalOrders: data.totalOrders || 0,
            totalDistance: data.totalDistance || 0,
            totalLivestock: data.totalLivestock || 0,
            totalAmount: Math.round(data.totalAmount || 0)
          }
          self.topRoutes = data.topRoutes || []
          self.typeDistribution = data.typeDistribution || []
          self.monthlyTrend = data.monthlyTrend || []
        }

        if (ordersRes.status && ordersRes.data) {
          self.records = ordersRes.data
          self.pagination.total = ordersRes.data.length
        }

        self.loading = false
        self.$nextTick(function() {
          self.initCharts()
        })
      }).catch(function(err) {
        console.error('加载运输报告数据失败', err)
        self.loading = false
        self.$message.error('加载数据失败，请稍后重试')
      })
    },
    
    initCharts() {
      this.initOrderChart()
      this.initTypeChart()
      this.initAmountChart()
    },

    initOrderChart() {
      var container = this.$refs.orderChart
      if (!container) return
      
      // Clean up existing instance
      if (this.orderChart) {
        this.orderChart.dispose()
      }

      var trend = this.monthlyTrend
      if (!trend || trend.length === 0) {
        container.innerHTML = '<div style="height: 200px; display: flex; align-items: center; justify-content: center; color: #999;">暂无订单数据</div>'
        return
      }

      // Clear container content
      container.innerHTML = ''
      
      this.orderChart = echarts.init(container)

      var months = trend.map(function(t) { return t.month.replace(/^0/, '') })
      var counts = trend.map(function(t) { return t.orderCount })
      var maxCount = Math.max.apply(null, counts) || 5 
      // Ensure yAxis has enough headroom for labels

      var option = {
        tooltip: {
          trigger: 'axis',
          formatter: '{b}<br />订单数: {c}单',
          backgroundColor: 'rgba(255, 255, 255, 0.9)',
          textStyle: { color: '#333' },
          borderColor: '#ddd',
          borderWidth: 1,
          padding: [8, 12]
        },
        grid: {
          top: '15%',
          left: '2%',
          right: '3%',
          bottom: '5%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: months,
          axisLine: { show: false },
          axisTick: { show: false },
          axisLabel: { color: '#999', fontSize: 11, margin: 12 }
        },
        yAxis: {
          type: 'value',
          minInterval: 1, // Ensure integers
          splitLine: { 
            lineStyle: { type: 'dashed', color: '#f0f0f0' }
          },
          axisLabel: { show: false }
        },
        series: [{
          name: '订单数',
          type: 'bar',
          barWidth: '20%', // Thinner bars for elegance
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{
              offset: 0,
              color: '#1890ff'
            }, {
              offset: 1,
              color: '#69c0ff'
            }]),
            borderRadius: [4, 4, 0, 0] // Rounded top corners
          },
          label: {
            show: true,
            position: 'top',
            color: '#1890ff',
            fontSize: 12,
            formatter: function(params) {
              return params.value > 0 ? params.value : '';
            }
          },
          data: counts,
          emphasis: {
            itemStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{
                offset: 0,
                color: '#40a9ff'
              }, {
                offset: 1,
                color: '#91d5ff'
              }])
            }
          }
        }]
      }
      
      this.orderChart.setOption(option)
    },

    initTypeChart() {
      var container = this.$refs.typeChart
      if (!container) return

      var types = this.typeDistribution
      if (!types || types.length === 0) {
        container.innerHTML = '<div style="height: 200px; display: flex; align-items: center; justify-content: center; color: #999;">暂无货物数据</div>'
        return
      }

      var legendHtml = types.map(function(item) {
        return '<div style="margin-bottom: 8px;">' +
          '<span style="display: inline-block; width: 12px; height: 12px; background: ' + item.color + '; margin-right: 8px; border-radius: 2px;"></span>' +
          item.type + ' ' + item.percent + '%' +
          '</div>'
      }).join('')

      // 生成饼图渐变
      var gradientParts = []
      var currentPercent = 0
      for (var i = 0; i < types.length; i++) {
        var nextPercent = currentPercent + types[i].percent
        gradientParts.push(types[i].color + ' ' + currentPercent + '% ' + nextPercent + '%')
        currentPercent = nextPercent
      }

      container.innerHTML = '<div style="display: flex; justify-content: center; align-items: center; height: 200px;">' +
        '<div style="width: 150px; height: 150px; border-radius: 50%; background: conic-gradient(' + gradientParts.join(', ') + '); position: relative;">' +
        '<div style="position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); width: 80px; height: 80px; background: #fff; border-radius: 50%;"></div>' +
        '</div>' +
        '<div style="margin-left: 24px;">' + legendHtml + '</div>' +
        '</div>'
    },

    initAmountChart() {
      var container = this.$refs.amountChart
      if (!container) return

      // Clean up existing instance
      if (this.amountChart) {
        this.amountChart.dispose()
      }

      var trend = this.monthlyTrend
      if (!trend || trend.length === 0) {
        container.innerHTML = '<div style="height: 200px; display: flex; align-items: center; justify-content: center; color: #999;">暂无消费数据</div>'
        return
      }
      
      // Clear container content (remove previous error/empty state HTML)
      container.innerHTML = ''
      
      this.amountChart = echarts.init(container)

      var months = trend.map(function(t) { return t.month.replace(/^0/, '') })
      var amounts = trend.map(function(t) { return t.amount })

      var option = {
        tooltip: {
          trigger: 'axis',
          formatter: '{b}<br />消费金额: ¥{c}',
          backgroundColor: 'rgba(255, 255, 255, 0.9)',
          textStyle: { color: '#333' },
          borderColor: '#ddd',
          borderWidth: 1,
          padding: [8, 12]
        },
        grid: {
          top: '15%',
          left: '2%',
          right: '3%',
          bottom: '5%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          boundaryGap: false,
          data: months,
          axisLine: { show: false },
          axisTick: { show: false },
          axisLabel: { color: '#999', fontSize: 11, margin: 12 }
        },
        yAxis: {
          type: 'value',
          splitLine: { 
            lineStyle: { type: 'dashed', color: '#f0f0f0' }
          },
          axisLabel: { show: false }
        },
        series: [{
          name: '消费金额',
          type: 'line',
          smooth: true, // Smooth curve
          symbol: 'circle',
          symbolSize: 6,
          showSymbol: false, 
          itemStyle: {
            color: '#52c41a',
            borderColor: '#fff',
            borderWidth: 2
          },
          lineStyle: {
            color: '#52c41a',
            width: 3,
            shadowColor: 'rgba(82, 196, 26, 0.3)',
            shadowBlur: 10,
            shadowOffsetY: 5
          },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{
              offset: 0,
              color: 'rgba(82, 196, 26, 0.3)'
            }, {
              offset: 1,
              color: 'rgba(82, 196, 26, 0.02)'
            }])
          },
          data: amounts,
          emphasis: {
            focus: 'series',
            scale: true
          }
        }]
      }
      
      this.amountChart.setOption(option)
    },

    handleResize() {
      if (this.amountChart) {
        this.amountChart.resize()
      }
      if (this.orderChart) {
        this.orderChart.resize()
      }
    },

    handleTableChange(pagination) {
      this.pagination.current = pagination.current
    },

    exportReport() {
      this.$message.success('报告导出中，请稍候...')
    },

    getStatusText(status) {
      var map = { 0: '待支付', 1: '已支付', 2: '待配送', 3: '运输中', 4: '已完成', [-1]: '已取消' }
      return map[status] || '未知'
    },

    getStatusColor(status) {
      var map = { 0: 'orange', 1: 'blue', 2: 'cyan', 3: 'geekblue', 4: 'green', [-1]: 'red' }
      return map[status] || 'default'
    }
  }
}
</script>

<style scoped>
.transport-report {
  padding: 24px;
  background: #f0f2f5;
  min-height: 100vh;
}

.stats-row {
  margin-bottom: 24px;
}

.stat-card {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}

.chart-card {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}

.chart-container {
  height: 250px;
}

.route-list {
  padding: 12px 0;
}

.route-item {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.route-item:last-child {
  border-bottom: none;
}

.route-rank {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 14px;
  margin-right: 12px;
}

.route-rank.rank-1 {
  background: linear-gradient(135deg, #ffd700, #ffb700);
  color: #fff;
}

.route-rank.rank-2 {
  background: linear-gradient(135deg, #c0c0c0, #a0a0a0);
  color: #fff;
}

.route-rank.rank-3 {
  background: linear-gradient(135deg, #cd7f32, #b5651d);
  color: #fff;
}

.route-info {
  flex: 1;
}

.route-name {
  font-weight: 500;
  margin-bottom: 4px;
}

.route-count {
  font-weight: 600;
  color: #1890ff;
  margin-left: 12px;
}
</style>
