# 民法典智能问答助手

基于民法典的AI智能问答系统，利用Elasticsearch进行法律条文检索，结合AI大模型提供专业的法律咨询服务。

## 📋 项目简介

本项目是一个法律咨询智能助手，用户可以提出法律相关问题，系统会以《中华人民共和国民法典》为依据，通过AI进行智能分析和解答。系统采用前后端分离架构，后端使用Spring Boot + Elasticsearch实现法律条文的高效检索，前端使用Vue.js构建友好的交互界面。

## 🛠️ 技术栈

### 后端
- **Java 17**
- **Spring Boot 3.5.6**
- **Elasticsearch 7.3.2** - 法律条文全文检索
- **Lombok** - 简化Java代码
- **OkHttp** - HTTP客户端
- **Gson** - JSON处理

### 前端
- **Vue.js** - 前端框架
- 位于 `frontend/my-app` 目录

## 📁 项目结构

```
.
├── src/
│   └── main/
│       └── java/
│           └── com/jiing/demo/
│               ├── config/          # 配置类
│               ├── constants/       # 常量定义（民法典、案例等）
│               ├── controller/      # 控制器层
│               ├── model/          # 数据模型
│               ├── service/        # 业务逻辑层
│               └── thirdparty/     # 第三方工具
├── frontend/
│   └── my-app/                     # Vue前端项目
├── pom.xml                         # Maven配置文件
└── README.md
```

## 🚀 快速开始

### 环境要求

- JDK 17+
- Maven 3.6+
- Node.js 14+ & npm/yarn
- Elasticsearch 7.3.2

### 后端启动

1. 克隆项目
```bash
git clone <your-repository-url>
cd <project-directory>
```

2. 启动Elasticsearch服务
```bash
# 请确保Elasticsearch 7.3.2已安装并运行在默认端口9200
```

3. 配置application.properties
```properties
# 根据实际情况配置Elasticsearch连接信息
```

4. 进入src\test\java\com\jiing\demo\ElasticCivilCodeTest.java，运行下面两个方法生成ES数据
    - createCivilCodeIndex
    - indexCivilCodeData

4. 编译并运行后端
```bash
./mvnw clean install
./mvnw spring-boot:run
```

后端服务默认运行在 `http://localhost:8080`

### 前端启动

1. 进入前端目录
```bash
cd frontend/my-app
```

2. 安装依赖
```bash
npm install
# 或
yarn install
```

3. 启动开发服务器
```bash
npm run serve
# 或
yarn serve
```

前端服务默认运行在 `http://localhost:8081`（具体端口请查看控制台输出）

## 📚 主要功能

- ✅ **智能问答**：基于民法典的法律问题智能解答
- ✅ **全文检索**：利用Elasticsearch实现法律条文快速检索
- ✅ **AI集成**：接入AI大模型提供专业分析
- ✅ **案例支持**：提供相关法律案例参考

## 🔧 核心模块

### 1. 搜索服务 (AskSearchService)
提供基于Elasticsearch的法律条文检索功能

### 2. 控制器 (SearchController)
处理用户问答请求，协调AI模型和检索服务

### 3. 常量管理
- `CivilCodeConstants` - 民法典条文常量
- `LawCaseConstants` - 法律案例常量
- `HotelConstants` - 其他业务常量

## 📝 API接口

详细API文档请参考项目中的Apifox文档或Swagger配置。

主要接口：
- `POST /api/search` - 提交法律问题并获取AI解答

## 🤝 贡献指南

欢迎提交Issue和Pull Request来帮助改进项目！

## 📄 许可证

[请根据实际情况添加许可证信息]

## 👥 作者

- **jiing** - 初始开发

## 📮 联系方式

如有问题或建议，欢迎通过Issue反馈。

---

**注意**：本项目仅供学习交流使用，不构成正式法律意见。如遇实际法律问题，请咨询专业律师。
