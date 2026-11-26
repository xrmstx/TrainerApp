package com.example.trainerapp.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.trainerapp.R
import com.example.trainerapp.ui.theme.MonoDark

@Composable
fun BackgroundLayer(
    imageResId: Int,
    overlayAlpha: Float = 0.8f, // 80% как ты просил
    content: @Composable () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Фон: картинка на весь экран
        Image(
            painter = painterResource(id = imageResId),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 1f
        )

        // Оверлей поверх картинки: цвет фона с прозрачностью
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MonoDark.copy(alpha = overlayAlpha))
        )

        // Контент поверх всего
        content()
    }
}

@Preview
@Composable
fun PreviewBackgroundLayer() {
    BackgroundLayer(imageResId = R.drawable.bg_placeholder) {
        // сюда можно вставить любой экран
    }
}
