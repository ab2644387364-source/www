<template>
  <div class="dashboard">
    <!-- 统计卡片区域 -->
    <a-row :gutter="16" class="stat-cards">
      <a-col :span="6">
        <a-card class="stat-card stat-card-primary">
          <a-statistic
            title="牲畜种类"
            :value="overview.commodityCount || 0"
            :value-style="{ color: '#1890ff', fontSize: '32px' }"
          >
            <template #prefix>
              <a-icon type="appstore" />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card class="stat-card stat-card-success">
          <a-statistic
            title="配送订单"
            :value="overview.distributionCount || 0"
            :value-style="{ color: '#52c41a', fontSize: '32px' }"
          >
            <template #prefix>
              <a-icon type="car" />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card class="stat-card stat-card-warning">
          <a-statistic
            title="运输中"
            :value="overview.transportingCount || 0"
            :value-style="{ color: '#fa8c16', fontSize: '32px' }"
          >
            <template #prefix>
              <a-icon type="sync" spin />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card class="stat-card stat-card-info">
          <a-statistic
            title="仓库数量"
            :value="overview.warehouseCount || 0"
            :value-style="{ color: '#722ed1', fontSize: '32px' }"
          >
            <template #prefix>
              <a-icon type="home" />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
    </a-row>

    <!-- 图表区域 -->
    <a-row :gutter="16" class="chart-row">
      <a-col :span="12">
        <a-card title="运输状态分布" :bordered="false" class="chart-card">
          <div ref="pieChart" class="chart-container"></div>
        </a-card>
      </a-col>
      <a-col :span="12">
        <a-card title="近7天运输趋势" :bordered="false" class="chart-card">
          <div ref="lineChart" class="chart-container"></div>
        </a-card>
      </a-col>
    </a-row>

    <!-- 最近动态 -->
    <a-card title="最新运输动态" :bordered="false" class="activity-card">
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
          <a-tag v-if="warningLevel===2" color="red">严重</a-tag>
          <a-tag v-else-if="warningLevel===1" color="orange">一般</a-tag>
          <a-tag v-else color="green">正常</a-tag>
        </span>
      </a-table>
    </a-card>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import { GetDashboard } from '../../api/statistics'

const activityColumns = [
  { title: '押运员', dataIndex: 'driver' },
  { title: '车牌号', dataIndex: 'number' },
  { title: '起点', dataIndex: 'origin' },
  { title: '目的地', dataIndex: 'destination' },
  { title: '当前位置', dataIndex: 'currentNode' },
  { title: '状态', dataIndex: 'status', scopedSlots: { customRender: 'status' } },
  { title: '异常', dataIndex: 'warningLevel', scopedSlots: { customRender: 'warningLevel' } },
]

export default {
  name: 'Dashboard',
  data() {
    return {
      loading: true,
      overview: {},
      transport: {},
      trend: [],
      activities: [],
      activityColumns,
      pieChart: null,
      lineChart: null,
    }
  },
  mounted() {
    this.loadData()
    window.addEventListener('resize', this.handleResize)
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.handleResize)
    if (this.pieChart) this.pieChart.dispose()
    if (this.lineChart) this.lineChart.dispose()
  },
  methods: {
    loadData() {
      this.loading = true
      GetDashboard().then(res => {
        this.overview = res.overview || {}
        this.transport = res.transport || {}
        this.trend = res.trend || []
        this.activities = res.activities || []
        this.loading = false
        this.$nextTick(() => {
          this.initPieChart()
          this.initLineChart()
        })
      }).catch(() => {
        this.loading = false
      })
    },
    initPieChart() {
      if (this.pieChart) this.pieChart.dispose()
      this.pieChart = echarts.init(this.$refs.pieChart)
      
      const option = {
        tooltip: { trigger: 'item' },
        legend: { bottom: '5%', left: 'center' },
        series: [{
          name: '运输状态',
          type: 'pie',
          radius: ['40%', '70%'],
          avoidLabelOverlap: false,
          itemStyle: {
            borderRadius: 10,
            borderColor: '#fff',
            borderWidth: 2
          },
          label: { show: false, position: 'center' },
          emphasis: {
            label: { show: true, fontSize: 20, fontWeight: 'bold' }
          },
          labelLine: { show: false },
          data: [
            { value: this.transport.pending || 0, name: '待调度', itemStyle: { color: '#fa8c16' } },
            { value: this.transport.transporting || 0, name: '运输中', itemStyle: { color: '#52c41a' } },
            { value: this.transport.completed || 0, name: '已完成', itemStyle: { color: '#1890ff' } },
            { value: this.transport.warning || 0, name: '异常', itemStyle: { color: '#f5222d' } },
          ]
        }]
      }
      this.pieChart.setOption(option)
    },
    initLineChart() {
      if (this.lineChart) this.lineChart.dispose()
      this.lineChart = echarts.init(this.$refs.lineChart)
      
      const dates = this.trend.map(item => item.date)
      const created = this.trend.map(item => item.created)
      const completed = this.trend.map(item => item.completed)
      
      const option = {
        tooltip: { trigger: 'axis' },
        legend: { data: ['新建订单', '完成订单'], bottom: '5%' },
        grid: { left: '3%', right: '4%', bottom: '15%', containLabel: true },
        xAxis: { type: 'category', boundaryGap: false, data: dates },
        yAxis: { type: 'value' },
        series: [
          {
            name: '新建订单',
            type: 'line',
            smooth: true,
            data: created,
            areaStyle: { opacity: 0.3 },
            itemStyle: { color: '#1890ff' }
          },
          {
            name: '完成订单',
            type: 'line',
            smooth: true,
            data: completed,
            areaStyle: { opacity: 0.3 },
            itemStyle: { color: '#52c41a' }
          }
        ]
      }
      this.lineChart.setOption(option)
    },
    handleResize() {
      if (this.pieChart) this.pieChart.resize()
      if (this.lineChart) this.lineChart.resize()
    }
  }
}
</script>

<style scoped>
.dashboard {
  padding: 24px;
  background: #f0f2f5;
  min-height: 100vh;
}

.stat-cards {
  margin-bottom: 24px;
}

.stat-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.09);
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.stat-card-primary {
  border-top: 3px solid #1890ff;
}

.stat-card-success {
  border-top: 3px solid #52c41a;
}

.stat-card-warning {
  border-top: 3px solid #fa8c16;
}

.stat-card-info {
  border-top: 3px solid #722ed1;
}

.chart-row {
  margin-bottom: 24px;
}

.chart-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.09);
}

.chart-container {
  height: 300px;
  width: 100%;
}

.activity-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.09);
}
</style>
