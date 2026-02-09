package com.bip

import android.os.Bundle
import android.view.KeyEvent
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.bip.ui.TimerScreen
import com.bip.ui.theme.BipTheme

class MainActivity : ComponentActivity() {

    private val viewModel: TimerViewModel by viewModels()
    private lateinit var audioService: AudioService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        audioService = AudioService(this)
        audioService.initialize {}

        setContent {
            BipTheme {
                TimerScreen(
                    viewModel = viewModel,
                    onScreenTap = { handleTrigger() }
                )
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            handleTrigger()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun handleTrigger() {
        val (isStarting, elapsedMillis) = viewModel.handleButtonPress()

        when {
            isStarting -> audioService.playStartTone()
            elapsedMillis != null -> audioService.speakResult(elapsedMillis)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        audioService.release()
    }
}
