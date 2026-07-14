# DecayLib 开发者指南 (AGENTS.md)

本文件为开发智能体 (Agent) 提供独立通用库 DecayLib 项目的架构、技术栈与开发规约介绍。

## 1. 技术栈介绍

* **项目定位**:
  DecayLib 是一个面向 Minecraft 1.7.10 的独立通用腐烂/变质核心库，设计用于解耦模组中的物品及容器腐烂逻辑，通过暴露事件与接口允许其他模组进行非侵入式扩展与自定义。

* **目标游戏版本**: Minecraft 1.7.10
* **开发框架**: Minecraft Forge (1.7.10)
* **核心语言**: Java
* **编译与构建工具**: Gradle (配备 Jabel 编译器插件)
  * 注: 本地构建采用的高版本 JDK (如 JDK 25) 搭配 Jabel 进行编译，编译输出目标保持 Java 8 兼容性，构建指令为 `.\gradlew.bat jar`。
* **依赖支持**:
  * **NotEnoughItems (NEI)** / **Forge**: 核心容器及配方系统支持。
  * **Mixin**: 使用 Mixin 框架向原版及前置模组的 Item 与 TileEntity 中注入每秒腐烂计时更新逻辑。

## 2. 项目结构介绍

项目的核心源代码位于 `src/main/java`，资源文件位于 `src/main/resources`。

### 2.1 源代码结构 (`src/main/java`)

所有核心逻辑均位于包 `com.pppopipupu.decaylib` 下：

* **`com.pppopipupu.decaylib`**:
  * 模组入口与核心管理类。
  * `DecayLib.java`: 模组主类，负责配置文件加载与 Mixin 辅助初始化。
  * `DecayRegistry.java`: 腐烂规则注册表中心，管理物品默认和活动腐烂设置。
  * `DecayManager.java`: 腐烂逻辑核心执行器，管理状态初始化、倒计时扣除和变质处理。
  * `DecayRule.java` / `DecayAction.java`: 腐烂规则与动作的数据配置实体类。
  * `IDecayTooltipProvider.java`: 用于物品自定义变质产物后提示词的通用接口。
* **`com.pppopipupu.decaylib.recipe`**:
  * 包含物品腐烂倒计时均值化合并配方 `DecayMergeRecipe.java` 的实现。
* **`com.pppopipupu.decaylib.event`**:
  * 包含 `DecayEvent.java`（变质发生）和 `DecayTickEvent.java`（每秒倒计时更新）事件类。
* **`com.pppopipupu.decaylib.mixin`**:
  * `MixinItemDecay.java`: 拦截物品更新 `onUpdate` 并渲染 Tooltip。
  * `MixinTileEntityDecay.java`: 拦截方块容器更新 `updateEntity`。

### 2.2 资源文件结构 (`src/main/resources`)

* **`assets/decaylib/lang`**:
  * 语言本地化文件，包含 `zh_CN.lang` 和 `en_US.lang`（用于通用腐烂提示词的本地化）。

## 3. 编码规约

* **注释规范**: 不要在函数或逻辑内部编写任何的内联注释。
* **Mixin规范**: 绝对不要使用任何的 `@Overwrite`，应当优先使用 `@Inject` 配合 Duck 接口或 `@Redirect` 等安全的注入机制。
* **导入规范**: 绝对不要使用任何包名全称，能import包必须使用import。绝对不可以偷懒直接写全称，必须用编辑工具正常在顶部插入import。
* **编译与运行测试**: 每次修改完代码后，必须使用配置好 Java 25 环境的终端运行 `.\gradlew.bat jar`（Linux 下使用 `./gradlew jar`）进行编译校验，确保没有语法和编译期报错。
* **日志规范**: 绝对禁止使用 `System.out` 或 `System.err` 输出调试或运行日志。在进行日志记录时，必须使用 Log4j 2 Logger，且必须只使用 `com.pppopipupu.decaylib.DecayLib.LOG`。
* **操作与读写限制**: 作为独立项目，你必须仅在 DecayLib 项目目录（以及用户明确指定的联调附属模组目录）下进行代码及资源的读取与修改，严禁擅自访问或修改其他本地无关物理路径下的文件。
* **禁止制造临时字节码桩**: 严禁通过生成临时物理 `.class` 字节码或反编译 `.java` 文件（例如解压放入 `/com/` 或 `/libs/com/`）来蒙混通过编译。如果因调试需要临时产生了此类非项目本身的物理文件，必须在 turn 结束前彻底清除。
