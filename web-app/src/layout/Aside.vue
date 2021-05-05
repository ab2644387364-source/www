<template>
  <a-layout-sider
      :style="{ overflow: 'auto', height: '100vh', position: 'fixed', left: 0 }"
      width="260">
    <div class="logo">
      <img src="../assets/logo.svg" alt="">
      牲畜物流转运系统
    </div>
    <a-menu theme="dark" mode="inline">
      <a-sub-menu v-for="(item, index) in menus" :key="index">
        <span slot="title">
          <a-icon :type="item.icon"/>
          <span>{{ item.title }}</span>
        </span>
        <a-menu-item v-for="menu in item.children" :key="menu.title">
          <router-link :to="menu.path">
            {{ menu.title }}
          </router-link>
        </a-menu-item>
      </a-sub-menu>
    </a-menu>
  </a-layout-sider>
</template>

<script>

export default {

  data() {
    return {
      adminMenus: [
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
          ]
        },
      ]
    }
  },

  computed: {
    menus() {
      const details = this.$store.state.user.details || {}
      const role = this.$store.state.user.role || (details.roles ? "admin" : "user")
      if (role === "admin") return this.adminMenus
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
      ]
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
