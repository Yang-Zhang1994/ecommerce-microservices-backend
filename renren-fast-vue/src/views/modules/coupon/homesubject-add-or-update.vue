<template>
  <el-dialog
    :title="!dataForm.id ? 'Add' : 'Edit'"
    :close-on-click-modal="false"
    :visible.sync="visible"
  >
    <el-form
      :model="dataForm"
      :rules="dataRule"
      ref="dataForm"
      @keyup.enter.native="dataFormSubmit()"
      label-width="120px"
    >
      <el-form-item label="Subject name" prop="name">
        <el-input v-model="dataForm.name" placeholder="Subject name"></el-input>
      </el-form-item>
      <el-form-item label="Subject title" prop="title">
        <el-input v-model="dataForm.title" placeholder="Subject title"></el-input>
      </el-form-item>
      <el-form-item label="Subject subtitle" prop="subTitle">
        <el-input v-model="dataForm.subTitle" placeholder="Subject subtitle"></el-input>
      </el-form-item>
      <el-form-item label="Display status" prop="status">
        <el-switch v-model="dataForm.status" active-color="#13ce66" inactive-color="#ff4949"></el-switch>
      </el-form-item>
      <el-form-item label="Detail URL" prop="url">
        <el-input v-model="dataForm.url" placeholder="Detail URL"></el-input>
      </el-form-item>
      <el-form-item label="Sort" prop="sort">
        <el-input v-model="dataForm.sort" placeholder="Sort"></el-input>
      </el-form-item>
      <el-form-item label="Subject image URL" prop="img">
        <single-upload v-model="dataForm.img"></single-upload>
      </el-form-item>
    </el-form>
    <span slot="footer" class="dialog-footer">
      <el-button @click="visible = false">Cancel</el-button>
      <el-button type="primary" @click="dataFormSubmit()">Confirm</el-button>
    </span>
  </el-dialog>
</template>

<script>
import SingleUpload from '@/components/upload/singleUpload'
export default {
  components:{SingleUpload},
  data() {
    return {
      visible: false,
      dataForm: {
        id: 0,
        name: "",
        title: "",
        subTitle: "",
        status: "",
        url: "",
        sort: "",
        img: ""
      },
      dataRule: {
        name: [
          { required: true, message: 'This field is required', trigger: "blur" }
        ],
        title: [
          { required: true, message: 'This field is required', trigger: "blur" }
        ],
        subTitle: [
          { required: true, message: 'This field is required', trigger: "blur" }
        ],
        status: [
          { required: true, message: 'This field is required', trigger: "blur" }
        ],
        url: [{ required: true, message: 'This field is required', trigger: "blur" }],
        sort: [{ required: true, message: 'Sort is required', trigger: "blur" }],
        img: [
          { required: true, message: 'This field is required', trigger: "blur" }
        ]
      }
    };
  },
  methods: {
    init(id) {
      this.dataForm.id = id || 0;
      this.visible = true;
      this.$nextTick(() => {
        this.$refs["dataForm"].resetFields();
        if (this.dataForm.id) {
          this.$http({
            url: this.$http.adornUrl(
              `/coupon/homesubject/info/${this.dataForm.id}`
            ),
            method: "get",
            params: this.$http.adornParams()
          }).then(({ data }) => {
            if (data && data.code === 0) {
              this.dataForm.name = data.homeSubject.name;
              this.dataForm.title = data.homeSubject.title;
              this.dataForm.subTitle = data.homeSubject.subTitle;
              this.dataForm.status = data.homeSubject.status;
              this.dataForm.url = data.homeSubject.url;
              this.dataForm.sort = data.homeSubject.sort;
              this.dataForm.img = data.homeSubject.img;
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
              `/coupon/homesubject/${!this.dataForm.id ? "save" : "update"}`
            ),
            method: "post",
            data: this.$http.adornData({
              id: this.dataForm.id || undefined,
              name: this.dataForm.name,
              title: this.dataForm.title,
              subTitle: this.dataForm.subTitle,
              status: this.dataForm.status,
              url: this.dataForm.url,
              sort: this.dataForm.sort,
              img: this.dataForm.img
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
    }
  }
};
</script>
