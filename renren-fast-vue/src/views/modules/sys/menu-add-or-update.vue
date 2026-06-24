<template>
  <el-dialog
    :title="!dataForm.id ? 'Add New' : 'Edit'"
    :close-on-click-modal="false"
    :visible.sync="visible">
    <el-form :model="dataForm" :rules="dataRule" ref="dataForm" @keyup.enter.native="dataFormSubmit()" label-width="120px">
      <el-form-item label="Type" prop="type">
        <el-radio-group v-model="dataForm.type">
          <el-radio v-for="(type, index) in dataForm.typeList" :label="index" :key="index">{{ type }}</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item :label="typeNameLabel" prop="name">
        <el-input v-model="dataForm.name" :placeholder="typeNameLabel"></el-input>
      </el-form-item>
      <el-form-item label="Parent Menu" prop="parentName">
        <el-popover
          ref="menuListPopover"
          placement="bottom-start"
          trigger="click"
          append-to-body
          popper-class="mod-menu__menu-tree-popover">
          <div class="mod-menu__menu-tree-wrap">
            <el-tree
              :data="menuList"
              :props="menuListTreeProps"
              node-key="menuId"
              ref="menuListTree"
              @current-change="menuListTreeCurrentChangeHandle"
              :default-expand-all="false"
              :highlight-current="true"
              :expand-on-click-node="false">
            </el-tree>
          </div>
        </el-popover>
        <el-input v-model="dataForm.parentName" v-popover:menuListPopover :readonly="true" placeholder="Select parent menu" class="menu-list__input"></el-input>
      </el-form-item>
      <el-form-item v-if="dataForm.type === 1" label="Menu Route" prop="url">
        <el-input v-model="dataForm.url" placeholder="Menu route"></el-input>
      </el-form-item>
      <el-form-item v-if="dataForm.type !== 0" label="Permission Key" prop="perms">
        <el-input v-model="dataForm.perms" placeholder="Comma-separated, e.g. user:list,user:create"></el-input>
      </el-form-item>
      <el-form-item v-if="dataForm.type !== 2" label="Sort Order" prop="orderNum">
        <el-input-number v-model="dataForm.orderNum" controls-position="right" :min="0" label="Sort order"></el-input-number>
      </el-form-item>
      <el-form-item v-if="dataForm.type !== 2" label="Menu Icon" prop="icon">
        <el-row>
          <el-col :span="22">
            <el-popover
              ref="iconListPopover"
              placement="bottom-start"
              trigger="click"
              popper-class="mod-menu__icon-popover">
              <div class="mod-menu__icon-inner">
                <div class="mod-menu__icon-list">
                  <el-button
                    v-for="(item, index) in iconList"
                    :key="index"
                    @click="iconActiveHandle(item)"
                    :class="{ 'is-active': item === dataForm.icon }">
                    <icon-svg :name="item"></icon-svg>
                  </el-button>
                </div>
              </div>
            </el-popover>
            <el-input v-model="dataForm.icon" v-popover:iconListPopover :readonly="true" placeholder="Icon name" class="icon-list__input"></el-input>
          </el-col>
          <el-col :span="2" class="icon-list__tips">
            <el-tooltip placement="top" effect="light">
              <div slot="content">SVG Sprite icons are recommended. See <a href="//github.com/daxiongYang/renren-fast-vue/blob/master/src/icons/index.js" target="_blank">icons/index.js</a>.</div>
              <i class="el-icon-warning"></i>
            </el-tooltip>
          </el-col>
        </el-row>
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
  import Icon from '@/icons'
  import { translateMenuName } from '@/utils/menuTranslation'
  export default {
    computed: {
      typeNameLabel () {
        const labels = ['Directory Name', 'Menu Name', 'Button Name']
        return labels[this.dataForm.type] || 'Name'
      }
    },
    data () {
      var validateUrl = (rule, value, callback) => {
        if (this.dataForm.type === 1 && !/\S/.test(value)) {
          callback(new Error('Menu URL is required'))
        } else {
          callback()
        }
      }
      return {
        visible: false,
        dataForm: {
          id: 0,
          type: 1,
          typeList: ['Directory', 'Menu', 'Button'],
          name: '',
          parentId: 0,
          parentName: '',
          url: '',
          perms: '',
          orderNum: 0,
          icon: '',
          iconList: []
        },
        dataRule: {
          name: [
            { required: true, message: 'Name is required', trigger: 'blur' }
          ],
          parentName: [
            { required: true, message: 'Parent menu is required', trigger: 'change' }
          ],
          url: [
            { validator: validateUrl, trigger: 'blur' }
          ]
        },
        menuList: [],
        menuListTreeProps: {
          label: 'name',
          children: 'children'
        }
      }
    },
    created () {
      this.iconList = Icon.getNameList()
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
      init (id) {
        this.dataForm.id = id || 0
        this.$http({
          url: this.$http.adornUrl('/sys/menu/select'),
          method: 'get',
          params: this.$http.adornParams()
        }).then(({data}) => {
          this.menuList = this.translateMenuTree(treeDataTranslate(data.menuList, 'menuId'))
        }).then(() => {
          this.visible = true
          this.$nextTick(() => {
            this.$refs['dataForm'].resetFields()
          })
        }).then(() => {
          if (!this.dataForm.id) {
            // Add
            this.menuListTreeSetCurrentNode()
          } else {
            // Edit
            this.$http({
              url: this.$http.adornUrl(`/sys/menu/info/${this.dataForm.id}`),
              method: 'get',
              params: this.$http.adornParams()
            }).then(({data}) => {
              this.dataForm.id = data.menu.menuId
              this.dataForm.type = data.menu.type
              this.dataForm.name = data.menu.name
              this.dataForm.parentId = data.menu.parentId
              this.dataForm.url = data.menu.url
              this.dataForm.perms = data.menu.perms
              this.dataForm.orderNum = data.menu.orderNum
              this.dataForm.icon = data.menu.icon
              this.menuListTreeSetCurrentNode()
            })
          }
        })
      },
      // 菜单树选中
      menuListTreeCurrentChangeHandle (data) {
        this.dataForm.parentId = data.menuId
        this.dataForm.parentName = translateMenuName(data.name)
        if (this.$refs.menuListPopover) {
          this.$refs.menuListPopover.doClose()
        }
      },
      // 菜单树设置当前选中节点
      menuListTreeSetCurrentNode () {
        this.$nextTick(() => {
          if (!this.$refs.menuListTree) return
          this.$refs.menuListTree.setCurrentKey(this.dataForm.parentId)
          const node = this.$refs.menuListTree.getCurrentNode() || {}
          const rawName = node.name || ''
          this.dataForm.parentName = rawName
            ? translateMenuName(rawName)
            : (this.dataForm.parentId === 0 ? translateMenuName('Top-level menu') : '')
        })
      },
      // 图标选中
      iconActiveHandle (iconName) {
        this.dataForm.icon = iconName
      },
      // form submit
      dataFormSubmit () {
        this.$refs['dataForm'].validate((valid) => {
          if (valid) {
            this.$http({
              url: this.$http.adornUrl(`/sys/menu/${!this.dataForm.id ? 'save' : 'update'}`),
              method: 'post',
              data: this.$http.adornData({
                'menuId': this.dataForm.id || undefined,
                'type': this.dataForm.type,
                'name': this.dataForm.name,
                'parentId': this.dataForm.parentId,
                'url': this.dataForm.url,
                'perms': this.dataForm.perms,
                'orderNum': this.dataForm.orderNum,
                'icon': this.dataForm.icon
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

<style lang="scss">
  .mod-menu {
    .menu-list__input,
    .icon-list__input {
       > .el-input__inner {
        cursor: pointer;
      }
    }
    &__icon-popover {
      width: 458px;
      overflow: hidden;
    }
    &__icon-inner {
      width: 478px;
      max-height: 258px;
      overflow-x: hidden;
      overflow-y: auto;
    }
    &__icon-list {
      width: 458px;
      padding: 0;
      margin: -8px 0 0 -8px;
      > .el-button {
        padding: 8px;
        margin: 8px 0 0 8px;
        > span {
          display: inline-block;
          vertical-align: middle;
          width: 18px;
          height: 18px;
          font-size: 18px;
        }
      }
    }
    .icon-list__tips {
      font-size: 18px;
      text-align: center;
      color: #e6a23c;
      cursor: pointer;
    }
  }

  .mod-menu__menu-tree-popover {
    padding: 8px 4px;
    max-width: 360px;
  }

  .mod-menu__menu-tree-wrap {
    max-height: 320px;
    overflow-x: hidden;
    overflow-y: auto;
    -webkit-overflow-scrolling: touch;
  }
</style>
