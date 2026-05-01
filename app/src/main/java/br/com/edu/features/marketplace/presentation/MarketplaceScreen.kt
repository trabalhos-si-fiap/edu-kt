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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.DashboardCustomize
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil.compose.SubcomposeAsyncImage
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import br.com.edu.core.theme.EduColors
import br.com.edu.core.theme.EduGradients
import br.com.edu.core.ui.EduCard
import br.com.edu.core.ui.formatBRL
import br.com.edu.core.ui.EduPurpleButton
import br.com.edu.core.ui.EduSoftButton
import br.com.edu.core.ui.EduTextField
import br.com.edu.core.ui.MainBottomBar
import br.com.edu.features.cart.presentation.CartUiState
import br.com.edu.features.cart.presentation.CartViewModel
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.CircleShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplaceScreen(
    onOpenCart: () -> Unit,
    onOpenOrders: () -> Unit,
    onOpenProfile: () -> Unit = {},
    onBack: () -> Unit = {},
    viewModel: MarketplaceViewModel = viewModel(),
    cartViewModel: CartViewModel = CartViewModel.get(),
) {
    val search by viewModel.query.collectAsState()
    val uiState by viewModel.state.collectAsState()
    val cartBusy by cartViewModel.busy.collectAsState()
    val cartState by cartViewModel.state.collectAsState()
    val reviewsState by viewModel.reviews.collectAsState()
    val cartCount = (cartState as? CartUiState.Ready)?.cart?.totalQuantity ?: 0

    LaunchedEffect(Unit) {
        if (cartState is CartUiState.Idle) cartViewModel.load()
    }

    ReviewsBottomSheet(
        state = reviewsState,
        onDismiss = viewModel::hideReviews,
        onRetry = {
            (reviewsState as? ReviewsUiState.Error)?.let { viewModel.showReviews(it.product) }
        },
    )

    Scaffold(
        containerColor = Color.Transparent,
        modifier = Modifier
            .fillMaxSize()
            .background(EduGradients.Background),
        topBar = {
            CenterAlignedTopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onOpenProfile) {
                        Icon(Icons.Outlined.PersonOutline, null, tint = EduColors.TextPrimary)
                    }
                },
                actions = {
                    CartIconWithBadge(count = cartCount, onClick = onOpenCart)
                    IconButton(onClick = onOpenOrders) {
                        Icon(Icons.Outlined.NotificationsNone, null, tint = EduColors.TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent),
            )
        },
        bottomBar = {
            MainBottomBar(
                selected = 0,
                onTabSelected = { index ->
                    when (index) {
                        1 -> onOpenOrders()
                        2 -> onOpenProfile()
                    }
                },
            )
        },
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    "EduMarketplace",
                    style = MaterialTheme.typography.displayLarge,
                    color = EduColors.TextPrimary,
                )
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                Surface(
                    shape = RoundedCornerShape(32.dp),
                    color = EduColors.White,
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    EduTextField(
                        value = search,
                        onValueChange = viewModel::onQueryChange,
                        placeholder = "Search courses, guides, or materials...",
                        leadingIcon = {
                            Icon(Icons.Outlined.Search, null, tint = EduColors.TextSecondary)
                        },
                    )
                }
            }
            when (val state = uiState) {
                is MarketplaceUiState.Loading -> item(span = { GridItemSpan(maxLineSpan) }) {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = EduColors.Primary)
                    }
                }
                is MarketplaceUiState.Error -> item(span = { GridItemSpan(maxLineSpan) }) {
                    Column(
                        Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            "Não foi possível carregar os produtos.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = EduColors.TextPrimary,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            state.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = EduColors.TextSecondary,
                        )
                        Spacer(Modifier.height(12.dp))
                        TextButton(onClick = { viewModel.load() }) {
                            Text("Tentar novamente", color = EduColors.Primary)
                        }
                    }
                }
                is MarketplaceUiState.Ready -> {
                    if (state.products.isEmpty()) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            EmptySearchResult(query = search)
                        }
                    } else {
                        items(state.products) { product ->
                            ProductCard(
                                category = product.subtype.uppercase().ifBlank { product.type.uppercase() },
                                title = product.name,
                                description = product.description,
                                price = formatBRL(product.price),
                                icon = iconFor(product.type),
                                imageUrl = product.imageUrl,
                                ratingAvg = product.ratingAvg,
                                ratingCount = product.ratingCount,
                                addEnabled = !cartBusy,
                                onAddToCart = { cartViewModel.addItem(product.id) },
                                onShowReviews = { viewModel.showReviews(product) },
                            )
                        }
                    }
                }
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
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptySearchResult(query: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            Icons.Outlined.SearchOff,
            contentDescription = null,
            tint = EduColors.TextSecondary,
            modifier = Modifier.size(56.dp),
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "Nenhum produto encontrado",
            style = MaterialTheme.typography.titleMedium,
            color = EduColors.TextPrimary,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(8.dp))
        val message = if (query.isBlank()) {
            "Tente buscar por outro termo."
        } else {
            "Não encontramos resultados para \"$query\". Tente outras palavras-chave."
        }
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = EduColors.TextSecondary,
            textAlign = TextAlign.Center,
        )
    }
}

private fun iconFor(type: String): ImageVector = when (type.lowercase()) {
    "apostila", "apostila_digital", "digital" -> Icons.AutoMirrored.Outlined.MenuBook
    else -> Icons.Outlined.AutoStories
}

@Composable
private fun ProductCard(
    category: String?,
    title: String,
    description: String,
    price: String,
    icon: ImageVector,
    imageUrl: String,
    ratingAvg: Double,
    ratingCount: Int,
    addEnabled: Boolean,
    onAddToCart: () -> Unit,
    onShowReviews: () -> Unit,
) {
    EduCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(12.dp),
        radius = 16.dp,
        shadow = 3.dp,
    ) {
        Column {
            Box(
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(EduColors.ImagePlaceholder),
                contentAlignment = Alignment.Center,
            ) {
                val placeholder: @Composable () -> Unit = {
                    Icon(
                        icon,
                        null,
                        tint = EduColors.TextSecondary.copy(alpha = 0.6f),
                        modifier = Modifier.size(48.dp),
                    )
                }
                if (imageUrl.isBlank()) {
                    placeholder()
                } else {
                    SubcomposeAsyncImage(
                        model = imageUrl,
                        contentDescription = title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        loading = { placeholder() },
                        error = { placeholder() },
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            if (category != null) {
                Text(
                    category,
                    color = EduColors.Purple,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    letterSpacing = 1.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(4.dp))
            }
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                color = EduColors.TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = EduColors.TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(8.dp))
            RatingStars(
                rating = ratingAvg,
                count = ratingCount,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(enabled = ratingCount > 0, onClick = onShowReviews),
            )
            Spacer(Modifier.height(10.dp))
            Text(
                price,
                style = MaterialTheme.typography.titleLarge,
                color = EduColors.TextPrimary,
                maxLines = 1,
            )
            Spacer(Modifier.height(10.dp))
            AddToCartButton(
                enabled = addEnabled,
                onAddToCart = onAddToCart,
            )
        }
    }
}

@Composable
private fun AddToCartButton(
    enabled: Boolean,
    onAddToCart: () -> Unit,
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
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (added) EduColors.GreenSoft else EduColors.InputFill,
            contentColor = if (added) EduColors.GreenDark else EduColors.TextPrimary,
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
                    Text("Adicionado", fontWeight = FontWeight.Bold)
                }
            } else {
                Text("+ Carrinho", fontWeight = FontWeight.Bold)
            }
        }
    }
}
