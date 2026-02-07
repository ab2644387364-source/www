<template>
  <a-layout-header class="header" :style="{ background: '#fff', padding: 0}">
    <a-tooltip placement="bottom">
      <template slot="title">
        <span>刷新页面</span>
      </template>
      <a-button @click="clickReload" class="reload-btn" icon="reload" type="link">刷新</a-button>
    </a-tooltip>

    <a-tooltip placement="bottom" class="question-btn">
      <template slot="title">
        <span>关于</span>
      </template>
      <a-button class="reload-btn" icon="question-circle" type="link"/>
    </a-tooltip>

    <!-- 通知铃铛 -->
    <a-badge :count="unreadCount" class="notification-badge">
      <a-button class="notification-btn" icon="bell" type="link" @click="goNotification"/>
    </a-badge>

    <a-dropdown>
      <a-button class="avatar-btn" type="link">
        <div class="avatar-btn-main">
          <a-avatar class="avatar"
                    :size="26"
                    src="https://gw.alipayobjects.com/zos/antfincdn/XAosXuNZyF/BiazfanxmamNRoxxVxka.png"/>
          <div class="avatar-btn-username">{{ displayName }}</div>
        </div>
      </a-button>
      <a-menu slot="overlay">
        <a-menu-item>
          <router-link :to="profilePath">
            <a-icon class="icon" type="smile"/>
            {{ profileLabel }}
          </router-link>
        </a-menu-item>
        <a-menu-item v-if="isAdmin">
          <router-link to="/role">
            <a-icon class="icon" type="setting"/>
            权限设置
          </router-link>
        </a-menu-item>
        <a-menu-divider/>
        <a-menu-item @click="handleLogout">
          <a-icon class="icon" type="logout"/>
          退出登录
        </a-menu-item>
      </a-menu>
    </a-dropdown>

  </a-layout-header>
</template>

<script>
import { GetUnreadCount } from '../api/notification'
import { eventBus } from '@/utils/eventBus'

export default {

  data() {
    return {
      unreadCount: 0
    }
  },

  computed: {
    details() {
      return this.$store.state.user.details ? this.$store.state.user.details : {}
    },
    isAdmin() {
      const role = this.$store.state.user.role
      if (role) return role === "admin"
      return !!(this.details && this.details.roles)
    },
    profilePath() {
      return this.isAdmin ? "/security" : "/user/profile"
    },
    profileLabel() {
      return this.isAdmin ? "个人中心" : "用户信息"
    },
    displayName() {
      // 优先显示姓名，没有姓名时显示邮箱的脱敏形式
      if (this.details.name) {
        return this.details.name
      }
      const email = this.details.email || ''
      if (email.includes('@')) {
        const [name, domain] = email.split('@')
        return name.slice(0, 3) + '***@' + domain
      }
      return '用户'
    }
  },

  mounted() {
    this.loadUnreadCount()
    // 每30秒刷新一次未读数量
    this.timer = setInterval(() => {
      this.loadUnreadCount()
    }, 30000)
    // 监听标记已读事件，立即刷新未读数
    eventBus.$on('notification-read', this.loadUnreadCount)
  },

  beforeDestroy() {
    if (this.timer) {
      clearInterval(this.timer)
    }
    eventBus.$off('notification-read', this.loadUnreadCount)
  },

  methods: {

    loadUnreadCount() {
      GetUnreadCount().then(res => {
        this.unreadCount = res.count || 0
      }).catch(() => {
        this.unreadCount = 0
      })
    },

    goNotification() {
      this.$router.push('/notification')
    },

    clickReload() {
      this.$router.go(0)
    },

    handleLogout() {
      let that = this
      this.$confirm({
        title: '你确定要注销你的登录信息吗?',
        content: '点击确定将删除你在网站保留的登录信息！',
        onOk() {
          that.$router.push('/login')
          that.$store.commit('user/userLogout')
        },
      });
    }

  }

}
</script>

<style scoped>

.reload-btn {
  font-size: 15px;
  margin-left: 10px;
  color: rgba(0, 0, 0, 0.65);
}

.question-btn {
  float: right;
  margin: 15px 12px 0 0;
}

.notification-badge {
  float: right;
  margin: 18px 16px 0 0;
}

.notification-btn {
  font-size: 18px;
  color: rgba(0, 0, 0, 0.65);
}

.notification-btn:hover {
  color: #1890ff;
}

.avatar-btn {
  float: right;
  font-size: 15px;
  color: rgba(0, 0, 0, 0.55);
  height: 64px;
}


.avatar-btn-main {
  display: flex;
}

.avatar-btn-username {
  line-height: 62px;
  font-size: 14px;
}

.avatar {
  margin-right: 12px;
  margin-top: 19px;
}

</style>
