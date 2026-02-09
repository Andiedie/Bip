package com.bip.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bip.TimerState
import com.bip.TimerViewModel
import com.bip.ui.theme.BrightGreen
import com.bip.ui.theme.LightGrey
import com.bip.ui.theme.NavyBlue
import com.bip.ui.theme.White

@Composable
fun TimerScreen(
    viewModel: TimerViewModel,
    onScreenTap: () -> Unit
) {
    val formattedTime by viewModel.formattedTime.collectAsState()
    val statusText by viewModel.statusText.collectAsState()
    val timerState by viewModel.timerState.collectAsState()

    val timeColor = when (timerState) {
        is TimerState.Running -> BrightGreen
        else -> White
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyBlue)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onScreenTap
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Bip",
                color = White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 48.dp)
            )

            Text(
                text = formattedTime,
                color = timeColor,
                fontSize = 96.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )

            Text(
                text = statusText,
                color = LightGrey,
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 64.dp)
            )
        }
    }
}
