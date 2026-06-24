<template>
  <div class="mod-config">
    <el-form :inline="true" class="spu-toolbar">
      <el-form-item>
        <el-button
          type="danger"
          :disabled="dataListSelections.length <= 0"
          @click="deleteHandle()"
        >Batch Delete</el-button>
      </el-form-item>
    </el-form>
    <el-table
      class="admin-word-table"
      :data="dataList"
      border
      v-loading="dataListLoading"
      @selection-change="selectionChangeHandle"
      style="width: 100%;"
    >
      <el-table-column type="selection" header-align="center" align="center" width="50"></el-table-column>
      <el-table-column prop="id" header-align="center" align="center" label="ID" width="72"></el-table-column>
      <el-table-column prop="spuName" header-align="center" align="left" label="Name" min-width="140"></el-table-column>
      <el-table-column prop="spuDescription" header-align="center" align="left" label="Description" min-width="200"></el-table-column>
      <el-table-column prop="catalogName" header-align="center" align="center" label="Category" min-width="120"></el-table-column>
      <el-table-column prop="brandName" header-align="center" align="center" label="Brand" min-width="100"></el-table-column>
      <el-table-column header-align="center" align="center" label="Weight" width="96">
        <template slot-scope="scope">
          {{ formatWeight(scope.row.weight) }}
        </template>
      </el-table-column>
      <el-table-column prop="publishStatus" header-align="center" align="center" label="Status" width="100">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.publishStatus == 0">New</el-tag>
          <el-tag v-if="scope.row.publishStatus == 1">On Sale</el-tag>
          <el-tag v-if="scope.row.publishStatus == 2">Off Sale</el-tag>
        </template>
      </el-table-column>
      <el-table-column header-align="center" align="center" label="Created" min-width="168">
        <template slot-scope="scope">
          {{ formatDateTime(scope.row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column header-align="center" align="center" label="Updated" min-width="168">
        <template slot-scope="scope">
          {{ formatDateTime(scope.row.updateTime) }}
        </template>
      </el-table-column>
      <el-table-column fixed="right" header-align="center" align="center" width="260" label="Operations">
        <template slot-scope="scope">
          <el-button type="text" size="small" @click="openEdit(scope.row)">Edit</el-button>
          <el-button
            v-if="scope.row.publishStatus == 0 || scope.row.publishStatus == 2"
            type="text"
            size="small"
            @click="productUp(scope.row.id)"
          >Put On Sale</el-button>
          <el-button
            v-if="scope.row.publishStatus == 1"
            type="text"
            size="small"
            @click="productDown(scope.row.id)"
          >Put Off Sale</el-button>
          <el-button type="text" size="small" class="danger-text" @click="deleteHandle(scope.row)">Delete</el-button>
          <el-dropdown trigger="click" @command="cmd => handleMore(scope.row, cmd)">
            <el-button type="text" size="small">
              More<i class="el-icon-arrow-down el-icon--right"></i>
            </el-button>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="specs">Specifications</el-dropdown-item>
              <el-dropdown-item command="images">Detail gallery</el-dropdown-item>
              <el-dropdown-item command="skus">SKUs</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </template>
      </el-table-column>
    </el-table>

    <spu-basic-edit
      :visible.sync="editVisible"
      :spu-id="editSpuId"
      @refresh="getDataList"
    />
    <spu-images-dialog
      :visible.sync="imagesVisible"
      :spu-id="imagesSpuId"
      :spu-name="imagesSpuName"
      @refresh="getDataList"
    />

    <el-pagination
      @size-change="sizeChangeHandle"
      @current-change="currentChangeHandle"
      :current-page="pageIndex"
      :page-sizes="[10, 20, 50, 100]"
      :page-size="pageSize"
      :total="totalPage"
      layout="total, sizes, prev, pager, next, jumper"
    ></el-pagination>
  </div>
</template>

<script>
import SpuBasicEdit from "./spu-basic-edit";
import SpuImagesDialog from "./spu-images-dialog";

export default {
  components: { SpuBasicEdit, SpuImagesDialog },
  data() {
    return {
      dataSub: null,
      dataForm: {},
      dataList: [],
      pageIndex: 1,
      pageSize: 10,
      totalPage: 0,
      dataListLoading: false,
      dataListSelections: [],
      editVisible: false,
      editSpuId: null,
      imagesVisible: false,
      imagesSpuId: null,
      imagesSpuName: ""
    };
  },
  props: {
    catId: {
      type: Number,
      default: 0
    }
  },
  activated() {
    this.getDataList();
    this.$nextTick(() => this.applyOpenSpuFromRoute());
  },
  watch: {
    "$route.query.openSpuId"() {
      this.applyOpenSpuFromRoute();
    }
  },
  methods: {
    formatDateTime(value) {
      if (value === null || value === undefined || value === "") {
        return "—";
      }
      if (typeof value === "string") {
        return value;
      }
      const d = value instanceof Date ? value : new Date(value);
      if (Number.isNaN(d.getTime())) {
        return "—";
      }
      const pad = (n) => String(n).padStart(2, "0");
      return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`;
    },
    formatWeight(w) {
      if (w === null || w === undefined || w === "") {
        return "—";
      }
      const n = Number(w);
      if (Number.isNaN(n)) {
        return "—";
      }
      return `${n} kg`;
    },
    applyOpenSpuFromRoute() {
      const raw = this.$route.query.openSpuId;
      if (!raw) return;
      const id = Number(raw);
      if (!Number.isFinite(id) || id <= 0) return;
      this.editSpuId = id;
      this.editVisible = true;
      const query = { ...this.$route.query };
      delete query.openSpuId;
      this.$router.replace({ path: this.$route.path, query });
    },
    openEdit(row) {
      this.editSpuId = row.id;
      this.editVisible = true;
    },
    openImages(row) {
      this.imagesSpuId = row.id;
      this.imagesSpuName = row.spuName || "";
      this.imagesVisible = true;
    },
    goSkuManager(row) {
      this.$router.push({
        path: "/product-manager",
        query: { spuId: String(row.id) }
      });
    },
    handleMore(row, command) {
      if (command === "specs") {
        this.attrUpdateShow(row);
      } else if (command === "images") {
        this.openImages(row);
      } else if (command === "skus") {
        this.goSkuManager(row);
      }
    },
    attrUpdateShow(row) {
      this.$router.push({
        name: "attr-update",
        query: {
          spuId: String(row.id),
          catalogId: String(row.catalogId)
        }
      });
    },
    // get data list
    getDataList() {
      this.dataListLoading = true;
      let param = {};
      Object.assign(param, this.dataForm, {
        page: this.pageIndex,
        limit: this.pageSize
      });
      this.$http({
        url: this.$http.adornUrl("/product/spuinfo/list"),
        method: "get",
        params: this.$http.adornParams(param)
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
    // page size
    sizeChangeHandle(val) {
      this.pageSize = val;
      this.pageIndex = 1;
      this.getDataList();
    },
    // current page
    currentChangeHandle(val) {
      this.pageIndex = val;
      this.getDataList();
    },
    // multi-select
    selectionChangeHandle(val) {
      this.dataListSelections = val;
    },
    // product listing (上架)
    productUp(spuId) {
      this.$http({
        url: this.$http.adornUrl(`/product/spuinfo/${spuId}/up`),
        method: "post"
      }).then(({ data }) => {
        if (data && data.code === 0) {
          this.$message.success("Put on sale success");
          this.getDataList();
        } else {
          this.$message.error(data.msg || "Put on sale failed");
        }
      }).catch(() => {
        this.$message.error("Put on sale failed");
      });
    },
    // product off-shelf (下架)
    productDown(spuId) {
      this.downSpu(spuId, true).then((ok) => {
        if (ok) {
          this.$message.success("Put off sale success");
          this.getDataList();
        }
      });
    },
    downSpu(spuId, showError) {
      return this.$http({
        url: this.$http.adornUrl(`/product/spuinfo/${spuId}/down`),
        method: "post"
      })
        .then(({ data }) => {
          if (data && data.code === 0) {
            return true;
          }
          if (showError) {
            this.$message.error(data.msg || "Put off sale failed");
          }
          return false;
        })
        .catch(() => {
          if (showError) {
            this.$message.error("Put off sale failed");
          }
          return false;
        });
    },
    deleteHandle(rowOrId) {
      const rows = this.resolveSpuDeleteRows(rowOrId);
      if (!rows.length) {
        return;
      }
      const ids = rows.map((r) => r.id);
      const single = rows.length === 1;
      const msg = single
        ? `Remove SPU #${ids[0]} (${rows[0].spuName || "unnamed"}) from the catalog? This deletes the SPU, all its SKUs, images, attributes, warehouse stock rows, and promotion rules. Historical orders are not deleted. Blocked if unpaid/in-progress orders or locked stock exist.`
        : `Remove ${ids.length} selected SPU(s) from the catalog (same scope as above)? Blocked if unpaid/in-progress orders or locked stock exist.`;
      this.$confirm(msg, "Delete Confirmation", {
        confirmButtonText: "Delete",
        cancelButtonText: "Cancel",
        type: "warning"
      })
        .then(() => this.runSpuDelete(ids))
        .catch(() => {});
    },
    resolveSpuDeleteRows(rowOrId) {
      if (rowOrId && typeof rowOrId === "object") {
        return [rowOrId];
      }
      if (rowOrId != null && rowOrId !== "") {
        const found = this.dataList.find((r) => r.id === rowOrId);
        return found ? [found] : [{ id: rowOrId, publishStatus: 0 }];
      }
      return this.dataListSelections.slice();
    },
    runSpuDelete(ids) {
      this.dataListLoading = true;
      this.$http({
        url: this.$http.adornUrl("/product/spuinfo/delete"),
        method: "post",
        data: this.$http.adornData(ids, false)
      })
        .then(({ data }) => {
          if (data && data.code === 0) {
            this.$message.success(data.msg || "SPU removed from catalog");
            this.getDataList();
          } else {
            this.$message.error(data.msg || "Delete failed");
            this.dataListLoading = false;
          }
        })
        .catch(() => {
          this.$message.error("Delete failed");
          this.dataListLoading = false;
        });
    }
  },
  mounted() {
    this.dataSub = PubSub.subscribe("dataForm", (msg, val) => {
      this.dataForm = val;
      this.getDataList();
    });
  },
  beforeDestroy() {
    PubSub.unsubscribe(this.dataSub);
  }
};
</script>

<style scoped>
.spu-toolbar {
  margin-bottom: 8px;
}
.danger-text {
  color: #f56c6c;
}
</style>
