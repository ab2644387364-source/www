<template>
  <div class="page">
    <a-card title="用户信息" :loading="loading">
      <a-form-model :model="form" :label-col="{span: 6}" :wrapper-col="{span: 14}">
        <a-form-model-item label="姓名">
          <a-input v-model="form.name" placeholder="请输入姓名"/>
        </a-form-model-item>
        <a-form-model-item label="邮箱">
          <a-input v-model="form.email" placeholder="请输入邮箱"/>
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
  </div>
</template>

<script>
import {FetchUserProfile, UpdateUserProfile} from "@/api/portal";

export default {
  data() {
    return {
      loading: false,
      saving: false,
      form: {
        name: "",
        email: "",
        phone: "",
        address: "",
        company: ""
      },
      original: {}
    }
  },

  mounted() {
    this.loadProfile()
  },

  methods: {
    loadProfile() {
      this.loading = true
      FetchUserProfile().then((res) => {
        if (res.status) {
          this.form = {...res.data}
          this.original = {...res.data}
        }
        this.loading = false
      })
    },

    saveProfile() {
      this.saving = true
      UpdateUserProfile(this.form).then((res) => {
        if (res.status) {
          this.$message.success("保存成功")
          this.original = {...this.form}
        }
        this.saving = false
      })
    },

    resetProfile() {
      this.form = {...this.original}
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
