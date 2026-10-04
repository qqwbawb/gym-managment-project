<template>
    <SysDialog
      :title="dialog.title"
      :height="dialog.height"
      :width="dialog.width"
      :visible="dialog.visible"
      @close="onClose"
      @confirm="confirm"
    >
      <template v-slot:content>
        <el-tree
          ref="parentTree"
          :data="treeData.list"
          node-key="menuId"
          default-expand-all
          :props="defaultProps"
          :highlight-current="true"
          :expand-on-click-node="false"
          @node-click="handleNodeClick"
        >
          <template #default="{ node, data }">
            <div class="custom-tree-container">
              <!-- 长度为0说明没有下级 -->
              <span v-if="data.children.length == 0">
                <DocumentRemove
                  style="
                    width: 1.3em;
                    height: 1.3em;
                    margin-right: 5px;
                    color: #8c8c8c;
                  "
                ></DocumentRemove>
              </span>
              <!-- 点击展开和关闭 -->
              <span v-else @click.stop="openBtn(data)">
                <component
                  style="
                    width: 1.1em;
                    height: 1.1em;
                    margin-right: 5px;
                    color: #8c8c8c;
                  "
                  :is="data.open ? Plus : Minus"
                />
              </span>
              <span>{{ node.label }}</span>
            </div>
          </template>
        </el-tree>
      </template>
    </SysDialog>
  </template>

  <script setup lang="ts">
  import {DocumentRemove,Plus,Minus} from '@element-plus/icons-vue'
  import SysDialog from "@/components/SysDialog.vue";
  import useDialog from "@/hooks/useDialog";
  import { getParentApi } from "@/api/menu";
  import { reactive,ref } from "vue";
  import type { MenuType, SelectNode } from "@/api/menu/MenuModel";
  import { ElTree } from "element-plus";
  //树的ref属性
  const parentTree = ref<InstanceType<typeof ElTree>>();
  //树属性配置
  const defaultProps = {
    children: "children",
    label: "title",
  };
  //上级树数据
  const treeData = reactive({
    list: [],
  });
  //选中数据
  const selectNode = reactive<SelectNode>({
    parentId: "",
    parentName: "",
  });
  //弹框属性
  const { dialog, onClose, onShow } = useDialog();
  //显示弹框
  const showParent = async () => {
    //查询树数据
    await getParent();
    dialog.width = 300;
    dialog.height = 450;
    onShow();
  };
  defineExpose({
    showParent,
  });
  //查询上级菜单数据
  const getParent = async () => {
    let res = await getParentApi();
    if (res && res.code == 200) {
      treeData.list = res.data;
    }
  };
  //树点击事件
  const handleNodeClick = (node: MenuType) => {
    console.log(node);
    selectNode.parentId = node.menuId;
    selectNode.parentName = node.title;
  };
  //注册事件
  const emits = defineEmits(["selectParent"]);
  //弹框确定
  const confirm = () => {
    emits("selectParent", selectNode);
    onClose()
  };
  //加号和减号的点击事件
  const openBtn = (data: MenuType) => {
    console.log(data);
    //设置展开或者关闭
    data.open = !data.open;
    if (parentTree.value) {
      parentTree.value.store.nodesMap[data.menuId].expanded =!data.open;
    }
  };
  </script>

  <style lang="scss">
  .el-tree {
    // 将每一行的设置为相对定位 方便后面before after 使用绝对定位来固定位置
    .el-tree-node {
      position: relative;
      padding-left: 10px;
    }
    // 子集像右偏移 给数线留出距离
    .el-tree-node__children {
      padding-left: 20px;
    }
    //这是竖线
    .el-tree-node :last-child:before {
      height: 40px;
    }
    .el-tree > .el-tree-node:before {
      border-left: none;
    }
    .el-tree > .el-tree-node:after {
      border-top: none;
    }
    //这自定义的线 的公共部分
    .el-tree-node:before,
    .el-tree-node:after {
      content: "";
      left: -4px;
      position: absolute;
      right: auto;
      border-width: 1px;
    }
    .tree :first-child .el-tree-node:before {
      border-left: none;
    }
    // 竖线
    .el-tree-node:before {
      border-left: 1px dotted #d9d9d9;
      bottom: 0px;
      height: 100%;
      top: -25px;
      width: 1px;
    }
    //横线
    .el-tree-node:after {
      border-top: 1px dotted #d9d9d9;
      height: 20px;
      top: 14px;
      width: 24px;
    }
    .el-tree-node__expand-icon.is-leaf {
      width: 8px;
    }
    //去掉elementui自带的展开按钮  一个向下的按钮,打开时向右
    .el-tree-node__content > .el-tree-node__expand-icon {
      display: none;
    }
    //每一行的高度
    .el-tree-node__content {
      line-height: 30px;
      height: 30px;
      padding-left: 10px !important;
    }
  }
  //去掉最上级的before  after 即是去电最上层的连接线
  .el-tree > div {
    &::before {
      display: none;
    }
    &::after {
      display: none;
    }
  }
  </style>