package br.com.edu.features.marketplace.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.edu.core.theme.EduColors
import br.com.edu.core.ui.EduTextField

@Composable
fun MarketplaceTopBar(
    search: String,
    onSearchChange: (String) -> Unit,
    selectedType: String?,
    availableTypes: List<String>,
    onTypeSelected: (String?) -> Unit,
    cartCount: Int,
    onOpenProfile: () -> Unit,
    onOpenCart: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onOpenProfile) {
                Icon(Icons.Outlined.PersonOutline, null, tint = EduColors.TextPrimary)
            }
            Surface(
                shape = RoundedCornerShape(32.dp),
                color = EduColors.White,
                shadowElevation = 2.dp,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp),
            ) {
                EduTextField(
                    value = search,
                    onValueChange = onSearchChange,
                    placeholder = "Buscar cursos, guias ou materiais...",
                    leadingIcon = {
                        Icon(Icons.Outlined.Search, null, tint = EduColors.TextSecondary)
                    },
                )
            }
            CartIconWithBadge(count = cartCount, onClick = onOpenCart)
        }
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                TypeFilterChip(
                    label = "Tudo",
                    selected = selectedType == null,
                    onClick = { onTypeSelected(null) },
                )
            }
            items(availableTypes) { type ->
                TypeFilterChip(
                    label = type.uppercase(),
                    selected = selectedType == type,
                    onClick = { onTypeSelected(type) },
                )
            }
        }
    }
}

@Composable
private fun CartIconWithBadge(count: Int, onClick: () -> Unit) {
    Box(contentAlignment = Alignment.TopEnd) {
        IconButton(onClick = onClick) {
            Icon(Icons.Outlined.ShoppingCart, null, tint = EduColors.TextPrimary)
        }
        AnimatedContent(
            targetState = count,
            transitionSpec = {
                (scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn())
                    .togetherWith(scaleOut(tween(120)) + fadeOut(tween(120)))
            },
            label = "cartBadge",
            modifier = Modifier.offset(x = (-6).dp, y = 6.dp),
        ) { value ->
            if (value > 0) {
                Box(
                    Modifier
                        .size(18.dp)
                        .background(EduColors.Purple, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        if (value > 99) "99+" else value.toString(),
                        color = EduColors.White,
                        fontSize = 10.sp,
                        lineHeight = 10.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        textAlign = TextAlign.Center,
                        style = LocalTextStyle.current.copy(
                            platformStyle = PlatformTextStyle(includeFontPadding = false),
                            lineHeightStyle = LineHeightStyle(
                                alignment = LineHeightStyle.Alignment.Center,
                                trim = LineHeightStyle.Trim.Both,
                            ),
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun TypeFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (selected) EduColors.Purple else EduColors.White,
        shadowElevation = if (selected) 0.dp else 1.dp,
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Text(
            text = label,
            color = if (selected) EduColors.White else EduColors.TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            letterSpacing = 0.5.sp,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
    }
}
