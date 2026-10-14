# 更新日志（CHANGELOG）

本项目所有重要变更均记录在此文件中，格式遵循 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.0.0/) 与 [语义化版本](https://semver.org/lang/zh-CN/)。

## [0.1.0] - 2026-09-26

### 新增（feat）

#### 前端
- 初始化 Vue3 + TypeScript + Vite 工程，集成 Element Plus、Pinia、Vue Router、Axios、Sass
- 完成主界面布局：左侧菜单（MenuBar/MenuItem/MenuLogo）、顶部 Header、面包屑、Tabs 选项卡、菜单收缩
- 完成路由配置（系统管理 / 会员管理 / 首页）与对应页面框架
- 封装 Axios 请求模块（baseURL、Token 携带、统一错误处理）
- 封装弹框组件 `SysDialog` 与 `useDialog` Hook
- 搭建 Pinia 状态管理（collapse 菜单收缩、tabs 标签页）

#### 后端
- 创建 Maven 多模块父工程（`gymnasium-common` + `gymnasium-service-web`）
- 集成 Spring Boot 2.7.18、MyBatis-Plus 3.4.1、Druid 连接池、Swagger2
- 统一返回结果封装：`ResultVo`、`ResultUtils`、`StatusCode`
- 配置 MyBatis-Plus 分页插件与 Mapper 扫描
- 配置开发环境数据源（`application-dev.yml`，数据库 `gym_system`）

### 优化（improve）
- 后端依赖统一版本管理（dependencyManagement）
- 前端路径别名 `@` 指向 `src`

## [0.2.0] - 2026-09-27

### 新增（feat）

#### 角色管理模块
- 后端完成角色管理 CRUD：`SysRole` / `RoleParm` 实体、`SysRoleMapper` + 映射文件、`SysRoleService` 业务层、`SysRoleController`
- 前端新增 `api/role`（接口请求封装）与 `composables/role`（业务逻辑）两层
- 前端新增角色新增页面 `RoleAdd.vue`，角色列表页 `RoleList.vue` 完成接口对接与弹框集成

#### 工程配置
- 正式整合 Swagger2 接口文档：升级为 springfox-swagger2 / swagger-ui 2.9.2，新增 `SwaggerConfig`，启动类开启 `@EnableSwagger2`
- 新增跨域配置 `WebMvcConfig`，解决前后端联调跨域问题

### 优化（improve）
- MySQL 驱动升级至 8.0.33，数据源 URL 补充时区与编码参数（serverTimezone / utf8）
- MyBatis-Plus 开启自动驼峰映射，Mapper 扫描适配多业务模块分包（`com.xq.*.*.mapper`）
- 配置 `spring.mvc.pathmatch.matching-strategy=ant_path_matcher`，解决 Spring Boot 2.7 与 Swagger 2.9.2 兼容问题
- 控制台日志输出改为彩色分级格式

## [0.3.0] - 2026-09-28

### 新增（feat）

#### 角色管理模块功能完善
- 前端完成角色列表联调：表格展示、搜索、重置、分页（页码/容量切换）、列表高度自适应
- 新增/编辑弹框支持角色类型选择（员工类型 / 会员类型），编辑模式数据回显
- 完成新增、编辑、删除接口对接：删除带确认提示，操作成功后自动刷新列表并提示

#### 前端公共能力建设
- Element Plus 启用中文国际化（zhCn）
- 新增工具层 `src/utils/`：`objCopy` 对象拷贝、`myconfirm` 信息确认提示框（全局挂载 `$objCoppy` / `$myconfirm`）
- 新增 `useInstance` Hook，统一获取组件实例与全局属性
- 新增枚举定义 `BaseEnum`（EditType / Title / UserType），`BaseType` 补充通用函数类型

### 优化（improve）
- 调整 Element Plus 注册与全局属性挂载顺序
- 移除 tsconfig 中不再需要的 `erasableSyntaxOnly` 配置

## [0.4.0] - 2026-10-01

### 新增（feat）

#### 用户管理模块（员工管理）
- 后端新增 `sys_user` 模块：`SysUser` 实体、Mapper + 映射文件、Service 业务层、`SysUserController`
- 完成员工接口开发：新增（MD5 密码加密 + 重名校验）、编辑、删除、分页列表（密码脱敏）、重置密码（默认 123456）、按用户查询角色
- 后端新增 `sys_user_role` 模块（用户-角色中间表）：实体、Mapper + 映射文件、Service
- 前端新增 `api/user` 与 `composables/user`（useUser / useTable / useSelectRole）
- 新增员工页面 `AddUser.vue`；`UserList.vue` 完成列表联调：搜索（电话/姓名）、表格展示、分页、编辑/删除/重置密码操作、权限指令 `v-permission`

#### 角色模块补充
- 新增角色下拉接口 `/api/role/getSelect` 与 `SelectType` 实体，用于新增/编辑员工时选择角色

## [0.5.0] - 2026-10-03

### 新增（feat）

#### 菜单管理模块
- 后端新增 `sys_menu` 模块：`SysMenu` 实体、树形构造工具 `MakeMenuTree`、`RouterVo`、Mapper + 映射文件、Service、`SysMenuController`
- 前端新增 `api/menu` 与 `composables/menu`（useMenu / useMenuTable）
- 新增菜单页面 `AddMenu.vue`、上级菜单选择组件 `ParentMenu.vue`，`MenuList.vue` 完成列表联调

#### 会员卡管理模块
- 后端新增 `member_card` 模块：`MemberCard` / `ListCard` 实体、Mapper + 映射文件、Service、`MemberCardController`
- 完成会员卡接口开发：新增、修改、删除、分页列表（按标题模糊查询）
- 前端新增 `api/member_card` 与 `composables/member_card`（useMember / useMemberTable）
- 新增会员卡页面 `AddCard.vue`，`CardType.vue` 完成列表联调

## [0.6.0] - 2026-10-04

### 新增（feat）

#### 会员管理模块
- 后端新增 `member` 模块：`Member` / `PageParam` / `JoinParam` / `RechargeParam` 实体、Mapper + 映射文件、Service、`MemberController`
- 完成会员接口开发：新增（会员卡号去重）、编辑、删除、分页列表（姓名/电话/卡号模糊查询）、按会员查询角色、启用中会员卡列表查询
- 前端新增 `api/member` 与 `composables/member`（useMember / useTable / useJoin / useRecharge）
- 新增会员页面 `AddMember.vue`，`MemberList.vue` 完成列表联调

#### 办卡与充值模块
- 后端新增 `member_apple`（办卡申请）、`member_recharge`（充值）、`member_role`（会员-角色中间表）模块：实体、Mapper + 映射文件、Service
- 完成办卡接口 `/api/member/joinApply`、充值接口 `/api/member/recharge`
- 前端新增办卡页面 `JoinApply.vue`、充值页面 `Recharge.vue`

### 工程优化（improve）
- 后端 Mapper 映射文件统一整理到 `resources/mapper/` 目录
- 修复用户分页参数 `nickNme` → `nickName` 拼写错误

## [0.7.0] - 2026-10-05

### 新增（feat）

#### 课程管理模块
- 后端新增 `course` 模块：`Course` / `CourseList` 实体、Mapper + 映射文件、Service、`CourseController`
- 完成课程接口开发：新增、编辑、删除、分页列表（按课程名 / 教练名模糊查询）
- 新增教练下拉接口 `/api/user/getTeacher`（筛选用户类型为教练的员工），用于添加课程时选择教练
- 前端新增 `api/course` 与 `composables/course`（useCourse / useTable / useEditor / useUpload / useSelectTeacher）
- 新增课程列表 `CourseList.vue` 与课程添加 `AddCourse.vue`（wangEditor 富文本编辑课程详情）
- 新增「我的课程」页面 `mycourse.vue`（页面框架）

#### 图片上传（MinIO）
- 后端整合 MinIO 对象存储（8.3.9）：`MinioConfig` / `MinioProp` / `MinioUtils`、上传接口 `/api/upload/uploadImage`
- `WebMvcConfig` 增加静态资源映射，支持图片访问
- 前端集成 wangEditor 富文本编辑器（课程详情编辑，支持图片上传）

### 工程优化（improve）
- 后端补充 minio / okhttp / easyexcel / fastjson 依赖
- 前端 vite 配置新增 `BASE_API` 全局变量，axios 请求地址统一管理

## [0.8.0] - 2026-10-06

### 新增（feat）

#### 器材管理模块
- 后端新增 `equipment` 模块：`Material` / `ListParam` 实体、Mapper + 映射文件、Service、`MaterialController`
- 完成器材接口开发：新增、编辑、删除、分页列表（按名称模糊查询）
- 前端新增 `api/material` 与 `composables/material`（useMaterial / useTable）
- 新增器材列表 `MaterialList.vue`、器材添加 `AddMaterial.vue`

#### 商品管理模块
- 后端新增 `goods` 模块：`Goods` / `GoodsParam` 实体、Mapper + 映射文件、Service、`GoodsController`
- 完成商品接口开发：新增、编辑、删除、分页列表（按名称模糊查询）
- 前端新增 `api/goods` 与 `composables/goods`（useGoods / useTable）
- 新增商品列表 `GoodsList.vue`、商品添加 `AddGoods.vue`

#### 失物招领模块
- 后端新增 `lost` 模块：`Lost` / `LostParam` 实体、Mapper + 映射文件、Service、`LostController`
- 完成失物招领接口开发：新增、编辑、删除、分页列表（按失物名称模糊查询）
- 前端新增 `api/lost` 与 `composables/lost`（useLost / useTable）
- 新增失物列表 `LostList.vue`、失物添加 `AddLost.vue`、失物招领人页面 `LostPerson.vue`

#### 建议反馈模块
- 后端新增 `suggest` 模块：`Suggest` / `SuggestParam` 实体、Mapper + 映射文件、Service、`SuggestController`
- 完成反馈接口开发：新增（自动记录反馈时间）、编辑、删除、分页列表（按标题模糊查询、按时间倒序）
- 前端新增 `api/suggest` 与 `composables/suggest`（useSuggest / useTable）
- 新增反馈列表 `SuggestList.vue`、反馈添加 `AddSuggest.vue`

### 工程优化（improve）
- 补充 wangEditor 类型声明（`types/wangeditor.d.ts`）
- 新增器材管理、商品管理、失物招领、建议反馈路由与侧边菜单

## [0.9.0] - 2026-10-07

### 新增（feat）

#### 登录认证模块
- 后端新增 `login` 模块：`LoginController`（验证码生成 `/api/login/image`、登录 `/api/login/login`，支持会员 / 员工双账号体系）、`LoginParam` / `LoginResult` 实体
- 整合 kaptcha 图形验证码（`ImageConfig` 配置，验证码存入 Session，Base64 返回前端）
- 新增 JWT 工具类 `JwtUtils`（java-jwt），登录成功后签发 Token；`application-dev.yml` 新增 JWT 配置（颁发者 / 密钥 / 过期时间）
- 登录校验：MD5 密码比对，按用户类型区分会员（member）与员工（sys_user）账号
- 会员新增接口补充密码 MD5 加密，为会员登录做准备
- 前端新增登录页 `Login.vue`、`api/login`、`composables/login`（useImage 验证码获取）、Pinia 用户状态 `store/user`（登录信息持久化）
- 新增 `/login` 路由与全局路由守卫（未登录自动跳转登录页）
- 集成 pinia-plugin-persistedstate 状态持久化；axios 请求 Token 改从用户状态读取、开启 withCredentials 保持 Session

### 工程优化（improve）
- 统一返回结果字段 `message` → `msg`，与前端请求封装对齐

## [0.10.0] - 2026-10-09

### 新增（feat）

#### 角色权限分配模块（权限树）
- 后端新增 `sys_role_menu`（角色-菜单中间表）模块：`RoleMenu` / `SaveMenuParam` 实体、Mapper + 映射文件、Service
- 新增权限分配接口：`/api/role/getMenuTree`（权限树回显：超级管理员查询全部菜单，普通用户查询本人可见菜单，并回显角色已分配菜单）、`/api/role/saveRoleMenu`（保存角色菜单分配）
- `sys_menu` 模块补充按用户 / 按角色查询菜单树接口，`MakeMenuTree` 组装权限树数据
- 前端新增 `composables/role/useAssign.ts` 与权限分配弹框 `AssignRole.vue`，角色列表 `RoleList.vue` 增加分配入口

### 工程优化（improve）
- 用户实体管理员标识字段统一为 `isAdmin`（修复原 `idAdmin` 拼写），用于权限树差异化查询

## [0.11.0] - 2026-10-10

### 新增（feat）

#### 动态菜单与用户信息
- 后端登录模块新增 `/api/login/getInfo`（查询用户信息与权限标识：会员按会员菜单、员工按员工菜单、管理员查全部菜单）与 `/api/login/getMenuList`（按用户查询动态菜单，组装路由树并过滤按钮类型）
- 新增 `InfoParam` / `UserInfo` 实体
- 前端新增 `store/menu`（菜单状态：拉取菜单并动态注册路由）与 `permission.ts`（全局路由守卫：白名单放行、登录后加载用户信息与动态菜单、加载失败清理并跳转登录页）
- 新增个人中心页面 `layout/center/center.vue`、退出登录组件 `LoginOut.vue`，头部增加用户头像与下拉入口
- 侧边菜单 `MenuBar.vue` 改为按权限动态渲染；路由改为「静态路由 + 动态菜单」模式
- 各业务页面权限指令 `v-permission` 联动权限标识

### 工程优化（improve）
- 路由守卫逻辑重构：动态菜单加载失败自动清理登录态

## [0.12.0] - 2026-10-11

### 新增（feat）

#### 选课报名与我的课程
- 后端新增 `member_course`（会员-课程中间表）模块：`MemberCourse` 实体、Mapper + 映射文件、Service
- 新增选课接口 `/api/course/joinCourse`（重复报名校验 + 余额判断 + 扣费）、`/api/course/getMyCourseList`（我的课程：会员查已报课程，教练查本人授课课程）
- 前端新增 `composables/mycourse`（useMyCourseTable），`mycourse.vue` 完成选课与我的课程页面

#### 我的充值
- 后端新增充值记录查询接口 `/api/member/getMyRecharge`（会员查自己的充值记录，员工查全部充值记录），`member_recharge` 模块补充按会员分页查询
- 会员列表接口按用户类型区分：会员仅能查看本人信息，员工查看全部，并按加入时间倒序
- 前端 `MyFee.vue` 完成我的充值页面，会员模块 API 补充

### 工程优化（improve）
- 修复角色 Mapper 映射文件命名（`mapper.SysRoleMapper.xml` → `SysRoleMapper.xml`）

## [0.13.0] - 2026-10-12

### 新增（feat）

#### 商品订单模块
- 后端新增 `goods_order` 模块：`GoodsOrder` / `OrderItem` / `OrderParam` 实体、Mapper + 映射文件、Service、`GoodsOrderController`
- 完成下单接口 `/api/order/down`（购物车批量下单：自动计算总价、记录操作人）与订单列表 `/api/order/list`（分页 + 按商品名模糊查询）
- 前端新增 `api/order` 与 `composables/order`（useOrderTable / useFaDrawer / useChildDrawer），订单列表 `OrderList.vue` 完成联调（含订单明细抽屉）
- 商品类型补充购物车参数（`num` / `AddCar`）

## [0.14.0] - 2026-10-13

### 新增（feat）

#### Spring Security 认证授权
- 整合 Spring Security：`SpringSecurityConfig`（登录 / 验证码接口放行、其余接口认证、Token 校验过滤器）、`CheckTokenFilter`（JWT Token 校验）、`CustomizedUserDetailService`（会员 / 员工双账号体系）、认证失败 / 权限不足统一处理器
- 登录接口重构：接入 `AuthenticationManager` 统一认证，密码加密升级为 BCrypt
- 新增重置密码 `/api/home/resetPassword`、修改密码 `/api/home/updatePassword`、退出登录 `/api/home/loginOut` 接口

#### 首页数据看板（ECharts）
- 后端新增 `home` 模块：`HomeController`（统计总数、最近反馈、热销商品、热销卡、热销课程）
- `goods_order` 模块补充热销商品 / 热销卡 / 热销课程统计查询
- 前端集成 ECharts，首页看板 `dashboard/Index.vue` 完成：总数统计卡片 + 热销图表 + 最近反馈

### 工程优化（improve）
- 配置类按功能分包整理（imageCode / minio / mybatisPlus / swagger）
- 新增数据库初始化脚本（`sql/gym_system_full_rebuild.sql`）

## [0.15.0] - 2026-10-14

### 新增（feat）

#### Spring Security 方法级权限控制
- 全部业务 Controller 增加 `@PreAuthorize` 方法级权限注解（`hasAuthority` 按钮级权限标识）
- 覆盖模块：会员（新增 / 编辑 / 删除 / 办卡 / 充值）、课程（新增 / 编辑 / 删除 / 报名）、员工、角色（编辑 / 删除 / 分配权限）、菜单、会员卡、器材、商品、订单、失物招领、建议反馈
- 与前端 `v-permission` 指令、动态菜单按钮权限标识联动，实现前后端一致的权限控制

#### 事务与并发安全增强（核心业务）
- 下单：`@Transactional` 事务包裹，库存改为 **SQL 原子扣减**（`UPDATE ... WHERE store >= ?`，影响行数为 0 即库存不足抛异常回滚），新增库存回补接口（订单取消 / 退货场景），从根上消除超卖
- 选课报名：业务逻辑下沉至 `MemberCourseService.joinCourse()`，`@Transactional` + 余额 **SQL 原子扣减**（`money >= ?`）+ 重复报名校验；`member_course` 增加联合唯一索引兜底防并发重复报名
- 充值：`@Transactional` + 余额 SQL 原子自增，操作人改为从 Spring Security 登录态获取（不再信任前端传入的 userId，防止伪造）

#### 前端路由重构与权限联动
- 删除 `router/index.ts` 中 227 行静态路由死代码，仅保留登录页 / 首页公共路由，业务路由全部由后端 `sys_menu` 动态下发、登录后 `addRoute` 注册
- 退出登录重构：本地清理登录态（token / 用户信息 / 持久化数据）+ 通知服务端清理 SecurityContext，接口失败也保证本地退出
- 各业务页面组件配合 `v-permission` 按钮级权限

#### 数据库脚本完善
- 清理源脚本测试脏数据，补齐一套可直接演示的业务数据（课程 / 商品 / 会员 / 订单等）
- 演示账号说明：员工端 `admin`（管理员）、`zs001`（员工）、`ls001`（教练），会员端 `2022001`（张三），密码均为 123456

### 修复（fix）
- 数据库脚本菜单 `url` 修正：7 条菜单的组件路径多了一层中间目录（如 `/system/user/UserList`），与 `src/views` 实际结构不符，导致前端 `import.meta.glob` 解析为 `undefined`、菜单点开空白；已对齐为 `/system/UserList` 等正确路径（14/14 菜单均可正常加载）
- `Lost` 实体字段 `found_addres`（原始 schema 拼写缺少末尾 s）显式映射列名，避免驼峰映射错位
- `application-dev.yml` 放行 `/api/home/loginOut`：token 过期后仍可正常退出登录

### 工程优化（improve）
- 清理前端 TypeScript 类型告警 30 处（未使用的解构变量 / 导入 / 校验回调参数），`npm run build`（`vue-tsc -b && vite build`）可完整通过并产出 `dist/`
- 数据库演示数据整理：清理源脚本中无意义的测试脏数据（如 `22`、`666`、`测试1`、`9999`），替换为完整可演示的健身房业务数据（课程 / 商品 / 会员 / 员工 / 失物 / 反馈），并统一演示密码为 `123456`

## [0.16.0] - 2026-10-14

### 文档（docs）
- 项目文档归档至 `docs/` 目录：`docs/README.md`（项目说明、技术栈、开发路线图、快速启动）、`docs/CHANGELOG.md`（版本迭代记录）；根目录 `README.md` 精简为指引页
- 完善 `docs/README.md`：
  - 技术栈补充 Spring Security 5.7 / MinIO 8.3.9 / ECharts 6.1 / wangEditor 5.1 / pinia-plugin-persistedstate，移除未实际使用的 EasyPoi
  - 修正 Swagger 访问路径为 `/swagger-ui.html`（springfox 2.9.2 的实际路径）
  - 新增演示账号表、MinIO 环境依赖说明，以及全量重建脚本会清空既有数据的警告
  - 修正文档内相对链接（`CHANGELOG.md` / `../LICENSE`），并同步阶段二 / 四 / 五中已过时的描述（密码加密演进、选课与下单的原子操作）
