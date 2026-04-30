package br.com.edu.core.theme

import androidx.compose.ui.graphics.Brush

object EduGradients {
    val Background: Brush
        get() = Brush.verticalGradient(
            colors = listOf(
                EduColors.Background,
                androidx.compose.ui.graphics.Color(0xFFBDD5E5),
                androidx.compose.ui.graphics.Color(0xFFD1E0EE),
            ),
        )

    val OverlayDark: Brush
        get() = Brush.verticalGradient(
            colors = listOf(EduColors.OverlayDark1, EduColors.OverlayDark2),
        )
}
