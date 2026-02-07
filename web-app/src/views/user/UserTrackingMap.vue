<template>
  <div class="tracking-map-page">
    <a-page-header title="物流轨迹" sub-title="查看您的订单运输实时位置">
      <template slot="extra">
        <a-select class="custom-search-select" v-model="selectedOrder" style="width: 320px" placeholder="选择订单查看轨迹" @change="loadTrack">
          <a-select-option v-for="item in orderList" :key="item.id" :value="item.distributionId">
            订单 #{{ item.id }} - {{ item.status === 'SHIPPED' ? '运输中' : '已完成' }}
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
            <a-descriptions-item label="运输里程">{{ (trackInfo.totalDistance || 0).toFixed(2) }} 公里</a-descriptions-item>
          </a-descriptions>
        </a-card>

        <a-card title="当前位置" :bordered="false" size="small" style="margin-top: 12px" v-if="trackInfo.latestPoint">
          <a-descriptions :column="1" size="small">
            <a-descriptions-item label="速度">{{ trackInfo.latestPoint.speed || 0 }} km/h</a-descriptions-item>
            <a-descriptions-item label="位置">{{ trackInfo.latestPoint.address || '正在获取...' }}</a-descriptions-item>
          </a-descriptions>
        </a-card>

        <a-card :bordered="false" size="small" style="margin-top: 12px">
          <a-statistic title="预计到达" :value="estimatedArrival" />
        </a-card>
      </div>

      <!-- 空状态 -->
      <div class="empty-state" v-if="!selectedOrder">
        <a-empty description="请选择一个运输中的订单查看轨迹" />
      </div>
    </div>
  </div>
</template>

<script>
import { GetTrack } from '@/api/tracking'
import { FetchOrders } from '@/api/order'

export default {
  name: 'UserTrackingMap',
  data() {
    return {
      map: null,
      polyline: null,
      marker: null,
      selectedOrder: null,
      orderList: [],
      trackInfo: null,
      loading: false,
      estimatedArrival: '--'
    }
  },
  mounted() {
    this.initMap()
    this.loadOrders()
  },
  beforeDestroy() {
    if (this.map) {
      this.map.destroy()
    }
  },
  methods: {
    initMap() {
      if (typeof AMap === 'undefined') {
        this.$message.warning('高德地图加载中，请稍后刷新页面')
        return
      }

      this.map = new AMap.Map('amap-container', {
        zoom: 10,
        center: [113.6254, 34.7466],
        viewMode: '2D'
      })

      this.map.addControl(new AMap.Scale())
      this.map.addControl(new AMap.ToolBar())
    },

    async loadOrders() {
      try {
        const res = await FetchOrders()
        if (res.status && res.data) {
          // 筛选有配送单且运输中/已完成的订单
          this.orderList = res.data.filter(o => 
            o.distributionId && ['SHIPPED', 'DELIVERED'].includes(o.status)
          )
        }
      } catch (e) {
        console.error('加载订单失败', e)
      }
    },

    async loadTrack() {
      if (!this.selectedOrder) return

      this.loading = true
      try {
        const res = await GetTrack(this.selectedOrder)
        if (res.data && res.data.status && res.data.data) {
          this.trackInfo = res.data.data
          this.renderTrack()
          this.calculateETA()
        } else {
          this.trackInfo = null
          this.$message.info('暂无轨迹数据')
        }
      } catch (e) {
        this.$message.error('加载轨迹失败')
      }
      this.loading = false
    },

    refreshTrack() {
      this.loadTrack()
    },

    calculateETA() {
      // 简单估算到达时间
      if (this.trackInfo && this.trackInfo.latestPoint) {
        const speed = this.trackInfo.latestPoint.speed || 60
        const remaining = 100 - (this.trackInfo.totalDistance || 0) // 假设总路程100公里
        const hours = remaining / speed
        if (hours > 0) {
          const now = new Date()
          now.setHours(now.getHours() + Math.ceil(hours))
          this.estimatedArrival = now.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
        }
      }
    },

    renderTrack() {
      if (!this.map || !this.trackInfo) return

      if (this.polyline) this.map.remove(this.polyline)
      if (this.marker) this.map.remove(this.marker)

      const points = this.trackInfo.points || []
      if (points.length === 0) return

      const path = points.map(p => [p.longitude, p.latitude])
      this.polyline = new AMap.Polyline({
        path: path,
        strokeColor: '#52c41a',
        strokeWeight: 6,
        strokeOpacity: 0.8,
        lineJoin: 'round'
      })
      this.map.add(this.polyline)

      const latest = this.trackInfo.latestPoint
      if (latest) {
        this.marker = new AMap.Marker({
          position: [latest.longitude, latest.latitude],
          title: '当前位置',
          icon: new AMap.Icon({
            size: new AMap.Size(52, 26),
            image: 'https://webapi.amap.com/images/car.png',
            imageSize: new AMap.Size(52, 26)
          }),
          offset: new AMap.Pixel(-26, -13)
        })
        this.map.add(this.marker)
        this.map.setCenter([latest.longitude, latest.latitude])
      }

      this.map.setFitView()
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
  height: 550px;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.info-panel {
  width: 280px;
}

.empty-state {
  flex: 1;
  height: 550px;
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
