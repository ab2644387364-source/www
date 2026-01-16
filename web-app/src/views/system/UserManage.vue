<template>
  <div>
    <a-table :columns="columns" :data-source="users" rowKey="id" :loading="loading">
      <span slot="status" slot-scope="disabled">
        <a-tag :color="disabled ? 'volcano' : 'green'">
          {{ disabled ? '已禁用' : '正常' }}
        </a-tag>
      </span>
      <span slot="action" slot-scope="text, record">
        <a-popconfirm
            :title="record.disabled ? '确定解封该账号吗？' : '确定禁用该账号吗？'"
            @confirm="() => toggleStatus(record)">
          <a-button type="link">
            {{ record.disabled ? '解封' : '禁用' }}
          </a-button>
        </a-popconfirm>
      </span>
    </a-table>
  </div>
</template>

<script>
import {FindAllUsers, UpdateUserStatus} from "@/api/user";

const columns = [
  {
    title: '邮箱',
    dataIndex: 'email',
    key: 'email',
  },
  {
    title: '密码',
    dataIndex: 'password',
    key: 'password',
  },
  {
    title: '添加日期',
    dataIndex: 'createAt',
    key: 'createAt',
  },
  {
    title: '状态',
    dataIndex: 'disabled',
    key: 'disabled',
    scopedSlots: {customRender: 'status'},
  },
  {
    title: '管理',
    key: 'action',
    scopedSlots: {customRender: 'action'},
  },
];

export default {
  data() {
    return {
      users: [],
      loading: false,
      columns,
    };
  },

  mounted() {
    this.load();
  },

  methods: {
    load() {
      this.loading = true;
      FindAllUsers().then((res) => {
        if (res.status) this.users = res.data;
        setTimeout(() => {
          this.loading = false;
        }, 600);
      });
    },

    toggleStatus(record) {
      const targetDisabled = !record.disabled;
      UpdateUserStatus(record.id, targetDisabled).then((res) => {
        if (res.status) {
          this.$message.success(targetDisabled ? '账号已禁用' : '账号已解封');
          this.load();
        }
      });
    },
  },
};
</script>
