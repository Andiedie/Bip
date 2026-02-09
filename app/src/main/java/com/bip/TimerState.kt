package com.bip

sealed class TimerState {
    data object Idle : TimerState()
    data class Running(val startTimeNanos: Long) : TimerState()
    data class Stopped(val elapsedMillis: Long) : TimerState()
}
