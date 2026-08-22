package com.commanderanalyst.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CynfulColors = darkColorScheme(
    primary = Color(0xFFE0A52F),
    secondary = Color(0xFFD2B06D),
    tertiary = Color(0xFFC12A1D),
    background = Color(0xFF050505),
    surface = Color(0xFF111111),
    surfaceVariant = Color(0xFF1C1712),
    onPrimary = Color(0xFF140D02),
    onSecondary = Color(0xFF140D02),
    onTertiary = Color.White,
    onBackground = Color(0xFFF4E8D0),
    onSurface = Color(0xFFF4E8D0),
    onSurfaceVariant = Color(0xFFD8C49A),
    outline = Color(0xFF7B6340)
)

@Composable
fun CommanderAnalystTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CynfulColors,
        content = content
    )
}
