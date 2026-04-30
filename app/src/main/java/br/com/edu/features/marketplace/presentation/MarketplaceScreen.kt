package br.com.edu.features.marketplace.presentation

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.DashboardCustomize
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ShoppingCart
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.edu.core.theme.EduColors
import br.com.edu.core.theme.EduGradients
import br.com.edu.core.ui.EduCard
import br.com.edu.core.ui.EduPurpleButton
import br.com.edu.core.ui.EduSoftButton
import br.com.edu.core.ui.EduTextField
import br.com.edu.core.ui.MainBottomBar
import br.com.edu.features.cart.presentation.CartViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplaceScreen(
    onOpenCart: () -> Unit,
    onOpenOrders: () -> Unit,
    onBack: () -> Unit = {},
    viewModel: MarketplaceViewModel = viewModel(),
    cartViewModel: CartViewModel = CartViewModel.get(),
) {
    var search by remember { mutableStateOf("") }
    val uiState by viewModel.state.collectAsState()
    val cartBusy by cartViewModel.busy.collectAsState()

    Scaffold(
        containerColor = EduColors.White,
        topBar = {
            CenterAlignedTopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Outlined.PersonOutline, null, tint = EduColors.TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = onOpenCart) {
                        Icon(Icons.Outlined.ShoppingCart, null, tint = EduColors.TextPrimary)
                    }
                    IconButton(onClick = onOpenOrders) {
                        Icon(Icons.Outlined.NotificationsNone, null, tint = EduColors.TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = EduColors.White),
            )
        },
        bottomBar = {
            MainBottomBar(
                selected = 0,
                onTabSelected = { if (it == 1) onOpenOrders() },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(EduColors.White),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            item {
                Text(
                    "EduMarketplace",
                    style = MaterialTheme.typography.displayLarge,
                    color = EduColors.TextPrimary,
                )
            }
            item {
                Surface(
                    shape = RoundedCornerShape(32.dp),
                    color = EduColors.White,
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    EduTextField(
                        value = search,
                        onValueChange = { search = it },
                        placeholder = "Search courses, guides, or materials...",
                        leadingIcon = {
                            Icon(Icons.Outlined.Search, null, tint = EduColors.TextSecondary)
                        },
                    )
                }
            }
            item { FeaturedCard(onExplore = {}) }

            when (val state = uiState) {
                is MarketplaceUiState.Loading -> item {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = EduColors.Primary)
                    }
                }
                is MarketplaceUiState.Error -> item {
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
                is MarketplaceUiState.Ready -> items(state.products) { product ->
                    ProductCard(
                        category = product.subtype.uppercase().ifBlank { product.type.uppercase() },
                        title = product.name,
                        description = product.description,
                        price = formatPrice(product.price),
                        icon = iconFor(product.type),
                        addEnabled = !cartBusy,
                        onAddToCart = { cartViewModel.addItem(product.id) },
                    )
                }
            }
        }
    }
}

private fun formatPrice(raw: String): String {
    val value = raw.toDoubleOrNull() ?: return "R$ $raw"
    return "R$ %,.2f".format(value).replace(',', 'X').replace('.', ',').replace('X', '.')
}

private fun iconFor(type: String): ImageVector = when (type.lowercase()) {
    "apostila", "apostila_digital", "digital" -> Icons.AutoMirrored.Outlined.MenuBook
    else -> Icons.Outlined.AutoStories
}

@Composable
private fun FeaturedCard(onExplore: () -> Unit) {
    EduCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(28.dp),
        color = EduColors.Primary,
        shadow = 8.dp,
    ) {
        Column {
            Text(
                "EDUCAÇÃO 5.0",
                color = EduColors.Success,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                letterSpacing = 1.5.sp,
            )
            Spacer(Modifier.height(18.dp))
            Text(
                "Potencialize sua\njornada cognitiva.",
                style = MaterialTheme.typography.displayLarge,
                color = EduColors.White,
                fontWeight = FontWeight.ExtraBold,
            )
            Spacer(Modifier.height(18.dp))
            Text(
                "Materiais selecionados que combinam pesquisa, prática e tecnologia.",
                color = EduColors.White.copy(alpha = 0.65f),
                style = MaterialTheme.typography.bodyLarge,
            )
            Spacer(Modifier.height(28.dp))
            EduPurpleButton(text = "Explorar Coleção", onClick = onExplore)
            Spacer(Modifier.height(24.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 10f)
                    .background(EduGradients.OverlayDark, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Outlined.DashboardCustomize,
                    null,
                    tint = EduColors.Cyan,
                    modifier = Modifier.size(64.dp),
                )
            }
        }
    }
}

@Composable
private fun ProductCard(
    category: String?,
    title: String,
    description: String,
    price: String,
    icon: ImageVector,
    addEnabled: Boolean,
    onAddToCart: () -> Unit,
) {
    EduCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        radius = 20.dp,
        shadow = 4.dp,
    ) {
        Column {
            Box(
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 11f)
                    .background(EduColors.ImagePlaceholder, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    icon,
                    null,
                    tint = EduColors.TextSecondary.copy(alpha = 0.6f),
                    modifier = Modifier.size(64.dp),
                )
            }
            Spacer(Modifier.height(16.dp))
            if (category != null) {
                Text(
                    category,
                    color = EduColors.Purple,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 1.2.sp,
                )
                Spacer(Modifier.height(8.dp))
            }
            Text(title, style = MaterialTheme.typography.titleLarge, color = EduColors.TextPrimary)
            Spacer(Modifier.height(10.dp))
            Text(description, style = MaterialTheme.typography.bodyMedium, color = EduColors.TextSecondary)
            Spacer(Modifier.height(14.dp))
            Text(price, style = MaterialTheme.typography.titleLarge, color = EduColors.TextPrimary)
            Spacer(Modifier.height(16.dp))
            EduSoftButton(
                text = "+ Carrinho",
                onClick = { if (addEnabled) onAddToCart() },
            )
        }
    }
}
