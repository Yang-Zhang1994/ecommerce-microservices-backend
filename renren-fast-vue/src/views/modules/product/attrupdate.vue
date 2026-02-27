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
              <!-- 遍历属性,每个tab-pane对应一个表单，每个属性是一个表单项  spu.baseAttrs[0] = [{attrId:xx,val:}]-->
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
                    :multiple="attr.valueType == 1"
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
            <el-button type="success" style="float:right" @click="submitSpuAttrs">Confirm Changes</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
export default {
  components: {},
  props: {},
  data() {
    return {
      spuId: "",
      catalogId: "",
      dataResp: {
        //后台返回的所有数据
        attrGroups: [],
        baseAttrs: []
      },
      spuAttrsMap: {}
    };
  },
  computed: {},
  methods: {
    /** 获取属性选项：displayOptions（含已保存值） + 当前值（allow-create 新增的） */
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
        (data.data || []).forEach(item => {
          this.spuAttrsMap["" + item.attrId] = item;
        });
      });
    },
    getQueryParams() {
      this.spuId = this.$route.query.spuId;
      this.catalogId = this.$route.query.catalogId;
    },
    showBaseAttrs() {
      let _this = this;
      this.$http({
        url: this.$http.adornUrl(
          `/product/attrgroup/${this.catalogId}/withattr`
        ),
        method: "get",
        params: this.$http.adornParams({})
      }).then(({ data }) => {
        // Normalize: backend may return attrs as null for groups with no attrs
        const groups = (data.data || []).map(item => ({ ...item, attrs: item.attrs || [] }));
        groups.forEach(item => {
          let attrArray = [];
          (item.attrs || []).forEach(attr => {
            let v = "";
            let displayOptions = ((attr.valueSelect || "").split(";")).map(s => s.trim()).filter(Boolean);
            if (_this.spuAttrsMap["" + attr.attrId]) {
              const raw = (_this.spuAttrsMap["" + attr.attrId].attrValue || "").trim();
              v = raw ? raw.split(";").map(s => s.trim()).filter(Boolean) : "";
              if (Array.isArray(v) && v.length === 1 && attr.valueType != 1) {
                v = v[0];
              }
              (Array.isArray(v) ? v : (v ? [v] : [])).forEach(sv => {
                if (sv && !displayOptions.includes(sv)) displayOptions.push(sv);
              });
            }
            attrArray.push({
              attrId: attr.attrId,
              attrName: attr.attrName,
              attrValues: v,
              displayOptions: displayOptions,
              showDesc: _this.spuAttrsMap["" + attr.attrId]
                ? _this.spuAttrsMap["" + attr.attrId].quickShow
                : attr.showDesc
            });
          });
          this.dataResp.baseAttrs.push(attrArray);
        });
        this.dataResp.attrGroups = groups;
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
            data: JSON.stringify(submitData)
          }).then(({ data }) => {
            this.$message({
              type: "success",
              message: "Specifications updated successfully!"
            });
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
  created() {},
  activated() {
    this.clearData();
    this.getQueryParams();
    if (this.spuId && this.catalogId) {
      this.getSpuBaseAttrs().then(() => this.showBaseAttrs());
    }
  }
};
</script>
<style scoped>
</style>