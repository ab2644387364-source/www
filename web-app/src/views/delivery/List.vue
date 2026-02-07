<template>
  <div class="page-container">
    <a-card :bordered="false">
      <!-- 顶部操作栏 -->
      <div class="table-operator">
        <a-button type="primary" icon="reload" @click="loadTableData">刷新</a-button>
      </div>

      <!-- 主表格 -->
      <a-table :loading="loading" :columns="columns" :data-source="data" rowKey="id" size="middle">
        <!-- 状态列 -->
        <span slot="status" slot-scope="status">
          <a-badge :status="getStatusBadge(status)" :text="getStatusText(status)" />
        </span>
        
        <!-- 异常等级列 -->
        <span slot="warningLevel" slot-scope="warningLevel">
          <a-tag v-if="warningLevel===2" color="#f5222d">严重</a-tag>
          <a-tag v-else-if="warningLevel===1" color="#fa8c16">一般</a-tag>
          <a-tag v-else color="green">正常</a-tag>
        </span>

        <!-- 司机/车辆信息列 -->
        <span slot="driverInfo" slot-scope="text, record">
          <div v-if="record.driver">{{ record.driver }} <span style="color: #999">|</span> {{ record.number }}</div>
          <div v-else style="color: #ccc">未分配</div>
        </span>

        <!-- 操作列 -->
        <template slot="operation" slot-scope="text, record, index">
          <div class="table-actions">
            <!-- 主要操作按钮 -->
            <a-button 
              type="link" 
              size="small" 
              icon="deployment-unit" 
              v-if="record.status===0" 
              @click="review(index)"
            >调度</a-button>
            <a-button 
              type="link" 
              size="small" 
              icon="environment" 
              v-else-if="record.status===1" 
              @click="review(index)"
            >跟踪</a-button>
            <a-button 
              type="link" 
              size="small" 
              icon="eye" 
              v-else 
              @click="review(index)"
            >详情</a-button>
            
            <a-divider type="vertical" />
            
            <!-- 编辑/保存逻辑 (保留简单编辑功能) -->
            <span v-if="record.editable">
              <a @click="() => save(record.id, index)">保存</a>
              <a-divider type="vertical" />
              <a-popconfirm title="确定取消编辑吗?" @confirm="() => cancel(record.id)">
                <a>取消</a>
              </a-popconfirm>
            </span>
            <span v-else>
              <a :disabled="editingKey !== ''" @click="() => edit(record.id)">编辑</a>
            </span>
          </div>
        </template>
      </a-table>
    </a-card>

    <!-- 押运员录入弹窗 (保持原样) -->
    <a-modal
        title="录入押运员信息"
        :visible="visible"
        @ok="submitForm"
        @cancel="visible = false"
    >
      <a-form-model :model="form" :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <a-form-model-item label="姓名">
          <a-input v-model="form.name" placeholder="请输入押运员姓名"/>
        </a-form-model-item>
        <a-form-model-item label="身份证号">
          <a-input v-model="form.idCard" placeholder="请输入押运员身份证信息"/>
        </a-form-model-item>
        <a-form-model-item label="联系方式">
          <a-input v-model="form.phone" placeholder="请输入手机号码"/>
        </a-form-model-item>
        <a-form-model-item label="驾照信息">
             <a-input-group compact>
                <a-input style="width: 60%" v-model="form.license" addon-before="驾驶证" default-value="0571"/>
                <a-input-number style="width: 40%" v-model="form.score" addon-before="分数" default-value="12" :min="0" :max="12"/>
             </a-input-group>
        </a-form-model-item>
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

    <!-- 转运调度与跟踪详情弹窗 -->
    <a-modal
        :title="getModalTitle(select.status)"
        :visible="visible2"
        width="800px"
        :footer="null"
        @cancel="visible2 = false"
        class="detail-modal"
    >
      <a-tabs default-active-key="1">
        
        <!-- 标签页 1: 概览与调度 -->
        <a-tab-pane key="1" tab="概览与调度">
          <div class="tab-content">
            <!-- 概要信息 -->
            <a-descriptions title="基础信息" bordered size="small" :column="2">
              <a-descriptions-item label="订单号">{{ select.orderNo }}</a-descriptions-item>
              <a-descriptions-item label="状态">
                 <a-badge :status="getStatusBadge(select.status)" :text="getStatusText(select.status)" />
              </a-descriptions-item>
              <a-descriptions-item label="起点">{{ select.origin }}</a-descriptions-item>
              <a-descriptions-item label="目的地">{{ select.destination }}</a-descriptions-item>
              <a-descriptions-item label="预计送达">{{ select.time }}</a-descriptions-item>
              <a-descriptions-item label="加急">{{ select.urgent ? '是' : '否' }}</a-descriptions-item>
              <a-descriptions-item label="联系人">{{ select.phone }}</a-descriptions-item>
              <a-descriptions-item label="详细地址">{{ select.address }}</a-descriptions-item>
              <a-descriptions-item label="注意事项" :span="2">{{ select.care }}</a-descriptions-item>
            </a-descriptions>

            <a-divider />

            <!-- 调度操作区域 -->
            <div v-if="select.status === 0" class="action-section">
               <h3><a-icon type="car" /> 车辆调度</h3>
               <div class="dispatch-form">
                 <p>当前分配：<strong>{{ select.driver || '未指定' }} / {{ select.number || '未指定' }}</strong></p>
                 <!-- 这里假设已经通过其他方式或者表格编辑分配了司机，这里只做确认 -->
                 <a-alert message="请确认车辆和人员信息无误后开始转运" type="info" show-icon style="margin-bottom: 16px" />
                 
                 <div class="dispatch-buttons">
                   <a-button type="primary" :loading="loading" @click="agree">
                     <a-icon type="check" /> 确认发车
                   </a-button>
                   <a-button @click="visible2 = false" style="margin-left: 10px">取消</a-button>
                 </div>
               </div>
            </div>

            <!-- 转运中操作 -->
             <div v-if="select.status === 1" class="action-section">
               <a-result status="processing" title="正在转运中">
                 <template #extra>
                   <a-button type="primary" @click="service">确认送达</a-button>
                 </template>
               </a-result>
             </div>

             <!-- 已完成 -->
             <div v-if="select.status === 2" class="action-section">
               <a-result status="success" title="转运已完成" />
             </div>
          </div>
        </a-tab-pane>

        <!-- 标签页 2: 跟踪与异常 -->
        <a-tab-pane key="2" tab="跟踪与异常" :disabled="select.status === 0">
           <div class="tab-content">
              <a-form-model :model="select" :label-col="{ span: 5 }" :wrapper-col="{ span: 18 }">
                <a-form-model-item label="当前位置">
                  <a-input v-model="select.currentNode" placeholder="更新当前位置"/>
                </a-form-model-item>
                <a-form-model-item label="路线规划">
                   <a-textarea v-model="select.routePlan" :rows="2" />
                </a-form-model-item>
                <a-form-model-item label="异常等级">
                  <a-radio-group v-model="select.warningLevel" button-style="solid">
                    <a-radio-button :value="0">正常</a-radio-button>
                    <a-radio-button :value="1">一般异常</a-radio-button>
                    <a-radio-button :value="2">严重异常</a-radio-button>
                  </a-radio-group>
                </a-form-model-item>
                <a-form-model-item label="异常说明" v-if="select.warningLevel > 0">
                  <a-textarea v-model="select.warningNote" :rows="3" placeholder="请详细描述异常情况"/>
                </a-form-model-item>
                
                <a-form-model-item :wrapper-col="{ span: 18, offset: 5 }">
                   <a-button type="primary" :loading="saving" @click="saveTrack">保存更新</a-button>
                </a-form-model-item>
              </a-form-model>
           </div>
        </a-tab-pane>

        <!-- 标签页 3: 物流时间线 -->
        <a-tab-pane key="3" tab="物流轨迹">
          <div class="tab-content">
             <a-row :gutter="24">
                <!-- 左侧时间线 -->
                <a-col :span="14" class="scrollable-timeline">
                    <a-timeline mode="left" v-if="trackRecords.length > 0">
                      <a-timeline-item v-for="record in trackRecords" :key="record.id" :color="getTimelineColor(record.status)">
                        <p class="timeline-head">
                          <span class="timeline-node">{{ record.node }}</span>
                          <span class="timeline-status-tag">{{ record.status }}</span>
                        </p>
                        <p class="timeline-remark">{{ record.remark || '无备注' }}</p>
                        <p class="timeline-time">{{ record.createTime }}</p>
                      </a-timeline-item>
                    </a-timeline>
                    <a-empty v-else description="暂无追踪记录" />
                </a-col>
                
                <!-- 右侧添加记录 -->
                <a-col :span="10">
                   <a-card title="添加新节点" size="small" :bordered="true" style="background-color: #f9f9f9">
                      <a-form-model :model="newTrack" layout="vertical">
                        <a-form-model-item label="节点位置">
                          <a-input v-model="newTrack.node" placeholder="如: 杭州分拨中心"/>
                        </a-form-model-item>
                        <a-form-model-item label="物流状态">
                          <a-select v-model="newTrack.status" placeholder="选择状态">
                            <a-select-option value="到达">到达</a-select-option>
                            <a-select-option value="离开">离开</a-select-option>
                            <a-select-option value="装货">装货</a-select-option>
                            <a-select-option value="卸货">卸货</a-select-option>
                            <a-select-option value="异常">异常</a-select-option>
                          </a-select>
                        </a-form-model-item>
                        <a-form-model-item label="备注说明">
                          <a-input v-model="newTrack.remark" type="textarea" :rows="2"/>
                        </a-form-model-item>
                        <a-button type="primary" block icon="plus" :loading="addingTrack" @click="addTrackRecord">提交记录</a-button>
                      </a-form-model>
                   </a-card>
                </a-col>
             </a-row>
          </div>
        </a-tab-pane>
      </a-tabs>
    </a-modal>

  </div>
</template>

<script>
import {FindAllDistribution, SaveDistribution} from "../../api/distribution";
import {GetTrackRecords, AddTrackRecord} from "../../api/notification";

// 简化后的表格定义
const columns = [
  { title: '订单号', dataIndex: 'orderNo', width: 150 },
  { title: '状态', dataIndex: 'status', scopedSlots: { customRender: 'status' }, width: 100 },
  { title: '起点', dataIndex: 'origin' },
  { title: '目的地', dataIndex: 'destination' },
  { title: '司机/车牌', scopedSlots: { customRender: 'driverInfo' } },
  { title: '当前位置', dataIndex: 'currentNode', scopedSlots: { customRender: 'currentNode' } },
  { title: '异常等级', dataIndex: 'warningLevel', scopedSlots: { customRender: 'warningLevel' }, width: 100 },
  { title: '当前位置编辑', dataIndex: 'currentNode', scopedSlots: { customRender: 'currentNode' }, colSpan: 0, customRender: (value, row, index) => ({ children: value, attrs: { colSpan: 0 } }) }, // Hidden hack or just remove if not needed, simplified above
  { title: '预计送达', dataIndex: 'time' },
  { title: '操作', scopedSlots: { customRender: 'operation' }, width: 220, fixed: 'right' },
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
      columns: [
          { title: '订单号', dataIndex: 'orderNo', width: 130 },
          { title: '状态', dataIndex: 'status', scopedSlots: { customRender: 'status' }, width: 100 },
          { title: '起点', dataIndex: 'origin', width: 120 },
          { title: '目的地', dataIndex: 'destination', width: 120},
          { title: '司机/车牌', scopedSlots: { customRender: 'driverInfo' }, width: 180 },
          { title: '当前位置/备注', dataIndex: 'currentNode', scopedSlots: { customRender: 'currentNode' } },
          { title: '异常', dataIndex: 'warningLevel', scopedSlots: { customRender: 'warningLevel' }, width: 80, align: 'center' },
          { title: '预计送达', dataIndex: 'time', width: 150 },
          { title: '操作', scopedSlots: { customRender: 'operation' }, width: 180, fixed: 'right' },
      ],
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
          // 确保数据中包含 editable 字段
          this.data.forEach(item => {
             if (item.editable === undefined) this.$set(item, 'editable', false);
          })
          this.cacheData = res.data.map(item => ({...item}))
        }
        setTimeout(() => {
          this.loading = false
        }, 600)
      })
    },
    
    // 获取状态 Badge
    getStatusBadge(status) {
      const map = { 0: 'warning', 1: 'processing', 2: 'success' };
      return map[status] || 'default';
    },
    
    // 获取状态文本
    getStatusText(status) {
      const map = { 0: '待调度', 1: '转运中', 2: '已完成' };
      return map[status] || '未知';
    },

    // 弹窗标题
    getModalTitle(status) {
       if (status === 0) return '转运调度';
       return '物流跟踪详情';
    },

    submitForm() {
      SaveDistribution(this.form).then((res) => {
        if (res.status) this.$message.success('押运员信息提交成功');
        this.visible = false
        this.loadTableData()
      })
    },
    
    // 行编辑逻辑
    handleChange(value, id, column) {
      const target = this.data.find(item => id === item.id);
      if (target) {
        target[column] = value;
      }
    },
    edit(id) {
      // 简单实现：将 editable 置为 true
      const target = this.data.find(item => id === item.id);
      this.editingKey = id;
      if (target) {
        target.editable = true;
      }
    },
    save(id, index) {
      const target = this.data.find(item => id === item.id);
      if (target) {
        delete target.editable;
        SaveDistribution(target).then((res) => {
          if (res.status) this.$message.success("信息保存成功")
        });
        this.editingKey = '';
      }
    },
    cancel(id) {
       const target = this.data.find(item => id === item.id);
       this.editingKey = '';
       if (target) {
         // 恢复原数据 (这里需简化，实际应从 cacheData 恢复)
         target.editable = false; 
         this.loadTableData(); // 简单重载
       }
    },

    review(index) {
      this.select = JSON.parse(JSON.stringify(this.data[index])) // Deep copy
      this.visible2 = true
      this.trackRecords = []
      this.loadTrackRecords()
      // 如果已派车，自动填入当前位置为起点或已有位置
      if (!this.select.currentNode && this.select.origin) {
         this.select.currentNode = this.select.origin;
      }
    },

    agree() {
      this.select.status = 1
      this.select.currentNode = this.select.origin // 初始位置设为起点
      this.saveDistributionAndClose();
    },

    service(){
      this.select.status = 2
      this.select.currentNode = this.select.destination // 结束位置设为终点
      this.saveDistributionAndClose();
    },
    
    saveDistributionAndClose() {
       this.loading = true;
       SaveDistribution(this.select).then(res => {
         if(res.status) {
            this.$message.success('操作成功');
            this.visible2 = false;
            this.loadTableData();
         }
         this.loading = false;
       })
    },

    saveTrack() {
      this.saving = true
      SaveDistribution(this.select).then((res) => {
        if (res.status) {
           this.$message.success("跟踪信息已更新");
           this.loadTableData(); // 刷新列表数据
        }
        this.saving = false
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
.page-container {
  padding: 24px;
  background-color: #f0f2f5;
  min-height: 100%;
}
.table-operator {
  margin-bottom: 16px;
}
.tab-content {
  padding: 16px 0;
}
.action-section {
  margin-top: 24px;
  padding: 16px;
  background: #f9f9f9;
  border-radius: 4px;
}
.dispatch-form {
  margin-top: 16px;
}
.scrollable-timeline {
  max-height: 400px;
  overflow-y: auto;
  padding-right: 16px;
}
.timeline-head {
  display: flex;
  justify-content: space-between;
  margin-bottom: 4px;
}
.timeline-node {
  font-weight: bold;
  font-size: 14px;
}
.timeline-status-tag {
  color: #1890ff;
  font-weight: 500;
}
.timeline-remark {
  color: #666;
  font-size: 13px;
  margin-bottom: 2px;
}
.timeline-time {
  color: #999;
  font-size: 12px;
}
</style>
