package br.com.edu.core.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import br.com.edu.core.theme.EduColors

@Composable
fun EduCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(20.dp),
    radius: Dp = 24.dp,
    color: Color = EduColors.White,
    shadow: Dp = 4.dp,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(radius),
        color = color,
        shadowElevation = shadow,
    ) {
        androidx.compose.foundation.layout.Box(Modifier.padding(contentPadding)) {
            content()
        }
    }
}
