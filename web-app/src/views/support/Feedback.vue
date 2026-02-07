<template>
  <div class="feedback-page">
    <a-page-header title="投诉建议" @back="() => $router.push('/support')">
    </a-page-header>

    <a-tabs v-model="activeTab">
      <!-- 提交反馈 -->
      <a-tab-pane key="submit" tab="提交反馈">
        <a-card>
          <a-form :form="form" @submit="handleSubmit">
            <a-form-item label="反馈类型">
              <a-radio-group v-decorator="['type', { initialValue: 'suggestion', rules: [{ required: true }] }]">
                <a-radio value="complaint">投诉</a-radio>
                <a-radio value="suggestion">建议</a-radio>
                <a-radio value="inquiry">咨询</a-radio>
              </a-radio-group>
            </a-form-item>

            <a-form-item label="标题">
              <a-input
                v-decorator="['title', { rules: [{ required: true, message: '请输入标题' }] }]"
                placeholder="请简要描述您的问题"
                :maxLength="50"
              />
            </a-form-item>

            <a-form-item label="详细内容">
              <a-textarea
                v-decorator="['content', { rules: [{ required: true, message: '请输入详细内容' }] }]"
                placeholder="请详细描述您遇到的问题或建议"
                :rows="6"
                :maxLength="500"
              />
            </a-form-item>

            <a-form-item>
              <a-button type="primary" html-type="submit" :loading="submitting" block size="large">
                <a-icon type="send" /> 提交反馈
              </a-button>
            </a-form-item>
          </a-form>
        </a-card>
      </a-tab-pane>

      <!-- 历史反馈 -->
      <a-tab-pane key="history" tab="我的反馈">
        <a-spin :spinning="loading">
          <a-list
            item-layout="vertical"
            :data-source="feedbackList"
            :locale="{ emptyText: '暂无反馈记录' }"
          >
            <a-list-item slot="renderItem" slot-scope="item">
              <a-list-item-meta>
                <template slot="title">
                  <span>{{ item.title }}</span>
                  <a-tag :color="getTypeColor(item.type)" style="margin-left: 8px">{{ getTypeText(item.type) }}</a-tag>
                  <a-tag :color="getStatusColor(item.status)">{{ getStatusText(item.status) }}</a-tag>
                </template>
                <template slot="description">
                  <div>{{ item.createTime }}</div>
                </template>
              </a-list-item-meta>
              <div class="feedback-content">{{ item.content }}</div>
              
              <!-- 回复区域 -->
              <div v-if="item.reply" class="reply-section">
                <a-divider style="margin: 12px 0" />
                <div class="reply-header">
                  <a-icon type="customer-service" style="color: #1890ff" />
                  <span style="margin-left: 8px; color: #1890ff; font-weight: 500">客服回复</span>
                  <span style="margin-left: 12px; color: #999; font-size: 12px">{{ item.replyTime }}</span>
                </div>
                <div class="reply-content">{{ item.reply }}</div>
              </div>
            </a-list-item>
          </a-list>
        </a-spin>
      </a-tab-pane>
    </a-tabs>
  </div>
</template>

<script>
import { CreateFeedback, GetUserFeedback } from '@/api/support'

export default {
  name: 'Feedback',
  data() {
    return {
      activeTab: 'submit',
      form: this.$form.createForm(this),
      submitting: false,
      loading: false,
      feedbackList: []
    }
  },
  computed: {
    userId() {
      const user = JSON.parse(localStorage.getItem('user') || '{}')
      return user.id
    }
  },
  watch: {
    activeTab(val) {
      if (val === 'history') {
        this.loadFeedback()
      }
    }
  },
  methods: {
    handleSubmit(e) {
      e.preventDefault()
      this.form.validateFields((err, values) => {
        if (!err) {
          this.submitting = true
          const data = {
            ...values,
            userId: this.userId
          }
          CreateFeedback(data).then(res => {
            if (res.data && res.data.status) {
              this.$message.success('反馈提交成功！我们会尽快处理')
              this.form.resetFields()
              this.activeTab = 'history'
              this.loadFeedback()
            } else {
              this.$message.error((res.data && res.data.message) || '提交失败')
            }
            this.submitting = false
          }).catch(() => {
            this.$message.error('提交失败，请稍后重试')
            this.submitting = false
          })
        }
      })
    },
    loadFeedback() {
      if (!this.userId) return
      this.loading = true
      GetUserFeedback(this.userId).then(res => {
        if (res.data && res.data.status) {
          this.feedbackList = res.data.data || []
        }
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    getTypeText(type) {
      const map = { complaint: '投诉', suggestion: '建议', inquiry: '咨询' }
      return map[type] || type
    },
    getTypeColor(type) {
      const map = { complaint: 'red', suggestion: 'blue', inquiry: 'orange' }
      return map[type] || 'default'
    },
    getStatusText(status) {
      const map = { pending: '待处理', processing: '处理中', resolved: '已解决', closed: '已关闭' }
      return map[status] || status
    },
    getStatusColor(status) {
      const map = { pending: 'orange', processing: 'blue', resolved: 'green', closed: 'default' }
      return map[status] || 'default'
    }
  }
}
</script>

<style scoped>
.feedback-page {
  padding: 24px;
  background: #fff;
  min-height: 100vh;
}

.feedback-content {
  color: #595959;
  line-height: 1.6;
  margin-top: 8px;
}

.reply-section {
  background: #f6ffed;
  padding: 12px;
  border-radius: 8px;
  margin-top: 12px;
}

.reply-header {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.reply-content {
  color: #52c41a;
  line-height: 1.6;
}
</style>
