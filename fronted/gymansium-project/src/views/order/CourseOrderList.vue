<template>
  <el-main>
    <!-- 搜索栏 -->
    <el-form :model="listParam" :inline="true" size="default">
      <el-form-item>
        <el-input v-model="listParam.courseName" placeholder="请输入课程名称">
        </el-input>
      </el-form-item>
      <el-form-item>
        <el-button :icon="Search" @click="searchBtn">搜索</el-button>
        <el-button :icon="Close" type="danger" plain @click="resetBtn">重置</el-button>
        <el-button v-permission="['sys:courseOrder:index']" :icon="Plus" type="primary" @click="order">下单</el-button>
      </el-form-item>
    </el-form>
    <!-- 课程订单表格 -->
    <el-table :height="courseOrderTableHeight" :data="tableData.list" border stripe>
      <el-table-column prop="image" width="90" label="课程封面" align="center">
        <template #default="scope">
          <el-image
            style="width: 60px; height: 60px; border-radius: 50%"
            :src="scope.row.image"
          ></el-image>
        </template>
      </el-table-column>
      <el-table-column prop="courseName" label="课程名称"></el-table-column>
      <el-table-column prop="courseHour" label="课时"></el-table-column>
      <el-table-column prop="coursePrice" label="课程价格"></el-table-column>
      <el-table-column prop="memberUsername" label="会员卡号"></el-table-column>
      <el-table-column prop="memberName" label="报名会员"></el-table-column>
      <el-table-column prop="teacherName" label="授课教师"></el-table-column>
    </el-table>
    <!-- 分页 -->
    <el-pagination
      @size-change="courseOrderSizeChange"
      @current-change="courseOrderCurrentChange"
      :current-page.sync="listParam.currentPage"
      :page-sizes="[10, 20, 40, 80, 100]"
      :page-size="listParam.pageSize"
      layout="total, sizes, prev, pager, next, jumper"
      :total="listParam.total"
      background
    >
    </el-pagination>

    <!-- 课程下单 -->
    <el-drawer
      :modal="false"
      custom-class="faDrawer"
      :append-to-body="true"
      :modal-append-to-body="false"
      size="50%"
      title="选课车"
      :close-on-click-modal="false"
      v-model="faDrawer"
      direction="rtl"
    >
      <template #title>
        <div class="car-title">
          <el-icon size="18px"><ShoppingCartFull /></el-icon>
          <span>选课车</span>
        </div>
        <el-button size="small" @click="memberDrawerShow">选择会员</el-button>
        <el-button size="small" @click="courseDrawerShow">选择课程</el-button>
        <!-- 选择会员抽屉 -->
        <el-drawer
          custom-class="childDrawer"
          size="50%"
          v-model="memberDrawer"
          title="会员列表"
          :append-to-body="true"
          :before-close="memberDrawerClose"
        >
          <el-form
            style="margin-top: 10px"
            :model="memberParam"
            :inline="true"
            size="default"
          >
            <el-form-item>
              <el-input
                v-model="memberParam.name"
                placeholder="请输入会员姓名"
              ></el-input>
            </el-form-item>
            <el-form-item>
              <el-input
                v-model="memberParam.username"
                placeholder="请输入会员卡号"
              ></el-input>
            </el-form-item>
            <el-form-item>
              <el-button :icon="Search" @click="searchMemberBtn">搜索</el-button>
              <el-button
                type="danger"
                plain
                :icon="Close"
                @click="resetMemberBtn"
                >重置</el-button
              >
            </el-form-item>
          </el-form>
          <el-table :height="memberHeight" :data="memberTableList.list" border stripe>
            <el-table-column prop="username" label="会员卡号"></el-table-column>
            <el-table-column prop="name" label="会员姓名"></el-table-column>
            <el-table-column prop="phone" label="联系电话"></el-table-column>
            <el-table-column label="操作" align="center">
              <template #default="scope">
                <el-button
                  @click="selectMemberBtn(scope.row)"
                  type="primary"
                  :icon="Plus"
                  size="default"
                  >选择</el-button
                >
              </template>
            </el-table-column>
          </el-table>
          <el-pagination
            @size-change="memberSizeChange"
            @current-change="memberCurrentChange"
            :current-page.sync="memberParam.currentPage"
            :page-sizes="[10, 20, 40, 80, 100]"
            :page-size="memberParam.pageSize"
            layout="total, sizes, prev, pager, next, jumper"
            :total="memberParam.total"
            background
          >
          </el-pagination>
        </el-drawer>
        <!-- 选择课程抽屉 -->
        <el-drawer
          custom-class="childDrawer"
          size="50%"
          v-model="courseDrawer"
          title="课程列表"
          :append-to-body="true"
          :before-close="courseDrawerClose"
        >
          <el-form
            style="margin-top: 10px"
            :model="courseParam"
            :inline="true"
            size="default"
          >
            <el-form-item>
              <el-input
                v-model="courseParam.courseName"
                placeholder="请输入课程名称"
              ></el-input>
            </el-form-item>
            <el-form-item>
              <el-button :icon="Search" @click="searchCourseBtn">搜索</el-button>
              <el-button
                type="danger"
                plain
                :icon="Close"
                @click="resetCourseBtn"
                >重置</el-button
              >
            </el-form-item>
          </el-form>
          <el-table :height="courseHeight" :data="courseTableList.list" border stripe>
            <el-table-column
              prop="image"
              width="90"
              label="课程封面"
              align="center"
            >
              <template #default="scope">
                <el-image
                  style="width: 40px; height: 40px; border-radius: 50%"
                  :src="scope.row.image"
                ></el-image>
              </template>
            </el-table-column>
            <el-table-column prop="courseName" label="课程名称"></el-table-column>
            <el-table-column prop="courseHour" label="课时"></el-table-column>
            <el-table-column prop="coursePrice" label="课程价格"></el-table-column>
            <el-table-column label="操作" align="center">
              <template #default="scope">
                <el-button
                  @click="addCourseBtn(scope.row)"
                  type="primary"
                  :icon="Plus"
                  size="default"
                  >加入</el-button
                >
              </template>
            </el-table-column>
          </el-table>
          <el-pagination
            @size-change="courseSizeChange"
            @current-change="courseCurrentChange"
            :current-page.sync="courseParam.currentPage"
            :page-sizes="[10, 20, 40, 80, 100]"
            :page-size="courseParam.pageSize"
            layout="total, sizes, prev, pager, next, jumper"
            :total="courseParam.total"
            background
          >
          </el-pagination>
        </el-drawer>
      </template>
      <template #default>
        <div style="margin-bottom: 10px">
          <el-tag type="success" v-if="selectedMember">
            报名会员：{{ selectedMember.name }}（{{ selectedMember.username }}）
          </el-tag>
          <el-tag type="info" v-else>请选择报名会员</el-tag>
        </div>
        <el-table :data="selectCourses.list" border stripe>
          <el-table-column prop="courseName" label="课程名称"></el-table-column>
          <el-table-column prop="courseHour" label="课时"></el-table-column>
          <el-table-column prop="coursePrice" label="课程价格"></el-table-column>
          <el-table-column label="操作" align="center">
            <template #default="scope">
              <el-button
                @click="deleteCourseBtn(scope.row)"
                type="danger"
                :icon="Delete"
                size="default"
                >删除</el-button
              >
            </template>
          </el-table-column>
        </el-table>
      </template>
      <template #footer>
        <div
          style="
            display: flex;
            align-items: center;
            justify-content: space-between;
          "
        >
          <div>
            <span
              style="
                font-size: 17px;
                font-weight: 600;
                padding-left: 50px;
                color: #009688;
              "
              >合计:</span
            >{{ totalPrice }}
          </div>
          <div style="flex: auto">
            <el-button type="danger" plain @click="cancelClick">取消</el-button>
            <el-button type="primary" @click="commit">确定</el-button>
          </div>
        </div>
      </template>
    </el-drawer>
  </el-main>
</template>

<script setup lang="ts">
import { ref, watch } from "vue";
import { Plus, Delete, Search, Close } from "@element-plus/icons-vue";
import useCourseOrderTable from "@/composables/courseOrder/useCourseOrderTable";
import useCourseOrderDrawer from "@/composables/courseOrder/useCourseOrderDrawer";

const {
  listParam,
  searchBtn,
  resetBtn,
  tableData,
  getList,
  courseOrderSizeChange,
  courseOrderCurrentChange,
  courseOrderTableHeight,
} = useCourseOrderTable();

const {
  faDrawer,
  memberDrawer,
  courseDrawer,
  selectedMember,
  order,
  cancelClick,
  commit,
  memberDrawerShow,
  memberDrawerClose,
  selectMemberBtn,
  courseDrawerShow,
  courseDrawerClose,
  addCourseBtn,
  deleteCourseBtn,
  memberParam,
  memberTableList,
  searchMemberBtn,
  resetMemberBtn,
  memberSizeChange,
  memberCurrentChange,
  memberHeight,
  courseParam,
  courseTableList,
  searchCourseBtn,
  resetCourseBtn,
  courseSizeChange,
  courseCurrentChange,
  courseHeight,
  selectCourses,
} = useCourseOrderDrawer(getList);

//合计金额
const totalPrice = ref<string | number>(0);
const change = () => {
  if (selectCourses.list.length > 0) {
    let sumPrice = selectCourses.list
      .map((row) => row.coursePrice)
      .reduce((total, num) => total + num, 0)
      .toFixed(2);
    totalPrice.value = sumPrice;
  } else {
    totalPrice.value = 0;
  }
};
//监听选课车数据变化，重算合计
watch(
  () => [...selectCourses.list],
  () => {
    change();
  }
);
</script>

<style lang="scss">
.faDrawer {
  .el-drawer__header {
    font-size: 16px;
    padding: 12.5px;
    margin-bottom: 0px !important;
    background-color: #009688 !important;
    .el-drawer__close {
      color: #fff !important;
    }
  }
  .car-title {
    color: #fff;
    font-size: 16px;
    display: flex;
    align-items: center;
  }
  .el-drawer__footer {
    border-top: 1px solid #e8eaec !important;
    padding: 10px;
  }
}
.childDrawer {
  .el-drawer__header {
    color: #fff;
    font-size: 16px;
    padding: 12.5px;
    margin-bottom: 0px !important;
    background-color: #009688 !important;
    .el-drawer__close {
      color: #fff !important;
    }
  }
  .el-drawer__body {
    padding: 5px;
  }
}
</style>
