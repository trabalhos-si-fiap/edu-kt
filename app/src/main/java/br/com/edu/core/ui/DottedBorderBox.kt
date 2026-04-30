package br.com.edu.core.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import br.com.edu.core.theme.EduColors

@Composable
fun DottedBorderBox(
    modifier: Modifier = Modifier,
    radius: Dp = 16.dp,
    color: Color = EduColors.TextSecondary.copy(alpha = 0.5f),
    strokeWidth: Dp = 1.5.dp,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .drawBehind {
                val strokePx = strokeWidth.toPx()
                val radiusPx = radius.toPx()
                drawRoundRect(
                    color = color,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(radiusPx, radiusPx),
                    style = Stroke(
                        width = strokePx,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f),
                    ),
                )
            }
            .padding(contentPadding),
    ) {
        content()
    }
}
