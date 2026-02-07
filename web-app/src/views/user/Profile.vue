<template>
  <div class="user-profile">
    <!-- 顶部 Banner -->
    <div class="profile-header">
      <div class="banner-content">
        <div class="user-info-overlay">
          <a-avatar :size="80" icon="user" class="user-avatar" style="backgroundColor: #87d068">{{ avatarText }}</a-avatar>
          <div class="user-text">
            <h1 class="user-name">{{ form.name || '用户' }}</h1>
            <p class="user-email">{{ form.email }}</p>
          </div>
        </div>
      </div>
    </div>

    <div class="profile-content">
      <a-row :gutter="24">
        <!-- 左侧个人卡片 -->
        <a-col :span="7">
          <a-card :bordered="false" class="profile-card">
            <div class="personal-info">
              <div class="info-item">
                <a-icon type="idcard" />
                <span class="label">身份</span>
                <span class="value"><a-tag color="blue">普通用户</a-tag></span>
              </div>
              <a-divider />
              <div class="info-item">
                <a-icon type="mobile" />
                <span class="label">手机</span>
                <span class="value">{{ form.phone || '未设置' }}</span>
              </div>
              <div class="info-item">
                <a-icon type="bank" />
                <span class="label">单位</span>
                <span class="value">{{ form.company || '未设置' }}</span>
              </div>
              <div class="info-item">
                <a-icon type="home" />
                <span class="label">地址</span>
                <span class="value">{{ form.address || '未设置' }}</span>
              </div>
              <a-divider />
              <div class="info-item">
                <a-icon type="calendar" />
                <span class="label">注册时间</span>
                <span class="value">2026-01-01</span> <!-- 这里暂时写死或从后端获取 -->
              </div>
            </div>
          </a-card>
        </a-col>

        <!-- 右侧设置面板 -->
        <a-col :span="17">
          <a-card :bordered="false" class="settings-card" :tabList="tabList" :active-tab-key="activeTabKey" @tabChange="key => onTabChange(key, 'activeTabKey')">
            
            <!-- 基本设置 -->
            <div v-if="activeTabKey === 'basic'" class="tab-content">
              <div class="setting-title">基本资料</div>
              <a-form-model :model="form" layout="vertical">
                <a-row :gutter="24">
                  <a-col :span="12">
                    <a-form-model-item label="姓名">
                      <a-input v-model="form.name" placeholder="请输入姓名"/>
                    </a-form-model-item>
                  </a-col>
                  <a-col :span="12">
                    <a-form-model-item label="手机号">
                      <a-input v-model="form.phone" placeholder="请输入手机号"/>
                    </a-form-model-item>
                  </a-col>
                  <a-col :span="12">
                    <a-form-model-item label="所属单位">
                      <a-input v-model="form.company" placeholder="请输入单位名称"/>
                    </a-form-model-item>
                  </a-col>
                  <a-col :span="12">
                    <a-form-model-item label="电子邮箱">
                      <a-input v-model="form.email" disabled placeholder="注册邮箱不可修改"/>
                    </a-form-model-item>
                  </a-col>
                  <a-col :span="24">
                    <a-form-model-item label="联系地址">
                      <a-textarea v-model="form.address" placeholder="请输入详细地址" :rows="3"/>
                    </a-form-model-item>
                  </a-col>
                </a-row>
                <a-form-model-item>
                  <a-button type="primary" size="large" :loading="saving" @click="saveProfile">更新基本信息</a-button>
                  <a-button size="large" style="margin-left: 12px" @click="resetProfile">重置</a-button>
                </a-form-model-item>
              </a-form-model>
            </div>

            <!-- 安全设置 -->
            <div v-if="activeTabKey === 'security'" class="tab-content">
              <div class="setting-title">账号安全</div>
              <a-alert message="为了您的账号安全，建议定期修改密码。" type="info" show-icon style="margin-bottom: 24px" />
              
              <a-form-model :model="passwordForm" layout="vertical" class="security-form">
                <a-form-model-item label="当前密码" required>
                  <a-input-password v-model="passwordForm.oldPassword" placeholder="请输入当前使用的密码"/>
                </a-form-model-item>
                <a-form-model-item label="新密码" required>
                  <a-input-password v-model="passwordForm.newPassword" placeholder="请输入新密码（至少6位）"/>
                </a-form-model-item>
                <a-form-model-item label="确认新密码" required>
                  <a-input-password v-model="passwordForm.confirmPassword" placeholder="请再次输入新密码"/>
                </a-form-model-item>
                <a-form-model-item>
                  <a-button type="primary" size="large" :loading="changingPassword" @click="changePassword">修改密码</a-button>
                  <a-button size="large" style="margin-left: 12px" @click="resetPasswordForm">重置</a-button>
                </a-form-model-item>
              </a-form-model>
            </div>

          </a-card>
        </a-col>
      </a-row>
    </div>
  </div>
</template>

<script>
import request from "@/utils/request";

export default {
  name: "UserProfile",
  data() {
    return {
      activeTabKey: 'basic',
      tabList: [
        { key: 'basic', tab: '基本设置' },
        { key: 'security', tab: '安全设置' },
      ],
      loading: false,
      saving: false,
      changingPassword: false,
      form: {
        name: "",
        email: "",
        phone: "",
        address: "",
        company: ""
      },
      original: {},
      passwordForm: {
        oldPassword: "",
        newPassword: "",
        confirmPassword: ""
      }
    }
  },

  computed: {
    avatarText() {
      return this.form.name ? this.form.name.substring(0, 1) : "U";
    }
  },

  mounted() {
    this.loadProfile()
  },

  methods: {
    onTabChange(key, type) {
      this[type] = key;
    },
    loadProfile() {
      this.loading = true
      request({
        url: '/user/profile',
        method: 'get'
      }).then((res) => {
        if (res.status && res.data) {
          this.form = {
            name: res.data.name || "",
            email: res.data.email || "",
            phone: res.data.phone || "",
            address: res.data.address || "",
            company: res.data.company || ""
          }
          this.original = {...this.form}
        }
        this.loading = false
      }).catch(() => {
        this.loading = false
        this.$message.error("加载用户信息失败")
      })
    },

    saveProfile() {
      this.saving = true
      request({
        url: '/user/profile',
        method: 'put',
        data: this.form
      }).then((res) => {
        if (res.status && res.data) {
          this.$message.success("保存成功")
          this.form = {
            name: res.data.name || "",
            email: res.data.email || "",
            phone: res.data.phone || "",
            address: res.data.address || "",
            company: res.data.company || ""
          }
          this.original = {...this.form}
          // 更新store中的用户信息
          this.$store.commit('user/saveLoginUser', res.data)
        }
        this.saving = false
      }).catch(() => {
        this.saving = false
        this.$message.error("保存失败")
      })
    },

    resetProfile() {
      this.form = {...this.original}
    },

    changePassword() {
      if (!this.passwordForm.oldPassword) {
        this.$message.warning("请输入原密码")
        return
      }
      if (!this.passwordForm.newPassword || this.passwordForm.newPassword.length < 6) {
        this.$message.warning("新密码长度至少6位")
        return
      }
      if (this.passwordForm.newPassword !== this.passwordForm.confirmPassword) {
        this.$message.warning("两次输入的密码不一致")
        return
      }
      this.changingPassword = true
      request({
        url: '/user/password',
        method: 'put',
        data: {
          oldPassword: this.passwordForm.oldPassword,
          newPassword: this.passwordForm.newPassword
        }
      }).then((res) => {
        if (res.status) {
          this.$message.success("密码修改成功")
          this.resetPasswordForm()
        }
        this.changingPassword = false
      }).catch(() => {
        this.changingPassword = false
      })
    },

    resetPasswordForm() {
      this.passwordForm = {
        oldPassword: "",
        newPassword: "",
        confirmPassword: ""
      }
    }
  }
}
</script>

<style scoped>
.user-profile {
  min-height: 100vh;
  background: #f0f2f5;
}

.profile-header {
  height: 200px;
  background: linear-gradient(135deg, #1890ff 0%, #36cfc9 100%);
  position: relative;
  margin-bottom: 60px;
}

.banner-content {
  max-width: 1200px;
  margin: 0 auto;
  position: relative;
  height: 100%;
}

.user-info-overlay {
  position: absolute;
  bottom: -40px;
  left: 32px;
  display: flex;
  align-items: flex-end;
}

.user-avatar {
  border: 4px solid #fff;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  font-size: 32px;
}

.user-text {
  margin-left: 20px;
  margin-bottom: 10px;
}

.user-name {
  color: #fff;
  font-size: 28px;
  font-weight: 600;
  margin: 0;
  text-shadow: 0 2px 4px rgba(0,0,0,0.2);
}

.user-email {
  color: rgba(255,255,255,0.9);
  margin: 4px 0 0 0;
  font-size: 14px;
}

.profile-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}

.profile-card, .settings-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  background: #fff;
  height: 100%;
}

.personal-info {
  padding: 12px 0;
}

.info-item {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
  color: #333;
}

.info-item .anticon {
  font-size: 16px;
  color: #999;
  margin-right: 12px;
}

.info-item .label {
  color: #666;
  width: 70px;
}

.info-item .value {
  flex: 1;
  color: #333;
  font-weight: 500;
  word-break: break-all;
}

.tab-content {
  padding: 8px 0;
}

.setting-title {
  font-size: 20px;
  font-weight: 500;
  color: #333;
  margin-bottom: 24px;
}

.security-form {
  max-width: 500px;
}
</style>
