<template>
  <div>
    <a-table :loading="loading" :columns="columns" :data-source="data" bordered rowKey="id">
      <span slot="status" slot-scope="status">
        <a-tag v-if="status===0" color="#f50">待调度</a-tag>
        <a-tag v-if="status===1" color="#87d068">转运中</a-tag>
        <a-tag v-if="status===2" color="#2db7f5">已完成</a-tag>
      </span>
      <span slot="warningLevel" slot-scope="warningLevel">
        <a-tag v-if="warningLevel===2" color="#f5222d">严重</a-tag>
        <a-tag v-else-if="warningLevel===1" color="#fa8c16">一般</a-tag>
        <a-tag v-else color="#52c41a">正常</a-tag>
      </span>
      <template
          v-for="col in ['phone','address','routePlan','currentNode']"
          :slot="col"
          slot-scope="text, record, index"
      >
        <div :key="col">
          <a-input
              v-if="record.editable"
              style="margin: -5px 0"
              :value="text"
              @change="e => handleChange(e.target.value, record.id, col)"
          />
          <template v-else>
            {{ text }}
          </template>
        </div>
      </template>
      <template slot="operation" slot-scope="text, record, index">
        <div class="editable-row-operations">
          <span v-if="record.editable">
          <a @click="() => save(record.id, index)">保存</a>
          <a-popconfirm title="Sure to cancel?" @confirm="() => cancel(record.id)">
            <a>取消</a>
          </a-popconfirm>
        </span>
          <span v-else>
          <a :disabled="editingKey !== ''" @click="() => edit(record.id)">编辑</a>
        </span>
          <a-button @click="review(index)" type="link" v-if="record.status===0">调度</a-button>
          <a-button @click="review(index)" type="link" v-if="record.status===1">跟踪</a-button>
          <a-button @click="review(index)" type="link" v-if="record.status===2">查看</a-button>
        </div>
      </template>
    </a-table>

    <a-modal
        title="转运详情"
        :visible="visible"
        @ok="submitForm"
        @cancel="visible = false"
    >
      <a-form-model :model="form">
        <a-form-model-item label="姓名">
          <a-input v-model="form.name" placeholder="请输入押运员姓名"/>
        </a-form-model-item>
        <a-form-model-item label="身份证号">
          <a-input v-model="form.idCard" placeholder="请输入押运员身份证信息"/>
        </a-form-model-item>
        <a-form-model-item label="联系方式">
          <a-input v-model="form.phone" placeholder="请输入手机号码"/>
        </a-form-model-item>
        <a-form-item label="驾照信息">
          <a-row :gutter="20">
            <a-col :span="12">
            <a-input v-model="form.license" addon-before="驾驶证" default-value="0571"/>
          </a-col>
            <a-col :span="7">
              <a-input-number v-model="form.score" addon-before="分数" default-value="12" :min="0" :max="12"/>
            </a-col>
          </a-row>
        </a-form-item>
        <a-form-model-item label="性别">
          <a-radio-group v-model="form.gender">
            <a-radio value="男性">男性</a-radio>
            <a-radio value="女性">女性</a-radio>
          </a-radio-group>
        </a-form-model-item>
        <a-form-model-item label="住址">
          <a-input v-model="form.address" type="textarea"/>
        </a-form-model-item>
      </a-form-model>
    </a-modal>

    <a-modal
        title="转运调度与跟踪"
        :visible="visible2"
        width="60%"
        :footer="null"
        @cancel="visible2 = false"
    >
      <a-steps :current="select.status" style="padding: 50px">
        <a-step title="确认调度信息"/>
        <a-step title="开始转运"/>
        <a-step title="转运完成"/>
      </a-steps>
      <div class="content">
        <div v-if="select.status === 0" class="check">
          <p>押运员： {{ select.driver }}</p>
          <p>车牌号码： {{ select.number }}</p>
          <p>起点： {{ select.origin }}</p>
          <p>目的地： {{ select.destination }}</p>
          <p>加急处理： {{ select.urgent }}</p>
          <p>注意事项： {{ select.care }}</p>
          <p>联系人电话： {{ select.phone }}</p>
          <p>目的地地址： {{ select.address }}</p>
          <p>预计送达： {{ select.time }}</p>
          <a-button type="danger" style="margin-right: 20px" :loading="loading" @click="agree">通过</a-button>
          <a-button @click="visible2 = false">不通过</a-button>
        </div>
        <div v-if="select.status === 1">
          <a-result
              status="success"
              title="审核通过，转运中"
              >
            <template #extra>
              <a-button @click="service" key="console" type="primary">
                已送达
              </a-button>
            </template>
          </a-result>
        </div>
        <div v-if="select.status === 2">
          <a-result
              status="success"
              title="转运单已完成"
          >
            <template #extra>
              <a-button @click="visible2 = false" key="console" type="primary">
                确定
              </a-button>
            </template>
          </a-result>
        </div>
        <div class="track-edit">
          <a-divider>路线跟踪与异常</a-divider>
          <a-form-model :model="select" :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
            <a-form-model-item label="路线规划">
              <a-input v-model="select.routePlan"/>
            </a-form-model-item>
            <a-form-model-item label="途经节点">
              <a-input v-model="select.routeNodes"/>
            </a-form-model-item>
            <a-form-model-item label="当前位置">
              <a-input v-model="select.currentNode"/>
            </a-form-model-item>
            <a-form-model-item label="异常等级">
              <a-select v-model="select.warningLevel">
                <a-select-option :value="0">无</a-select-option>
                <a-select-option :value="1">一般</a-select-option>
                <a-select-option :value="2">严重</a-select-option>
              </a-select>
            </a-form-model-item>
            <a-form-model-item label="异常说明">
              <a-input v-model="select.warningNote" type="textarea"/>
            </a-form-model-item>
          </a-form-model>
          <a-button type="primary" :loading="saving" @click="saveTrack">
            保存跟踪信息
          </a-button>
        </div>

        <!-- 物流时间线 -->
        <div class="timeline-section">
          <a-divider>物流追踪时间线</a-divider>
          <a-row :gutter="16">
            <a-col :span="12">
              <a-timeline mode="left" v-if="trackRecords.length > 0">
                <a-timeline-item
                  v-for="record in trackRecords"
                  :key="record.id"
                  :color="getTimelineColor(record.status)"
                >
                  <p class="timeline-title">{{ record.node }} - {{ record.status }}</p>
                  <p class="timeline-desc">{{ record.remark }}</p>
                  <p class="timeline-time">{{ record.createTime }}</p>
                </a-timeline-item>
              </a-timeline>
              <a-empty v-else description="暂无追踪记录" />
            </a-col>
            <a-col :span="12">
              <a-card title="添加追踪记录" size="small">
                <a-form-model :model="newTrack" :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
                  <a-form-model-item label="节点">
                    <a-input v-model="newTrack.node" placeholder="如: 检查站A"/>
                  </a-form-model-item>
                  <a-form-model-item label="状态">
                    <a-select v-model="newTrack.status" placeholder="选择状态">
                      <a-select-option value="到达">到达</a-select-option>
                      <a-select-option value="离开">离开</a-select-option>
                      <a-select-option value="装货">装货</a-select-option>
                      <a-select-option value="卸货">卸货</a-select-option>
                      <a-select-option value="异常">异常</a-select-option>
                    </a-select>
                  </a-form-model-item>
                  <a-form-model-item label="备注">
                    <a-input v-model="newTrack.remark" type="textarea" placeholder="补充说明"/>
                  </a-form-model-item>
                  <a-form-model-item :wrapper-col="{ span: 16, offset: 6 }">
                    <a-button type="primary" :loading="addingTrack" @click="addTrackRecord">
                      添加记录
                    </a-button>
                  </a-form-model-item>
                </a-form-model>
              </a-card>
            </a-col>
          </a-row>
        </div>
      </div>

    </a-modal>

  </div>
</template>

<script>
import {FindAllDistribution, SaveDistribution} from "../../api/distribution";
import {GetTrackRecords, AddTrackRecord} from "../../api/notification";

const columns = [
  {
    title: '押运员',
    dataIndex: 'driver',
    scopedSlots: {customRender: 'driver'},
  },
  {
    title: '车牌号',
    dataIndex: 'number',
    scopedSlots: {customRender: 'number'},
  },
  {
    title: '起点',
    dataIndex: 'origin',
  },
  {
    title: '目的地',
    dataIndex: 'destination',
  },
  {
    title: '联系人电话',
    dataIndex: 'phone',
    scopedSlots: {customRender: 'phone'},
  },
  {
    title: '目的地地址',
    dataIndex: 'address',
    scopedSlots: {customRender: 'address'},
  },
  {
    title: '路线规划',
    dataIndex: 'routePlan',
    scopedSlots: {customRender: 'routePlan'},
  },
  {
    title: '当前位置',
    dataIndex: 'currentNode',
    scopedSlots: {customRender: 'currentNode'},
  },
  {
    title: '异常等级',
    dataIndex: 'warningLevel',
    scopedSlots: {customRender: 'warningLevel'},
  },
  {
    title: '注意事项',
    dataIndex: 'care',
    scopedSlots: {customRender: 'care'},
  },
  {
    title: '预计送达',
    dataIndex: 'time',
    scopedSlots: {customRender: 'time'},
  },
  {
    title: '当前状态',
    dataIndex: 'status',
    scopedSlots: {customRender: 'status'},
  },
  {
    title: '操作',
    dataIndex: 'operation',
    scopedSlots: {customRender: 'operation'},
  },
];

export default {
  data() {
    return {
      select: {},
      loading: false,
      saving: false,
      form: {
        cacheData: [],
        name: '',
        gender: '男性',
        phone: '',
        address: '',
        idCard: '',
        license: '',
        score: 12,
      },
      visible: false,
      visible2: false,
      data: [],
      columns,
      editingKey: '',
      trackRecords: [],
      addingTrack: false,
      newTrack: {
        node: '',
        status: undefined,
        remark: ''
      }
    };
  },
  mounted() {
    this.loadTableData()
  },
  methods: {
    loadTableData() {
      this.loading = true
      FindAllDistribution().then((res) => {
        if (res.status) {
          this.data = res.data
          this.cacheData = res.data.map(item => ({...item}))
        }
        setTimeout(() => {
          this.loading = false
        }, 600)
      })
    },
    submitForm() {
      SaveDistribution(this.form).then((res) => {
        if (res.status) this.$message.success('押运员信息提交成功');
        this.visible = false
        this.loadTableData()
      })
    },
    handleChange(value, id, column) {
      const newData = [...this.data];
      const target = newData.filter(item => id === item.id)[0];
      if (target) {
        target[column] = value;
        this.data = newData;
      }
    },
    edit(id) {
      const newData = [...this.data];
      const target = newData.filter(item => id === item.id)[0];
      this.editingKey = id;
      if (target) {
        target.editable = true;
        this.data = newData;
      }
    },

    save(id, index) {
      const newData = [...this.data];
      const newCacheData = [...this.cacheData];
      const target = newData.filter(item => id === item.id)[0];
      const targetCache = newCacheData.filter(item => id === item.id)[0];
      if (target && targetCache) {
        delete target.editable;
        this.data = newData;
        Object.assign(targetCache, target);
        this.cacheData = newCacheData;
      }
      this.editingKey = '';
      SaveDistribution(newData[index]).then((res) => {
        if (res.status) this.$message.success("信息保存成功")
      })
    },

    cancel(id) {
      const newData = [...this.data];
      const target = newData.filter(item => id === item.id)[0];
      this.editingKey = '';
      if (target) {
        Object.assign(target, this.cacheData.filter(item => id === item.id)[0]);
        delete target.editable;
        this.data = newData;
      }
    },

    review(index) {
      this.select = this.data[index]
      this.visible2 = true
      this.trackRecords = []
      this.loadTrackRecords()
    },

    agree() {
      this.select.status = 1
      this.select.currentNode = this.select.currentNode || this.select.origin
      SaveDistribution(this.select)
    },

    service(){
      this.select.status = 2
      this.select.currentNode = this.select.destination || this.select.currentNode
      SaveDistribution(this.select)
    },

    saveTrack() {
      this.saving = true
      SaveDistribution(this.select).then((res) => {
        if (res.status) this.$message.success("跟踪信息已更新")
        this.saving = false
        this.loadTableData()
      })
    },

    loadTrackRecords() {
      if (this.select.id) {
        GetTrackRecords(this.select.id).then((res) => {
          this.trackRecords = res || []
        })
      }
    },

    addTrackRecord() {
      if (!this.newTrack.node || !this.newTrack.status) {
        this.$message.warning('请填写节点和状态')
        return
      }
      this.addingTrack = true
      AddTrackRecord(this.select.id, this.newTrack).then((res) => {
        this.$message.success('追踪记录已添加')
        this.newTrack = { node: '', status: undefined, remark: '' }
        this.loadTrackRecords()
        this.addingTrack = false
      }).catch(() => {
        this.addingTrack = false
      })
    },

    getTimelineColor(status) {
      const colors = {
        '到达': 'green',
        '离开': 'blue',
        '装货': 'orange',
        '卸货': 'purple',
        '异常': 'red'
      }
      return colors[status] || 'gray'
    },

  },
};
</script>
<style scoped>

.editable-add-btn {
  margin-bottom: 15px;
}

.editable-row-operations a {
  margin-right: 8px;
}

.content {
  padding: 50px 0;
}

.track-edit {
  margin-top: 20px;
}

.check {
  padding-left: 200px;
}

.check p {
  padding-bottom: 20px;
}

.timeline-section {
  margin-top: 24px;
}

.timeline-title {
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}

.timeline-desc {
  color: #666;
  font-size: 13px;
  margin-bottom: 4px;
}

.timeline-time {
  color: #999;
  font-size: 12px;
}
</style>
