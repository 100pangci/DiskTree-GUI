package io.github.disktreegui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF4F46E5),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE3E7FF),
    onPrimaryContainer = Color(0xFF1C1B4B),
    secondary = Color(0xFF7C6CF2),
    onSecondary = Color(0xFFFFFFFF),
    tertiary = Color(0xFF14B8A6),
    background = Color(0xFFF5F7FB),
    onBackground = Color(0xFF131722),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF181C25),
    surfaceVariant = Color(0xFFF0F3FA),
    onSurfaceVariant = Color(0xFF667085),
    outline = Color(0xFFD4D9E6)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF9EA6FF),
    onPrimary = Color(0xFF141532),
    primaryContainer = Color(0xFF22274A),
    onPrimaryContainer = Color(0xFFDCE0FF),
    secondary = Color(0xFFC2B8FF),
    onSecondary = Color(0xFF231B4A),
    tertiary = Color(0xFF6EE7D8),
    background = Color(0xFF0B1020),
    onBackground = Color(0xFFE8ECF8),
    surface = Color(0xFF131A2D),
    onSurface = Color(0xFFF1F4FB),
    surfaceVariant = Color(0xFF1A2238),
    onSurfaceVariant = Color(0xFF99A3B8),
    outline = Color(0xFF313A55)
)

@Composable
fun DiskTreeTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}