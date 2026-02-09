package com.bip

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.ToneGenerator
import android.net.Uri
import android.speech.tts.TextToSpeech
import android.widget.Toast
import java.util.Locale

class AudioService(private val context: Context) {

    private var tts: TextToSpeech? = null
    private var toneGenerator: ToneGenerator? = null
    private var audioManager: AudioManager? = null
    private var audioFocusRequest: AudioFocusRequest? = null

    private var isTtsReady = false

    private val chineseDigits = arrayOf("零", "一", "二", "三", "四", "五", "六", "七", "八", "九")

    fun initialize(onReady: () -> Unit) {
        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
        } catch (e: Exception) {
            showToast("音效初始化失败")
        }

        initTts(onReady)
    }

    private fun initTts(onReady: () -> Unit) {
        tts = TextToSpeech(context) { status ->
            when {
                status != TextToSpeech.SUCCESS -> {
                    showToast("TTS 初始化失败，请安装语音引擎")
                    promptInstallTtsEngine()
                }
                else -> {
                    val result = tts?.setLanguage(Locale.CHINESE)
                    isTtsReady = result != TextToSpeech.LANG_MISSING_DATA &&
                            result != TextToSpeech.LANG_NOT_SUPPORTED

                    if (!isTtsReady) {
                        if (result == TextToSpeech.LANG_MISSING_DATA) {
                            showToast("缺少中文语音数据，正在打开设置...")
                            promptInstallTtsData()
                        } else {
                            showToast("不支持中文语音")
                        }
                    }
                }
            }
            onReady()
        }
    }

    private fun promptInstallTtsEngine() {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("market://details?id=com.google.android.tts")
                setPackage("com.android.vending")
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.tts")
                }
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            } catch (e2: Exception) {
                openTtsSettings()
            }
        }
    }

    private fun promptInstallTtsData() {
        try {
            val intent = Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            openTtsSettings()
        }
    }

    private fun openTtsSettings() {
        try {
            val intent = Intent("com.android.settings.TTS_SETTINGS")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            showToast("请在系统设置中安装语音引擎")
        }
    }

    fun playStartTone() {
        requestAudioFocus()
        toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 100)
    }

    fun speakResult(elapsedMillis: Long) {
        if (!isTtsReady) {
            playStartTone()
            return
        }

        requestAudioFocus()

        val seconds = elapsedMillis / 1000
        val centiseconds = (elapsedMillis % 1000) / 10

        val text = buildTtsText(seconds, centiseconds)
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "result")
    }

    private fun buildTtsText(seconds: Long, centiseconds: Long): String {
        val secondsPart = convertToChineseNumber(seconds)
        val centisecondsPart = convertTwoDigitChinese(centiseconds)
        return "${secondsPart}点${centisecondsPart}秒"
    }

    private fun convertToChineseNumber(number: Long): String {
        if (number == 0L) return chineseDigits[0]

        val sb = StringBuilder()
        var n = number

        if (n >= 100) {
            sb.append(chineseDigits[(n / 100).toInt()])
            sb.append("百")
            n %= 100
            if (n in 1..9) sb.append(chineseDigits[0])
        }

        if (n >= 10) {
            if (n >= 20 || sb.isNotEmpty()) {
                sb.append(chineseDigits[(n / 10).toInt()])
            }
            sb.append("十")
            n %= 10
        }

        if (n > 0) {
            sb.append(chineseDigits[n.toInt()])
        }

        return sb.toString()
    }

    private fun convertTwoDigitChinese(number: Long): String {
        val tens = (number / 10).toInt()
        val ones = (number % 10).toInt()
        return "${chineseDigits[tens]}${chineseDigits[ones]}"
    }

    private fun requestAudioFocus() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
            .setAudioAttributes(audioAttributes)
            .setAcceptsDelayedFocusGain(false)
            .setWillPauseWhenDucked(false)
            .build()

        audioManager?.requestAudioFocus(audioFocusRequest!!)
    }

    fun release() {
        audioFocusRequest?.let { audioManager?.abandonAudioFocusRequest(it) }
        toneGenerator?.release()
        tts?.stop()
        tts?.shutdown()

        toneGenerator = null
        tts = null
        audioManager = null
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
