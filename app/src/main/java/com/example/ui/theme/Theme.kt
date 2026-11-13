package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val NaraLightColorScheme = lightColorScheme(
    primary = SkyBlueBright,
    onPrimary = Color.White,
    primaryContainer = SoftBlueTint,
    onPrimaryContainer = SkyBlueDeep,
    secondary = CandyGreenMid,
    onSecondary = Color.White,
    tertiary = CoinGold,
    onTertiary = SlateInk,
    background = Color(0xFFE0F2FE),
    onBackground = SlateInk,
    surface = CreamSurface,
    onSurface = SlateInk,
    surfaceVariant = SoftBlueTint,
    onSurfaceVariant = SlateMuted,
    error = ErrorCoral,
    onError = Color.White
)

@Composable
fun NaraGameTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = NaraLightColorScheme,
        typography = Typography,
        content = content
    )
}
