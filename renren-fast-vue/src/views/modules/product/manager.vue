<template>
  <div class="mod-config">
    <el-alert
      v-if="filterSpuId"
      type="info"
      :closable="true"
      show-icon
      class="sku-spu-filter-alert"
      @close="clearSpuFilter"
    >
      Showing SKUs for SPU ID: <strong>{{ filterSpuId }}</strong>
      <span class="sku-spu-filter-hint"> (category/brand filters are ignored while this filter is active)</span>
    </el-alert>
    <el-form :inline="true" :model="dataForm" @keyup.enter.native="getDataList()">
      <el-form :inline="true" :model="dataForm">
        <el-form-item label="Category">
          <category-cascader :catelogPath.sync="catelogPath"></category-cascader>
        </el-form-item>
        <el-form-item label="Brand">
          <brand-select style="width:160px"></brand-select>
        </el-form-item>
        <el-form-item label="Price">
          <el-input-number style="width:160px" v-model="dataForm.price.min" :min="0"></el-input-number>-
          <el-input-number style="width:160px" v-model="dataForm.price.max" :min="0"></el-input-number>
        </el-form-item>
        <el-form-item label="Keyword">
          <el-input style="width:160px" v-model="dataForm.key" clearable></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchSkuInfo">Query</el-button>
          <el-button
            type="danger"
            :disabled="dataListSelections.length <= 0"
            @click="deleteHandle()"
          >Batch Delete</el-button>
        </el-form-item>
      </el-form>
    </el-form>
    <el-table
      :data="dataList"
      border
      v-loading="dataListLoading"
      @selection-change="selectionChangeHandle"
      style="width: 100%;"
      @expand-change="getSkuDetails"
    >
      <el-table-column type="expand">
        <template slot-scope="scope">
          Product Title: {{ scope.row.skuTitle }}
          <br />
          Product Subtitle: {{ scope.row.skuSubtitle }}
          <br />
          Product Description: {{ scope.row.skuDesc }}
          <br />
          Category ID: {{ scope.row.catalogId }}
          <br />
          SpuID: {{ scope.row.spuId }}
          <br />
          Brand ID: {{ scope.row.brandId }}
          <br />
        </template>
      </el-table-column>
      <el-table-column type="selection" header-align="center" align="center" width="50"></el-table-column>
      <el-table-column prop="skuId" header-align="center" align="center" label="SKU ID"></el-table-column>
      <el-table-column prop="skuName" header-align="center" align="center" label="Name"></el-table-column>
      <el-table-column prop="skuDefaultImg" header-align="center" align="center" label="Default Image">
        <template slot-scope="scope">
          <span class="admin-img admin-img--md"><img :src="scope.row.skuDefaultImg" alt="" /></span>
        </template>
      </el-table-column>
      <el-table-column prop="price" header-align="center" align="center" label="Price"></el-table-column>
      <el-table-column prop="saleCount" header-align="center" align="center" label="Sales"></el-table-column>
      <el-table-column fixed="right" header-align="center" align="center" width="320" label="Operations">
        <template slot-scope="scope">
          <el-button type="text" size="small" @click="previewHandle(scope.row)">Preview</el-button>
          <el-button type="text" size="small" @click="uploadImagesHandle(scope.row)">Images</el-button>
          <el-button type="text" size="small" @click="stockSettingsHandle(scope.row)">Stock</el-button>
          <el-button type="text" size="small" class="danger-text" @click="deleteHandle(scope.row)">Delete</el-button>
          <el-dropdown trigger="click" @command="cmd => handleCommand(scope.row, cmd)">
            <el-button type="text" size="small">
              More<i class="el-icon-arrow-down el-icon--right"></i>
            </el-button>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="viewSpu">View SPU</el-dropdown-item>
              <el-dropdown-item command="editSku">Edit Name / Title</el-dropdown-item>
              <el-dropdown-item command="editPrice">Edit Price</el-dropdown-item>
              <el-dropdown-item command="copyLink">Copy Mall Link</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </template>
      </el-table-column>
    </el-table>

    <!-- Upload Images Dialog -->
    <el-dialog
      title="Upload SKU Images"
      :visible.sync="uploadDialogVisible"
      width="600px"
      @close="closeUploadDialog"
    >
      <div v-if="uploadDialogSku">
        <p><strong>{{ uploadDialogSku.skuName }}</strong> (SKU ID: {{ uploadDialogSku.skuId }})</p>
        <el-form label-width="100px">
          <el-form-item label="Images">
            <multi-upload v-model="skuImageUrls" :max-count="10"></multi-upload>
          </el-form-item>
          <el-form-item label="Set Default" v-if="skuImageUrls.length > 0">
            <el-radio-group v-model="defaultImgIndex">
              <el-radio
                v-for="(url, idx) in skuImageUrls"
                :key="idx"
                :label="idx"
                style="display: block; margin-bottom: 8px;"
              >
                <span class="admin-img admin-img--sm" style="margin-right: 8px;"><img :src="url" alt="" /></span>
                Image {{ idx + 1 }}
              </el-radio>
            </el-radio-group>
          </el-form-item>
        </el-form>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="uploadDialogVisible = false">Cancel</el-button>
        <el-button type="primary" :loading="uploadSaveLoading" @click="saveSkuImages">Save</el-button>
      </span>
    </el-dialog>

    <!-- Edit sku name / title (mall display) -->
    <el-dialog
      title="Edit SKU Name & Title"
      :visible.sync="skuEditDialogVisible"
      width="520px"
      @close="closeSkuEditDialog"
    >
      <p v-if="skuEditDialogSku" style="margin: 0 0 8px; color: #666; font-size: 13px;">
        SKU {{ skuEditDialogSku.skuId }}
        <span v-if="skuEditDialogSku.spuId"> · SPU {{ skuEditDialogSku.spuId }}</span>
      </p>
      <el-alert
        type="info"
        :closable="false"
        show-icon
        style="margin-bottom: 12px;"
        title="Mall detail uses Title first, then Name. Search list uses Title. If the SPU is on sale, search syncs automatically when you save."
      />
      <el-form label-width="110px">
        <el-form-item label="Name (skuName)">
          <el-input v-model="skuEditForm.skuName" placeholder="Internal / list name, e.g. Apple Red 4+128G" clearable />
        </el-form-item>
        <el-form-item label="Title (skuTitle)">
          <el-input v-model="skuEditForm.skuTitle" placeholder="Mall search & detail headline" clearable />
        </el-form-item>
        <el-form-item label="Subtitle">
          <el-input v-model="skuEditForm.skuSubtitle" placeholder="Detail page subtitle (optional)" clearable />
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="skuEditDialogVisible = false">Cancel</el-button>
        <el-button type="primary" :loading="skuEditSaveLoading" @click="saveSkuText">Save</el-button>
      </span>
    </el-dialog>

    <!-- Quick price edit -->
    <el-dialog
      title="Edit SKU Price"
      :visible.sync="priceDialogVisible"
      width="420px"
      @close="closePriceDialog"
    >
      <p v-if="priceDialogSku">
        <strong>{{ priceDialogSku.skuName }}</strong> (SKU {{ priceDialogSku.skuId }})
      </p>
      <el-form label-width="80px" style="margin-top: 12px;">
        <el-form-item label="Price">
          <el-input-number v-model="priceForm.price" :min="0" :precision="2" :step="1" style="width: 100%;"></el-input-number>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="priceDialogVisible = false">Cancel</el-button>
        <el-button type="primary" :loading="priceSaveLoading" @click="saveSkuPrice">Save</el-button>
      </span>
    </el-dialog>

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
import CategoryCascader from "../common/category-cascader";
import BrandSelect from "../common/brand-select";
import MultiUpload from "@/components/upload/multiUpload";
import { mallItemDetailUrl } from "@/utils/mallUrl";
import { formatSaveWithSearchSync } from "@/utils/searchSyncMessage";

export default {
  data() {
    return {
      uploadDialogVisible: false,
      uploadDialogSku: null,
      skuImageUrls: [],
      defaultImgIndex: 0,
      uploadSaveLoading: false,
      skuEditDialogVisible: false,
      skuEditDialogSku: null,
      skuEditForm: { skuName: "", skuTitle: "", skuSubtitle: "" },
      skuEditSaveLoading: false,
      priceDialogVisible: false,
      priceDialogSku: null,
      priceForm: { price: 0 },
      priceSaveLoading: false,
      catPathSub: null,
      brandIdSub: null,
      filterSpuId: "",
      dataForm: {
        key: "",
        brandId: 0,
        catelogId: 0,
        spuId: "",
        price: {
          min: 0,
          max: 0
        }
      },
      dataList: [],
      pageIndex: 1,
      pageSize: 10,
      totalPage: 0,
      dataListLoading: false,
      dataListSelections: [],
      catelogPath: []
    };
  },
  components: {
    CategoryCascader,
    BrandSelect,
    MultiUpload
  },
  watch: {
    $route() {
      this.applyRouteSpuFilter();
    }
  },
  mounted() {
    this.applyRouteSpuFilter();
    this.getDataList();
    this.catPathSub = PubSub.subscribe("catPath", (msg, val) => {
      this.dataForm.catelogId = val[val.length - 1];
    });
    this.brandIdSub = PubSub.subscribe("brandId", (msg, val) => {
      this.dataForm.brandId = val;
    });
  },
  activated() {
    this.applyRouteSpuFilter();
    this.getDataList();
  },
  methods: {
    applyRouteSpuFilter() {
      const spuId = this.$route.query.spuId;
      if (spuId) {
        this.filterSpuId = String(spuId);
        this.dataForm.spuId = String(spuId);
        // SPU filter is authoritative; leftover brand/category filters hide rows (e.g. OPPO SPU 8 + Xiaomi brand).
        this.dataForm.brandId = 0;
        this.dataForm.catelogId = 0;
        this.catelogPath = [];
        this.PubSub.publish("brandId", 0);
      }
    },
    clearSpuFilter() {
      this.filterSpuId = "";
      this.dataForm.spuId = "";
      const query = { ...this.$route.query };
      delete query.spuId;
      this.$router.replace({ path: this.$route.path, query });
      this.getDataList();
    },
    getSkuDetails(row, expand) {
      console.log("Expand row...", row, expand);
    },
    previewHandle(row) {
      const url = mallItemDetailUrl(row.skuId);
      window.open(url, "_blank", "noopener,noreferrer");
    },
    handleCommand(row, command) {
      if (command === "viewSpu") {
        this.viewSpuHandle(row);
      } else if (command === "editSku") {
        this.editSkuHandle(row);
      } else if (command === "editPrice") {
        this.editPriceHandle(row);
      } else if (command === "copyLink") {
        this.copyMallLinkHandle(row);
      }
    },
    viewSpuHandle(row) {
      if (!row.spuId) {
        this.$message.warning("This SKU has no linked SPU");
        return;
      }
      this.$router.push({
        path: "/product-spu",
        query: { openSpuId: String(row.spuId) }
      });
    },
    editSkuHandle(row) {
      this.skuEditDialogSku = row;
      this.skuEditDialogVisible = true;
      this.skuEditForm = {
        skuName: row.skuName || "",
        skuTitle: row.skuTitle || "",
        skuSubtitle: row.skuSubtitle || ""
      };
      this.$http({
        url: this.$http.adornUrl(`/product/skuinfo/info/${row.skuId}`),
        method: "get"
      })
        .then(({ data }) => {
          if (data && data.code === 0 && data.skuInfo) {
            const s = data.skuInfo;
            this.skuEditForm.skuName = s.skuName || "";
            this.skuEditForm.skuTitle = s.skuTitle || "";
            this.skuEditForm.skuSubtitle = s.skuSubtitle || "";
          }
        })
        .catch(() => {});
    },
    closeSkuEditDialog() {
      this.skuEditDialogSku = null;
      this.skuEditForm = { skuName: "", skuTitle: "", skuSubtitle: "" };
    },
    saveSkuText() {
      if (!this.skuEditDialogSku) return;
      const name = (this.skuEditForm.skuName || "").trim();
      const title = (this.skuEditForm.skuTitle || "").trim();
      if (!name && !title) {
        this.$message.warning("Enter at least Name or Title");
        return;
      }
      this.skuEditSaveLoading = true;
      this.$http({
        url: this.$http.adornUrl(`/product/skuinfo/info/${this.skuEditDialogSku.skuId}`),
        method: "get"
      })
        .then(({ data }) => {
          if (!data || data.code !== 0 || !data.skuInfo) {
            throw new Error((data && data.msg) || "Failed to load SKU");
          }
          const sku = { ...data.skuInfo };
          sku.skuName = name || sku.skuName;
          sku.skuTitle = title;
          sku.skuSubtitle = (this.skuEditForm.skuSubtitle || "").trim();
          return this.$http({
            url: this.$http.adornUrl("/product/skuinfo/update"),
            method: "post",
            data: this.$http.adornData(sku)
          });
        })
        .then(({ data }) => {
          if (data && data.code === 0) {
            const msg = formatSaveWithSearchSync(data, "Saved");
            this.$message.success(msg || "Saved");
            this.skuEditDialogVisible = false;
            this.getDataList();
          } else {
            this.$message.error((data && data.msg) || "Save failed");
          }
        })
        .catch((err) => {
          this.$message.error(err.message || "Save failed");
        })
        .finally(() => {
          this.skuEditSaveLoading = false;
        });
    },
    editPriceHandle(row) {
      this.priceDialogSku = row;
      this.priceForm.price = Number(row.price) || 0;
      this.priceDialogVisible = true;
    },
    closePriceDialog() {
      this.priceDialogSku = null;
      this.priceForm.price = 0;
    },
    saveSkuPrice() {
      if (!this.priceDialogSku) return;
      const price = Number(this.priceForm.price);
      if (!Number.isFinite(price) || price < 0) {
        this.$message.warning("Please enter a valid price");
        return;
      }
      this.priceSaveLoading = true;
      this.$http({
        url: this.$http.adornUrl(`/product/skuinfo/info/${this.priceDialogSku.skuId}`),
        method: "get"
      })
        .then(({ data }) => {
          if (!data || data.code !== 0 || !data.skuInfo) {
            throw new Error((data && data.msg) || "Failed to load SKU");
          }
          const sku = { ...data.skuInfo, price };
          return this.$http({
            url: this.$http.adornUrl("/product/skuinfo/update"),
            method: "post",
            data: this.$http.adornData(sku)
          });
        })
        .then(({ data }) => {
          if (data && data.code === 0) {
            const msg = formatSaveWithSearchSync(data, "Price updated");
            this.$message.success(msg || "Price updated");
            this.priceDialogVisible = false;
            this.getDataList();
          } else {
            this.$message.error((data && data.msg) || "Update failed");
          }
        })
        .catch((err) => {
          this.$message.error(err.message || "Update failed");
        })
        .finally(() => {
          this.priceSaveLoading = false;
        });
    },
    copyMallLinkHandle(row) {
      const url = mallItemDetailUrl(row.skuId);
      if (navigator.clipboard && navigator.clipboard.writeText) {
        navigator.clipboard.writeText(url).then(() => {
          this.$message.success("Mall link copied");
        }).catch(() => {
          this.$message.info(url);
        });
      } else {
        this.$message.info(url);
      }
    },
    uploadImagesHandle(row) {
      this.uploadDialogSku = row;
      this.uploadDialogVisible = true;
      this.loadSkuImages(row.skuId);
    },
    loadSkuImages(skuId) {
      this.$http({
        url: this.$http.adornUrl(`/product/skuimages/bysku/${skuId}`),
        method: "get"
      }).then(({ data }) => {
        if (data && data.code === 0 && data.list) {
          this.skuImageUrls = data.list
            .sort((a, b) => (a.imgSort || 0) - (b.imgSort || 0))
            .map((item) => item.imgUrl);
          const defaultIdx = data.list.findIndex((item) => item.defaultImg === 1);
          this.defaultImgIndex = defaultIdx >= 0 ? defaultIdx : 0;
        } else {
          this.skuImageUrls = [];
          this.defaultImgIndex = 0;
        }
      }).catch(() => {
        this.skuImageUrls = [];
        this.defaultImgIndex = 0;
      });
    },
    closeUploadDialog() {
      this.uploadDialogSku = null;
      this.skuImageUrls = [];
      this.defaultImgIndex = 0;
    },
    saveSkuImages() {
      if (!this.uploadDialogSku || this.skuImageUrls.length === 0) {
        this.$message.warning("Please upload at least one image");
        return;
      }
      this.uploadSaveLoading = true;
      const images = this.skuImageUrls.map((imgUrl, i) => ({
        imgUrl,
        defaultImg: i === this.defaultImgIndex ? 1 : 0,
        imgSort: i
      }));
      this.$http({
        url: this.$http.adornUrl("/product/skuimages/saveBatch"),
        method: "post",
        data: this.$http.adornData({
          skuId: this.uploadDialogSku.skuId,
          images
        })
      })
        .then(({ data }) => {
          if (data && data.code === 0) {
            const msg = formatSaveWithSearchSync(data, "Images saved");
            this.$message.success(msg || "Images saved");
            this.uploadDialogVisible = false;
            this.getDataList();
          } else {
            this.$message.error(data.msg || "Save failed");
          }
        })
        .catch((err) => {
          this.$message.error(err.message || "Save failed");
        })
        .finally(() => {
          this.uploadSaveLoading = false;
        });
    },
    stockSettingsHandle(row) {
      this.$router.push({ path: "/ware-sku", query: { skuId: String(row.skuId) } });
    },
    searchSkuInfo() {
      this.getDataList();
    },
    getDataList() {
      this.dataListLoading = true;
      this.$http({
        url: this.$http.adornUrl("/product/skuinfo/list"),
        method: "get",
        params: this.$http.adornParams({
          page: this.pageIndex,
          limit: this.pageSize,
          key: this.dataForm.key,
          catelogId: this.dataForm.catelogId,
          brandId: this.dataForm.brandId,
          spuId: this.dataForm.spuId || 0,
          min: this.dataForm.price.min,
          max: this.dataForm.price.max
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
    sizeChangeHandle(val) {
      this.pageSize = val;
      this.pageIndex = 1;
      this.getDataList();
    },
    currentChangeHandle(val) {
      this.pageIndex = val;
      this.getDataList();
    },
    selectionChangeHandle(val) {
      this.dataListSelections = val;
    },
    deleteHandle(rowOrId) {
      const rows = this.resolveSkuDeleteRows(rowOrId);
      if (!rows.length) {
        return;
      }
      const ids = rows.map((r) => r.skuId);
      const single = rows.length === 1;
      const msg = single
        ? `Remove SKU #${ids[0]} (${rows[0].skuName || "unnamed"}) from the catalog? This deletes product, image, warehouse stock, and promotion rows for this SKU. Historical orders are not deleted. Blocked if unpaid/in-progress orders or locked stock exist.`
        : `Remove ${ids.length} selected SKU(s) from the catalog (same scope)? Blocked if unpaid/in-progress orders or locked stock exist.`;
      this.$confirm(msg, "Delete Confirmation", {
        confirmButtonText: "Delete",
        cancelButtonText: "Cancel",
        type: "warning"
      })
        .then(() => this.runSkuDelete(ids))
        .catch(() => {});
    },
    resolveSkuDeleteRows(rowOrId) {
      if (rowOrId && typeof rowOrId === "object") {
        return [rowOrId];
      }
      if (rowOrId != null && rowOrId !== "") {
        const found = this.dataList.find((r) => r.skuId === rowOrId);
        return found ? [found] : [{ skuId: rowOrId }];
      }
      return this.dataListSelections.slice();
    },
    runSkuDelete(ids) {
      this.dataListLoading = true;
      this.$http({
        url: this.$http.adornUrl("/product/skuinfo/delete"),
        method: "post",
        data: this.$http.adornData(ids, false)
      })
        .then(({ data }) => {
          if (data && data.code === 0) {
            this.$message.success(data.msg || "SKU removed from catalog");
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
  beforeDestroy() {
    PubSub.unsubscribe(this.catPathSub);
    PubSub.unsubscribe(this.brandIdSub);
  }
};
</script>

<style scoped>
.sku-spu-filter-hint {
  font-size: 12px;
  font-weight: normal;
  color: #909399;
}
.sku-spu-filter-alert {
  margin-bottom: 12px;
}
.danger-text {
  color: #f56c6c;
}
</style>
