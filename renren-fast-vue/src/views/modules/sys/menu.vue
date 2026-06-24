<template>
  <div class="mod-menu">
    <el-form :inline="true" :model="dataForm">
      <el-form-item>
        <el-button v-if="isAuth('sys:menu:save')" type="primary" @click="addOrUpdateHandle()">Add</el-button>
      </el-form-item>
    </el-form>

    <el-table
      :data="dataList"
      row-key="menuId"
      border
      style="width: 100%; ">
      <el-table-column
        prop="name"
        header-align="center"
        min-width="150"
        label="Name">
        <template slot-scope="scope">
          {{ translateMenuName(scope.row.name) }}
        </template>
      </el-table-column>
      <el-table-column
        prop="parentName"
        header-align="center"
        align="center"
        width="120"
        label="Parent Menu">
        <template slot-scope="scope">
          {{ scope.row.parentName ? translateMenuName(scope.row.parentName) : '' }}
        </template>
      </el-table-column>
      <el-table-column
        header-align="center"
        align="center"
        label="Icon">
        <template slot-scope="scope">
          <icon-svg :name="scope.row.icon || ''"></icon-svg>
        </template>
      </el-table-column>
      <el-table-column
        prop="type"
        header-align="center"
        align="center"
        label="Type">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.type === 0" size="small">Directory</el-tag>
          <el-tag v-else-if="scope.row.type === 1" size="small" type="success">Menu</el-tag>
          <el-tag v-else-if="scope.row.type === 2" size="small" type="info">Button</el-tag>
        </template>
      </el-table-column>
      <el-table-column
        prop="orderNum"
        header-align="center"
        align="center"
        label="Sort Order">
      </el-table-column>
      <el-table-column
        prop="url"
        header-align="center"
        align="center"
        width="150"
        :show-overflow-tooltip="true"
        label="Menu URL">
      </el-table-column>
      <el-table-column
        prop="perms"
        header-align="center"
        align="center"
        width="150"
        :show-overflow-tooltip="true"
        label="Permission Key">
      </el-table-column>
      <el-table-column
        fixed="right"
        header-align="center"
        align="center"
        width="150"
        label="Actions">
        <template slot-scope="scope">
          <el-button v-if="isAuth('sys:menu:update')" type="text" size="small" @click="addOrUpdateHandle(scope.row.menuId)">Edit</el-button>
          <el-button v-if="isAuth('sys:menu:delete')" type="text" size="small" @click="deleteHandle(scope.row.menuId)">Delete</el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 弹窗, Add / Edit -->
    <add-or-update v-if="addOrUpdateVisible" ref="addOrUpdate" @refreshDataList="getDataList"></add-or-update>
  </div>
</template>

<script>
  import AddOrUpdate from './menu-add-or-update'
  import { treeDataTranslate } from '@/utils'
  import { translateMenuName } from '@/utils/menuTranslation'
  export default {
    data () {
      return {
        dataForm: {},
        dataList: [],
        dataListLoading: false,
        addOrUpdateVisible: false
      }
    },
    components: {
      AddOrUpdate
    },
    activated () {
      this.getDataList()
    },
    methods: {
      translateMenuName,
      getDataList () {
        this.dataListLoading = true
        this.$http({
          url: this.$http.adornUrl('/sys/menu/list'),
          method: 'get',
          params: this.$http.adornParams()
        }).then(({data}) => {
          this.dataList = treeDataTranslate(data, 'menuId')
          this.dataListLoading = false
        })
      },
      // Add / Edit
      addOrUpdateHandle (id) {
        this.addOrUpdateVisible = true
        this.$nextTick(() => {
          this.$refs.addOrUpdate.init(id)
        })
      },
      // Delete
      deleteHandle (id) {
        this.$confirm(`Are you sure you want to delete item [id=${id}]?`, 'Confirm', {
          confirmButtonText: 'Confirm',
          cancelButtonText: 'Cancel',
          type: 'warning'
        }).then(() => {
          this.$http({
            url: this.$http.adornUrl(`/sys/menu/delete/${id}`),
            method: 'post',
            data: this.$http.adornData()
          }).then(({data}) => {
            if (data && data.code === 0) {
              this.$message({
                message: 'Operation successful',
                type: 'success',
                duration: 1500,
                onClose: () => {
                  this.getDataList()
                }
              })
            } else {
              this.$message.error(data.msg)
            }
          })
        }).catch(() => {})
      }
    }
  }
</script>
