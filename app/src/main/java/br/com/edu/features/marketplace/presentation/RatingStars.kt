package br.com.edu.features.marketplace.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.StarHalf
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import br.com.edu.core.theme.EduColors
import kotlin.math.roundToInt

private val StarColor = Color(0xFFF59E0B)

@Composable
fun RatingStars(
    rating: Double,
    count: Int,
    modifier: Modifier = Modifier,
    starSize: Dp = 14.dp,
    showCount: Boolean = true,
) {
    val halves = (rating * 2).roundToInt().coerceIn(0, 10)
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        for (i in 0 until 5) {
            val filledHalves = halves - (i * 2)
            when {
                filledHalves >= 2 -> Icon(
                    Icons.Filled.Star,
                    contentDescription = null,
                    tint = StarColor,
                    modifier = Modifier.size(starSize),
                )
                filledHalves == 1 -> Icon(
                    Icons.AutoMirrored.Filled.StarHalf,
                    contentDescription = null,
                    tint = StarColor,
                    modifier = Modifier.size(starSize),
                )
                else -> Icon(
                    Icons.Outlined.StarBorder,
                    contentDescription = null,
                    tint = StarColor,
                    modifier = Modifier.size(starSize),
                )
            }
        }
        if (showCount) {
            Spacer(Modifier.width(4.dp))
            val label = if (count == 0) "Sem avaliações"
            else String.format("%.1f (%d)", rating, count)
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                color = EduColors.TextSecondary,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}
