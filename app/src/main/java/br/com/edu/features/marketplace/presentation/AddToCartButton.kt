package br.com.edu.features.marketplace.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import br.com.edu.core.theme.EduColors
import kotlinx.coroutines.delay

@Composable
fun AddToCartButton(
    enabled: Boolean,
    onAddToCart: () -> Unit,
    modifier: Modifier = Modifier,
    label: String = "+ Carrinho",
    addedLabel: String = "Adicionado",
    minHeight: Dp = 0.dp,
    idleContainerColor: Color = EduColors.InputFill,
    idleContentColor: Color = EduColors.TextPrimary,
) {
    var added by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (added) 1.06f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "addToCartScale",
    )

    LaunchedEffect(added) {
        if (added) {
            delay(1200)
            added = false
        }
    }

    Button(
        onClick = {
            if (enabled && !added) {
                added = true
                onAddToCart()
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = minHeight)
            .scale(scale),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (added) EduColors.GreenSoft else idleContainerColor,
            contentColor = if (added) EduColors.GreenDark else idleContentColor,
        ),
    ) {
        AnimatedContent(
            targetState = added,
            transitionSpec = {
                (scaleIn(animationSpec = tween(180), initialScale = 0.6f) + fadeIn(tween(180)))
                    .togetherWith(scaleOut(tween(180), targetScale = 0.6f) + fadeOut(tween(180)))
            },
            label = "addToCartLabel",
        ) { isAdded ->
            if (isAdded) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.size(8.dp))
                    Text(addedLabel, fontWeight = FontWeight.Bold)
                }
            } else {
                Text(label, fontWeight = FontWeight.Bold)
            }
        }
    }
}
