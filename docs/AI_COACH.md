# AI 健身教练模块

会员在系统里与 AI 对话，AI 根据他的**画像 + 近期训练记录 + 历史对话**生成个性化训练计划。

## 一、这个模块解决什么问题

不是「调个 API 生成一段话」，而是：

- **有记忆**：记忆来自 MySQL 真实存储，不是硬塞进上下文
- **结构化**：模型输出 JSON，能存库、能渲染成表格
- **有兜底**：模型不保证遵守约束，代码层再校验一遍

## 二、三层记忆

| 层 | 表 | 作用 | 数据来源 |
|---|---|---|---|
| ① 用户画像 | `ai_user_profile` | 目标、水平、频次、时长、器械、伤病 | 会员填表单（模型无权修改） |
| ② 训练记录 | `ai_training_log` | 判断「最近练过什么」，避免同一部位连续练 | 会员在计划卡片上点「完成今天训练」 |
| ③ 对话历史 | `ai_chat_message` | 多轮上下文，支持「把第二天换成练腿」 | 每次对话自动落库 |

**为什么不用向量库**：用户量小、对话短，按 `member_id + 时间` 查最近 N 条就够。
如果对话量变大，才需要考虑摘要或向量检索。

## 三、三道防线

| 防线 | 位置 | 拦什么 |
|---|---|---|
| ① Prompt 约束 | `PromptBuilder.SYSTEM_PROMPT` | 尝试让模型主动遵守规则 |
| ② JSON 清洗 | `AiPlanParser`（纯函数，两条链路共用） | 挡住代码块包裹、前后解释文字、字段缺失；同步链路解析失败会重试一次 |
| ③ 规则引擎 | `ExerciseRuleEngine` | 即使模型没遵守，代码层强制校验并**真实替换**冲突动作 |

### 关于第三道防线（重要）

很多同类实现里，规则引擎的 `fix()` 是个空壳（校验出来却不改），
结果「膝盖有伤不排深蹲」这条验收项会静默失败。

本模块的 `fix()` 是**真实实现**：命中伤病禁忌的动作会被替换成安全替代动作；
实在没有可用替代的，就从当天移除，并在返回的 `warnings` 里明确告诉用户
「第 X 天的『深蹲』与膝关节伤病冲突，已替换为『臀桥』」。

会员**看得见 AI 改了什么**，而不是被悄悄改掉。

### 3.1 两条链路共用一套实现

同步 `/api/ai/chat` 与流式 `/api/ai/chat/stream` 不各写一套，而是共用三个组件：

| 组件 | 职责 |
|---|---|
| `AiPlanPipeline` | 校验 → 修复 → 落库 → 课程推荐；只有 `persist()` 标 `@Transactional`，模型调用永远在事务外 |
| `AiPlanParser` | 纯函数解析与清洗，便于单测覆盖各种畸形输出 |
| `AiGenerationGuard` | 同一会员同一时刻只允许一个生成任务，凭证式释放 + 4 分钟超时接管 |

`AiChatStreamService` 只负责「调模型、推 token、收尾」，业务规则全部走 pipeline，
避免出现「流式忘了推荐课程」「两边修复用的器械不一致」这类分叉。

## 四、环境准备

### 4.1 数据库

```bash
# 1. 建表
mysql -u root -p gym_system < sql/ai_schema.sql
# 2. 演示数据（可选，但强烈建议——没数据的话「近期训练记录」永远是空的）
mysql -u root -p gym_system < sql/ai_demo_seed.sql
# 3. 菜单与权限（只分配给会员角色）
mysql -u root -p gym_system < sql/ai_menu.sql
```

> `ai_schema.sql` 会先 `DROP` 再 `CREATE` 这 4 张表，重复执行会清空已有 AI 数据。
> `ai_menu.sql` 不硬编码 `menu_id`：按 `code` 删旧记录再插入，靠自增拿 id，
> 可重复执行，也可以在任何 id 起点的库上执行。菜单只分配给会员角色（`role_id = 5`）。

### 4.2 本地模型（默认，免费）

```bash
# 安装 Ollama: https://ollama.com/download
ollama pull qwen2.5:7b      # 约 4.7GB，质量更好
# 或
ollama pull qwen2.5:3b      # 约 2GB，更快但 JSON 稳定性稍差
```

拉完确认服务在跑：`ollama list` 能看到模型即可。

### 4.3 切换到在线模型

不改代码，只改环境变量或 `application-dev.yml`：

```bash
# DeepSeek（推荐，本项目默认）
AI_PROVIDER=openai                                    # 协议名固定是 openai，不是 deepseek
OPENAI_BASE_URL=https://api.deepseek.com              # 注意：不要带 /anthropic 后缀
OPENAI_API_KEY=sk-xxx                                 # 建议放环境变量 DEEPSEEK_API_KEY
OPENAI_MODEL=deepseek-flash                           # 或 deepseek-v4-pro

# 豆包（火山方舟，OpenAI 兼容）
AI_PROVIDER=openai
OPENAI_BASE_URL=https://ark.cn-beijing.volces.com/api/v3
OPENAI_API_KEY=你的方舟key
OPENAI_MODEL=doubao-seed-2-1-lite-xxxx
```

**两个容易踩的坑**（实测踩过）：

1. `spring.ai.model.chat` 填的是**协议名**不是服务商名。
   DeepSeek 和豆包都是 OpenAI 协议，所以填 `openai`；填 `deepseek` 会导致聊天模型不被装配。
2. DeepSeek 有两个 base-url，`/anthropic` 是 Anthropic 格式，
   Spring AI 走 OpenAI 协议，**必须用 `https://api.deepseek.com`**。

另外 `spring-ai-starter-model-openai` 是"全家桶"，会同时装配语音/图像/Embedding 等模型。
这些子模块不检查 `model.chat` 开关，只要 api-key 为空就会启动失败。
本项目只用聊天模型，所以配置里把其余模块显式设为 `none`。

### 4.4 关于 DeepSeek 思考模式

DeepSeek 默认开启 thinking，会先输出 `reasoning_content` 再给 `content`。
两者是**独立字段**，`content` 里仍是干净 JSON，不影响解析，只是延迟和 token 略高。

关闭需要 `extraBody` 传 `{"thinking":{"type":"disabled"}}`，
但这是 Spring AI 2.x 的特性，1.0.9 不支持，故本项目暂不关闭。

## 五、接口一览

所有接口的 `memberId` **一律取自登录态**，不接受前端传入——
防止会员改成别人的 id 读别人画像/对话（水平越权）。

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/ai/profile` | 查询我的画像 |
| POST | `/api/ai/profile` | 保存/更新我的画像 |
| GET | `/api/ai/profile/options` | 表单选项字典 |
| POST | `/api/ai/chat` | 同步生成计划 |
| POST | `/api/ai/chat/stream` | **SSE 流式**生成计划 |
| GET | `/api/ai/plans` | 我的历史计划（分页） |
| GET | `/api/ai/plans/{planId}` | 计划详情 |
| GET | `/api/ai/messages?sessionId=` | 会话消息 |
| POST | `/api/ai/training-log` | 训练打卡 |
| GET | `/api/ai/training-log` | 我的训练记录 |
| DELETE | `/api/ai/training-log/{logId}` | 删除训练记录 |

### SSE 事件格式

```
event:ping     data:{}                                    # 每 15 秒心跳，防止网关空闲断连
event:token    data:{"text":"{\"title\":\"..."}            # 增量文本（模型原始 JSON 片段）
event:result   data:{sessionId, planId, plan, warnings, costMs, recommendCourses}
event:done     data:{}                                    # 正常收尾
event:error    data:{"message":"..."}
```

前端收到 `result` 后用表格替换掉打字机文本——因为模型吐的是 JSON，
直接展示给用户没有意义。

前端以收到 `result` 或 `done` 作为「后端正常收尾」：两者都没收到就断了，
说明是网络中断，会提示「连接中断，内容可能不完整」，而不是显示「就绪」。
`ping` 事件直接忽略。

## 六、课程推荐（重要边界）

生成计划后会推荐相关课程，但**只推荐、不代报名**。

AI 不会、也不能替会员报名或扣费。报名必须由会员本人点击，
走 `MemberCourseService.joinCourse()` 那条带事务与余额校验的正式链路。
这是刻意设计：AI 不能越权操作账户资金相关动作。

## 七、验收清单

- [ ] 四个菜单只出现在**会员**账号下，管理员/员工/教练看不到
- [ ] 会员未填画像时，对话页提示「请先完善画像」而不是报错
- [ ] 选「膝关节」伤病的会员，生成的计划里查不到深蹲/跑跳/弓步
- [ ] 规则引擎替换动作时，页面能看到「已按你的伤病情况调整」的提示
- [ ] 点「完成今天训练」后，`ai_training_log` 有记录，下次生成的 Prompt 里带上它
- [ ] 员工账号调 `/api/ai/profile` 返回「仅对会员开放」
- [ ] 会员 A 无法通过改参数查到会员 B 的计划（越权测试）
- [ ] 流式生成中点「停止」：后端日志出现「用户取消生成，已终止模型流且不落库」，`ai_plan` 不新增记录
- [ ] 同一计划同一天重复打卡：第二次返回「这条训练今天已经打过卡了，不用重复记录」
- [ ] 模型 120 秒不返回：前端收到中文超时提示，而不是英文 `Timeout on blocking read`
- [ ] 员工 / 教练账号直接调 AI 接口：返回「当前账号没有 AI 教练权限」（HTTP 200 + `code:700`）

## 八、已知限制

1. **token 统计是估算值**：用字符数推算，没有为了精确统计而多调一次模型。
2. **流式链路的重试简化**：SSE 模式下解析失败直接提示重试，不做自动重试
   （流已经推给前端了，重试会造成内容错乱）。同步接口有自动重试。
3. **规则引擎的关键词表是静态的**：目前覆盖 7 类常见伤病。
   动作名称千变万化，关键词包含匹配能覆盖大部分情况，但不可能穷尽。
   遇到未覆盖的动作，靠 Prompt 约束 + 用户在 `injury_note` 里的补充说明兜底。
4. **历史会话切换**：对话页已有「新建对话」按钮（清空 `sessionId` 重新开一轮），
   但历史会话的列表 / 切换 UI 还没做，`/api/ai/messages` 目前只在需要时按会话查询。
5. **生成锁是单机内存锁**：`AiGenerationGuard` 基于进程内 `ConcurrentHashMap`，
   多实例部署时各实例互不可见；水平扩展前需要换成 Redis（`SETNX + TTL`）。
6. **流式阶段展示的是原始 JSON 片段**：打字机阶段推的是模型原文，收到 `result` 后才替换成表格，
   所以生成过程中会短暂看到 `{"title":...` 这样的内容，属于当前链路的产品取舍。

## 九、实测记录（2026-11-20）

用会员 1（张三，右膝半月板旧伤）实测三轮，模型为 `deepseek-flash`：

| 场景 | 结果 |
|---|---|
| 常规减脂请求 | 模型主动避开膝伤动作，32.9s 生成，`warnings` 为空 |
| **明确索要深蹲/弓步蹲/腿举** | 模型**拒绝套用模板**，全部替换为臀桥/髋外展/罗马尼亚硬拉，并在 notes 里写明"请勿自行加入" |
| **强制按模板输出 + 加跑步机快走** | 模型仍部分遵守模板时，**规则引擎捕获 2 处冲突并替换**，`warnings` 有记录 |

**修复的两个 bug**（本次实测暴露）：

1. **替代动作重复**：早期实现只取候选列表第一个，导致一天里三个腿部动作
   全变成「臀桥」。已改为按训练部位匹配 + 记录已用替代动作去重。
2. **替换后参数荒谬**：把「跑步机快走 25-30 分钟」替换成「直腿硬拉」时，
   原样保留了时长参数，变成「直腿硬拉做 25-30 分钟、休息 0 秒」。
   已增加 `normalizeAfterReplace()`，替换后按动作类型重置组数/次数/休息。

这两个问题都是**只有真正跑起来看输出**才能发现的，
单看代码觉得逻辑没问题——这也是为什么规则引擎必须做端到端验证。

## 十、复审与加固（2026-11-24 ~ 11-26）

端到端演示前做了一轮静态审查 + 单元测试，修复的问题：

| 问题 | 处理 |
|---|---|
| 流式链路缺 `recommendCourses`、未按会员器械过滤替代动作 | 抽出 `AiPlanPipeline`，两条链路共用 |
| `@Transactional` 包住模型调用，长事务占连接池 | 事务下沉到 `persist()`，模型调用在事务外 |
| 点「停止」后模型仍跑完并落库 | 取消位 + `blockLast` 中断，取消后不落库 |
| 模型永不返回导致该会员永久锁死 | `blockLast` / `get(timeout)` 超时 + guard 4 分钟接管 |
| 超长标题 `Data too long` 回滚整笔事务 | title / summary 按列宽截断（128 / 500） |
| 打卡去重因 DATE 列与带时分秒的 Date 比较而失效 | 统一归一到当天 0 点，按「当天区间」查重 |
| 403 被前端当成登录失效踢下线 | 401 / 403 分流，并识别项目统一的 `code:700` |
| A 超时被 B 接管后，A 的 finally 误删 B 的锁 | 凭证式 `release(memberId, token)` |
| 画像首次保存并发返回 500 | `DuplicateKeyException` 转更新 |

单元测试：`ExerciseRuleEngineTest` 9 个、`AiPlanParserTest` 14 个、`AiGenerationGuardTest` 5 个，共 28 个全部通过；
前端 `npm run build`（`vue-tsc -b && vite build`）通过。
