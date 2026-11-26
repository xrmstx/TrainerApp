package com.example.trainerapp.ui.screens

import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.trainerapp.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onReady: () -> Unit) {
    var shown by remember { mutableStateOf(false) }
    // Медленное появление всей сцены: картинка + оверлей как один слой
    val alpha by animateFloatAsState(
        targetValue = if (shown) 1f else 0f,
        animationSpec = tween(durationMillis = 1500), // медленный фейд — 1,5 сек
        label = "splashFade"
    )

    LaunchedEffect(Unit) {
        delay(120)          // пауза, чтобы анимация стартовала гарантированно
        shown = true        // старт фейда
        delay(2600)         // 1,5 с появление + ~1 с на удержание
        onReady()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { this.alpha = alpha }
    ) {
        Image(
            painter = painterResource(R.drawable.app_splash),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        // Тот же оверлей 80%, что и на остальных экранах
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.8f))
        )
    }
}
