package com.example.trainerapp.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.Typography
import androidx.core.content.res.ResourcesCompat
import com.example.trainerapp.R

// Подключаем шрифты из res/font
private val ptSans = FontFamily(
    Font(R.font.pt_sans, FontWeight.Normal),
    Font(R.font.pt_sans, FontWeight.Bold)
)

private val ptSerif = FontFamily(
    Font(R.font.pt_serif, FontWeight.Normal),
    Font(R.font.pt_serif, FontWeight.Bold)
)

val TrainerTypography = Typography(
    // Заголовки экранов и сплэш — PT Serif (выразительно, но не слишком «витиевато»)
    displayLarge = androidx.compose.ui.text.TextStyle(
        fontFamily = ptSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        letterSpacing = 0.5.em
    ),
    headlineLarge = androidx.compose.ui.text.TextStyle(
        fontFamily = ptSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp
    ),
    titleLarge = androidx.compose.ui.text.TextStyle(
        fontFamily = ptSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp
    ),

    // Основной текст — PT Sans (чётко, цифры читаются сразу)
    bodyLarge = androidx.compose.ui.text.TextStyle(
        fontFamily = ptSans,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp
    ),
    bodyMedium = androidx.compose.ui.text.TextStyle(
        fontFamily = ptSans,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        color = MonoSubtext
    ),
    labelMedium = androidx.compose.ui.text.TextStyle(
        fontFamily = ptSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp
    )
)
