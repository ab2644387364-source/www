<template>
  <a-layout-sider
      :style="{ overflow: 'auto', height: '100vh', position: 'fixed', left: 0 }"
      width="260">
    <div class="logo">
      <img src="../assets/logo.svg" alt="">
      牲畜物流转运系统
    </div>
    <a-menu 
      theme="dark" 
      mode="inline"
      :open-keys="openKeys"
      :selected-keys="selectedKeys"
      @openChange="onOpenChange"
    >
      <template v-for="(item, index) in menus">
        <!-- 独立菜单项（无子菜单） -->
        <a-menu-item v-if="item.standalone" :key="item.path">
          <router-link :to="item.path">
            <a-badge :count="item.badge || 0" :offset="[10, 0]">
              <a-icon :type="item.icon" style="color: rgba(255,255,255,0.65)"/>
            </a-badge>
            <span style="margin-left: 10px">{{ item.title }}</span>
          </router-link>
        </a-menu-item>
        <!-- 子菜单 -->
        <a-sub-menu v-else :key="String(index)">
          <span slot="title">
            <a-icon :type="item.icon"/>
            <span>{{ item.title }}</span>
          </span>
          <a-menu-item v-for="menu in item.children" :key="menu.path">
            <router-link :to="menu.path">
              {{ menu.title }}
            </router-link>
          </a-menu-item>
        </a-sub-menu>
      </template>
    </a-menu>
  </a-layout-sider>
</template>

<script>
import { GetUnreadCount } from '@/api/notification'

export default {

  data() {
    return {
      openKeys: [],
      unreadCount: 0,
      adminMenus: [
        {
          title: '仪表盘',
          icon: 'dashboard',
          children: [
            {title: '概览', path: '/dashboard'},
          ]
        },
        {
          title: '牲畜基础管理',
          icon: 'home',
          children: [
            {title: '牲畜档案', path: '/commodity'},
            {title: '合作单位', path: '/company'},
            {title: '人员管理', path: '/employee'},
            {title: '中转站管理', path: '/warehouse'},
          ]
        },
        {
          title: '流转记录管理',
          icon: 'pay-circle',
          children: [
            {title: '流转登记', path: '/sale/create'},
            {title: '流转记录', path: '/sale/record'},
          ]
        },
        {
          title: '转运调度管理',
          icon: 'car',
          children: [
            {title: '申请转运', path: '/delivery/create'},
            {title: '转运列表', path: '/delivery/list'},
          ]
        },
        {
          title: '运力资源管理',
          icon: 'rocket',
          children: [
            {title: '车辆资料', path: '/vehicle'},
            {title: '押运员资料', path: '/driver'},
          ]
        },
        {
          title: '图表分析',
          icon: 'line-chart',
          children: [
            {title: '入栏分析', path: '/analyze/in'},
            {title: '出栏分析', path: '/analyze/out'},
          ]
        },
        {
          title: '系统信息管理',
          icon: 'tool',
          children: [
            {title: '安全设置', path: '/security'},
            {title: '操作员管理', path: '/admin'},
            {title: '权限列表', path: '/role'},
            {title: '用户管理', path: '/user-manage'},
          ]
        },
      ]
    }
  },

  computed: {
    menus() {
      const details = this.$store.state.user.details || {}
      const role = this.$store.state.user.role || (details.roles ? "admin" : "user")
      if (role === "admin") {
        // 管理员菜单：在最后添加独立的消息中心
        return [
          ...this.adminMenus,
          {
            title: '消息中心',
            icon: 'bell',
            path: '/notification',
            standalone: true,
            badge: this.unreadCount
          }
        ]
      }
      return [
        {
          title: '用户中心',
          icon: 'profile',
          children: [
            {title: '用户信息', path: '/user/profile'},
          ]
        },
        {
          title: '订单服务',
          icon: 'shopping-cart',
          children: [
            {title: '创建订单', path: '/user/order/create'},
            {title: '路线查询', path: '/user/route'},
            {title: '订单支付', path: '/user/pay'},
          ]
        },
        {
          title: '消息通知',
          icon: 'bell',
          path: '/notification',
          standalone: true,
          badge: this.unreadCount
        },
      ]
    },
    selectedKeys() {
      // 根据当前路由路径返回选中的菜单项
      const path = this.$route.path
      return [path]
    }
  },

  watch: {
    // 监听路由变化，自动展开对应菜单
    '$route.path': {
      immediate: true,
      handler(path) {
        this.updateOpenKeys(path)
        // 进入通知页面时刷新未读数
        if (path === '/notification') {
          this.loadUnreadCount()
        }
      }
    }
  },

  mounted() {
    this.loadUnreadCount()
    // 每30秒刷新一次未读数
    this.unreadTimer = setInterval(() => {
      this.loadUnreadCount()
    }, 30000)
  },

  beforeDestroy() {
    if (this.unreadTimer) {
      clearInterval(this.unreadTimer)
    }
  },

  methods: {
    loadUnreadCount() {
      GetUnreadCount().then(res => {
        if (res.status && res.data) {
          this.unreadCount = res.data.count || 0
        }
      }).catch(() => {
        // 忽略错误
      })
    },
    updateOpenKeys(path) {
      // 根据当前路径找到对应的父级菜单索引
      for (let i = 0; i < this.menus.length; i++) {
        const menu = this.menus[i]
        if (menu.standalone) continue
        const found = menu.children && menu.children.some(child => child.path === path)
        if (found) {
          this.openKeys = [String(i)]
          return
        }
      }
    },
    onOpenChange(keys) {
      // 手动展开/收起菜单时更新
      this.openKeys = keys
    }
  }

}

</script>

<style scoped>

#components-layout-demo-fixed-sider .logo {
  padding: 10px 15px;
  height: 50px;
  font-size: 15px;
  margin: 16px;
  color: #ffffff;
  letter-spacing: 2px;
  white-space: nowrap;
}

.ant-menu {
  letter-spacing: 1px;
}

.logo img {
  width: 32px;
  height: 32px;
  margin-right: 5px;
}

</style>
