<template>
  <a-layout id="components-layout-demo-fixed-sider">
    <Aside/>
    <a-layout class="content-layout" :style="{ marginLeft: '260px' }">
      <Header/>
      <Main/>
      <Footer/>
    </a-layout>
  </a-layout>
</template>

<script>
import Aside from "@/layout/Aside";
import Header from "@/layout/Header";
import Main from "@/layout/Main";
import Footer from "@/layout/Footer";

export default {

  components: {Aside, Header, Main, Footer},

  mounted() {
    const details = this.$store.state.user.details || {}
    const role = this.$store.state.user.role || (details.roles ? "admin" : "user")
    const title = role === "admin" ? "管理员" : "用户"
    this.$message.success(
        '欢迎' + title + ' ' + (details.email || ''),
        6,
    );
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
}

.logo img {
  width: 32px;
  height: 32px;
  margin-right: 5px;
}

.header {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1)
}

.content-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

</style>
