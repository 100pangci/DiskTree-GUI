# DiskTree GUI

一个用于读取 `Tree.py` 导出文本的 Kotlin 跨平台 GUI。它不会重新扫描磁盘，而是直接解析已有的树形文本，然后像浏览本地文件一样展开/折叠目录层级。

`Tree.py` 不再保存在当前仓库中；构建时会从 `https://github.com/100pangci/MyTools.git` 拉取 `src/Python/4.扫描文件树/Tree.py`，并打包后随发布产物一起提供。

## 当前方案

- **技术栈**：Kotlin Multiplatform + Compose Multiplatform
- **平台覆盖**：
  - Android
  - Desktop（JVM，当前 CI 覆盖 Windows / macOS）
- **CI/CD**：全程通过 GitHub Actions 构建，不要求本地先建环境

## 支持的输入格式

当前解析的是 `MyTools` 仓库中 `src/Python/4.扫描文件树/Tree.py` 生成的纯文本格式，例如：

```text
==================================================
 文件树扫描报告
 扫描时间: 2026-06-10 10:00:00
 扫描位置: D:\
 深度限制: 4 层
==================================================

磁盘/挂载点: D:\

D:\
├── Games/
│   ├── Saves/
│   ├── Steam/
│   └── config.ini
└── Media/
    ├── Videos/
    └── cover.jpg
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
build-release.*      # 构建 GUI + 拉取并打包外部 Tree.py 的发布脚本
```

## GitHub Actions

仓库已添加两个构建任务：

- `ubuntu-latest`：构建 Android Debug 包，以及 Linux Desktop/Tree 发布产物
- `macos-latest`：构建 macOS Desktop 发行包，并打包 `Tree.py`
- `windows-latest`：构建 Windows Desktop 发行包，并打包 `Tree.py`

手动触发（`workflow_dispatch`）时，`version` 输入现在可以留空；如果不填写，默认使用 `v1.0.0` 作为版本号。

另外，手动触发只会执行各平台构建并上传 **Artifacts**，不会创建 GitHub Release，也不会向 **Releases** 页面上传文件。只有 `v*` tag push 时才会创建正式 Release。

### Tree.py 来源说明

- 来源仓库：`https://github.com/100pangci/MyTools.git`
- 来源路径：`src/Python/4.扫描文件树/Tree.py`
- 当前仓库在 CI / 发布脚本中会在构建时临时拉取该文件
- 然后使用 `PyInstaller` 打包为单文件可执行程序，并放入 `Releases/` 与其他构建产物一起上传

说明：当前仓库**不依赖本地预生成的 Gradle Wrapper**，CI 通过 `gradle/actions/setup-gradle` 安装并调用指定 Gradle 版本来完成构建。

如果你在 GitHub Actions 日志里看到还是 **Gradle 8.1**，而不是这里配置的 **8.10**，通常说明：

1. 触发构建的提交还没包含这次 workflow 更新；或
2. 你查看的是旧的 workflow run。

请确认把最新提交 push 上去后，再重新触发一次 Actions。

## Android 签名（Release）

项目已经支持通过 **GitHub Secrets** 注入签名信息来构建签名版 APK。

### 1. 本地生成 keystore

Windows 可直接运行：

```bat
keytool -genkeypair -v -keystore android-release.jks -keyalg RSA -keysize 2048 -validity 36500 -alias disktree-key
```

生成过程中你需要输入：

- keystore 密码
- key 密码（可与 keystore 密码相同）
- 证书信息（姓名/组织等，可按需填写）

### 2. 转成 Base64

PowerShell：

```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("android-release.jks")) | Set-Content android-release.base64.txt
```

把 `android-release.base64.txt` 里的整段内容复制出来。

### 3. 添加 GitHub Secrets

仓库页面：

`Settings` → `Secrets and variables` → `Actions` → `New repository secret`

新增以下 4 个 Secrets：

- `ANDROID_KEYSTORE_BASE64`：`android-release.jks` 的 Base64 内容
- `ANDROID_KEYSTORE_PASSWORD`：keystore 密码
- `ANDROID_KEY_ALIAS`：例如 `disktree-key`
- `ANDROID_KEY_PASSWORD`：key 密码

### 4. Actions 自动做的事情

当你 push 到 `main/master` 时：

- CI 会先构建 debug APK
- 如果存在签名 Secrets，会自动：
  - 解码 keystore
  - 注入签名环境变量
  - 执行 `:composeApp:assembleRelease`
  - 上传 APK 构建产物

### 5. 产物位置

GitHub Actions 运行完成后，在 workflow 的 **Artifacts** 中下载：

- `releases-android-*`
- `releases-linux-*`
- `releases-macos-*`
- `releases-windows-portable-*`

其中 Desktop 平台的 `Releases/` 目录内会同时包含 GUI 桌面构建产物，以及从 `MyTools` 仓库拉取后打包得到的 `Tree` 可执行文件。