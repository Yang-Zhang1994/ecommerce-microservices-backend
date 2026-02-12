<template>
  <div class="mod-config">
    <el-form :inline="true" :model="dataForm" @keyup.enter.native="getDataList()">
      <el-form-item>
        <el-input v-model="dataForm.key" placeholder="Parameter Name" clearable></el-input>
      </el-form-item>
      <el-form-item>
        <el-button @click="getDataList()">Query</el-button>
        <el-button
          v-if="isAuth('coupon:coupon:save')"
          type="primary"
          @click="addOrUpdateHandle()"
        >Add</el-button>
        <el-button
          v-if="isAuth('coupon:coupon:delete')"
          type="danger"
          @click="deleteHandle()"
          :disabled="dataListSelections.length <= 0"
        >Batch Delete</el-button>
      </el-form-item>
    </el-form>
    <el-table
      :data="dataList"
      border
      v-loading="dataListLoading"
      @selection-change="selectionChangeHandle"
      style="width: 100%;"
    >
      <el-table-column type="selection" header-align="center" align="center" width="50"></el-table-column>
      <el-table-column prop="id" header-align="center" align="center" label="id"></el-table-column>
      <el-table-column prop="couponType" header-align="center" align="center" label="Coupon Type">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.couponType==0">Site-wide Coupon</el-tag>
          <el-tag type="info" v-if="scope.row.couponType==1">Member Coupon</el-tag>
          <el-tag type="success" v-if="scope.row.couponType==2">Shopping Coupon</el-tag>
          <el-tag type="warning" v-if="scope.row.couponType==3">Registration Coupon</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="couponImg" header-align="center" align="center" label="Coupon Image"></el-table-column>
      <el-table-column prop="couponName" header-align="center" align="center" label="Coupon Name"></el-table-column>
      <el-table-column prop="num" header-align="center" align="center" label="Quantity"></el-table-column>
      <el-table-column prop="amount" header-align="center" align="center" label="Amount"></el-table-column>
      <el-table-column prop="perLimit" header-align="center" align="center" label="Limit Per Person"></el-table-column>
      <el-table-column prop="minPoint" header-align="center" align="center" label="Minimum Threshold"></el-table-column>
      <el-table-column prop="startTime" header-align="center" align="center" label="Start Time"></el-table-column>
      <el-table-column prop="endTime" header-align="center" align="center" label="End Time"></el-table-column>
      <el-table-column prop="useType" header-align="center" align="center" label="Use Type">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.useType==0">Site-wide</el-tag>
          <el-tag type="info" v-if="scope.row.useType==1">Specified Category</el-tag>
          <el-tag type="success" v-if="scope.row.useType==2">Specified Product</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="note" header-align="center" align="center" label="Note"></el-table-column>
      <el-table-column prop="publishCount" header-align="center" align="center" label="Published Quantity"></el-table-column>
      <el-table-column prop="useCount" header-align="center" align="center" label="Used Quantity"></el-table-column>
      <el-table-column prop="receiveCount" header-align="center" align="center" label="Received Quantity"></el-table-column>
      <el-table-column label="Available Date Range">
        <el-table-column prop="enableStartTime" header-align="center" align="center" label="Start Date"></el-table-column>
        <el-table-column prop="enableEndTime" header-align="center" align="center" label="End Date"></el-table-column>
      </el-table-column>
      <el-table-column prop="code" header-align="center" align="center" label="Coupon Code"></el-table-column>
      <el-table-column prop="memberLevel" header-align="center" align="center" label="Required Member Level">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.memberLevel==0">Any Level</el-tag>
          <el-tag type="info" v-else>{{getLevel(scope.row.memberLevel)}}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="publish" header-align="center" align="center" label="Publish Status">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.publish==0">Unpublished</el-tag>
          <el-tag type="success" v-else>Published</el-tag>
        </template>
      </el-table-column>
      <el-table-column fixed="right" header-align="center" align="center" width="150" label="Actions">
        <template slot-scope="scope">
          <el-button type="text" size="small" @click="addOrUpdateHandle(scope.row.id)">Edit</el-button>
          <el-button type="text" size="small" @click="deleteHandle(scope.row.id)">Delete</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      @size-change="sizeChangeHandle"
      @current-change="currentChangeHandle"
      :current-page="pageIndex"
      :page-sizes="[10, 20, 50, 100]"
      :page-size="pageSize"
      :total="totalPage"
      layout="total, sizes, prev, pager, next, jumper"
    ></el-pagination>
    <!-- Dialog, Add / Edit -->
    <add-or-update v-if="addOrUpdateVisible" ref="addOrUpdate" @refreshDataList="getDataList"></add-or-update>
  </div>
</template>

<script>
import AddOrUpdate from "./coupon-add-or-update";
export default {
  data() {
    return {
      dataForm: {
        key: ""
      },
      dataList: [],
      pageIndex: 1,
      pageSize: 10,
      totalPage: 0,
      dataListLoading: false,
      dataListSelections: [],
      addOrUpdateVisible: false,
      memberLevels: {}
    };
  },
  components: {
    AddOrUpdate
  },
  activated() {
    this.getDataList();
    this.getMemberLevels();
  },
  methods: {
    getLevel(level) {
      let name = this.memberLevels["level_" + level];
      if (name) {
        return name;
      } else {
        return "";
      }
    },
    getMemberLevels() {
      // Get all member levels
      this.$http({
        url: this.$http.adornUrl("/member/memberlevel/list"),
        method: "get",
        params: this.$http.adornParams({
          page: 1,
          limit: 500
        })
      }).then(({ data }) => {
        data.page.list.forEach(item => {
          this.memberLevels["level_" + item.id] = item.name;
        });
      });
    },
    // 获取数据列表
    getDataList() {
      this.dataListLoading = true;
      this.$http({
        url: this.$http.adornUrl("/coupon/coupon/list"),
        method: "get",
        params: this.$http.adornParams({
          page: this.pageIndex,
          limit: this.pageSize,
          key: this.dataForm.key
        })
      }).then(({ data }) => {
        if (data && data.code === 0) {
          this.dataList = data.page.list;
          this.totalPage = data.page.totalCount;
        } else {
          this.dataList = [];
          this.totalPage = 0;
        }
        this.dataListLoading = false;
      });
    },
    // 每页数
    sizeChangeHandle(val) {
      this.pageSize = val;
      this.pageIndex = 1;
      this.getDataList();
    },
    // 当前页
    currentChangeHandle(val) {
      this.pageIndex = val;
      this.getDataList();
    },
    // 多选
    selectionChangeHandle(val) {
      this.dataListSelections = val;
    },
    // 新增 / 修改
    addOrUpdateHandle(id) {
      this.addOrUpdateVisible = true;
      this.$nextTick(() => {
        this.$refs.addOrUpdate.init(id);
      });
    },
    // 删除
    deleteHandle(id) {
      var ids = id
        ? [id]
        : this.dataListSelections.map(item => {
            return item.id;
          });
      this.$confirm(
        `Are you sure you want to [${id ? "delete" : "batch delete"}] [id=${ids.join(",")}]?`,
        "Tip",
        {
          confirmButtonText: "Confirm",
          cancelButtonText: "Cancel",
          type: "warning"
        }
      ).then(() => {
        this.$http({
          url: this.$http.adornUrl("/coupon/coupon/delete"),
          method: "post",
          data: this.$http.adornData(ids, false)
        }).then(({ data }) => {
          if (data && data.code === 0) {
            this.$message({
              message: "Operation successful",
              type: "success",
              duration: 1500,
              onClose: () => {
                this.getDataList();
              }
            });
          } else {
            this.$message.error(data.msg);
          }
        });
      });
    }
  }
};
</script>
