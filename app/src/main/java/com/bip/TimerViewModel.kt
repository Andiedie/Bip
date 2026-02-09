package com.bip

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class TimerViewModel : ViewModel() {

    companion object {
        private const val DEBOUNCE_INTERVAL_MS = 300L
        private const val UI_REFRESH_INTERVAL_MS = 16L
    }

    private val _timerState = MutableStateFlow<TimerState>(TimerState.Idle)
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()

    private val _formattedTime = MutableStateFlow("0.00")
    val formattedTime: StateFlow<String> = _formattedTime.asStateFlow()

    private val _statusText = MutableStateFlow("等待开始")
    val statusText: StateFlow<String> = _statusText.asStateFlow()

    private var lastPressTimeMillis = 0L
    private var refreshJob: Job? = null

    /**
     * @return Pair(isStarting, elapsedMillisIfStopped)
     */
    fun handleButtonPress(): Pair<Boolean, Long?> {
        val now = System.currentTimeMillis()

        if (now - lastPressTimeMillis < DEBOUNCE_INTERVAL_MS) {
            return Pair(false, null)
        }
        lastPressTimeMillis = now

        return when (val currentState = _timerState.value) {
            is TimerState.Idle, is TimerState.Stopped -> {
                startTimer()
                Pair(true, null)
            }

            is TimerState.Running -> {
                val elapsedMillis = stopTimer(currentState.startTimeNanos)
                Pair(false, elapsedMillis)
            }
        }
    }

    private fun startTimer() {
        val startTimeNanos = System.nanoTime()
        _timerState.value = TimerState.Running(startTimeNanos)
        _statusText.value = "计时中..."

        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            while (isActive) {
                val currentState = _timerState.value
                if (currentState is TimerState.Running) {
                    val elapsedNanos = System.nanoTime() - currentState.startTimeNanos
                    val elapsedMillis = elapsedNanos / 1_000_000
                    _formattedTime.value = formatTime(elapsedMillis)
                }
                delay(UI_REFRESH_INTERVAL_MS)
            }
        }
    }

    private fun stopTimer(startTimeNanos: Long): Long {
        refreshJob?.cancel()
        refreshJob = null

        val elapsedNanos = System.nanoTime() - startTimeNanos
        val elapsedMillis = elapsedNanos / 1_000_000

        _timerState.value = TimerState.Stopped(elapsedMillis)
        _formattedTime.value = formatTime(elapsedMillis)
        _statusText.value = "已完成"

        return elapsedMillis
    }

    // Format: "SS.mm" (e.g., 9050ms -> "9.05")
    private fun formatTime(millis: Long): String {
        val seconds = millis / 1000
        val centiseconds = (millis % 1000) / 10
        return "$seconds.${centiseconds.toString().padStart(2, '0')}"
    }

    override fun onCleared() {
        super.onCleared()
        refreshJob?.cancel()
    }
}
