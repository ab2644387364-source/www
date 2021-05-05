<template>
  <div>
    <div style="display: flex">
      <a-button size="large" class="editable-add-btn" @click="commodityVisible = true">
        <a-icon type="plus"/>
        新增牲畜档案
      </a-button>
      <a-input-search
          placeholder="请输入畜种名称"
          enter-button="搜索牲畜"
          style="width: 400px;margin-left: 20px"
          size="large"
          @search="onSearch"
      />
      <a-button style="margin-left: 10px" size="large" type="danger" @click="loadTableData">
        重置
      </a-button>
    </div>
    <a-table :loading="loading" :columns="columns" :data-source="data" rowKey="id">
      <a slot="name" slot-scope="text">{{ text }}</a>
      <span slot="customTitle"><a-icon type="smile-o"/> 畜种名称</span>
      <span slot="action" slot-scope="text, record, index">
        <a-button @click="handleUpdate(record)" type="link"><a-icon type="edit"/> Update</a-button>
      <a-divider type="vertical"/>
         <a-popconfirm placement="top" ok-text="确定" cancel-text="取消" @confirm="handleDelete(record)">
           <template slot="title">
             <p>删除牲畜档案后将无法恢复，确定要删除吗？</p>
           </template>
           <a-button type="link"><a-icon type="delete"/> Delete</a-button>
         </a-popconfirm>
    </span>
    </a-table>

    <a-modal
        title="牲畜信息"
        :closable="false"
        :visible="commodityVisible"
    >
      <a-form-model ref="ruleForm" :model="commodity">
        <a-form-model-item ref="name" label="畜种名称" prop="name">
          <a-input v-model="commodity.name"/>
        </a-form-model-item>
        <a-form-model-item label="价格/kg" prop="price">
          <a-input-number id="input" v-model="commodity.price" :min="1"/>
        </a-form-model-item>
        <a-form-model-item v-if="commodity.id" label="存栏数量" prop="count">
          <a-input-number v-model="commodity.count" :min="0"/>
        </a-form-model-item>
        <a-form-model-item label="健康说明" prop="description">
          <a-input v-model="commodity.description" type="textarea"/>
        </a-form-model-item>
      </a-form-model>
      <template slot="footer">
        <a-button key="back" @click="commodityVisible = false">
          取消
        </a-button>
        <a-button key="submit" type="primary" :loading="modalLoading" @click="submitCommodity">
          提交
        </a-button>
      </template>
    </a-modal>

  </div>
</template>

<script>
import {DeleteCommodityById, FindAllCommodity, SaveCommodity, UpdateCommodity} from "@/api/commodity";
import {SearchCommodity} from "../../api/commodity";

const columns = [
  {
    dataIndex: 'name',
    key: 'name',
    slots: {title: 'customTitle'},
    scopedSlots: {customRender: 'name'},
  },
  {
    title: '存栏数量',
    dataIndex: 'count',
    key: 'age',
  },
  {
    title: '健康说明',
    dataIndex: 'description',
    key: 'description',
  },
  {
    title: '入栏时间',
    dataIndex: 'createAt',
    key: 'address',
  },
  {
    title: '价格/kg',
    key: 'price',
    dataIndex: 'price',
    scopedSlots: {customRender: 'tags'},
  },
  {
    title: '更多操作',
    key: 'action',
    scopedSlots: {customRender: 'action'},
  },
];

const data = [];

export default {

  data() {
    return {
      commodity: {
        name: '',
        description: '健康说明',
        count: 0,
        price: 9.99,
      },
      loading: false,
      modalLoading: false,
      commodityVisible: false,
      commodityLoading: false,
      data: [],
      columns,
    };
  },

  mounted() {
    this.loadTableData()
  },

  methods: {

    onSearch(value) {
      if (value){
        this.loading = true
        SearchCommodity(value).then((res) => {
          console.log(res)
          if (res.data.length === 0) {
            this.$message.warn("未搜索到任何数据")
            setTimeout(() => {
              this.loading = false
              this.data = res.data
            }, 600)
          } else {
            setTimeout(() => {
              this.$message.success("搜索到" + res.data.length + "条牲畜档案")
              this.loading = false
              this.data = res.data
            }, 600)
          }
        })
      }else {
        this.$message.warn("请输入搜索内容")
      }
    },

    loadTableData() {
      this.loading = true
      FindAllCommodity().then((res) => {
        setTimeout(() => {
          this.loading = false
          this.data = res.data
        }, 600)
      })
    },

    submitCommodity() {
      this.modalLoading = true
      const request = this.commodity.id ? UpdateCommodity(this.commodity) : SaveCommodity(this.commodity)
      request.then((res) => {
        if (res.status) {
          setTimeout(() => {
            this.modalLoading = false
            this.commodityVisible = false
            this.$message.success(this.commodity.id ? '牲畜信息更新成功' : '牲畜信息提交成功');
            this.loadTableData()
          }, 600)
        } else {
          setTimeout(() => {
            this.modalLoading = false
          }, 600)
        }
      })
    },

    handleDelete(r) {
      DeleteCommodityById(r.id).then((res) => {
        if (res.status) this.$message.success('牲畜信息删除成功');
        this.loadTableData()
      })
    },

    handleUpdate(r) {
      this.commodity = r
      this.commodityVisible = true
    },

  },

};
</script>

<style scoped>
.editable-add-btn {
  margin-bottom: 15px;
}
</style>
