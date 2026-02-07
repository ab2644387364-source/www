<template>
  <div id="login-container">
    <div class="login-box">
      <div>
        <div class="box-header">
          <img class="logo" src="../assets/logo.svg" alt=""/>
          <div class="box-header-t">登录</div>
        </div>
        <div class="title">牲畜物流转运系统</div>
        <a-tabs @change="tabClick" default-active-key="1" :tabBarStyle="{ textAlign: 'center' }">
          <a-tab-pane key="1" tab="用户登录">
            <a-input
                v-model="userLogin.email"
                size="large"
                style="margin-top: 10px"
                class="input"
                placeholder="邮箱"
                autocomplete="off">
              <a-icon slot="prefix" type="mail"/>
            </a-input>
            <a-input-password
                v-model="userLogin.password"
                size="large"
                class="input"
                placeholder="密码"
                autocomplete="new-password">
              <a-icon slot="prefix" type="lock"/>
            </a-input-password>
          </a-tab-pane>
          <a-tab-pane key="2" tab="用户注册" force-render>
            <a-input
                v-model="registerForm.email"
                size="large"
                style="margin-top: 10px"
                class="input"
                placeholder="邮箱"
                autocomplete="off">
              <a-icon slot="prefix" type="mail"/>
            </a-input>
            <div style="display: flex">
              <a-input
                  v-model="registerForm.code"
                  size="large"
                  class="input"
                  placeholder="验证码"
                  autocomplete="off">
                <a-icon slot="prefix" type="safety-certificate"/>
              </a-input>
              <a-button class="code-btn" :loading="sendLoading" @click="sendRegisterEmail">
                获取验证码
              </a-button>
            </div>
            <a-input-password
                v-model="registerForm.password"
                size="large"
                class="input"
                placeholder="设置密码"
                autocomplete="new-password">
              <a-icon slot="prefix" type="lock"/>
            </a-input-password>
            <a-input-password
                v-model="registerForm.confirmPassword"
                size="large"
                class="input"
                placeholder="确认密码"
                autocomplete="new-password">
              <a-icon slot="prefix" type="lock"/>
            </a-input-password>
          </a-tab-pane>
          <a-tab-pane key="3" tab="管理员登录" force-render>
            <a-input
                v-model="form.email"
                size="large"
                style="margin-top: 10px"
                class="input"
                placeholder="邮箱"
                autocomplete="off">
              <a-icon slot="prefix" type="mail"/>
            </a-input>
            <a-input-password
                v-model="form.password"
                size="large"
                class="input"
                placeholder="密码"
                autocomplete="new-password">
              <a-icon slot="prefix" type="lock"/>
            </a-input-password>
            <div style="margin-bottom: 20px">
              <a-checkbox v-model="form.remember">自动登录</a-checkbox>
              <a-button type="link" style="margin-left: 158px">
                忘记密码 ?
              </a-button>
            </div>
          </a-tab-pane>
        </a-tabs>
        <div class="hint">提示：系统无默认账号和密码，请使用注册账号或初始化管理员账号登录。</div>
        <a-button :loading="submitLoading" class="submit-btn" type="primary" @click="submitLogin">
          {{ submitType === '2' ? '确认注册' : '确认登录' }}
        </a-button>
      </div>
    </div>
  </div>
</template>

<script>
import {AdminLogin} from "@/api/admin";
import {RegisterUser, SendUserEmail, LoginUser} from "@/api/user";
import {IsInit} from "../api/admin";

export default {

  data() {
    return {
      sendLoading: false,
      submitType: '1', //1用户登录 2用户注册 3管理员登录
      submitLoading: false,
      form: {
        password: '',
        email: '',
        code: '',
        remember: false,
      },
      userLogin: {
        email: '',
        password: '',
      },
      registerForm: {
        email: '',
        code: '',
        password: '',
        confirmPassword: '',
      },
    }
  },

  mounted() {
    IsInit().then((res) => {
      if (!res.data) this.$router.push('/init')
    })
  },

  methods: {

    submitLogin() {
      if (this.submitType === '2') {
        this.submitRegister()
        return
      }
      if (this.submitType === '1') {
        this.submitUserLogin()
        return
      }
      if (this.checkEmail(this.form.email)) {
        let type = "passwrod"
        this.submitLoading = true
        AdminLogin(type, this.form).then((res) => {
          if (res.status) {
            this.$store.commit('user/saveToken', res.data.token)
            this.$store.commit('user/saveLoginUser', res.data.admin)
            this.$store.commit('user/saveLoginRole', 'admin')
            setTimeout(() => {
              this.$router.push("/dashboard")
              this.submitLoading = false
            }, 700)
            this.$message.success("登录成功")
          } else {
            this.submitLoading = false
          }
        })
      }
    },

    submitUserLogin() {
      if (!this.checkEmail(this.userLogin.email)) {
        return
      }
      if (!this.userLogin.password || this.userLogin.password.length < 6) {
        this.$message.error("密码长度至少6位")
        return
      }
      this.submitLoading = true
      LoginUser({
        email: this.userLogin.email,
        password: this.userLogin.password,
      }).then((res) => {
        if (res.status) {
          this.$store.commit('user/saveToken', res.data.token)
          this.$store.commit('user/saveLoginUser', res.data.user)
          this.$store.commit('user/saveLoginRole', 'user')
          setTimeout(() => {
            this.$router.push("/user/dashboard")
            this.submitLoading = false
          }, 700)
          this.$message.success("登录成功")
        } else {
          this.submitLoading = false
        }
      })
    },

    submitRegister() {
      if (!this.checkEmail(this.registerForm.email)) {
        return
      }
      if (!this.registerForm.code) {
        this.$message.error("请输入验证码")
        return
      }
      if (!this.registerForm.password || this.registerForm.password.length < 6) {
        this.$message.error("密码长度至少6位")
        return
      }
      if (this.registerForm.password !== this.registerForm.confirmPassword) {
        this.$message.error("两次输入的密码不一致")
        return
      }
      this.submitLoading = true
      RegisterUser({
        email: this.registerForm.email,
        password: this.registerForm.password,
        code: this.registerForm.code,
      }).then((res) => {
        if (res.status) {
          this.$message.success("注册成功")
          this.registerForm = {email: '', code: '', password: '', confirmPassword: ''}
          this.submitType = '1'
        }
        this.submitLoading = false
      })
    },

    sendRegisterEmail() {
      if (!this.checkEmail(this.registerForm.email)) {
        return
      }
      this.sendLoading = true
      SendUserEmail(this.registerForm.email).then((res) => {
        if (res.status) this.$message.success("验证码发送成功")
        this.sendLoading = false
      })
    },

    tabClick(key) {
      this.submitType = key
    },

    checkEmail(emailValue) {
      const emailRegex = new RegExp('^\\w{3,}(\\.\\w+)*@[A-z0-9]+(\\.[A-z]{2,5}){1,2}$')
      if (!emailRegex.test(emailValue)) {
        this.$message.error('请输入正确格式的邮箱');
        return false
      } else {
        return true
      }
    },

  }

}
</script>

<style scoped>

#login-container {
  background: url('../assets/login-background.png');
  background-size: cover;
  background-repeat: no-repeat;
  background-position: center;
  height: 100vh;
  width: 100vw;
  margin: 0;
  overflow: hidden;
  position: fixed;
  top: 0;
  left: 0;
}

>>> .ant-btn-primary {
  border-color: #5a84fd;
}

.login-box {
  width: 350px;
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
}

.box-header {
  display: flex;
  align-items: center;
  justify-content: center;
}

.box-header-t {
  font-weight: bolder;
  font-size: 38px;
  letter-spacing: 6px;
}

.logo {
  width: 42px;
  height: 42px;
  margin-right: 14px;
}

>>> .ant-input-affix-wrapper .ant-input {
  font-size: 12px !important;
}

.title {
  color: #91949c;
  padding-top: 15px;
  padding-bottom: 35px;
  font-size: 13px;
  text-align: center;
  letter-spacing: 4px;
}

.input {
  margin-bottom: 25px;
  font-size: 10px;
}

.hint {
  color: #91949c;
  font-size: 12px;
  line-height: 18px;
  margin: -10px 0 16px;
}

.code-btn {
  height: 40px;
  margin-left: 20px;
}

.submit-btn {
  letter-spacing: 2px;
  background: #5a84fd;
  width: 100%;
  height: 45px;
}

>>> .ant-tabs-bar {
  border-bottom: none !important;
}

>>> .ant-tabs-nav {
  width: 100%;
}

>>> .ant-tabs-tab {
  padding: 0 10px;
}

>>> .ant-tabs-ink-bar {
  display: none !important;
}

</style>
