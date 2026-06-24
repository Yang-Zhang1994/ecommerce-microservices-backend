<template>
  <div class="mod-config">
    <el-form :inline="true" :model="dataForm" @keyup.enter.native="getDataList()">
      <el-form-item>
        <el-input v-model="dataForm.key" placeholder="Parameter Name" clearable></el-input>
      </el-form-item>
      <el-form-item>
        <el-button @click="getDataList()">Query</el-button>
      </el-form-item>
    </el-form>
    <el-table
      class="admin-table-wide admin-table-ellipsis"
      :fit="false"
      :data="dataList"
      border
      v-loading="dataListLoading"
      @selection-change="selectionChangeHandle"
      style="width: 100%;"
    >
      <el-table-column type="selection" header-align="center" align="center" width="50"></el-table-column>
      <el-table-column prop="id" header-align="center" align="center" width="70" label="ID"></el-table-column>
      <el-table-column prop="levelId" header-align="center" align="center" min-width="80" label="Level"></el-table-column>
      <el-table-column prop="username" header-align="center" align="center" min-width="120" show-overflow-tooltip label="Username"></el-table-column>
      <el-table-column prop="nickname" header-align="center" align="center" min-width="100" show-overflow-tooltip label="Nickname"></el-table-column>
      <el-table-column prop="mobile" header-align="center" align="center" min-width="130" label="Phone">
        <template slot-scope="scope">
          <el-tooltip v-if="phoneCell(scope.row.mobile).hint" :content="phoneCell(scope.row.mobile).hint" placement="top">
            <span class="member-phone-placeholder">{{ phoneCell(scope.row.mobile).text }}</span>
          </el-tooltip>
          <span v-else>{{ phoneCell(scope.row.mobile).text }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="email" header-align="center" align="center" min-width="160" show-overflow-tooltip label="Email"></el-table-column>
      <el-table-column prop="header" header-align="center" align="center" width="72" label="Avatar" class-name="member-avatar-col">
        <template slot-scope="scope">
          <div v-if="scope.row.header" class="member-avatar-wrap">
            <el-image
              :src="scope.row.header"
              fit="cover"
              :preview-src-list="[scope.row.header]">
            </el-image>
          </div>
          <span v-else>—</span>
        </template>
      </el-table-column>
      <el-table-column prop="gender" header-align="center" align="center" min-width="90" label="Gender">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.gender != null && scope.row.gender !== ''" :type="genderTagType(scope.row.gender)" size="small">
            {{ formatGender(scope.row.gender) }}
          </el-tag>
          <span v-else>—</span>
        </template>
      </el-table-column>
      <el-table-column prop="birth" header-align="center" align="center" min-width="100" show-overflow-tooltip label="Birthday"></el-table-column>
      <el-table-column prop="city" header-align="center" align="center" min-width="90" show-overflow-tooltip label="City"></el-table-column>
      <el-table-column prop="job" header-align="center" align="center" min-width="100" show-overflow-tooltip label="Job"></el-table-column>
      <el-table-column prop="sign" header-align="center" align="center" min-width="120" show-overflow-tooltip label="Bio"></el-table-column>
      <el-table-column prop="sourceType" header-align="center" align="center" min-width="90" label="Source">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.sourceType != null && scope.row.sourceType !== ''" :type="memberSourceTagType(scope.row.sourceType)" size="small">
            {{ formatMemberSource(scope.row.sourceType) }}
          </el-tag>
          <span v-else>—</span>
        </template>
      </el-table-column>
      <el-table-column prop="integration" header-align="center" align="center" min-width="80" label="Points"></el-table-column>
      <el-table-column prop="createTime" header-align="center" align="center" min-width="110" show-overflow-tooltip label="Registered"></el-table-column>
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
    <add-or-update v-if="addOrUpdateVisible" ref="addOrUpdate" @refreshDataList="getDataList"></add-or-update>
  </div>
</template>

<script>
import AddOrUpdate from './member-add-or-update'
import {
  formatGender,
  genderTagType,
  formatMemberSource,
  memberSourceTagType,
  formatMemberPhone
} from './member-admin-meta'

export default {
  data () {
    return {
      dataForm: {
        key: ''
      },
      dataList: [],
      pageIndex: 1,
      pageSize: 10,
      totalPage: 0,
      dataListLoading: false,
      dataListSelections: [],
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
    formatGender,
    genderTagType,
    formatMemberSource,
    memberSourceTagType,
    formatMemberPhone,
    phoneCell (mobile) {
      return formatMemberPhone(mobile)
    },
    getDataList () {
      this.dataListLoading = true
      this.$http({
        url: this.$http.adornUrl('/member/member/list'),
        method: 'get',
        params: this.$http.adornParams({
          page: this.pageIndex,
          limit: this.pageSize,
          key: this.dataForm.key
        })
      }).then(({ data }) => {
        if (data && data.code === 0) {
          this.dataList = data.page.list
          this.totalPage = data.page.totalCount
        } else {
          this.dataList = []
          this.totalPage = 0
        }
        this.dataListLoading = false
      })
    },
    sizeChangeHandle (val) {
      this.pageSize = val
      this.pageIndex = 1
      this.getDataList()
    },
    currentChangeHandle (val) {
      this.pageIndex = val
      this.getDataList()
    },
    selectionChangeHandle (val) {
      this.dataListSelections = val
    },
    addOrUpdateHandle (id) {
      this.addOrUpdateVisible = true
      this.$nextTick(() => {
        this.$refs.addOrUpdate.init(id)
      })
    }
  }
}
</script>

<style scoped>
.member-phone-placeholder {
  color: #909399;
  cursor: help;
}

.member-avatar-wrap {
  width: 40px;
  height: 40px;
  margin: 0 auto;
  overflow: hidden;
  border-radius: 4px;
  line-height: 0;
}

.member-avatar-wrap >>> .el-image {
  width: 40px !important;
  height: 40px !important;
  display: block !important;
}

.member-avatar-wrap >>> .el-image__inner,
.member-avatar-wrap >>> img {
  width: 40px !important;
  height: 40px !important;
  max-width: 40px !important;
  max-height: 40px !important;
  object-fit: cover;
  display: block;
}
</style>

<style>
/* Table cell: keep avatar row height fixed (Element UI renders image outside scoped). */
.mod-config .member-avatar-col .cell {
  padding: 6px 4px !important;
  overflow: hidden;
  line-height: 0;
}
</style>
