<template>
  <div class="tracking-map-page">
    <a-page-header title="实时轨迹追踪" sub-title="查看运输车辆的实时位置和历史轨迹">
      <template slot="extra">
        <a-select class="custom-search-select" v-model="selectedDistribution" style="width: 300px" placeholder="选择配送单" @change="loadTrack">
          <a-select-option v-for="item in distributionList" :key="item.id" :value="item.id">
            {{ item.id.substring(0, 8) }}... - {{ item.vehiclePlate || '未分配车辆' }}
          </a-select-option>
        </a-select>
        <a-button type="primary" @click="refreshTrack" :loading="loading" style="margin-left: 12px">
          <a-icon type="reload" /> 刷新
        </a-button>
      </template>
    </a-page-header>

    <div class="map-container">
      <!-- 地图区域 -->
      <div id="amap-container" class="amap-box"></div>

      <!-- 信息面板 -->
      <div class="info-panel" v-if="trackInfo">
        <a-card title="运输信息" :bordered="false" size="small">
          <a-descriptions :column="1" size="small">
            <a-descriptions-item label="车牌号">
              <a-tag color="blue">{{ trackInfo.vehiclePlate || '未知' }}</a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="押运员">{{ trackInfo.driverName || '未知' }}</a-descriptions-item>
            <a-descriptions-item label="总里程">{{ (trackInfo.totalDistance || 0).toFixed(2) }} 公里</a-descriptions-item>
            <a-descriptions-item label="轨迹点数">{{ trackInfo.points ? trackInfo.points.length : 0 }} 个</a-descriptions-item>
          </a-descriptions>
        </a-card>

        <a-card title="最新位置" :bordered="false" size="small" style="margin-top: 12px" v-if="trackInfo.latestPoint">
          <a-descriptions :column="1" size="small">
            <a-descriptions-item label="经度">{{ trackInfo.latestPoint.longitude }}</a-descriptions-item>
            <a-descriptions-item label="纬度">{{ trackInfo.latestPoint.latitude }}</a-descriptions-item>
            <a-descriptions-item label="速度">{{ trackInfo.latestPoint.speed || 0 }} km/h</a-descriptions-item>
            <a-descriptions-item label="地址">{{ trackInfo.latestPoint.address || '未知' }}</a-descriptions-item>
          </a-descriptions>
        </a-card>

        <!-- 模拟上报按钮 (测试用) -->
        <a-card title="模拟GPS上报" :bordered="false" size="small" style="margin-top: 12px">
          <a-button type="dashed" block @click="simulateReport" :loading="simulating">
            <a-icon type="environment" /> 模拟位置上报
          </a-button>
          <p style="margin-top: 8px; color: #999; font-size: 12px">
            点击按钮将在当前轨迹基础上模拟一个新的GPS位置点
          </p>
        </a-card>
      </div>

      <!-- 空状态 -->
      <div class="empty-state" v-if="!selectedDistribution">
        <a-empty description="请选择一个配送单查看轨迹" />
      </div>
    </div>
  </div>
</template>

<script>
import { GetTrack, ReportLocation, CreateTrack } from '@/api/tracking'

export default {
  name: 'TrackingMap',
  data() {
    return {
      map: null,
      polyline: null,
      marker: null,
      selectedDistribution: null,
      distributionList: [],
      trackInfo: null,
      loading: false,
      simulating: false
    }
  },
  mounted() {
    this.initMap()
    this.loadDistributions()
  },
  beforeDestroy() {
    if (this.map) {
      this.map.destroy()
    }
  },
  methods: {
    initMap() {
      // 检查高德地图是否加载
      if (typeof AMap === 'undefined') {
        this.$message.warning('高德地图加载中，请稍后刷新页面')
        return
      }

      this.map = new AMap.Map('amap-container', {
        zoom: 10,
        center: [113.6254, 34.7466], // 默认郑州坐标
        viewMode: '2D'
      })

      // 添加控件
      this.map.addControl(new AMap.Scale())
      this.map.addControl(new AMap.ToolBar())
    },

    loadDistributions() {
      // 模拟获取配送单列表（实际应从API获取）
      // 这里简化处理，实际项目中应调用配送单API
      this.distributionList = [
        { id: 'demo-distribution-001', vehiclePlate: '豫A12345' },
        { id: 'demo-distribution-002', vehiclePlate: '豫B67890' }
      ]
    },

    async loadTrack() {
      if (!this.selectedDistribution) return

      this.loading = true
      try {
        const res = await GetTrack(this.selectedDistribution)
        if (res.data && res.data.status && res.data.data) {
          this.trackInfo = res.data.data
          this.renderTrack()
        } else {
          // 如果轨迹不存在，尝试创建
          await this.createDemoTrack()
        }
      } catch (e) {
        this.$message.error('加载轨迹失败')
      }
      this.loading = false
    },

    async createDemoTrack() {
      try {
        const res = await CreateTrack({
          distributionId: this.selectedDistribution,
          vehicleId: 'V001',
          vehiclePlate: '豫A12345',
          driverName: '张师傅'
        })
        if (res.data && res.data.status) {
          this.trackInfo = res.data.data
          this.$message.success('已创建轨迹记录，请使用模拟上报添加位置点')
        }
      } catch (e) {
        console.error(e)
      }
    },

    refreshTrack() {
      this.loadTrack()
    },

    renderTrack() {
      if (!this.map || !this.trackInfo) return

      // 清除旧轨迹
      if (this.polyline) {
        this.map.remove(this.polyline)
      }
      if (this.marker) {
        this.map.remove(this.marker)
      }

      const points = this.trackInfo.points || []
      if (points.length === 0) {
        this.$message.info('暂无轨迹数据，请先上报GPS位置')
        return
      }

      // 绘制轨迹线
      const path = points.map(p => [p.longitude, p.latitude])
      this.polyline = new AMap.Polyline({
        path: path,
        strokeColor: '#1890ff',
        strokeWeight: 6,
        strokeOpacity: 0.8,
        lineJoin: 'round'
      })
      this.map.add(this.polyline)

      // 添加当前位置标记
      const latest = this.trackInfo.latestPoint
      if (latest) {
        this.marker = new AMap.Marker({
          position: [latest.longitude, latest.latitude],
          title: '当前位置',
          icon: new AMap.Icon({
            size: new AMap.Size(32, 32),
            image: 'https://a.amap.com/jsapi_demos/static/demo-center/icons/poi-marker-default.png',
            imageSize: new AMap.Size(32, 32)
          })
        })
        this.map.add(this.marker)
        this.map.setCenter([latest.longitude, latest.latitude])
      }

      // 适应视野
      this.map.setFitView()
    },

    async simulateReport() {
      if (!this.selectedDistribution) {
        this.$message.warning('请先选择配送单')
        return
      }

      this.simulating = true
      try {
        // 生成模拟位置（基于郑州附近随机偏移）
        let baseLng = 113.6254
        let baseLat = 34.7466

        if (this.trackInfo && this.trackInfo.latestPoint) {
          baseLng = this.trackInfo.latestPoint.longitude
          baseLat = this.trackInfo.latestPoint.latitude
        }

        const newLng = baseLng + (Math.random() - 0.5) * 0.02
        const newLat = baseLat + (Math.random() - 0.5) * 0.02
        const speed = 50 + Math.random() * 30

        const res = await ReportLocation({
          distributionId: this.selectedDistribution,
          longitude: newLng,
          latitude: newLat,
          speed: speed,
          address: '模拟位置'
        })

        if (res.data && res.data.status) {
          this.$message.success('位置上报成功')
          this.loadTrack()
        } else {
          this.$message.error(res.data.msg || '上报失败')
        }
      } catch (e) {
        this.$message.error('上报失败')
      }
      this.simulating = false
    }
  }
}
</script>

<style scoped>
.tracking-map-page {
  padding: 24px;
  background: #f0f2f5;
  min-height: 100vh;
}

.map-container {
  display: flex;
  gap: 16px;
  margin-top: 16px;
}

.amap-box {
  flex: 1;
  height: 600px;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.info-panel {
  width: 300px;
}

.empty-state {
  flex: 1;
  height: 600px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  border-radius: 8px;
}

/* 自定义搜索框样式增强 */
.custom-search-select >>> .ant-select-selection {
  border: 1px solid #1890ff !important;
  box-shadow: 0 2px 6px rgba(24, 144, 255, 0.2);
}

.custom-search-select >>> .ant-select-selection__placeholder {
  color: #333;
  font-weight: 500;
}
</style>
