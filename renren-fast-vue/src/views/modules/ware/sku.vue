<template>
  <div class="mod-config">
    <el-form :inline="true" :model="dataForm" @keyup.enter.native="getDataList()">
      <el-form-item label="Warehouse">
        <el-select style="width:160px;" v-model="dataForm.wareId" placeholder="Please select warehouse" clearable>
          <el-option :label="w.name" :value="w.id" v-for="w in wareList" :key="w.id"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="SKU ID">
        <el-input v-model="dataForm.skuId" placeholder="SKU ID" clearable></el-input>
      </el-form-item>
      <el-form-item>
        <el-button @click="getDataList()">Query</el-button>
        <el-button v-if="isAuth('ware:waresku:save')" type="warning" @click="syncFromProduct()">Sync SKU Names</el-button>
        <el-button v-if="isAuth('ware:waresku:save')" type="primary" @click="addOrUpdateHandle()">Add</el-button>
        <el-button
          v-if="isAuth('ware:waresku:delete')"
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
      <el-table-column prop="id" header-align="center" align="center" min-width="72" label="id"></el-table-column>
      <el-table-column prop="skuId" header-align="center" align="center" min-width="100" label="sku_id"></el-table-column>
      <el-table-column prop="wareId" header-align="center" align="center" min-width="120" label="Warehouse ID"></el-table-column>
      <el-table-column prop="stock" header-align="center" align="center" min-width="130" label="Stock Quantity"></el-table-column>
      <el-table-column prop="skuName" header-align="center" align="center" min-width="180" show-overflow-tooltip label="SKU Name"></el-table-column>
      <el-table-column prop="stockLocked" header-align="center" align="center" min-width="120" label="Locked Stock">
        <template slot-scope="scope">
          {{ scope.row.stockLocked == null ? 0 : scope.row.stockLocked }}
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
import AddOrUpdate from "./waresku-add-or-update";
export default {
  data() {
    return {
      wareList: [],
      dataForm: {
        wareId: "",
        skuId: ""
      },
      dataList: [],
      pageIndex: 1,
      pageSize: 10,
      totalPage: 0,
      dataListLoading: false,
      dataListSelections: [],
      addOrUpdateVisible: false
    };
  },
  components: {
    AddOrUpdate
  },
  activated() {
    this.applyRouteSkuFilter();
    this.getWares();
    this.getDataList();
  },
  watch: {
    "$route.query.skuId"(val) {
      if (val) {
        this.dataForm.skuId = String(val);
        this.getDataList();
      }
    }
  },
  methods: {
    applyRouteSkuFilter() {
      const skuId = this.$route.query.skuId;
      if (skuId) {
        this.dataForm.skuId = String(skuId);
      }
    },
    getWares() {
      this.$http({
        url: this.$http.adornUrl("/ware/wareinfo/list"),
        method: "get",
        params: this.$http.adornParams({
          page: 1,
          limit: 500
        })
      }).then(({ data }) => {
        this.wareList = data.page.list;
      });
    },
    syncFromProduct() {
      this.$http({
        url: this.$http.adornUrl('/ware/waresku/syncFromProduct'),
        method: 'post'
      }).then(({ data }) => {
        if (data && data.code === 0) {
          const stats = data.data || {}
          this.$message({
            message: `Synced: ${stats.updated || 0} updated of ${stats.total || 0} rows`,
            type: 'success',
            duration: 2000,
            onClose: () => this.getDataList()
          })
          this.getDataList()
        } else {
          this.$message.error(data.msg || 'Sync failed')
        }
      })
    },
    // Get data list
    getDataList() {
      this.dataListLoading = true;
      this.$http({
        url: this.$http.adornUrl("/ware/waresku/list"),
        method: "get",
        params: this.$http.adornParams({
          page: this.pageIndex,
          limit: this.pageSize,
          skuId: this.dataForm.skuId,
          wareId: this.dataForm.wareId
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
    // Page size
    sizeChangeHandle(val) {
      this.pageSize = val;
      this.pageIndex = 1;
      this.getDataList();
    },
    // Current page
    currentChangeHandle(val) {
      this.pageIndex = val;
      this.getDataList();
    },
    // Multiple selection
    selectionChangeHandle(val) {
      this.dataListSelections = val;
    },
    // Add / Edit
    addOrUpdateHandle(id) {
      this.addOrUpdateVisible = true;
      this.$nextTick(() => {
        this.$refs.addOrUpdate.init(id);
      });
    },
    // Delete
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
          url: this.$http.adornUrl("/ware/waresku/delete"),
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
