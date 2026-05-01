package br.com.edu.features.marketplace.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.edu.core.theme.EduColors
import br.com.edu.core.theme.EduGradients
import br.com.edu.core.ui.EduCard
import br.com.edu.core.ui.formatBRL
import br.com.edu.features.cart.presentation.CartUiState
import br.com.edu.features.cart.presentation.CartViewModel
import coil.compose.SubcomposeAsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: Int,
    onBack: () -> Unit,
    onOpenProfile: () -> Unit = {},
    onOpenCart: () -> Unit = {},
    onSearchInteract: () -> Unit = {},
    marketplaceViewModel: MarketplaceViewModel = viewModel(),
    viewModel: ProductDetailViewModel = viewModel(
        key = "product-detail-$productId",
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                ProductDetailViewModel(productId) as T
        },
    ),
    cartViewModel: CartViewModel = CartViewModel.get(),
) {
    val state by viewModel.state.collectAsState()
    val cartBusy by cartViewModel.busy.collectAsState()
    val cartState by cartViewModel.state.collectAsState()
    val cartCount = (cartState as? CartUiState.Ready)?.cart?.totalQuantity ?: 0

    val search by marketplaceViewModel.query.collectAsState()
    val selectedType by marketplaceViewModel.selectedType.collectAsState()
    val marketplaceState by marketplaceViewModel.state.collectAsState()
    val availableTypes = remember(marketplaceState) {
        (marketplaceState as? MarketplaceUiState.Ready)
            ?.products
            ?.map { it.type }
            ?.filter { it.isNotBlank() }
            ?.distinct()
            ?: emptyList()
    }

    Scaffold(
        containerColor = Color.Transparent,
        modifier = Modifier
            .fillMaxSize()
            .background(EduGradients.Background),
        topBar = {
            MarketplaceTopBar(
                search = search,
                onSearchChange = { value ->
                    marketplaceViewModel.onQueryChange(value)
                    onSearchInteract()
                },
                selectedType = selectedType,
                availableTypes = availableTypes,
                onTypeSelected = { type ->
                    marketplaceViewModel.onTypeSelected(type)
                    onSearchInteract()
                },
                cartCount = cartCount,
                onOpenProfile = onOpenProfile,
                onOpenCart = onOpenCart,
            )
        },
        bottomBar = {
            (state as? ProductDetailUiState.Ready)?.let { ready ->
                Surface(
                    color = EduColors.White.copy(alpha = 0.94f),
                    shadowElevation = 6.dp,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 24.dp, vertical = 12.dp),
                    ) {
                        AddToCartButton(
                            enabled = !cartBusy,
                            onAddToCart = { cartViewModel.addItem(ready.product.id) },
                            label = "Adicionar ao carrinho",
                            minHeight = 52.dp,
                            idleContainerColor = EduColors.Blue,
                            idleContentColor = EduColors.White,
                        )
                    }
                }
            }
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
        ) {
            when (val s = state) {
                is ProductDetailUiState.Loading -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = EduColors.Primary)
                }
                is ProductDetailUiState.Error -> Column(
                    Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        "Não foi possível abrir o produto.",
                        style = MaterialTheme.typography.titleMedium,
                        color = EduColors.TextPrimary,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        s.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = EduColors.TextSecondary,
                    )
                    Spacer(Modifier.height(12.dp))
                    TextButton(onClick = { viewModel.load() }) {
                        Text("Tentar novamente", color = EduColors.Primary)
                    }
                }
                is ProductDetailUiState.Ready -> ProductDetailContent(
                    state = s,
                    onRetryReviews = viewModel::retryReviews,
                )
            }
        }
    }
}

@Composable
private fun ProductDetailContent(
    state: ProductDetailUiState.Ready,
    onRetryReviews: () -> Unit,
) {
    val product = state.product
    val category = product.subtype.uppercase().ifBlank { product.type.uppercase() }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            HeroImage(imageUrl = product.imageUrl, fallbackIcon = iconForDetail(product.type), title = product.name)
        }
        item {
            CategoryTag(text = category)
        }
        item {
            EduCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(20.dp),
                radius = 20.dp,
                shadow = 3.dp,
            ) {
                Column {
                    Text(
                        product.name,
                        style = MaterialTheme.typography.headlineMedium,
                        color = EduColors.TextPrimary,
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        RatingStars(
                            rating = product.ratingAvg,
                            count = product.ratingCount,
                            starSize = 18.dp,
                        )
                        Text(
                            formatBRL(product.price),
                            style = MaterialTheme.typography.titleLarge,
                            color = EduColors.Purple,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
        item {
            EduCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(20.dp),
                radius = 20.dp,
                shadow = 3.dp,
            ) {
                Column {
                    Text(
                        "Sobre o produto",
                        style = MaterialTheme.typography.titleMedium,
                        color = EduColors.TextPrimary,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        product.description.ifBlank { "Sem descrição disponível." },
                        style = MaterialTheme.typography.bodyMedium,
                        color = EduColors.TextSecondary,
                    )
                }
            }
        }
        item {
            Text(
                if (product.ratingCount > 0) "Avaliações (${product.ratingCount})" else "Avaliações",
                style = MaterialTheme.typography.titleMedium,
                color = EduColors.TextPrimary,
                fontWeight = FontWeight.Bold,
            )
        }
        when {
            state.reviewsLoading -> item {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = EduColors.Primary)
                }
            }
            state.reviewsError != null -> item {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(state.reviewsError, color = EduColors.TextSecondary)
                    TextButton(onClick = onRetryReviews) {
                        Text("Tentar novamente", color = EduColors.Primary)
                    }
                }
            }
            state.reviews.isEmpty() -> item {
                Text(
                    "Ainda não há avaliações.",
                    style = MaterialTheme.typography.bodySmall,
                    color = EduColors.TextSecondary,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            }
            else -> items(state.reviews, key = { it.id }) { review ->
                ReviewItem(review)
            }
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun HeroImage(imageUrl: String, fallbackIcon: ImageVector, title: String) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(280.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(EduColors.ImagePlaceholder),
        contentAlignment = Alignment.Center,
    ) {
        val placeholder: @Composable () -> Unit = {
            Icon(
                fallbackIcon,
                null,
                tint = EduColors.TextSecondary.copy(alpha = 0.6f),
                modifier = Modifier.size(72.dp),
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
}

@Composable
private fun CategoryTag(text: String) {
    if (text.isBlank()) return
    Surface(
        color = EduColors.PurpleSoft,
        shape = RoundedCornerShape(8.dp),
    ) {
        Text(
            text,
            color = EduColors.Purple,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
        )
    }
}

private fun iconForDetail(type: String): ImageVector = when (type.lowercase()) {
    "apostila", "apostila_digital", "digital" -> Icons.AutoMirrored.Outlined.MenuBook
    else -> Icons.Outlined.AutoStories
}

