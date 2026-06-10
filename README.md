# DiskTree GUI

一个用于读取 `src/Tree.py` 导出文本的 Kotlin 跨平台 GUI。它不会重新扫描磁盘，而是直接解析已有的树形文本，然后像浏览本地文件一样展开/折叠目录层级。

## 当前方案

- **技术栈**：Kotlin Multiplatform + Compose Multiplatform
- **平台覆盖**：
  - Android
  - Desktop（JVM，适用于 Windows / macOS；也顺带可跑 Linux）
- **CI/CD**：全程通过 GitHub Actions 构建，不要求本地先建环境

## 支持的输入格式

当前解析的是 `src/Tree.py` 生成的纯文本格式，例如：

```text
==================================================
 文件树扫描报告
 扫描时间: 2026-06-10 10:00:00
 扫描位置: D:\
 深度限制: 4 层
==================================================

磁盘/挂载点: D:\

D:\
├── Games
│   ├── Steam
│   └── Saves
└── Media
    └── Videos
```

## 已实现功能

- 粘贴 `Tree.py` 导出文本
- 解析 `├── / └── / │` 树结构
- 右侧以可展开树形式浏览
- 共享解析器与共享 UI，可同时服务 Android 和 Desktop

## 项目结构

```text
composeApp/
  src/commonMain/    # 共享解析器、状态、Compose UI
  src/androidMain/   # Android 入口
  src/desktopMain/   # Desktop 入口
.github/workflows/   # GitHub Actions
src/Tree.py          # 原始导出脚本
```

## GitHub Actions

仓库已添加两个构建任务：

- `ubuntu-latest`：构建 Android Debug 包
- `macos-latest`：构建 Desktop 当前系统发行包

说明：当前仓库**不依赖本地预生成的 Gradle Wrapper**，CI 通过 `gradle/actions/setup-gradle` 安装并调用指定 Gradle 版本来完成构建。

## 后续建议

如果你要把它真正做成“替换软件”，下一步最值得补的是：

1. **文件导入器**
   - Desktop: 系统文件选择器打开 `.txt`
   - Android: SAF 文档选择器
2. **搜索/过滤**
   - 按目录名快速定位节点
3. **大文件优化**
   - 针对几十万节点做懒解析/增量展开
4. **导出会话状态**
   - 记住上次展开位置