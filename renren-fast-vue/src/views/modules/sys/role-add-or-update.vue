<template>
  <el-dialog
    :title="!dataForm.id ? 'Add New' : 'Edit'"
    :close-on-click-modal="false"
    :visible.sync="visible">
    <el-form :model="dataForm" :rules="dataRule" ref="dataForm" @keyup.enter.native="dataFormSubmit()" label-width="120px">
      <el-form-item label="Role Name" prop="roleName">
        <el-input v-model="dataForm.roleName" placeholder="Role name"></el-input>
      </el-form-item>
      <el-form-item label="Remark" prop="remark">
        <el-input v-model="dataForm.remark" placeholder="Remark"></el-input>
      </el-form-item>
      <el-form-item size="mini" label="Permissions">
        <div class="mod-menu__menu-tree-wrap">
          <el-tree
            :data="menuList"
            :props="menuListTreeProps"
            node-key="menuId"
            ref="menuListTree"
            :default-expand-all="false"
            show-checkbox>
          </el-tree>
        </div>
      </el-form-item>
    </el-form>
    <span slot="footer" class="dialog-footer">
      <el-button @click="visible = false">Cancel</el-button>
      <el-button type="primary" @click="dataFormSubmit()">Confirm</el-button>
    </span>
  </el-dialog>
</template>

<script>
  import { treeDataTranslate } from '@/utils'
  import { translateMenuName } from '@/utils/menuTranslation'
  import { filterHiddenAdminMenuRows } from '@/config/adminMenuVisibility'
  export default {
    data () {
      return {
        visible: false,
        menuList: [],
        menuListTreeProps: {
          label: 'name',
          children: 'children'
        },
        dataForm: {
          id: 0,
          roleName: '',
          remark: ''
        },
        dataRule: {
          roleName: [
            { required: true, message: 'Role name is required', trigger: 'blur' }
          ]
        },
        tempKey: -666666 // 临时key, 用于解决tree半选中Status项不能传给后台接口问题. # 待优化
      }
    },
    methods: {
      translateMenuTree (list) {
        if (!list || !list.length) return list
        return list.map(item => ({
          ...item,
          name: translateMenuName(item.name),
          children: item.children ? this.translateMenuTree(item.children) : item.children
        }))
      },
      collectMenuIds (list, out = []) {
        if (!list || !list.length) return out
        list.forEach(item => {
          if (item.menuId != null) out.push(item.menuId)
          if (item.children && item.children.length) this.collectMenuIds(item.children, out)
        })
        return out
      },
      init (id) {
        this.dataForm.id = id || 0
        this.$http({
          url: this.$http.adornUrl('/sys/menu/list'),
          method: 'get',
          params: this.$http.adornParams()
        }).then(({data}) => {
          const rows = filterHiddenAdminMenuRows(Array.isArray(data) ? data : [])
          this.menuList = this.translateMenuTree(treeDataTranslate(rows, 'menuId'))
        }).then(() => {
          this.visible = true
          this.$nextTick(() => {
            this.$refs['dataForm'].resetFields()
            this.$refs.menuListTree.setCheckedKeys([])
          })
        }).then(() => {
          if (this.dataForm.id) {
            this.$http({
              url: this.$http.adornUrl(`/sys/role/info/${this.dataForm.id}`),
              method: 'get',
              params: this.$http.adornParams()
            }).then(({data}) => {
              if (data && data.code === 0) {
                this.dataForm.roleName = data.role.roleName
                this.dataForm.remark = data.role.remark
                var idx = data.role.menuIdList.indexOf(this.tempKey)
                if (idx !== -1) {
                  data.role.menuIdList.splice(idx, data.role.menuIdList.length - idx)
                }
                this.$refs.menuListTree.setCheckedKeys(data.role.menuIdList)
              }
            })
          }
        })
      },
      // form submit
      dataFormSubmit () {
        this.$refs['dataForm'].validate((valid) => {
          if (valid) {
            const allowedMenuIds = new Set(this.collectMenuIds(this.menuList))
            const checked = [].concat(
              this.$refs.menuListTree.getCheckedKeys(),
              [this.tempKey],
              this.$refs.menuListTree.getHalfCheckedKeys()
            ).filter(id => allowedMenuIds.has(id))
            this.$http({
              url: this.$http.adornUrl(`/sys/role/${!this.dataForm.id ? 'save' : 'update'}`),
              method: 'post',
              data: this.$http.adornData({
                'roleId': this.dataForm.id || undefined,
                'roleName': this.dataForm.roleName,
                'remark': this.dataForm.remark,
                'menuIdList': checked
              })
            }).then(({data}) => {
              if (data && data.code === 0) {
                this.$message({
                  message: 'Operation successful',
                  type: 'success',
                  duration: 1500,
                  onClose: () => {
                    this.visible = false
                    this.$emit('refreshDataList')
                  }
                })
              } else {
                this.$message.error(data.msg)
              }
            })
          }
        })
      }
    }
  }
</script>
