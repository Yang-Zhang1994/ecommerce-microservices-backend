<template>
  <el-dialog
    :title="!dataForm.id ? 'Add' : 'Edit'"
    :close-on-click-modal="false"
    :visible.sync="visible"
    @closed="dialogClose"
  >
    <el-form :model="dataForm" :rules="dataRule" ref="dataForm" label-width="120px">
      <!--       @keyup.enter.native="dataFormSubmit()" -->
      <el-form-item label="Attribute Name" prop="attrName">
        <el-input v-model="dataForm.attrName" placeholder="Attribute Name"></el-input>
      </el-form-item>
      <el-form-item label="Attribute Type" prop="attrType">
        <el-select v-model="dataForm.attrType" placeholder="Please Select">
          <el-option label="Base Attribute" :value="1"></el-option>
          <el-option label="Sale Attribute" :value="0"></el-option>
        </el-select>
      </el-form-item>

      <el-form-item label="Value Type" prop="valueType">
        <el-switch
          v-model="dataForm.valueType"
          active-text="Allow Multiple Values"
          inactive-text="Single Value Only"
          active-color="#13ce66"
          inactive-color="#ff4949"
          :inactive-value="0"
          :active-value="1"
        ></el-switch>
      </el-form-item>
      <el-form-item label="Selectable Values" prop="valueSelect">
        <!-- <el-input v-model="dataForm.valueSelect"></el-input> -->
        <el-select
          v-model="dataForm.valueSelect"
          multiple
          filterable
          allow-create
          placeholder="Please Enter"
        ></el-select>
      </el-form-item>
      <el-form-item label="Attribute Icon" prop="icon">
        <el-input v-model="dataForm.icon" placeholder="Attribute Icon"></el-input>
      </el-form-item>
      <el-form-item label="Category" prop="catelogId">
        <category-cascader :catelogPath.sync="catelogPath"></category-cascader>
      </el-form-item>
      <el-form-item label="Group" prop="attrGroupId" v-if="type == 1">
        <el-select ref="groupSelect" v-model="dataForm.attrGroupId" placeholder="Please Select">
          <el-option
            v-for="item in attrGroups"
            :key="item.attrGroupId"
            :label="item.attrGroupName"
            :value="item.attrGroupId"
          ></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="Searchable" prop="searchType" v-if="type == 1">
        <el-switch
          v-model="dataForm.searchType"
          active-color="#13ce66"
          inactive-color="#ff4949"
          :active-value="1"
          :inactive-value="0"
        ></el-switch>
      </el-form-item>
      <el-form-item label="Quick Display" prop="showDesc" v-if="type == 1">
        <el-switch
          v-model="dataForm.showDesc"
          active-color="#13ce66"
          inactive-color="#ff4949"
          :active-value="1"
          :inactive-value="0"
        ></el-switch>
      </el-form-item>
      <el-form-item label="Enable Status" prop="enable">
        <el-switch
          v-model="dataForm.enable"
          active-color="#13ce66"
          inactive-color="#ff4949"
          :active-value="1"
          :inactive-value="0"
        ></el-switch>
      </el-form-item>
    </el-form>
    <span slot="footer" class="dialog-footer">
      <el-button @click="visible = false">Cancel</el-button>
      <el-button type="primary" @click="dataFormSubmit()">Confirm</el-button>
    </span>
  </el-dialog>
</template>

<script>
import CategoryCascader from "../common/category-cascader";
export default {
  data() {
    return {
      visible: false,
      dataForm: {
        attrId: 0,
        attrName: "",
        searchType: 0,
        valueType: 1,
        icon: "",
        valueSelect: "",
        attrType: 1,
        enable: 1,
        catelogId: "",
        attrGroupId: "",
        showDesc: 0
      },
      catelogPath: [],
      attrGroups: [],
      dataRule: {
        attrName: [
          { required: true, message: "Attribute name cannot be empty", trigger: "blur" }
        ],
        searchType: [
          {
            required: true,
            message: "Searchable status cannot be empty",
            trigger: "blur"
          }
        ],
        valueType: [
          {
            required: true,
            message: "Value type cannot be empty",
            trigger: "blur"
          }
        ],
        icon: [
          { required: true, message: "Attribute icon cannot be empty", trigger: "blur" }
        ],
        attrType: [
          {
            required: true,
            message: "Attribute type cannot be empty",
            trigger: "blur"
          }
        ],
        enable: [
          {
            required: true,
            message: "Enable status cannot be empty",
            trigger: "blur"
          }
        ],
        catelogId: [
          {
            required: true,
            message: "Please select correct level 3 category data",
            trigger: "blur"
          }
        ],
        showDesc: [
          {
            required: true,
            message: "Quick display cannot be empty",
            trigger: "blur"
          }
        ]
      }
    };
  },
  props:{
    type:{
      type: Number,
      default: 1
    }
  },
  watch: {
    catelogPath(path) {
      //listen to path change, need to get group info for this level 3 category
      console.log("Path changed", path);
      this.attrGroups = [];
      this.dataForm.attrGroupId = "";
      this.dataForm.catelogId = path[path.length - 1];
      if (path && path.length == 3) {
        this.$http({
          url: this.$http.adornUrl(
            `/product/attrgroup/list/${path[path.length - 1]}`
          ),
          method: "get",
          params: this.$http.adornParams({ page: 1, limit: 10000000 })
        }).then(({ data }) => {
          if (data && data.code === 0) {
            this.attrGroups = data.page.list;
          } else {
            this.$message.error(data.msg);
          }
        });
      } else if (path.length == 0) {
        this.dataForm.catelogId = "";
      } else {
        this.$message.error("Please select correct category");
        this.dataForm.catelogId = "";
      }
    }
  },
  components: { CategoryCascader },
  methods: {
    init(id) {
      this.dataForm.attrId = id || 0;
      this.dataForm.attrType = this.type;
      this.visible = true;
      this.$nextTick(() => {
        this.$refs["dataForm"].resetFields();
        if (this.dataForm.attrId) {
          this.$http({
            url: this.$http.adornUrl(
              `/product/attr/info/${this.dataForm.attrId}`
            ),
            method: "get",
            params: this.$http.adornParams()
          }).then(({ data }) => {
            if (data && data.code === 0) {
              this.dataForm.attrName = data.attr.attrName;
              this.dataForm.searchType = data.attr.searchType;
              this.dataForm.valueType = data.attr.valueType;
              this.dataForm.icon = data.attr.icon;
              this.dataForm.valueSelect = data.attr.valueSelect.split(";");
              this.dataForm.attrType = data.attr.attrType;
              this.dataForm.enable = data.attr.enable;
              this.dataForm.catelogId = data.attr.catelogId;
              this.dataForm.showDesc = data.attr.showDesc;
              //attrGroupId
              //catelogPath
              this.catelogPath = data.attr.catelogPath;
              this.$nextTick(() => {
                this.dataForm.attrGroupId = data.attr.attrGroupId;
              });
            }
          });
        }
      });
    },
    // form submit
    dataFormSubmit() {
      this.$refs["dataForm"].validate(valid => {
        if (valid) {
          this.$http({
            url: this.$http.adornUrl(
              `/product/attr/${!this.dataForm.attrId ? "save" : "update"}`
            ),
            method: "post",
            data: this.$http.adornData({
              attrId: this.dataForm.attrId || undefined,
              attrName: this.dataForm.attrName,
              searchType: this.dataForm.searchType,
              valueType: this.dataForm.valueType,
              icon: this.dataForm.icon,
              valueSelect: this.dataForm.valueSelect.join(";"),
              attrType: this.dataForm.attrType,
              enable: this.dataForm.enable,
              catelogId: this.dataForm.catelogId,
              attrGroupId: this.dataForm.attrGroupId,
              showDesc: this.dataForm.showDesc
            })
          }).then(({ data }) => {
            if (data && data.code === 0) {
              this.$message({
                message: "Operation successful",
                type: "success",
                duration: 1500,
                onClose: () => {
                  this.visible = false;
                  this.$emit("refreshDataList");
                }
              });
            } else {
              this.$message.error(data.msg);
            }
          });
        }
      });
    },
    //dialogClose
    dialogClose() {
      this.catelogPath = [];
    }
  }
};
</script>
