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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.PersonOutline
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.edu.core.theme.EduColors
import br.com.edu.core.ui.EduCard
import br.com.edu.core.ui.EduSoftButton

enum class OrderStep { Picking, Transit, Delivered }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(onBack: () -> Unit) {
    Scaffold(
        containerColor = EduColors.White,
        topBar = {
            CenterAlignedTopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, null, tint = EduColors.TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Outlined.PersonOutline, null, tint = EduColors.TextPrimary)
                    }
                    IconButton(onClick = {}) {
                        Icon(Icons.Outlined.NotificationsNone, null, tint = EduColors.TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = EduColors.White),
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
                    "Seus pedidos",
                    style = MaterialTheme.typography.displayLarge,
                    color = EduColors.TextPrimary,
                )
            }
            item {
                ActiveOrderCard(
                    id = "#EDU-882910",
                    total = "R\$242",
                    purchaseDate = "22 de abril, 2026",
                    deliveryEstimate = "Entrega prevista para 27/04",
                    locationInfo = "Atualmente no Centro de Distribuição em Cajamar",
                    currentStep = OrderStep.Transit,
                )
            }
            item {
                DeliveredOrderCard(
                    id = "#EDU-881204",
                    date = "12 de setembro, 2023",
                    itemsCount = 4,
                    total = "R\$ 128,00",
                )
            }
        }
    }
}

@Composable
private fun ActiveOrderCard(
    id: String,
    total: String,
    purchaseDate: String,
    deliveryEstimate: String,
    locationInfo: String,
    currentStep: OrderStep,
) {
    EduCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(20.dp)) {
        Column {
            Surface(color = EduColors.PurpleSoft, shape = RoundedCornerShape(20.dp)) {
                Text(
                    "Pedido ativo",
                    color = EduColors.Purple,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                )
            }
            Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(id, style = MaterialTheme.typography.titleLarge, color = EduColors.TextPrimary, fontSize = 22.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(purchaseDate, style = MaterialTheme.typography.bodySmall, color = EduColors.TextSecondary)
                }
                Spacer(Modifier.width(12.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Text("Valor total", style = MaterialTheme.typography.bodySmall, color = EduColors.TextSecondary)
                    Spacer(Modifier.height(4.dp))
                    Text(total, fontWeight = FontWeight.ExtraBold, fontSize = 26.sp, color = EduColors.TextPrimary)
                }
            }
            Spacer(Modifier.height(24.dp))
            OrderStepper(currentStep)
            Spacer(Modifier.height(20.dp))
            Surface(color = EduColors.InputFill, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                    Icon(Icons.Outlined.Info, null, tint = EduColors.Purple, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(deliveryEstimate, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold), color = EduColors.TextPrimary)
                        Spacer(Modifier.height(4.dp))
                        Text(locationInfo, style = MaterialTheme.typography.bodySmall, color = EduColors.TextSecondary)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            EduSoftButton(text = "Detalhes do pedido", onClick = {})
        }
    }
}

@Composable
private fun OrderStepper(current: OrderStep) {
    val steps = listOf(
        Triple("SEPARAÇÃO", Icons.Outlined.Check, OrderStep.Picking),
        Triple("TRÂNSITO", Icons.Outlined.LocalShipping, OrderStep.Transit),
        Triple("ENTREGUE", Icons.Outlined.Home, OrderStep.Delivered),
    )
    val currentIdx = steps.indexOfFirst { it.third == current }

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            steps.forEachIndexed { i, (_, icon, _) ->
                val active = i <= currentIdx
                if (i > 0) {
                    Box(
                        Modifier
                            .weight(1f)
                            .height(2.dp)
                            .background(if (active) EduColors.Purple else EduColors.PurpleSoft),
                    )
                }
                Box(
                    Modifier
                        .size(36.dp)
                        .background(if (active) EduColors.Purple else EduColors.InputFill, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        icon,
                        null,
                        tint = if (active) EduColors.White else EduColors.TextSecondary,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Row {
            steps.forEachIndexed { i, (label, _, _) ->
                Text(
                    label,
                    color = if (i <= currentIdx) EduColors.TextPrimary else EduColors.TextSecondary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun DeliveredOrderCard(id: String, date: String, itemsCount: Int, total: String) {
    EduCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(20.dp),
        color = EduColors.InputFill,
        shadow = 0.dp,
    ) {
        Column {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(id, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, color = EduColors.TextPrimary)
                    Spacer(Modifier.height(4.dp))
                    Text(date, style = MaterialTheme.typography.bodySmall, color = EduColors.TextSecondary)
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                ItemThumb(Icons.Outlined.MenuBook, EduColors.Purple)
                Spacer(Modifier.width(8.dp))
                ItemThumb(Icons.Outlined.MenuBook, EduColors.GreenDark)
                Spacer(Modifier.width(8.dp))
                Box(
                    Modifier
                        .size(44.dp)
                        .background(EduColors.White, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("+2", fontWeight = FontWeight.Bold, color = EduColors.TextPrimary)
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(
                        "$itemsCount itens no pedido",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = EduColors.TextPrimary,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text("Total: $total", style = MaterialTheme.typography.bodySmall, color = EduColors.TextSecondary)
                }
            }
            Spacer(Modifier.height(16.dp))
            Row {
                Box(Modifier.weight(1f)) {
                    EduSoftButton(
                        text = "Comprar novamente",
                        onClick = {},
                        container = EduColors.PurpleSoft,
                        content = EduColors.Purple,
                    )
                }
                Spacer(Modifier.width(12.dp))
                Box(Modifier.weight(1f)) {
                    EduSoftButton(
                        text = "Avaliar itens",
                        onClick = {},
                        container = EduColors.White,
                        content = EduColors.TextPrimary,
                    )
                }
            }
        }
    }
}

@Composable
private fun ItemThumb(icon: ImageVector, color: Color) {
    Box(
        Modifier
            .size(44.dp)
            .background(color, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, null, tint = EduColors.White.copy(alpha = 0.85f), modifier = Modifier.size(20.dp))
    }
}
