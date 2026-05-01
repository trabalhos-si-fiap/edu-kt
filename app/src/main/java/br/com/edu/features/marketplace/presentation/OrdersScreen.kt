package br.com.edu.features.marketplace.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.edu.core.theme.EduColors
import br.com.edu.core.theme.EduGradients
import br.com.edu.core.ui.EduCard
import br.com.edu.core.ui.EduSoftButton
import br.com.edu.core.ui.MainBottomBar
import br.com.edu.features.orders.domain.Order
import br.com.edu.features.orders.domain.OrderItem
import br.com.edu.features.orders.presentation.OrdersAction
import br.com.edu.features.orders.presentation.OrdersUiState
import br.com.edu.features.orders.presentation.OrdersViewModel
import coil.compose.SubcomposeAsyncImage
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(
    onBack: () -> Unit,
    onOpenCheckout: () -> Unit = {},
    onOpenMarketplace: () -> Unit = {},
    onOpenSupport: () -> Unit = {},
    onOpenProfile: () -> Unit = {},
    viewModel: OrdersViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()
    val action by viewModel.action.collectAsState()
    val snackbarHost = remember { SnackbarHostState() }
    var reviewingOrder by remember { mutableStateOf<Order?>(null) }

    LaunchedEffect(Unit) { viewModel.load() }

    LaunchedEffect(action) {
        when (val a = action) {
            is OrdersAction.RebuySuccess -> {
                viewModel.consumeAction()
                onOpenCheckout()
            }
            is OrdersAction.ReviewsSubmitted -> {
                snackbarHost.showSnackbar("Avaliações enviadas. Obrigado!")
                viewModel.consumeAction()
            }
            is OrdersAction.Error -> {
                snackbarHost.showSnackbar(a.message)
                viewModel.consumeAction()
            }
            null -> Unit
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        modifier = Modifier
            .fillMaxSize()
            .background(EduGradients.Background),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Seus pedidos",
                        style = MaterialTheme.typography.titleLarge,
                        color = EduColors.TextPrimary,
                        fontWeight = FontWeight.ExtraBold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, null, tint = EduColors.TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHost) },
        bottomBar = {
            MainBottomBar(
                selected = 1,
                onTabSelected = { index ->
                    when (index) {
                        0 -> onOpenMarketplace()
                        2 -> onOpenSupport()
                        3 -> onOpenProfile()
                    }
                },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
        ) {
            when (val s = state) {
                is OrdersUiState.Loading -> LoadingState()
                is OrdersUiState.Error -> ErrorState(message = s.message, onRetry = { viewModel.load() })
                is OrdersUiState.Ready -> OrdersList(
                    orders = s.orders,
                    onRebuy = { viewModel.rebuy(it.id) },
                    onReview = { reviewingOrder = it },
                )
            }
        }
    }

    reviewingOrder?.let { order ->
        ReviewOrderDialog(
            order = order,
            onDismiss = { reviewingOrder = null },
            onSubmit = { ratings ->
                viewModel.submitReviews(ratings)
                reviewingOrder = null
            },
        )
    }
}

@Composable
private fun LoadingState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = EduColors.Purple)
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            "Não foi possível carregar seus pedidos",
            style = MaterialTheme.typography.titleMedium,
            color = EduColors.TextPrimary,
            fontWeight = FontWeight.ExtraBold,
        )
        Spacer(Modifier.height(8.dp))
        Text(message, style = MaterialTheme.typography.bodySmall, color = EduColors.TextSecondary)
        Spacer(Modifier.height(20.dp))
        EduSoftButton(text = "Tentar novamente", onClick = onRetry)
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            "Você ainda não fez nenhum pedido",
            style = MaterialTheme.typography.titleMedium,
            color = EduColors.TextPrimary,
            fontWeight = FontWeight.ExtraBold,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Quando comprar algo no marketplace, ele aparece aqui.",
            style = MaterialTheme.typography.bodySmall,
            color = EduColors.TextSecondary,
        )
    }
}

@Composable
private fun OrdersList(
    orders: List<Order>,
    onRebuy: (Order) -> Unit,
    onReview: (Order) -> Unit,
) {
    if (orders.isEmpty()) {
        EmptyState()
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        items(orders, key = { it.id }) { order ->
            DeliveredOrderCard(
                order = order,
                onRebuy = { onRebuy(order) },
                onReview = { onReview(order) },
            )
        }
    }
}

private fun formatOrderId(id: Int): String = "#EDU-${id.toString().padStart(6, '0')}"

private fun formatBrl(value: String): String {
    val normalized = value.replace(',', '.')
    val number = normalized.toBigDecimalOrNull() ?: return "R$ $value"
    val parts = number.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString().split('.')
    return "R$ ${parts[0]},${parts[1]}"
}

private fun formatOrderDate(iso: String): String {
    val parsers = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
        "yyyy-MM-dd'T'HH:mm:ssXXX",
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
    )
    val date = parsers.firstNotNullOfOrNull { pattern ->
        runCatching {
            SimpleDateFormat(pattern, Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }.parse(iso)
        }.getOrNull()
    } ?: return iso
    val out = SimpleDateFormat("d 'de' MMMM, yyyy", Locale("pt", "BR"))
    return out.format(date)
}

@Composable
private fun DeliveredOrderCard(
    order: Order,
    onRebuy: () -> Unit,
    onReview: () -> Unit,
) {
    EduCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(20.dp),
        color = EduColors.InputFill,
        shadow = 0.dp,
    ) {
        Column {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(
                        formatOrderId(order.id),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp,
                        color = EduColors.TextPrimary,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        formatOrderDate(order.createdAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = EduColors.TextSecondary,
                    )
                }
                Surface(color = EduColors.Success, shape = RoundedCornerShape(12.dp)) {
                    Text(
                        "ENTREGUE",
                        color = EduColors.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            ItemsCarousel(items = order.items)
            Spacer(Modifier.height(16.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    "${order.itemsCount} itens no pedido",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = EduColors.TextPrimary,
                )
                Text(
                    "Total: ${formatBrl(order.total)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = EduColors.TextSecondary,
                )
            }
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(1f)) {
                    EduSoftButton(
                        text = "Comprar\nnovamente",
                        onClick = onRebuy,
                        container = EduColors.PurpleSoft,
                        content = EduColors.Purple,
                    )
                }
                Spacer(Modifier.width(12.dp))
                Box(Modifier.weight(1f)) {
                    EduSoftButton(
                        text = "Avaliar\nitens",
                        onClick = onReview,
                        container = EduColors.White,
                        content = EduColors.TextPrimary,
                    )
                }
            }
        }
    }
}

@Composable
private fun ItemsCarousel(items: List<OrderItem>) {
    if (items.isEmpty()) return
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 0.dp),
    ) {
        items(items, key = { it.productId }) { item ->
            CarouselItemCard(item = item)
        }
    }
}

@Composable
private fun CarouselItemCard(item: OrderItem) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = EduColors.White,
        shadowElevation = 0.dp,
        modifier = Modifier
            .width(180.dp)
            .height(232.dp),
    ) {
        Column(Modifier.padding(12.dp).fillMaxSize()) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(EduColors.CartImageBlue),
                contentAlignment = Alignment.Center,
            ) {
                val placeholder: @Composable () -> Unit = {
                    Icon(
                        Icons.AutoMirrored.Outlined.MenuBook,
                        null,
                        tint = EduColors.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(36.dp),
                    )
                }
                if (item.imageUrl.isBlank()) {
                    placeholder()
                } else {
                    SubcomposeAsyncImage(
                        model = item.imageUrl,
                        contentDescription = item.productName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        loading = { placeholder() },
                        error = { placeholder() },
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                item.productName,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = EduColors.TextPrimary,
                minLines = 2,
                maxLines = 2,
            )
            Spacer(Modifier.weight(1f))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.Star,
                    null,
                    tint = Color(0xFFF59E0B),
                    modifier = Modifier.size(14.dp),
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    if (item.ratingCount == 0) "Sem avaliações"
                    else String.format(Locale("pt", "BR"), "%.1f (%d)", item.ratingAvg, item.ratingCount),
                    style = MaterialTheme.typography.bodySmall,
                    color = EduColors.TextSecondary,
                )
            }
            Spacer(Modifier.height(6.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "x${item.quantity}",
                    style = MaterialTheme.typography.bodySmall,
                    color = EduColors.TextSecondary,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    formatBrl(item.unitPrice),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = EduColors.TextPrimary,
                )
            }
        }
    }
}

@Composable
private fun ReviewOrderDialog(
    order: Order,
    onDismiss: () -> Unit,
    onSubmit: (Map<Int, Int>) -> Unit,
) {
    val ratings = remember(order.id) { mutableStateMapOf<Int, Int>() }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Avaliar itens") },
        text = {
            Column {
                Text(
                    "Toque nas estrelas para avaliar cada item.",
                    style = MaterialTheme.typography.bodySmall,
                    color = EduColors.TextSecondary,
                )
                Spacer(Modifier.height(12.dp))
                order.items.forEach { item ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            item.productName,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyMedium,
                            color = EduColors.TextPrimary,
                            maxLines = 2,
                        )
                        Spacer(Modifier.width(8.dp))
                        StarPicker(
                            value = ratings[item.productId] ?: 0,
                            onChange = { ratings[item.productId] = it },
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = ratings.values.any { it in 1..5 },
                onClick = { onSubmit(ratings.toMap()) },
            ) { Text("Enviar", color = EduColors.Purple) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
    )
}

@Composable
private fun StarPicker(value: Int, onChange: (Int) -> Unit) {
    Row {
        for (i in 1..5) {
            val filled = i <= value
            Icon(
                imageVector = if (filled) Icons.Outlined.Star else Icons.Outlined.StarBorder,
                contentDescription = "Nota $i",
                tint = if (filled) Color(0xFFF59E0B) else EduColors.TextSecondary,
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onChange(i) },
            )
        }
    }
}
