# Bip

<p align="center">
  一个简单的语音秒表，支持蓝牙遥控器触发。
</p>

<p align="center">
  <a href="README.md">English</a>
</p>

## 功能

- 音量键触发计时
- 中文语音播报结果
- 点击屏幕备用触发
- 播放音乐时自动降低背景音量
- 使用时屏幕保持常亮

## 使用方法

1. 按音量键 → 开始计时（播放 "Bip" 音效）
2. 再按 → 语音播报时长
3. 再按 → 重置并开始新计时

无需手动重置。

## 构建

```bash
./gradlew assembleDebug
```

或在 Android Studio 中：`Build` → `Build APK(s)`

输出位置：`app/build/outputs/apk/debug/app-debug.apk`

## 技术栈

- Kotlin
- Jetpack Compose (Material 3)
- MVVM 架构
- Coroutines + StateFlow

## 项目结构

```
app/src/main/java/com/bip/
├── MainActivity.kt       # 入口，按键拦截
├── TimerState.kt         # 计时器状态密封类
├── TimerViewModel.kt     # 核心逻辑，防抖
├── AudioService.kt       # TTS + 音效
└── ui/
    ├── TimerScreen.kt    # Compose UI
    └── theme/            # 颜色和主题
```

## 系统要求

- Android 8.0+ (API 26)
- 需要支持中文的 TTS 引擎

## 测试环境

- OPPO Find X8s

## 已知问题

部分国产手机未预装 TTS 引擎，首次使用时会提示安装。

## 许可证

MIT
