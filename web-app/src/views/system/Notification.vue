<template>
  <div class="notification-page">
    <a-page-header title="消息通知中心">
      <template slot="extra">
        <a-button type="primary" @click="markAllRead" :loading="loading">
          <a-icon type="check" /> 全部已读
        </a-button>
      </template>
    </a-page-header>

    <a-list
      class="notification-list"
      :loading="loading"
      item-layout="horizontal"
      :data-source="notifications"
    >
      <a-list-item slot="renderItem" slot-scope="item">
        <a-list-item-meta>
          <template slot="avatar">
            <a-badge :dot="!item.isRead">
              <a-avatar :style="getAvatarStyle(item.type)" :icon="getIcon(item.type)" />
            </a-badge>
          </template>
          <template slot="title">
            <span :class="{ 'unread-title': !item.isRead }">{{ item.title }}</span>
            <a-tag v-if="item.type === 'warning'" color="red" style="margin-left: 8px">预警</a-tag>
            <a-tag v-else-if="item.type === 'transport'" color="blue" style="margin-left: 8px">物流</a-tag>
            <a-tag v-else color="default" style="margin-left: 8px">系统</a-tag>
          </template>
          <template slot="description">
            <div class="notification-content">{{ item.content }}</div>
            <div class="notification-time">
              <a-icon type="clock-circle" style="margin-right: 4px" />
              {{ item.createTime }}
            </div>
          </template>
        </a-list-item-meta>
        <template slot="actions">
          <a v-if="!item.isRead" @click="markRead(item.id)">标记已读</a>
          <span v-else style="color: #52c41a">已读</span>
        </template>
      </a-list-item>
      <div v-if="notifications.length === 0 && !loading" class="empty-state">
        <a-empty description="暂无通知" />
      </div>
    </a-list>
  </div>
</template>

<script>
import { GetNotifications, MarkAsRead, MarkAllAsRead } from '../../api/notification'

export default {
  name: 'Notification',
  data() {
    return {
      loading: true,
      notifications: []
    }
  },
  mounted() {
    this.loadData()
  },
  methods: {
    loadData() {
      this.loading = true
      GetNotifications().then(res => {
        this.notifications = res || []
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    markRead(id) {
      MarkAsRead(id).then(() => {
        const item = this.notifications.find(n => n.id === id)
        if (item) item.isRead = true
        this.$message.success('已标记为已读')
      })
    },
    markAllRead() {
      this.loading = true
      MarkAllAsRead().then(() => {
        this.notifications.forEach(n => n.isRead = true)
        this.$message.success('已全部标记为已读')
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    getIcon(type) {
      const icons = {
        'warning': 'warning',
        'transport': 'car',
        'system': 'notification'
      }
      return icons[type] || 'notification'
    },
    getAvatarStyle(type) {
      const styles = {
        'warning': { backgroundColor: '#ff4d4f' },
        'transport': { backgroundColor: '#1890ff' },
        'system': { backgroundColor: '#722ed1' }
      }
      return styles[type] || { backgroundColor: '#722ed1' }
    }
  }
}
</script>

<style scoped>
.notification-page {
  padding: 24px;
  background: #fff;
  min-height: 100vh;
}

.notification-list {
  margin-top: 16px;
}

.unread-title {
  font-weight: 600;
  color: #1890ff;
}

.notification-content {
  color: #666;
  margin-bottom: 8px;
}

.notification-time {
  color: #999;
  font-size: 12px;
}

.empty-state {
  padding: 48px;
  text-align: center;
}
</style>
