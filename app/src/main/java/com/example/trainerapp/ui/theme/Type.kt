package com.example.trainerapp.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.trainerapp.R

private val ptSans = FontFamily(
    Font(R.font.pt_sans_regular, FontWeight.Normal),
    Font(R.font.pt_sans_regular, FontWeight.Medium),
    Font(R.font.pt_sans_bold, FontWeight.Bold)
)

private val ptSerif = FontFamily(
    Font(R.font.pt_serif_regular, FontWeight.Normal),
    Font(R.font.pt_serif_regular, FontWeight.SemiBold),
    Font(R.font.pt_serif_bold, FontWeight.Bold)
)

// Заголовки — PT Serif, основной текст — PT Sans
private fun serifTitle(weight: FontWeight, size: Int, line: Int) = TextStyle(
    fontFamily = ptSerif,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = line.sp
)

private fun sansBody(weight: FontWeight, size: Int, line: Int) = TextStyle(
    fontFamily = ptSans,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = line.sp
)

val AppTypography = Typography(
    headlineMedium = serifTitle(FontWeight.Bold, 26, 32),
    headlineSmall = serifTitle(FontWeight.Bold, 22, 28),
    titleLarge = serifTitle(FontWeight.SemiBold, 20, 26),
    titleMedium = sansBody(FontWeight.SemiBold, 17, 23),
    titleSmall = sansBody(FontWeight.SemiBold, 15, 20),
    bodyLarge = sansBody(FontWeight.Normal, 16, 22),
    bodyMedium = sansBody(FontWeight.Normal, 14, 20),
    bodySmall = sansBody(FontWeight.Normal, 12, 17),
    labelLarge = sansBody(FontWeight.SemiBold, 14, 19),
    labelMedium = sansBody(FontWeight.Medium, 12, 16),
    labelSmall = sansBody(FontWeight.Medium, 11, 15)
)
