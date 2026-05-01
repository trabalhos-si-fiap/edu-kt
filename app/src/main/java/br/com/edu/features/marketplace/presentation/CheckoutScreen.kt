package br.com.edu.features.marketplace.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Pix
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.SubcomposeAsyncImage
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
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.edu.core.theme.EduColors
import br.com.edu.core.theme.EduGradients
import br.com.edu.core.ui.DottedBorderBox
import br.com.edu.core.ui.EduPurpleButton
import br.com.edu.core.ui.formatBRL
import br.com.edu.features.cart.domain.Cart
import br.com.edu.features.cart.domain.CartItem
import br.com.edu.features.cart.presentation.CartUiState
import br.com.edu.features.cart.presentation.CartViewModel
import br.com.edu.features.orders.data.OrdersRepository
import br.com.edu.features.payment.domain.PaymentMethod
import br.com.edu.features.payment.domain.PaymentMethodType
import br.com.edu.features.payment.presentation.PaymentMethodViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    onBack: () -> Unit,
    onAddPaymentMethod: () -> Unit,
    onEditPaymentMethod: (String) -> Unit = {},
    cartViewModel: CartViewModel = CartViewModel.get(),
    paymentViewModel: PaymentMethodViewModel = PaymentMethodViewModel.get(),
) {
    val cartState by cartViewModel.state.collectAsState()
    val busy by cartViewModel.busy.collectAsState()
    val paymentMethods by paymentViewModel.methods.collectAsState()
    var selectedPaymentId by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingDelete by remember { mutableStateOf<PaymentMethod?>(null) }
    var showFinalizeDialog by remember { mutableStateOf(false) }
    val snackbarHost = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val ordersRepository = remember { OrdersRepository() }
    var placingOrder by remember { mutableStateOf(false) }
    val cart = (cartState as? CartUiState.Ready)?.cart
    val canFinalize = !busy && !placingOrder &&
        cart != null && cart.items.isNotEmpty() &&
        selectedPaymentId != null
    val selectedMethod = paymentMethods.firstOrNull { it.id == selectedPaymentId }

    LaunchedEffect(paymentMethods) {
        val current = selectedPaymentId
        val stillExists = current != null && paymentMethods.any { it.id == current }
        if (!stillExists) {
            selectedPaymentId = paymentMethods.firstOrNull { it.isDefault }?.id
                ?: paymentMethods.firstOrNull()?.id
        }
    }

    LaunchedEffect(Unit) { cartViewModel.load() }

    Scaffold(
        containerColor = Color.Transparent,
        modifier = Modifier
            .fillMaxSize()
            .background(EduGradients.Background),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Finalizar Pedido",
                        style = MaterialTheme.typography.titleMedium,
                        color = EduColors.TextPrimary,
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
            Surface(
                color = EduColors.White,
                shadowElevation = 12.dp,
            ) {
                Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp)) {
                    if (cart != null && cart.items.isNotEmpty()) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                "Total",
                                style = MaterialTheme.typography.bodyMedium,
                                color = EduColors.TextSecondary,
                            )
                            Text(
                                formatBRL(cart.total),
                                style = MaterialTheme.typography.titleLarge,
                                color = EduColors.TextPrimary,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                    }
                    EduPurpleButton(
                        text = if (busy || placingOrder) "Processando..." else "Finalizar Pedido",
                        onClick = {
                            when {
                                cart == null || cart.items.isEmpty() ->
                                    scope.launch { snackbarHost.showSnackbar("Adicione itens ao carrinho.") }
                                selectedPaymentId == null ->
                                    scope.launch { snackbarHost.showSnackbar("Selecione um método de pagamento.") }
                                else -> showFinalizeDialog = true
                            }
                        },
                        enabled = canFinalize,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        },
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 8.dp),
        ) {
            Text(
                "Revisão do Carrinho",
                style = MaterialTheme.typography.headlineMedium,
                color = EduColors.TextPrimary,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Confirme os itens selecionados",
                style = MaterialTheme.typography.bodyLarge,
                color = EduColors.TextSecondary,
            )
            Spacer(Modifier.height(20.dp))

            when (val s = cartState) {
                is CartUiState.Idle, is CartUiState.Loading -> {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = EduColors.Primary)
                    }
                }
                is CartUiState.Error -> {
                    Column(Modifier.fillMaxWidth().padding(16.dp)) {
                        Text(
                            "Não foi possível carregar o carrinho.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = EduColors.TextPrimary,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(s.message, color = EduColors.TextSecondary)
                        Spacer(Modifier.height(12.dp))
                        TextButton(onClick = { cartViewModel.load() }) {
                            Text("Tentar novamente", color = EduColors.Primary)
                        }
                    }
                }
                is CartUiState.Ready -> CartItemsList(
                    cart = s.cart,
                    busy = busy,
                    onIncrement = { cartViewModel.addItem(it.productId) },
                    onDecrement = { cartViewModel.decrementItem(it.productId) },
                    onRemoveAll = { cartViewModel.removeAll(it.productId) },
                )
            }

            Spacer(Modifier.height(32.dp))

            Text(
                "Método de Pagamento",
                style = MaterialTheme.typography.titleLarge,
                color = EduColors.TextPrimary,
            )
            Spacer(Modifier.height(16.dp))
            if (paymentMethods.isEmpty()) {
                Text(
                    "Você ainda não cadastrou nenhum método de pagamento.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = EduColors.TextSecondary,
                    modifier = Modifier.padding(bottom = 12.dp),
                )
            } else {
                paymentMethods.forEachIndexed { idx, method ->
                    if (idx > 0) Spacer(Modifier.height(12.dp))
                    PaymentMethodCard(
                        method = method,
                        selected = selectedPaymentId == method.id,
                        onClick = { selectedPaymentId = method.id },
                        onEdit = { onEditPaymentMethod(method.id) },
                        onDelete = { pendingDelete = method },
                        onToggleDefault = {
                            if (!method.isDefault) paymentViewModel.setDefault(method.id)
                        },
                    )
                }
                Spacer(Modifier.height(12.dp))
            }
            DottedBorderBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAddPaymentMethod() },
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Outlined.Add, null, tint = EduColors.TextSecondary)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Outro método",
                        color = EduColors.TextSecondary,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    pendingDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Remover método") },
            text = { Text("Deseja remover ${paymentTitle(target)}?") },
            confirmButton = {
                TextButton(onClick = {
                    paymentViewModel.delete(target.id)
                    pendingDelete = null
                }) { Text("Remover", color = EduColors.Danger) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("Cancelar") }
            },
        )
    }

    if (showFinalizeDialog && cart != null && selectedMethod != null) {
        AlertDialog(
            onDismissRequest = { showFinalizeDialog = false },
            title = { Text("Confirmar pedido") },
            text = {
                Text(
                    "Total: ${formatBRL(cart.total)}\n" +
                        "Pagamento: ${paymentTitle(selectedMethod)}",
                )
            },
            confirmButton = {
                TextButton(
                    enabled = !placingOrder,
                    onClick = {
                        showFinalizeDialog = false
                        placingOrder = true
                        scope.launch {
                            val result = runCatching { ordersRepository.placeOrder() }
                            placingOrder = false
                            result
                                .onSuccess {
                                    cartViewModel.load()
                                    snackbarHost.showSnackbar("Pedido finalizado com sucesso!")
                                    onBack()
                                }
                                .onFailure {
                                    snackbarHost.showSnackbar(
                                        it.message ?: "Não foi possível finalizar o pedido.",
                                    )
                                }
                        }
                    },
                ) { Text("Confirmar", color = EduColors.Purple) }
            },
            dismissButton = {
                TextButton(onClick = { showFinalizeDialog = false }) { Text("Cancelar") }
            },
        )
    }
}

private fun paymentTitle(m: PaymentMethod): String = when (m.type) {
    PaymentMethodType.CREDIT_CARD -> "${m.cardBrand ?: "Cartão"} •••• ${m.cardLast4 ?: "----"}"
    PaymentMethodType.PIX -> "PIX"
    PaymentMethodType.BOLETO -> "Boleto"
}

private fun paymentSubtitle(m: PaymentMethod): String = when (m.type) {
    PaymentMethodType.CREDIT_CARD -> {
        val parts = listOfNotNull(
            m.cardholderName?.takeIf { it.isNotBlank() },
            m.cardExpiry?.takeIf { it.length == 4 }?.let { "Validade ${it.substring(0, 2)}/${it.substring(2)}" },
        )
        if (parts.isEmpty()) "Cartão de crédito" else parts.joinToString(" • ")
    }
    PaymentMethodType.PIX -> {
        val key = m.pixKey.orEmpty()
        if (key.length > 12) key.take(8) + "…" else key.ifBlank { "Aprovação imediata" }
    }
    PaymentMethodType.BOLETO -> "Compensação em até 2 dias úteis"
}

@Composable
private fun PaymentMethodCard(
    method: PaymentMethod,
    selected: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleDefault: () -> Unit,
) {
    val (icon, tint) = when (method.type) {
        PaymentMethodType.CREDIT_CARD -> Icons.Outlined.CreditCard to EduColors.Purple
        PaymentMethodType.PIX -> Icons.Outlined.Pix to EduColors.GreenDark
        PaymentMethodType.BOLETO -> Icons.Outlined.ReceiptLong to EduColors.TextSecondary
    }
    val border = if (selected) BorderStroke(2.dp, EduColors.Purple) else BorderStroke(0.dp, Color.Transparent)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(border, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        color = if (selected) EduColors.White else EduColors.InputFill,
        shadowElevation = if (selected) 2.dp else 0.dp,
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(48.dp)
                        .background(EduColors.White, RoundedCornerShape(12.dp))
                        .border(1.dp, EduColors.InputBorder, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center,
                ) { Icon(icon, null, tint = tint) }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            paymentTitle(method),
                            style = MaterialTheme.typography.titleMedium,
                            color = EduColors.TextPrimary,
                        )
                        if (method.isDefault) {
                            Spacer(Modifier.width(8.dp))
                            Surface(
                                color = EduColors.PurpleSoft,
                                shape = RoundedCornerShape(20.dp),
                            ) {
                                Text(
                                    "PADRÃO",
                                    color = EduColors.Purple,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(
                        paymentSubtitle(method),
                        style = MaterialTheme.typography.bodySmall,
                        color = EduColors.TextSecondary,
                    )
                }
                if (selected) {
                    Box(
                        Modifier
                            .size(24.dp)
                            .background(EduColors.Purple, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Outlined.Check, null, tint = EduColors.White, modifier = Modifier.size(16.dp))
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onToggleDefault, modifier = Modifier.size(36.dp)) {
                    Icon(
                        if (method.isDefault) Icons.Outlined.Star else Icons.Outlined.StarBorder,
                        contentDescription = "Definir como padrão",
                        tint = if (method.isDefault) EduColors.Purple else EduColors.TextSecondary,
                        modifier = Modifier.size(20.dp),
                    )
                }
                IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.Outlined.Edit,
                        contentDescription = "Editar",
                        tint = EduColors.TextSecondary,
                        modifier = Modifier.size(20.dp),
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.Outlined.Delete,
                        contentDescription = "Remover",
                        tint = EduColors.Danger,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun CartItemsList(
    cart: Cart,
    busy: Boolean,
    onIncrement: (CartItem) -> Unit,
    onDecrement: (CartItem) -> Unit,
    onRemoveAll: (CartItem) -> Unit,
) {
    if (cart.items.isEmpty()) {
        Text(
            "Seu carrinho está vazio.",
            style = MaterialTheme.typography.bodyLarge,
            color = EduColors.TextSecondary,
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        )
        return
    }
    cart.items.forEachIndexed { idx, item ->
        if (idx > 0) Spacer(Modifier.height(16.dp))
        CartItemCard(
            item = item,
            enabled = !busy,
            onIncrement = { onIncrement(item) },
            onDecrement = { onDecrement(item) },
            onRemoveAll = { onRemoveAll(item) },
        )
    }
    Spacer(Modifier.height(16.dp))
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text("Total", style = MaterialTheme.typography.titleLarge, color = EduColors.TextPrimary)
        Text(
            formatBRL(cart.total),
            style = MaterialTheme.typography.titleLarge,
            color = EduColors.TextPrimary,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun CartItemCard(
    item: CartItem,
    enabled: Boolean,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onRemoveAll: () -> Unit,
) {
    val (categoryBg, categoryFg) = colorsFor(item.type)
    val icon = iconFor(item.type)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = EduColors.White,
        shadowElevation = 4.dp,
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    Modifier
                        .size(88.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(EduColors.CartImageBlue),
                    contentAlignment = Alignment.Center,
                ) {
                    val placeholder: @Composable () -> Unit = {
                        Icon(
                            icon,
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
                            contentDescription = item.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                            loading = { placeholder() },
                            error = { placeholder() },
                        )
                    }
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.Top) {
                        Surface(
                            color = categoryBg,
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.weight(1f, fill = false),
                        ) {
                            Text(
                                item.subtype.uppercase().ifBlank { item.type.uppercase() },
                                color = categoryFg,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                maxLines = 1,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            )
                        }
                        Spacer(Modifier.weight(1f))
                        IconButton(
                            onClick = { if (enabled) onRemoveAll() },
                            modifier = Modifier.size(28.dp),
                        ) {
                            Icon(
                                Icons.Outlined.Close,
                                contentDescription = "Excluir item",
                                tint = EduColors.TextSecondary,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        item.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = EduColors.TextPrimary,
                        maxLines = 2,
                    )
                    Spacer(Modifier.height(4.dp))
                    RatingStars(
                        rating = item.ratingAvg,
                        count = item.ratingCount,
                        starSize = 12.dp,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "${formatBRL(item.price)} cada",
                        style = MaterialTheme.typography.bodySmall,
                        color = EduColors.TextSecondary,
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                QuantityStepper(
                    quantity = item.quantity,
                    enabled = enabled,
                    onIncrement = onIncrement,
                    onDecrement = onDecrement,
                )
                Text(
                    formatBRL(item.subtotal),
                    style = MaterialTheme.typography.titleLarge,
                    color = EduColors.TextPrimary,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun QuantityStepper(
    quantity: Int,
    enabled: Boolean,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = EduColors.InputFill,
    ) {
        Row(
            Modifier.heightIn(min = 36.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = { if (enabled) onDecrement() },
                modifier = Modifier.size(36.dp),
            ) {
                Icon(
                    Icons.Outlined.Remove,
                    contentDescription = "Remover 1",
                    tint = EduColors.Danger,
                    modifier = Modifier.size(18.dp),
                )
            }
            Text(
                quantity.toString(),
                color = EduColors.TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 12.dp),
            )
            IconButton(
                onClick = { if (enabled) onIncrement() },
                modifier = Modifier.size(36.dp),
            ) {
                Icon(
                    Icons.Outlined.Add,
                    contentDescription = "Adicionar 1",
                    tint = EduColors.Purple,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

private fun iconFor(type: String): ImageVector = when (type.lowercase()) {
    "apostila", "apostila_digital", "digital" -> Icons.AutoMirrored.Outlined.MenuBook
    else -> Icons.Outlined.Insights
}

private fun colorsFor(type: String): Pair<Color, Color> = when (type.lowercase()) {
    "apostila", "apostila_digital", "digital" -> EduColors.PurpleSoft to EduColors.Purple
    else -> EduColors.GreenSoft to EduColors.GreenDark
}

@Composable
private fun PaymentOption(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val border = if (selected) BorderStroke(2.dp, EduColors.Purple) else BorderStroke(0.dp, Color.Transparent)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(border, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        color = if (selected) EduColors.White else EduColors.InputFill,
        shadowElevation = if (selected) 2.dp else 0.dp,
    ) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(48.dp)
                    .background(EduColors.White, RoundedCornerShape(12.dp))
                    .border(1.dp, EduColors.InputBorder, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, null, tint = iconTint)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = EduColors.TextPrimary)
                Spacer(Modifier.height(2.dp))
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = EduColors.TextSecondary)
            }
            if (selected) {
                Box(
                    Modifier
                        .size(24.dp)
                        .background(EduColors.Purple, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Outlined.Check, null, tint = EduColors.White, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
