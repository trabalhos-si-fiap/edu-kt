package br.com.edu.core.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val EduColorScheme = lightColorScheme(
    primary = EduColors.Primary,
    onPrimary = EduColors.White,
    secondary = EduColors.Purple,
    onSecondary = EduColors.White,
    background = EduColors.Background,
    onBackground = EduColors.TextPrimary,
    surface = EduColors.White,
    onSurface = EduColors.TextPrimary,
    error = EduColors.Danger,
    onError = EduColors.White,
)

@Composable
fun EduTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = EduColorScheme,
        typography = EduTypography,
        shapes = EduShapes,
        content = content,
    )
}
