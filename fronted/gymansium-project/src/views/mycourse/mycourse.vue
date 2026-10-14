<template>
    <el-main>
      <!-- 表格 -->
      <el-table :height="tableHeight" :data="tableData.list" border stripe>
        <el-table-column prop="image" width="90" label="课程封面" align="center">
          <template #default="scope">
            <el-image
              style="width: 60px; height: 60px; border-radius:50%" :src="scope.row.image"></el-image>
          </template>
        </el-table-column>
        <el-table-column prop="courseName" label="课程名称"></el-table-column>
        <el-table-column prop="courseHour" label="课时"></el-table-column>
        <el-table-column prop="coursePrice" label="价格"></el-table-column>
        <el-table-column prop="teacherName" label="授课教师"></el-table-column>
        <el-table-column label="操作" align="center" width="290">
          <template #default="scope">
            <el-button
              v-permission="['sys:mycourse:export']"
              type="primary"
              :icon="View"
              size="default"
              @click="viewStudentsBtn(scope.row)"
              >查看学生</el-button
            >
          </template>
        </el-table-column>
      </el-table>
      <!-- 分页 -->
      <el-pagination
        @size-change="sizeChange"
        @current-change="currentChange"
        :current-page.sync="listParam.currentPage"
        :page-sizes="[10, 20, 40, 80, 100]"
        :page-size="listParam.pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="listParam.total"
        background
      >
      </el-pagination>

      <!-- 报名学生名单抽屉 -->
      <el-drawer
        v-model="studentDrawer"
        title="报名学生"
        size="45%"
        :close-on-click-modal="false"
      >
        <template #title>
          <div>
            <span style="font-weight: 600">报名学生</span>
            <span v-if="studentCourseName" style="margin-left: 8px; color: #909399">
              —— {{ studentCourseName }}
            </span>
          </div>
        </template>
        <el-table :data="studentList.list" border stripe>
          <el-table-column prop="memberUsername" label="会员卡号"></el-table-column>
          <el-table-column prop="memberName" label="姓名"></el-table-column>
          <el-table-column prop="memberPhone" label="电话"></el-table-column>
        </el-table>
        <div
          v-if="studentList.list.length == 0"
          style="text-align: center; color: #909399; padding: 30px 0"
        >
          暂无学生报名
        </div>
      </el-drawer>
    </el-main>
  </template>

<script setup lang="ts">
  import { reactive, ref } from "vue";
  import { View } from "@element-plus/icons-vue";
  import useMyCourseTable from
"../../composables/mycourse/useMyCourseTable";
  import { getCourseStudentsApi } from "@/api/course";
  import type { CourseType } from "@/api/course/CourseModel";
  //列表
  const {
    listParam,
    tableData,
    sizeChange,
    currentChange,
    tableHeight,
  } = useMyCourseTable();

  //报名学生名单抽屉
  const studentDrawer = ref(false)
  const studentCourseName = ref("")
  const studentList = reactive({
    list: [] as Array<Record<string, any>>
  })

  //查看报名该课程的学生
  const viewStudentsBtn = async (row: CourseType) => {
    studentCourseName.value = row.courseName
    studentList.list = []
    let res = await getCourseStudentsApi(row.courseId)
    if (res && res.code == 200) {
      studentList.list = res.data
      studentDrawer.value = true
    }
  }
  </script>
<style scoped></style>
