<template>
  <div>
    <el-row :gutter="20">
      <el-col :span="16">
        <el-card class="box-card">
          <el-tabs tab-position="left" style="width:98%">
            <el-tab-pane
              :label="group.attrGroupName"
              v-for="(group,gidx) in dataResp.attrGroups"
              :key="group.attrGroupId"
            >
              <!-- 遍历属性,每个tab-pane对应一个表单，每个属性Yes一个表单项  spu.baseAttrs[0] = [{attrId:xx,val:}]-->
              <el-form ref="form" :model="dataResp">
                <el-form-item
                  :label="attr.attrName"
                  v-for="(attr,aidx) in group.attrs"
                  :key="attr.attrId"
                >
                  <el-input
                    v-model="dataResp.baseAttrs[gidx][aidx].attrId"
                    type="hidden"
                    v-show="false"
                  ></el-input>
                  <el-select
                    v-model="dataResp.baseAttrs[gidx][aidx].attrValues"
                    :multiple="Number(attr.valueType) === 1"
                    filterable
                    allow-create
                    default-first-option
                    placeholder="Select or enter value"
                  >
                    <el-option
                      v-for="(val,vidx) in getAttrOptions(dataResp.baseAttrs[gidx][aidx], dataResp.baseAttrs[gidx][aidx].attrValues)"
                      :key="vidx"
                      :label="val"
                      :value="val"
                    ></el-option>
                  </el-select>
                  <el-checkbox
                    v-model="dataResp.baseAttrs[gidx][aidx].showDesc"
                    :true-label="1"
                    :false-label="0"
                  >Quick Display</el-checkbox>
                </el-form-item>
              </el-form>
            </el-tab-pane>
          </el-tabs>
          <div style="margin:auto">
            <el-button type="success" style="float:right" @click="submitSpuAttrs">Are you sure you want to Changes</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { formatSaveWithSearchSync } from "@/utils/searchSyncMessage";

export default {
  components: {},
  props: {},
  data() {
    return {
      spuId: "",
      catalogId: "",
      dataResp: {
        //后台Back的所有数据
        attrGroups: [],
        baseAttrs: []
      },
      spuAttrsMap: {}
    };
  },
  computed: {},
  watch: {
    $route() {
      this.loadPage();
    }
  },
  methods: {
    loadPage() {
      this.clearData();
      this.getQueryParams();
      if (this.spuId && this.catalogId) {
        this.getSpuBaseAttrs().then(() => this.showBaseAttrs());
      }
    },
    /** Map saved attr_value to el-select model (single vs multi by valueType). */
    resolveAttrValues(attr, raw) {
      const trimmed = (raw || "").trim();
      if (!trimmed) {
        return Number(attr.valueType) === 1 ? [] : "";
      }
      const parts = trimmed.split(";").map(s => s.trim()).filter(Boolean);
      return Number(attr.valueType) === 1 ? parts : (parts[0] || "");
    },
    /** 获取属性选项：displayOptions（含已Save值） + 当前值（allow-create Add的） */
    getAttrOptions(attrItem, currentVal) {
      const opts = [...(attrItem.displayOptions || [])];
      const vals = currentVal == null || currentVal === "" ? [] : (Array.isArray(currentVal) ? currentVal : [currentVal]);
      vals.forEach(v => {
        const vStr = v != null ? String(v).trim() : "";
        if (vStr && !opts.includes(vStr)) opts.push(vStr);
      });
      return opts;
    },
    clearData(){
      this.dataResp.attrGroups = [];
      this.dataResp.baseAttrs = [];
      this.spuAttrsMap = {};
    },
    getSpuBaseAttrs() {
      return this.$http({
        url: this.$http.adornUrl(`/product/attr/base/listforspu/${this.spuId}`),
        method: "get"
      }).then(({ data }) => {
        if (!data || data.code !== 0) {
          return;
        }
        (data.data || []).forEach(item => {
          this.spuAttrsMap[String(item.attrId)] = item;
        });
      });
    },
    getQueryParams() {
      this.spuId = this.$route.query.spuId;
      this.catalogId = this.$route.query.catalogId;
    },
    showBaseAttrs() {
      const spuAttrsMap = this.spuAttrsMap;
      return this.$http({
        url: this.$http.adornUrl(
          `/product/attrgroup/${this.catalogId}/withattr`
        ),
        method: "get",
        params: this.$http.adornParams({})
      }).then(({ data }) => {
        if (!data || data.code !== 0) {
          return;
        }
        const groups = (data.data || []).map(item => ({ ...item, attrs: item.attrs || [] }));
        const baseAttrs = [];
        groups.forEach(item => {
          const attrArray = [];
          (item.attrs || []).forEach(attr => {
            const displayOptions = (attr.valueSelect || "")
              .split(";")
              .map(s => s.trim())
              .filter(Boolean);
            const saved = spuAttrsMap[String(attr.attrId)];
            const v = saved
              ? this.resolveAttrValues(attr, saved.attrValue)
              : Number(attr.valueType) === 1
                ? []
                : "";
            const savedParts = Array.isArray(v) ? v : v ? [v] : [];
            savedParts.forEach(sv => {
              if (sv && !displayOptions.includes(sv)) {
                displayOptions.push(sv);
              }
            });
            attrArray.push({
              attrId: attr.attrId,
              attrName: attr.attrName,
              attrValues: v,
              displayOptions,
              showDesc: saved ? saved.quickShow : attr.showDesc
            });
          });
          baseAttrs.push(attrArray);
        });
        this.dataResp.attrGroups = groups;
        this.dataResp.baseAttrs = baseAttrs;
      });
    },
    submitSpuAttrs() {
      //spu_id  attr_id  attr_name             attr_value             attr_sort  quick_show
      let submitData = [];
      this.dataResp.baseAttrs.forEach(item => {
        item.forEach(attr => {
          let val = "";
          if (attr.attrValues instanceof Array) {
            val = attr.attrValues.join(";");
          } else {
            val = attr.attrValues;
          }

          if (val != "") {
            submitData.push({
              attrId: attr.attrId,
              attrName: attr.attrName,
              attrValue: val,
              quickShow: attr.showDesc
            });
          }
        });
      });
      this.$confirm("Update product specifications. Continue?", "Confirm", {
        confirmButtonText: "OK",
        cancelButtonText: "Cancel",
        type: "warning"
      })
        .then(() => {
          this.$http({
            url: this.$http.adornUrl(`/product/attr/update/${this.spuId}`),
            method: "post",
            data: submitData
          }).then(({ data }) => {
            if (data && data.code === 0) {
              const msg = formatSaveWithSearchSync(data, "Specifications updated");
              this.$message.success(msg || "Specifications updated");
              this.loadPage();
            } else {
              this.$message.error((data && data.msg) || "Update failed");
            }
          });
        })
        .catch((e) => {
          this.$message({
            type: "info",
            message: "Update cancelled"
          });
        });
    }
  },
  mounted() {
    this.loadPage();
  },
  activated() {
    this.loadPage();
  }
};
</script>
<style scoped>
</style>