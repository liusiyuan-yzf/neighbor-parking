# Neighbor Parking Server

免费车位共享 MVP 的 Spring Boot REST 服务。

## 运行

```powershell
$env:JAVA_HOME='D:\java\Java\jdk1.8.0_101'
.\mvnw.cmd spring-boot:run
```

默认使用 H2 内存数据库和演示身份。Swagger UI：`http://localhost:8080/swagger-ui.html`。

## 测试

```powershell
$env:JAVA_HOME='D:\java\Java\jdk1.8.0_101'
.\mvnw.cmd test
```

集成测试覆盖开发登录、JWT 身份、管理员权限隔离、预约创建与时间冲突拦截。

MySQL 配置见 `application-mysql.yml`，生产运行必须显式提供数据库和 JWT 环境变量，并禁用 `demo` profile 与开发登录。数据库结构由 Flyway 迁移维护。
