<template>
  <div class="dashboard">
    <!-- 欢迎横幅 -->
    <div class="welcome-banner">
      <div class="welcome-content">
        <h1>欢迎回来，管理员</h1>
        <p>以下是系统运营数据概览，祝您工作愉快！</p>
      </div>
      <div class="welcome-time">
        <a-icon type="clock-circle" />
        <span>{{ currentTime }}</span>
      </div>
    </div>

    <!-- 统计卡片区域 -->
    <a-row :gutter="24" class="stat-cards">
      <a-col :span="6">
        <div class="stat-card gradient-blue">
          <div class="stat-icon-bg">
            <a-icon type="appstore" />
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ overview.commodityCount || 0 }}</div>
            <div class="stat-title">牲畜种类</div>
          </div>
        </div>
      </a-col>
      <a-col :span="6">
        <div class="stat-card gradient-green">
          <div class="stat-icon-bg">
            <a-icon type="car" />
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ overview.distributionCount || 0 }}</div>
            <div class="stat-title">配送订单</div>
          </div>
        </div>
      </a-col>
      <a-col :span="6">
        <div class="stat-card gradient-orange">
          <div class="stat-icon-bg">
            <a-icon type="sync" :spin="overview.transportingCount > 0" />
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ overview.transportingCount || 0 }}</div>
            <div class="stat-title">运输中</div>
          </div>
        </div>
      </a-col>
      <a-col :span="6">
        <div class="stat-card gradient-purple">
          <div class="stat-icon-bg">
            <a-icon type="home" />
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ overview.warehouseCount || 0 }}</div>
            <div class="stat-title">仓库数量</div>
          </div>
        </div>
      </a-col>
    </a-row>

    <!-- 运输状态快速指标 -->
    <div class="quick-stats">
      <div class="quick-stat-item">
        <span class="dot dot-orange"></span>
        <span class="label">待调度</span>
        <span class="value">{{ transport.pending || 0 }}</span>
      </div>
      <div class="quick-stat-item">
        <span class="dot dot-green"></span>
        <span class="label">运输中</span>
        <span class="value">{{ transport.transporting || 0 }}</span>
      </div>
      <div class="quick-stat-item">
        <span class="dot dot-blue"></span>
        <span class="label">已完成</span>
        <span class="value">{{ transport.completed || 0 }}</span>
      </div>
      <div class="quick-stat-item">
        <span class="dot dot-red"></span>
        <span class="label">异常</span>
        <span class="value">{{ transport.warning || 0 }}</span>
      </div>
    </div>

    <!-- 图表区域 -->
    <a-row :gutter="24" class="chart-row">
      <a-col :span="12">
        <a-card :bordered="false" class="chart-card">
          <template slot="title">
            <div class="card-title">
              <a-icon type="pie-chart" />
              <span>运输状态分布</span>
            </div>
          </template>
          <div ref="pieChart" class="chart-container"></div>
        </a-card>
      </a-col>
      <a-col :span="12">
        <a-card :bordered="false" class="chart-card">
          <template slot="title">
            <div class="card-title">
              <a-icon type="line-chart" />
              <span>近7天运输趋势</span>
            </div>
          </template>
          <div ref="lineChart" class="chart-container"></div>
        </a-card>
      </a-col>
    </a-row>

    <!-- 最近动态 -->
    <a-card :bordered="false" class="activity-card">
      <template slot="title">
        <div class="card-title">
          <a-icon type="history" />
          <span>最新运输动态</span>
        </div>
      </template>
      <template slot="extra">
        <a-button type="link" @click="$router.push('/delivery/list')">查看全部</a-button>
      </template>
      <a-table
        :columns="activityColumns"
        :data-source="activities"
        :pagination="false"
        :loading="loading"
        rowKey="id"
        size="middle"
      >
        <span slot="status" slot-scope="status">
          <a-tag v-if="status===0" color="orange">待调度</a-tag>
          <a-tag v-if="status===1" color="green">运输中</a-tag>
          <a-tag v-if="status===2" color="blue">已完成</a-tag>
        </span>
        <span slot="warningLevel" slot-scope="warningLevel">
          <a-badge v-if="warningLevel===2" status="error" text="严重" />
          <a-badge v-else-if="warningLevel===1" status="warning" text="一般" />
          <a-badge v-else status="success" text="正常" />
        </span>
      </a-table>
    </a-card>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import { GetDashboard } from '../../api/statistics'

var activityColumns = [
  { title: '押运员', dataIndex: 'driver', width: 100 },
  { title: '车牌号', dataIndex: 'number', width: 100 },
  { title: '起点', dataIndex: 'origin' },
  { title: '目的地', dataIndex: 'destination' },
  { title: '当前位置', dataIndex: 'currentNode' },
  { title: '状态', dataIndex: 'status', scopedSlots: { customRender: 'status' }, width: 100 },
  { title: '异常', dataIndex: 'warningLevel', scopedSlots: { customRender: 'warningLevel' }, width: 100 }
]

export default {
  name: 'Dashboard',
  data: function() {
    return {
      loading: true,
      overview: {},
      transport: {},
      trend: [],
      activities: [],
      activityColumns: activityColumns,
      pieChart: null,
      lineChart: null,
      currentTime: ''
    }
  },
  mounted: function() {
    this.loadData()
    this.updateTime()
    this.timeInterval = setInterval(this.updateTime, 1000)
    window.addEventListener('resize', this.handleResize)
  },
  beforeDestroy: function() {
    window.removeEventListener('resize', this.handleResize)
    if (this.pieChart) this.pieChart.dispose()
    if (this.lineChart) this.lineChart.dispose()
    if (this.timeInterval) clearInterval(this.timeInterval)
  },
  methods: {
    updateTime: function() {
      var now = new Date()
      this.currentTime = now.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      })
    },
    loadData: function() {
      var self = this
      self.loading = true
      GetDashboard().then(function(res) {
        self.overview = res.overview || {}
        self.transport = res.transport || {}
        self.trend = res.trend || []
        self.activities = res.activities || []
        self.loading = false
        self.$nextTick(function() {
          self.initPieChart()
          self.initLineChart()
        })
      }).catch(function() {
        self.loading = false
      })
    },
    initPieChart: function() {
      var self = this
      if (self.pieChart) self.pieChart.dispose()
      self.pieChart = echarts.init(self.$refs.pieChart)
      
      var option = {
        tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
        legend: { bottom: '5%', left: 'center' },
        series: [{
          name: '运输状态',
          type: 'pie',
          radius: ['45%', '75%'],
          center: ['50%', '45%'],
          avoidLabelOverlap: false,
          itemStyle: {
            borderRadius: 8,
            borderColor: '#fff',
            borderWidth: 3
          },
          label: { show: false, position: 'center' },
          emphasis: {
            label: { show: true, fontSize: 18, fontWeight: 'bold' },
            itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0, 0, 0, 0.5)' }
          },
          labelLine: { show: false },
          data: [
            { value: self.transport.pending || 0, name: '待调度', itemStyle: { color: '#faad14' } },
            { value: self.transport.transporting || 0, name: '运输中', itemStyle: { color: '#52c41a' } },
            { value: self.transport.completed || 0, name: '已完成', itemStyle: { color: '#1890ff' } },
            { value: self.transport.warning || 0, name: '异常', itemStyle: { color: '#ff4d4f' } }
          ]
        }]
      }
      self.pieChart.setOption(option)
    },
    initLineChart: function() {
      var self = this
      if (self.lineChart) self.lineChart.dispose()
      self.lineChart = echarts.init(self.$refs.lineChart)
      
      var dates = self.trend.map(function(item) { return item.date })
      var created = self.trend.map(function(item) { return item.created })
      var completed = self.trend.map(function(item) { return item.completed })
      
      var option = {
        tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
        legend: { data: ['新建订单', '完成订单'], bottom: '0%' },
        grid: { left: '3%', right: '4%', bottom: '15%', top: '10%', containLabel: true },
        xAxis: { type: 'category', boundaryGap: false, data: dates, axisLine: { lineStyle: { color: '#d9d9d9' } }, axisLabel: { color: '#666' } },
        yAxis: { type: 'value', axisLine: { show: false }, splitLine: { lineStyle: { color: '#f0f0f0' } }, axisLabel: { color: '#666' } },
        series: [
          {
            name: '新建订单',
            type: 'line',
            smooth: true,
            symbol: 'circle',
            symbolSize: 8,
            data: created,
            areaStyle: { 
              color: {
                type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
                colorStops: [
                  { offset: 0, color: 'rgba(24, 144, 255, 0.3)' },
                  { offset: 1, color: 'rgba(24, 144, 255, 0.05)' }
                ]
              }
            },
            lineStyle: { color: '#1890ff', width: 3 },
            itemStyle: { color: '#1890ff', borderWidth: 2, borderColor: '#fff' }
          },
          {
            name: '完成订单',
            type: 'line',
            smooth: true,
            symbol: 'circle',
            symbolSize: 8,
            data: completed,
            areaStyle: { 
              color: {
                type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
                colorStops: [
                  { offset: 0, color: 'rgba(82, 196, 26, 0.3)' },
                  { offset: 1, color: 'rgba(82, 196, 26, 0.05)' }
                ]
              }
            },
            lineStyle: { color: '#52c41a', width: 3 },
            itemStyle: { color: '#52c41a', borderWidth: 2, borderColor: '#fff' }
          }
        ]
      }
      self.lineChart.setOption(option)
    },
    handleResize: function() {
      if (this.pieChart) this.pieChart.resize()
      if (this.lineChart) this.lineChart.resize()
    }
  }
}
</script>

<style scoped>
.dashboard {
  padding: 24px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8eb 100%);
  min-height: 100vh;
}

/* 欢迎横幅 */
.welcome-banner {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 28px 32px;
  margin-bottom: 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #fff;
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.3);
}
.welcome-content h1 {
  margin: 0;
  font-size: 26px;
  font-weight: 600;
}
.welcome-content p {
  margin: 8px 0 0 0;
  opacity: 0.85;
  font-size: 14px;
}
.welcome-time {
  font-size: 16px;
  opacity: 0.9;
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 统计卡片 */
.stat-cards {
  margin-bottom: 24px;
}
.stat-card {
  border-radius: 16px;
  padding: 24px;
  color: #fff;
  display: flex;
  align-items: center;
  height: 120px;
  box-shadow: 0 6px 20px rgba(0,0,0,0.12);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}
.stat-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 28px rgba(0,0,0,0.18);
}
.gradient-blue {
  background: linear-gradient(135deg, #1890ff 0%, #36cfc9 100%);
}
.gradient-green {
  background: linear-gradient(135deg, #52c41a 0%, #95de64 100%);
}
.gradient-orange {
  background: linear-gradient(135deg, #fa8c16 0%, #ffc53d 100%);
}
.gradient-purple {
  background: linear-gradient(135deg, #722ed1 0%, #b37feb 100%);
}
.stat-icon-bg {
  font-size: 48px;
  opacity: 0.25;
  margin-right: 20px;
}
.stat-info {
  flex: 1;
}
.stat-value {
  font-size: 36px;
  font-weight: bold;
  line-height: 1.2;
}
.stat-title {
  font-size: 14px;
  opacity: 0.9;
  margin-top: 4px;
}

/* 快速指标条 */
.quick-stats {
  background: #fff;
  border-radius: 12px;
  padding: 16px 32px;
  margin-bottom: 24px;
  display: flex;
  justify-content: space-around;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}
.quick-stat-item {
  display: flex;
  align-items: center;
  gap: 10px;
}
.dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}
.dot-orange { background: #faad14; }
.dot-green { background: #52c41a; }
.dot-blue { background: #1890ff; }
.dot-red { background: #ff4d4f; }
.quick-stat-item .label {
  color: #666;
  font-size: 14px;
}
.quick-stat-item .value {
  font-size: 18px;
  font-weight: bold;
  color: #333;
}

/* 图表卡片 */
.chart-row {
  margin-bottom: 24px;
}
.chart-card {
  border-radius: 16px;
  box-shadow: 0 4px 16px rgba(0,0,0,0.08);
}
.card-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 16px;
  font-weight: 500;
  color: #333;
}
.card-title i {
  color: #1890ff;
}
.chart-container {
  height: 320px;
  width: 100%;
}

/* 活动卡片 */
.activity-card {
  border-radius: 16px;
  box-shadow: 0 4px 16px rgba(0,0,0,0.08);
}
</style>
