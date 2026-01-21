<template>
  <div class="page">
    <a-card title="用户信息" :loading="loading">
      <a-form-model :model="form" :label-col="{span: 6}" :wrapper-col="{span: 14}">
        <a-form-model-item label="姓名">
          <a-input v-model="form.name" placeholder="请输入姓名"/>
        </a-form-model-item>
        <a-form-model-item label="邮箱">
          <a-input v-model="form.email" disabled placeholder="注册邮箱"/>
        </a-form-model-item>
        <a-form-model-item label="手机号">
          <a-input v-model="form.phone" placeholder="请输入手机号"/>
        </a-form-model-item>
        <a-form-model-item label="所属单位">
          <a-input v-model="form.company" placeholder="请输入单位"/>
        </a-form-model-item>
        <a-form-model-item label="联系地址">
          <a-input v-model="form.address" placeholder="请输入地址"/>
        </a-form-model-item>
      </a-form-model>
      <div class="actions">
        <a-button type="primary" :loading="saving" @click="saveProfile">保存</a-button>
        <a-button style="margin-left: 8px" @click="resetProfile">重置</a-button>
      </div>
    </a-card>

    <a-card title="修改密码" style="margin-top: 16px">
      <a-form-model :model="passwordForm" :label-col="{span: 6}" :wrapper-col="{span: 14}">
        <a-form-model-item label="原密码">
          <a-input-password v-model="passwordForm.oldPassword" placeholder="请输入原密码"/>
        </a-form-model-item>
        <a-form-model-item label="新密码">
          <a-input-password v-model="passwordForm.newPassword" placeholder="请输入新密码（至少6位）"/>
        </a-form-model-item>
        <a-form-model-item label="确认密码">
          <a-input-password v-model="passwordForm.confirmPassword" placeholder="请再次输入新密码"/>
        </a-form-model-item>
      </a-form-model>
      <div class="actions">
        <a-button type="primary" :loading="changingPassword" @click="changePassword">修改密码</a-button>
        <a-button style="margin-left: 8px" @click="resetPasswordForm">重置</a-button>
      </div>
    </a-card>
  </div>
</template>

<script>
import request from "@/utils/request";

export default {
  data() {
    return {
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

  mounted() {
    this.loadProfile()
  },

  methods: {
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
          // 更新store中的用户信息，让右上角显示更新后的姓名
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
.page {
  padding: 24px;
}

.actions {
  text-align: right;
}
</style>
