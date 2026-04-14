# 后端契约规范（BACKEND-CONTRACT）

> **适用范围**：`apple-chain-platform` 所有 `apple-module-*` 模块的 Controller、DTO、错误码、枚举
> **强制级别**：P0（CR + CI 守护）
>
> ⚠️ **权威源不是代码**：契约权威源是 [`contract/modules/*.md`](../../contract/modules/) 中的 Markdown 设计文档。本文件告诉你**如何按文档实现代码**以及必须遵守的编码规范。

## 目录

0. [实现前必读](#0-实现前必读)
1. [DTO 命名铁律](#1-dto-命名铁律)
2. [Controller 注解规范](#2-controller-注解规范)
3. [DTO 字段 @Schema 注解规范](#3-dto-字段-schema-注解规范)
4. [错误码规范](#4-错误码规范)
5. [枚举序列化规范](#5-枚举序列化规范)
6. [分页与时间约定](#6-分页与时间约定)
7. [对照验证工具：Knife4j /doc.html](#7-对照验证工具knife4j-dochtml)
8. [违规检查清单](#8-违规检查清单)

---

## 0. 实现前必读

**写代码前**，打开对应的契约文档：

```
contract/modules/<your-module>.md
```

- 文档中的 URL、Method、请求体字段、响应体字段、错误码是**已经拍板的设计**
- 你的任务是**忠实翻译成 Java 代码**，不是发挥创造
- 如果你发现设计有缺陷或实现不了：
  1. ❌ 不允许擅自改字段名、加字段、删字段
  2. ✅ 正确做法：找架构师/产品讨论 → 提 `contract/` 的修订 PR → 合并后再实现
- `@Schema` 注解的 `description` / `example` **建议直接从契约文档复制过来**，保证描述一致

---

---

## 1. DTO 命名铁律

| 用途 | 命名后缀 | 示例 | 禁止 |
|------|---------|------|------|
| HTTP 请求体 | `XxxRequest` | `LoginRequest` | `LoginDTO` / `LoginParam` / `LoginForm` |
| HTTP 响应体 | `XxxResponse` | `CurrentUserResponse` | `CurrentUserVO` / `CurrentUserDTO` |
| 数据库实体 | `XxxDO` | `UserDO` | `UserEntity` / `UserPO` |
| 分页响应 | `PageResult<XxxResponse>` | `PageResult<OrchardResponse>` | 自定义分页壳 |
| 内部传递对象 | `XxxBO`（业务对象） | `OrchardBO` | — |

**统一原则**：
- 对外暴露（Controller 入参出参）：**只能是** `Request` 或 `Response`
- 对内传递（Service / Mapper）：`BO` 或 `DO`
- 禁止一个类同时出现在 Controller 和 Mapper 层

**迁移策略**：
- 存量 `XxxDTO` / `XxxVO` 在**下次修改该文件时**一并重命名，不强制全量迁移
- 新增文件必须遵守新命名

---

## 2. Controller 注解规范

每个 Controller 类与方法都必须满足以下最低注解要求：

### 类级（必填）

```java
@RestController
@RequestMapping("/api/user")
@Tag(name = "用户管理", description = "用户注册、登录、权限等操作")
public class UserController {
    // ...
}
```

- `@Tag.name`：中文短语，出现在 Knife4j 文档侧栏
- `@Tag.description`：一句话说明该 Controller 的业务边界

### 方法级（必填）

```java
@PostMapping("/login")
@Operation(
    summary = "用户登录",
    description = "使用用户名密码登录，返回 JWT Token 及基础用户信息"
)
@ApiResponse(responseCode = "200", description = "登录成功")
@ApiResponse(responseCode = "401", description = "用户名或密码错误")
public R<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
    // ...
}
```

- `@Operation.summary`：一句中文概括
- `@Operation.description`：可选，用于说明副作用、前置条件
- `@ApiResponse`：至少覆盖成功和一个典型失败场景

### 参数级（条件必填）

当路径参数或查询参数的含义不能从参数名直接看出时：

```java
public R<OrchardResponse> getOrchard(
    @Parameter(description = "果园唯一编号") @PathVariable Long id
) { ... }
```

---

## 3. DTO 字段 @Schema 注解规范

所有出现在 Controller 入参或出参中的 DTO 字段**必须**有 `@Schema` 注解。

### 基础字段

```java
public class LoginRequest {
    @Schema(
        description = "用户名（手机号或邮箱）",
        example = "13800138000",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "用户名不能为空")
    private String username;

    @Schema(
        description = "密码（至少 8 位，包含数字和字母）",
        example = "Abc12345",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "密码不能为空")
    private String password;
}
```

### 枚举字段

```java
public class UserResponse {
    @Schema(
        description = "用户状态",
        implementation = UserStatus.class
    )
    private UserStatus status;
}
```

### 嵌套对象字段

```java
public class OrderResponse {
    @Schema(description = "订单基础信息")
    private OrderBase base;

    @Schema(description = "订单项列表")
    private List<OrderItemResponse> items;
}
```

### 必填标记

- 入参字段：与 `@NotNull` / `@NotBlank` 配套，加 `requiredMode = REQUIRED`
- 出参字段：必返字段加 `requiredMode = REQUIRED`，允许 `null` 的字段不加

---

## 4. 错误码规范

### 4.1 接口层

所有错误码必须实现统一接口 `IErrorCode`：

```java
public interface IErrorCode {
    int getCode();
    String getMessage();
}
```

### 4.2 号段划分

| 模块 | 错误码段 | 示例 |
|------|---------|------|
| 通用 | `0 ~ 99999` | `ResultCode` 保留 |
| user | `100000 ~ 199999` | `100001 用户不存在` |
| planting | `200000 ~ 299999` | `200001 果园不存在` |
| trace | `300000 ~ 399999` | `300001 溯源记录不存在` |
| chain | `400000 ~ 499999` | |
| trade | `500000 ~ 599999` | |
| warehouse | `600000 ~ 699999` | |
| input | `700000 ~ 799999` | |
| coldchain | `800000 ~ 899999` | |
| iot | `900000 ~ 999999` | |
| finance | `1000000 ~ 1099999` | |
| bigdata | `1100000 ~ 1199999` | |

### 4.3 枚举实现

每个模块在 `apple-module-xxx/src/main/java/.../enums/XxxErrorCode.java` 定义：

```java
@Getter
@AllArgsConstructor
public enum UserErrorCode implements IErrorCode {
    USER_NOT_FOUND(100001, "用户不存在"),
    PASSWORD_INVALID(100002, "密码错误"),
    ACCOUNT_LOCKED(100003, "账号已锁定"),
    ;

    private final int code;
    private final String message;
}
```

### 4.4 异常抛出

```java
// ✅ 正确
if (user == null) {
    throw new BizException(UserErrorCode.USER_NOT_FOUND);
}

// ❌ 错误
throw new RuntimeException("用户不存在");
throw new BizException("用户不存在"); // 无错误码
```

---

## 5. 枚举序列化规范

所有业务枚举**必须**实现 `BaseEnum` 接口，统一序列化为 `{code, desc}` 对象：

```java
public interface BaseEnum {
    int getCode();
    String getDesc();
}
```

实现示例：

```java
@Getter
@AllArgsConstructor
public enum UserStatus implements BaseEnum {
    ACTIVE(1, "正常"),
    LOCKED(2, "锁定"),
    DELETED(3, "已删除"),
    ;

    private final int code;
    private final String desc;
}
```

**禁止**：
- 在响应中返回纯数字（`1`）而不是对象（`{code: 1, desc: "正常"}`）
- 在响应中返回枚举 `name()`（`"ACTIVE"`）作为契约字段——前端无法从中得知中文含义

---

## 6. 分页与时间约定

### 分页请求

```java
public R<PageResult<OrchardResponse>> listOrchards(PageParam pageParam) { ... }
```

- `PageParam.page` 从 1 开始
- `PageParam.size` 默认 10，最大 100
- `PageParam.keyword` 可选，统一模糊搜索字段

### 分页响应

```java
// PageResult<T> 统一结构
{
  "records": [...],
  "total": 123,
  "current": 1,
  "size": 10
}
```

### 时间字段

- Java 类型：**必须**使用 `java.time.LocalDateTime`（禁止 `java.util.Date`）
- 序列化格式：`yyyy-MM-dd HH:mm:ss`（全局 Jackson 配置）
- 时区：`Asia/Shanghai`

---

## 7. 对照验证工具：Knife4j /doc.html

> ⚠️ **Knife4j / OpenAPI JSON 不是契约权威源**——它是**验证工具**，帮你检查你的实现是否忠实于契约文档。

### 7.1 Maven profile `-Pcontract`

`apple-web/pom.xml` 已配置 `contract` profile，在 `integration-test` 阶段：
1. 启动 Spring Boot
2. `springdoc-openapi-maven-plugin` 请求 `/v3/api-docs`
3. 把 OpenAPI JSON 写到 `apple-chain-platform/contract/openapi.json`
4. 停掉 Spring Boot

```bash
mvn -Pcontract verify
# 产物：apple-chain-platform/contract/openapi.json
```

日常开发 `mvn compile` / `mvn test` 不会触发这个流程，不影响构建速度。

### 7.2 对照验证（手动）

启动应用后：

```bash
mvn -pl apple-web spring-boot:run
# 浏览器访问 http://localhost:8080/doc.html
```

- 在 Knife4j 左侧选到你改的接口
- 逐字段与 `contract/modules/<module>.md` 对照
- 字段名、类型、必填性、示例应完全一致
- 发现不一致：
  - 你实现写错了 → 改代码
  - 文档有笔误 → 提契约 PR 修订

### 7.3 对照验证（脚本 · Phase 3 交付物）

> 下面的脚本是 Phase 3 要落地的工具，目前尚未实现。

```bash
scripts/verify-contract.sh
# 做的事：
# 1. mvn -Pcontract verify → 生成 contract/openapi.json
# 2. 从 contract/modules/*.md 解析 interface 代码块 → JSON Schema
# 3. 比对两者字段
# 4. 差异 → 退出码非 0
```

脚本不一致时，**修代码或修文档**（看哪边是对的），不允许临时禁用脚本。

### 7.4 禁止事项

- ❌ **禁止**把 `contract/openapi.json` 当成契约权威源来修改
- ❌ **禁止**前端从 `openapi.json` 生成 TypeScript 类型（应从 Markdown 文档复制 `interface`）
- ❌ **禁止**手工编辑 `openapi.json`，它是生成产物

---

## 8. 违规检查清单

提交 PR 前自查：

- [ ] 新增的 Controller 方法都有 `@Operation`
- [ ] 新增的 DTO 字段都有 `@Schema(description, example)`
- [ ] 新增的业务异常都走 `throw new BizException(XxxErrorCode.YYY)`
- [ ] 新增的业务枚举都实现了 `BaseEnum`
- [ ] 时间字段是 `LocalDateTime`，不是 `Date`
- [ ] DTO 命名是 `Request` / `Response` / `BO` / `DO`，不是 `DTO` / `VO`
- [ ] 已本地 `mvn verify`，`contract/openapi.json` 已更新并 `git add`
- [ ] 如果改了字段名或类型，已通知前端同步

---

## 参考文件（当前规范基线）

- `apple-common/src/main/java/com/apple/chain/common/result/R.java` — 统一返回包装
- `apple-common/src/main/java/com/apple/chain/common/result/ResultCode.java` — 通用错误码
- `apple-common/src/main/java/com/apple/chain/common/exception/GlobalExceptionHandler.java` — 全局异常处理
- `apple-common/src/main/java/com/apple/chain/common/exception/BizException.java` — 业务异常
- `apple-common/src/main/java/com/apple/chain/common/dto/PageParam.java` — 分页入参
- `apple-common/src/main/java/com/apple/chain/common/result/PageResult.java` — 分页出参
- `apple-module-user/src/main/java/com/apple/chain/user/controller/UserController.java` — Controller 样板

## Phase 1 待新建文件

- `apple-common/src/main/java/com/apple/chain/common/result/IErrorCode.java`
- `apple-common/src/main/java/com/apple/chain/common/enums/BaseEnum.java`
- `apple-module-user/src/main/java/com/apple/chain/user/enums/UserErrorCode.java`
