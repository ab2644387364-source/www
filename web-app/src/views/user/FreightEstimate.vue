<template>
  <div class="freight-estimate-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1><a-icon type="calculator" /> 运费估算</h1>
      <p>填写货运信息，快速获取预估费用</p>
    </div>

    <a-row :gutter="24">
      <!-- 左侧：表单区域 -->
      <a-col :span="14">
        <a-card title="货运信息" :bordered="false" class="form-card">
          <a-form-model :model="form" :label-col="{span: 5}" :wrapper-col="{span: 17}">
            <a-form-model-item label="起点" required>
              <a-input 
                v-model="form.origin" 
                placeholder="请输入起点城市或地址"
                size="large"
              >
                <a-icon slot="prefix" type="environment" style="color: #52c41a" />
              </a-input>
            </a-form-model-item>

            <a-form-model-item label="终点" required>
              <a-input 
                v-model="form.destination" 
                placeholder="请输入目的地城市或地址"
                size="large"
              >
                <a-icon slot="prefix" type="environment" style="color: #1890ff" />
              </a-input>
            </a-form-model-item>

            <a-form-model-item label="货物类型" required>
              <a-radio-group v-model="form.type" button-style="solid" size="large">
                <a-radio-button value="牛">
                  🐂 牛
                </a-radio-button>
                <a-radio-button value="羊">
                  🐑 羊
                </a-radio-button>
                <a-radio-button value="猪">
                  🐷 猪
                </a-radio-button>
              </a-radio-group>
            </a-form-model-item>

            <a-form-model-item label="数量" required>
              <a-input-number 
                v-model="form.quantity" 
                :min="1" 
                :max="1000"
                size="large"
                style="width: 200px"
              />
              <span class="unit-label">{{ getUnitLabel(form.type) }}</span>
            </a-form-model-item>

            <a-form-model-item :wrapper-col="{span: 17, offset: 5}">
              <a-button 
                type="primary" 
                size="large" 
                :loading="estimating"
                @click="handleEstimate"
                icon="search"
              >
                开始估算
              </a-button>
              <a-button size="large" style="margin-left: 12px" @click="resetForm">
                重置
              </a-button>
            </a-form-model-item>
          </a-form-model>
        </a-card>

        <!-- 价格参考 -->
        <a-card title="价格参考" :bordered="false" class="price-card" style="margin-top: 20px">
          <a-table 
            :columns="priceColumns" 
            :data-source="priceList" 
            :pagination="false"
            size="small"
            rowKey="type"
          >
            <template slot="type" slot-scope="text">
              <span class="type-icon">{{ getTypeIcon(text) }}</span>
              {{ text }}
            </template>
          </a-table>
          <div class="price-note">
            <a-icon type="info-circle" /> 
            跨城运输运费 × 1.5，同城运输按基础价格计算
          </div>
        </a-card>
      </a-col>

      <!-- 右侧：结果区域 -->
      <a-col :span="10">
        <a-card :bordered="false" class="result-card" :class="{'has-result': result}">
          <template v-if="result">
            <div class="result-header">
              <a-icon type="check-circle" theme="filled" class="success-icon" />
              <span>估算完成</span>
            </div>
            
            <div class="result-amount">
              <span class="currency">¥</span>
              <span class="value">{{ result.amount }}</span>
            </div>

            <a-divider />

            <div class="result-details">
              <div class="detail-row">
                <span class="label">路线</span>
                <span class="value">
                  <a-icon type="environment" style="color: #52c41a" />
                  {{ result.origin }}
                  <a-icon type="arrow-right" style="margin: 0 8px" />
                  <a-icon type="environment" style="color: #1890ff" />
                  {{ result.destination }}
                </span>
              </div>
              <div class="detail-row">
                <span class="label">货物</span>
                <span class="value">{{ result.type }} × {{ result.quantity }}{{ getUnitLabel(result.type) }}</span>
              </div>
              <div class="detail-row">
                <span class="label">单价</span>
                <span class="value">¥{{ result.breakdown.unitPrice }}/{{ getUnitLabel(result.type) }}</span>
              </div>
              <div class="detail-row">
                <span class="label">基础运费</span>
                <span class="value">¥{{ result.breakdown.baseFreight }}</span>
              </div>
              <div class="detail-row" v-if="result.breakdown.isCrossCity">
                <span class="label">跨城系数</span>
                <span class="value highlight">× {{ result.breakdown.distanceFactor }}</span>
              </div>
            </div>

            <div class="result-actions">
              <a-button type="primary" size="large" block @click="goCreateOrder">
                <a-icon type="shopping-cart" /> 立即下单
              </a-button>
            </div>
          </template>

          <template v-else>
            <div class="empty-result">
              <a-icon type="calculator" class="empty-icon" />
              <p>填写左侧信息后点击"开始估算"</p>
              <p class="sub-text">获取预估运费</p>
            </div>
          </template>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script>
import { EstimateFreight } from '@/api/freight'

export default {
  name: 'FreightEstimate',
  data() {
    return {
      estimating: false,
      form: {
        origin: '',
        destination: '',
        type: '牛',
        quantity: 1
      },
      result: null,
      priceColumns: [
        { title: '货物类型', dataIndex: 'type', scopedSlots: { customRender: 'type' } },
        { title: '单价', dataIndex: 'price' },
        { title: '计量单位', dataIndex: 'unit' }
      ],
      priceList: [
        { type: '牛', price: '¥200', unit: '元/头' },
        { type: '羊', price: '¥50', unit: '元/只' },
        { type: '猪', price: '¥80', unit: '元/头' }
      ]
    }
  },

  methods: {
    handleEstimate() {
      // 表单校验
      if (!this.form.origin) {
        this.$message.warning('请输入起点')
        return
      }
      if (!this.form.destination) {
        this.$message.warning('请输入终点')
        return
      }
      if (!this.form.type) {
        this.$message.warning('请选择货物类型')
        return
      }
      if (!this.form.quantity || this.form.quantity <= 0) {
        this.$message.warning('请输入有效数量')
        return
      }

      this.estimating = true
      EstimateFreight(this.form)
        .then(res => {
          if (res.status && res.data) {
            this.result = res.data
            this.$message.success('估算完成')
          } else {
            this.$message.error(res.message || '估算失败')
          }
          this.estimating = false
        })
        .catch(() => {
          this.$message.error('估算失败，请稍后重试')
          this.estimating = false
        })
    },

    resetForm() {
      this.form = {
        origin: '',
        destination: '',
        type: '牛',
        quantity: 1
      }
      this.result = null
    },

    goCreateOrder() {
      // 携带参数跳转到创建订单页面
      this.$router.push({
        path: '/user/order/create',
        query: {
          origin: this.form.origin,
          destination: this.form.destination,
          type: this.form.type,
          quantity: this.form.quantity
        }
      })
    },

    getUnitLabel(type) {
      const map = { '牛': '头', '羊': '只', '猪': '头' }
      return map[type] || '头'
    },

    getTypeIcon(type) {
      const map = { '牛': '🐂', '羊': '🐑', '猪': '🐷' }
      return map[type] || ''
    }
  }
}
</script>

<style scoped>
.freight-estimate-page {
  padding: 24px;
  background: #f0f2f5;
  min-height: 100vh;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h1 {
  font-size: 24px;
  font-weight: 600;
  margin: 0;
  color: #333;
}

.page-header p {
  color: #666;
  margin: 8px 0 0 0;
}

.form-card, .result-card, .price-card {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}

.unit-label {
  margin-left: 12px;
  color: #666;
}

/* 价格参考卡片 */
.price-note {
  margin-top: 16px;
  padding: 12px;
  background: #f6f8fa;
  border-radius: 6px;
  color: #666;
  font-size: 13px;
}

.type-icon {
  margin-right: 6px;
}

/* 结果卡片 */
.result-card {
  min-height: 400px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.result-card.has-result {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}

.result-header {
  text-align: center;
  margin-bottom: 16px;
}

.result-header .success-icon {
  font-size: 32px;
  color: #52c41a;
  margin-right: 8px;
}

.result-header span {
  font-size: 18px;
  font-weight: 500;
}

.result-amount {
  text-align: center;
  padding: 20px 0;
}

.result-amount .currency {
  font-size: 24px;
  vertical-align: top;
}

.result-amount .value {
  font-size: 56px;
  font-weight: 700;
}

.result-card.has-result .ant-divider {
  border-color: rgba(255, 255, 255, 0.3);
}

.result-details {
  padding: 0 16px;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  font-size: 14px;
}

.detail-row .label {
  opacity: 0.8;
}

.detail-row .highlight {
  color: #ffd666;
  font-weight: 600;
}

.result-actions {
  margin-top: 24px;
  padding: 0 16px;
}

.result-actions .ant-btn-primary {
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.4);
  height: 48px;
  font-size: 16px;
}

.result-actions .ant-btn-primary:hover {
  background: rgba(255, 255, 255, 0.3);
}

/* 空状态 */
.empty-result {
  text-align: center;
  padding: 60px 20px;
  color: #999;
}

.empty-result .empty-icon {
  font-size: 64px;
  color: #d9d9d9;
  margin-bottom: 16px;
}

.empty-result p {
  margin: 8px 0;
  font-size: 16px;
}

.empty-result .sub-text {
  font-size: 14px;
  color: #bbb;
}
</style>
