package com.example.trainerapp.ui.theme

import androidx.compose.ui.graphics.Color

// Базовая палитра
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650A4)
val PurpleGrey40 = Color(0xFF625B71)
val Pink40 = Color(0xFF7D5260)

// Семантические цвета статусов тренировок
val StatusDone = Color(0xFF2E7D32)            // зелёный — выполнено
val StatusDoneContainer = Color(0xFFDCEDC8)   // фон карточки «выполнено»
val StatusOverdue = Color(0xFFE65100)         // оранжевый — просрочено
val StatusOverdueContainer = Color(0xFFFFE0B2)
val StatusPlanned = Color(0xFF546E7A)         // нейтральный — в плане
val StatusPlannedContainer = Color(0xFFECEFF1)

// Градиенты для аватаров-инициалов (подбираются по id клиента)
val AvatarGradients = listOf(
    listOf(Color(0xFF7C4DFF), Color(0xFF448AFF)),
    listOf(Color(0xFF00897B), Color(0xFF80CBC4)),
    listOf(Color(0xFFE65100), Color(0xFFFFB74D)),
    listOf(Color(0xFFC2185B), Color(0xFFF48FB1)),
    listOf(Color(0xFF3949AB), Color(0xFF7986CB))
)
