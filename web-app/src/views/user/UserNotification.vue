<template>
  <div class="user-notification-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <h1><a-icon type="bell" /> 消息通知</h1>
        <p class="subtitle">查看系统消息和订单状态更新</p>
      </div>
    </div>

    <div class="page-body">
      <div class="notification-card">
        <!-- 工具栏 -->
        <div class="toolbar">
          <div class="tabs">
            <div 
              class="tab" 
              :class="{ 'active': filter === 'all' }"
              @click="filter = 'all'"
            >
              全部
              <span class="badge" v-if="totalCount">{{ totalCount }}</span>
            </div>
            <div 
              class="tab" 
              :class="{ 'active': filter === 'unread' }"
              @click="filter = 'unread'"
            >
              未读
              <span class="badge red" v-if="unreadCount">{{ unreadCount }}</span>
            </div>
          </div>
          <a-button type="primary" @click="markAllRead" :loading="marking" :disabled="unreadCount === 0">
            <a-icon type="check" /> 全部已读
          </a-button>
        </div>

        <!-- 通知列表 -->
        <a-spin :spinning="loading">
          <div v-if="filteredNotifications.length > 0" class="notification-list">
            <div 
              v-for="item in filteredNotifications" 
              :key="item.id" 
              class="notification-item"
              :class="{ 'unread': !item.isRead }"
            >
              <div class="item-icon" :style="getIconStyle(item.type)">
                <a-icon :type="getIcon(item.type)" />
              </div>
              <div class="item-content">
                <div class="item-header">
                  <span class="title">{{ item.title }}</span>
                  <a-tag :color="getTypeColor(item.type)" size="small">{{ getTypeText(item.type) }}</a-tag>
                </div>
                <div class="content">{{ item.content }}</div>
                <div class="time">
                  <a-icon type="clock-circle" /> {{ item.createTime }}
                </div>
              </div>
              <div class="item-action">
                <a-button 
                  v-if="!item.isRead" 
                  type="link" 
                  size="small"
                  @click="markRead(item.id)"
                >
                  标记已读
                </a-button>
                <a-icon v-else type="check-circle" theme="filled" style="color: #52c41a; font-size: 18px" />
              </div>
            </div>
          </div>

          <div v-else class="empty-state">
            <a-empty :description="filter === 'unread' ? '没有未读消息' : '暂无通知消息'" :image="simpleImage" />
          </div>
        </a-spin>
      </div>
    </div>
  </div>
</template>

<script>
import { GetUserNotifications, GetUserUnreadCount, MarkAsRead, MarkAllAsReadByUser } from '@/api/notification'
import { eventBus } from '@/utils/eventBus'
import { Empty } from 'ant-design-vue'

export default {
  name: 'UserNotification',
  data() {
    return {
      simpleImage: Empty.PRESENTED_IMAGE_SIMPLE,
      loading: true,
      marking: false,
      filter: 'all',
      notifications: []
    }
  },

  computed: {
    userId() {
      const user = JSON.parse(localStorage.getItem('user') || '{}')
      return user.id || ''
    },
    filteredNotifications() {
      if (this.filter === 'unread') {
        return this.notifications.filter(n => !n.isRead)
      }
      return this.notifications
    },
    unreadCount() {
      return this.notifications.filter(n => !n.isRead).length
    },
    totalCount() {
      return this.notifications.length
    }
  },

  mounted() {
    this.loadData()
  },

  methods: {
    loadData() {
      if (!this.userId) {
        this.loading = false
        return
      }
      
      this.loading = true
      GetUserNotifications(this.userId).then(res => {
        if (res.status && res.data) {
          this.notifications = res.data
        } else {
          this.notifications = []
        }
        this.loading = false
      }).catch(() => {
        this.notifications = []
        this.loading = false
      })
    },

    markRead(id) {
      MarkAsRead(id).then(() => {
        const item = this.notifications.find(n => n.id === id)
        if (item) item.isRead = true
        this.$message.success('已标记为已读')
        eventBus.$emit('notification-read')
      })
    },

    markAllRead() {
      if (!this.userId) return
      
      this.marking = true
      MarkAllAsReadByUser(this.userId).then(() => {
        this.notifications.forEach(n => n.isRead = true)
        this.$message.success('已全部标记为已读')
        this.marking = false
        eventBus.$emit('notification-read')
      }).catch(() => {
        this.marking = false
      })
    },

    getIcon(type) {
      const icons = {
        'warning': 'warning',
        'transport': 'car',
        'order': 'file-text',
        'system': 'notification'
      }
      return icons[type] || 'notification'
    },

    getIconStyle(type) {
      const styles = {
        'warning': { background: 'linear-gradient(135deg, #ff4d4f 0%, #cf1322 100%)' },
        'transport': { background: 'linear-gradient(135deg, #1890ff 0%, #096dd9 100%)' },
        'order': { background: 'linear-gradient(135deg, #52c41a 0%, #389e0d 100%)' },
        'system': { background: 'linear-gradient(135deg, #722ed1 0%, #531dab 100%)' }
      }
      return styles[type] || styles['system']
    },

    getTypeColor(type) {
      const colors = {
        'warning': 'red',
        'transport': 'blue',
        'order': 'green',
        'system': 'purple'
      }
      return colors[type] || 'default'
    },

    getTypeText(type) {
      const texts = {
        'warning': '预警',
        'transport': '物流',
        'order': '订单',
        'system': '系统'
      }
      return texts[type] || '通知'
    }
  }
}
</script>

<style scoped>
.user-notification-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8ec 100%);
}

/* 页面头部 */
.page-header {
  background: linear-gradient(135deg, #fa8c16 0%, #d46b08 100%);
  padding: 32px;
  color: #fff;
}

.header-content {
  max-width: 1000px;
  margin: 0 auto;
}

.header-content h1 {
  color: #fff;
  font-size: 28px;
  margin: 0 0 8px 0;
  font-weight: 600;
}

.header-content .subtitle {
  color: rgba(255, 255, 255, 0.85);
  font-size: 14px;
  margin: 0;
}

/* 页面主体 */
.page-body {
  max-width: 1000px;
  margin: -24px auto 0;
  padding: 0 24px 40px;
  position: relative;
}

/* 通知卡片 */
.notification-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

/* 工具栏 */
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  border-bottom: 1px solid #f0f0f0;
  background: #fafafa;
}

.tabs {
  display: flex;
  gap: 8px;
}

.tab {
  padding: 8px 16px;
  border-radius: 20px;
  cursor: pointer;
  font-size: 14px;
  color: #666;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 6px;
}

.tab:hover {
  background: #f0f0f0;
}

.tab.active {
  background: #fa8c16;
  color: #fff;
}

.badge {
  background: #e8e8e8;
  color: #666;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
}

.tab.active .badge {
  background: rgba(255, 255, 255, 0.3);
  color: #fff;
}

.badge.red {
  background: #ff4d4f;
  color: #fff;
}

/* 通知列表 */
.notification-list {
  padding: 0;
}

.notification-item {
  display: flex;
  align-items: flex-start;
  padding: 20px 24px;
  border-bottom: 1px solid #f5f5f5;
  transition: background 0.2s;
}

.notification-item:hover {
  background: #fafafa;
}

.notification-item:last-child {
  border-bottom: none;
}

.notification-item.unread {
  background: #fff7e6;
}

.notification-item.unread:hover {
  background: #fff1d6;
}

.item-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 20px;
  flex-shrink: 0;
}

.item-content {
  flex: 1;
  margin: 0 16px;
  min-width: 0;
}

.item-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.item-header .title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

.notification-item.unread .item-header .title {
  color: #fa8c16;
}

.content {
  font-size: 14px;
  color: #666;
  line-height: 1.5;
  margin-bottom: 8px;
}

.time {
  font-size: 12px;
  color: #999;
  display: flex;
  align-items: center;
  gap: 4px;
}

.item-action {
  flex-shrink: 0;
  display: flex;
  align-items: center;
}

/* 空状态 */
.empty-state {
  padding: 60px 24px;
  text-align: center;
}

/* 响应式 */
@media (max-width: 768px) {
  .page-body {
    padding: 0 16px 24px;
  }
  
  .toolbar {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
  }
  
  .tabs {
    justify-content: center;
  }
  
  .notification-item {
    padding: 16px;
  }
  
  .item-icon {
    width: 36px;
    height: 36px;
    font-size: 16px;
  }
}
</style>
</CodeContent>
<parameter name="EmptyFile">false
