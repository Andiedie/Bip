# 产品需求文档 (PRD) - Bip v1.0

| 项目 | 内容 |
| --- | --- |
| **产品名称** | **Bip** |
| **版本号** | v1.0.0 |
| **平台** | Android (Min SDK 26+) |
| **目标用户** | 开发者自用 (个人训练/计时场景) |
| **核心价值** | 极致的盲操作体验，通过物理按键实现毫秒级响应的语音秒表 |
| **更新日期** | 2026-02-08 |

---

## 1. 产品概述 (Overview)

**Bip** 是一款专为盲操作设计的 Android 秒表应用。它利用蓝牙自拍杆（模拟音量键）作为物理触发器，让用户在无需观看屏幕的情况下进行高频计时。

**设计哲学：**

* **极简 (Minimalist):** 致敬 Wise (原 TransferWise) 的设计语言，高对比度，无干扰。
* **鲁棒 (Robust):** 拒绝误触，保证毫秒级响应，即使在播放音乐时也能清晰反馈。
* **即时 (Instant):** 按下即开始，再次按下即播报。

---

## 2. 用户流程 (User Flow)

1. **准备阶段：**
* 用户打开 Bip 应用。
* 屏幕保持常亮，界面显示准备状态（归零）。


2. **开始计时：**
* 用户按下蓝牙按钮（音量键）。
* **听觉反馈：** 立即播放短促响亮的 "Bip" 音效。
* **视觉反馈：** 屏幕数字开始滚动。


3. **停止与播报：**
* 用户再次按下蓝牙按钮。
* **听觉反馈：** TTS 立即播报时长：“XX 点 XX 秒”（例如 "九点零五秒" 或 "七十五点三零秒"）。
* **视觉反馈：** 数字定格。


4. **重置与循环：**
* 用户再次按下按钮。
* 无需单独重置，系统自动归零并立即开始新一轮计时（触发 "Bip" 音效）。



---

## 3. 功能需求 (Functional Requirements)

### 3.1 物理交互 (Physical Interaction)

* **按键拦截：** 必须在 `Activity` 层级拦截 `KEYCODE_VOLUME_UP` 和 `KEYCODE_VOLUME_DOWN`。
* **系统屏蔽：** 拦截成功后返回 `true`，确保**不改变系统音量**，且**不显示系统音量滑块 UI**。
* **防抖动 (Debounce):** 设置 **300ms** 的输入冷却时间，防止蓝牙设备发送重复信号导致误操作。

### 3.2 计时逻辑 (Timing Logic)

* **精度：** 内部记录使用 `System.nanoTime()` 或 `System.currentTimeMillis()`，精确到毫秒。
* **显示：** UI 刷新频率不低于 60fps，显示格式为 `SS.mm` (秒.毫秒前两位)。
* 例：`9.05`，`120.45`。



### 3.3 音频反馈 (Audio Feedback)

* **音频焦点 (Audio Focus):**
* 请求 `AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK`。
* **场景：** 如果用户正在用 Spotify/网易云听歌，按下按钮时背景音乐应自动降低音量 (Duck)，TTS 播报完毕后恢复。


* **启动音效 (Start Tone):**
* 使用 `ToneGenerator` 或短 PCM 音频文件播放高频 "Bip" 声。
* 要求：零延迟，甚至优先于 TTS 引擎的初始化。


* **结束播报 (Stop TTS):**
* 文本模板：`"{秒}点{毫秒}秒"`。
* 数字处理：
* `09.05` -> 读作 "九点零五秒"。
* `12.50` -> 读作 "十二点五零秒"（为了记录方便，不换算成分钟）。


* 语言：强制使用中文 TTS 引擎。



### 3.4 屏幕行为 (Display Behavior)

* **常亮 (Keep Screen On):** 应用在前台运行时，强制持有 `FLAG_KEEP_SCREEN_ON`，防止手机休眠导致蓝牙断连。
* **省电模式 (Dark Mode Friendly):** 虽然 UI 鲜艳，但背景应适配 OLED 屏幕的省电特性（可选纯黑背景开关）。

---

## 4. UI/UX 设计规范 (Wise Style)

**整体风格：** 现代、扁平、高饱和度强调色。

### 4.1 调色板

* **Background (背景):** `Navy Blue (#163355)` -以此作为主背景，凸显专业感和沉浸感。
* **Accent (强调/激活):** `Bright Green (#2ED06E)` - 用于正在计时状态或重要数字。
* **Text (文本):** `White (#FFFFFF)` - 用于显示时间。
* **Secondary Text (辅助):** `Light Grey (#AAB3BD)` - 用于状态提示。

### 4.2 界面布局

* **单页应用 (Single Page):**
* **中央：** 巨大的计时数字 (自适应宽度，确保最大化可视性)。字体建议使用 `Roboto Mono` 或 `JetBrains Mono` 等等宽字体，防止数字跳动导致的视觉抖动。
* **顶部：** Bip Logo (小尺寸)。
* **底部：** 状态提示文案（"等待开始" / "计时中..." / "已完成"）。



---

## 5. 技术实施细节 (Technical Stack & Implementation)

为了满足“现代且维护良好”的要求，本项目将严格采用 Android 推荐的架构组件。

### 5.1 技术栈

* **语言:** Kotlin
* **UI 框架:** Jetpack Compose (Material 3)
* **架构:** MVVM (Model-View-ViewModel) + Unidirectional Data Flow (单向数据流)
* **异步:** Kotlin Coroutines + StateFlow
* **音频:**
* `android.speech.tts.TextToSpeech` (语音)
* `android.media.ToneGenerator` (Bip 音效，低延迟)
* `android.media.AudioManager` (音频焦点管理)



### 5.2 核心模块设计

#### A. MainActivity (入口)

* 负责 UI 承载 (Compose)。
* 负责 `onKeyDown` 拦截。
* 负责生命周期管理 (TTS 初始化/销毁)。
* 负责屏幕常亮 Flag 设置。

#### B. TimerViewModel (逻辑核心)

* 维护 `TimerState` (Sealed Class):
* `Idle`: 初始状态。
* `Running(startTime: Long)`: 计时中。
* `Stopped(finalTime: Long)`: 停止并展示结果。


* 暴露 `formattedTime: StateFlow<String>` 给 UI 层。
* 处理去抖动 (Debounce) 逻辑。

#### C. AudioService (音频管理器)

* 封装 TTS 和 ToneGenerator。
* **关键方法:**
* `playStartTone()`: 播放 "Bip"。
* `speakResult(seconds: Long, millis: Long)`: 构造文本并朗读。
* `requestAudioFocus()`: 处理背景音乐压低逻辑。



### 5.3 关键代码片段 (伪代码逻辑)

**按键处理逻辑：**

```kotlin
fun handleButtonPress() {
    val now = System.currentTimeMillis()
    if (now - lastPressTime < 300) return // 防抖

    lastPressTime = now

    when (currentState) {
        is Idle, is Stopped -> {
            audioService.playStartTone()
            startTimer()
        }
        is Running -> {
            stopTimer()
            val (sec, ms) = calculateDuration()
            audioService.speakResult(sec, ms)
        }
    }
}

```

---

## 6. 异常处理与鲁棒性 (Robustness)

1. **TTS 初始化失败：**
* 如果设备不支持中文 TTS 或引擎初始化失败，App 不应崩溃。
* **降级方案：** 仅播放 "Bip" 音效来提示开始和结束，并在屏幕上显示 Toast 错误提示。


2. **Activity 被销毁 (配置变更)：**
* 锁定屏幕方向为 `PORTRAIT` (竖屏)，防止旋转导致的 Activity 重建和计时中断（虽然 ViewModel 可以保存状态，但简单的锁定能避免很多 UI 适配问题）。


3. **蓝牙断连：**
* 如果蓝牙遥控器断开，用户仍可通过触摸屏幕（点击整个屏幕区域）作为备用触发方式。
