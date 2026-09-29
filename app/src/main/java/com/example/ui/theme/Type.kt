package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

val Typography = Typography(
  displayLarge = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Bold,
    fontSize = 34.sp,
    lineHeight = 41.sp,
    letterSpacing = 0.37.sp
  ),
  displayMedium = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.SemiBold,
    fontSize = 28.sp,
    lineHeight = 34.sp,
    letterSpacing = 0.36.sp
  ),
  titleLarge = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.SemiBold,
    fontSize = 22.sp,
    lineHeight = 28.sp,
    letterSpacing = 0.35.sp
  ),
  titleMedium = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Medium,
    fontSize = 17.sp,
    lineHeight = 22.sp,
    letterSpacing = (-0.41).sp
  ),
  bodyLarge = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Normal,
    fontSize = 17.sp,
    lineHeight = 24.sp,
    letterSpacing = (-0.41).sp
  ),
  bodyMedium = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Normal,
    fontSize = 15.sp,
    lineHeight = 20.sp,
    letterSpacing = (-0.24).sp
  ),
  labelLarge = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.SemiBold,
    fontSize = 15.sp,
    lineHeight = 20.sp,
    letterSpacing = (-0.24).sp
  ),
  labelMedium = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Medium,
    fontSize = 13.sp,
    lineHeight = 18.sp,
    letterSpacing = (-0.08).sp
  ),
  labelSmall = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Normal,
    fontSize = 12.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.sp
  )
)

/**
 * Extension function for adaptive typography font sizes based on device window configuration.
 * Automatically scales font size for compact, medium, and expanded device displays.
 */
@Composable
fun TextUnit.adaptive(): TextUnit {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val fontScale = configuration.fontScale
    
    val widthFactor = when {
        screenWidthDp < 360 -> 0.92f
        screenWidthDp in 360..600 -> 1.0f
        screenWidthDp in 601..840 -> 1.12f
        else -> 1.25f
    }
    
    // Respect system font scaling while applying adaptive window class scaling
    val rawValue = this.value * widthFactor
    return (rawValue / fontScale).sp
}

/**
 * Extension function to apply adaptive typography directly to a TextStyle.
 */
@Composable
fun TextStyle.adaptive(): TextStyle {
    return this.copy(
        fontSize = this.fontSize.adaptive(),
        lineHeight = if (this.lineHeight != TextUnit.Unspecified) this.lineHeight.adaptive() else TextUnit.Unspecified
    )
}


