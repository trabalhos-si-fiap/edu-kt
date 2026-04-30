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
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Payments
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.edu.core.theme.EduColors
import br.com.edu.core.ui.DottedBorderBox

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(onBack: () -> Unit, onAddPaymentMethod: () -> Unit) {
    var selectedPayment by remember { mutableIntStateOf(0) }

    Scaffold(
        containerColor = EduColors.White,
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
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = EduColors.White),
            )
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

            CartItemCard(
                category = "PREMIUM COURSE",
                categoryBg = EduColors.PurpleSoft,
                categoryFg = EduColors.Purple,
                title = "Mastering Data Synthesis",
                subtitle = "Education 5.0 Advanced Module",
                price = "R\$ 189,90",
                icon = Icons.Outlined.MenuBook,
                imageBg = EduColors.CartImageBlue,
            )
            Spacer(Modifier.height(16.dp))
            CartItemCard(
                category = "DIGITAL TOOL",
                categoryBg = EduColors.GreenSoft,
                categoryFg = EduColors.GreenDark,
                title = "Diagnostic AI Toolkit",
                subtitle = "Lifetime Access Key",
                price = "R\$ 45,00",
                icon = Icons.Outlined.Insights,
                imageBg = EduColors.CartImageDark,
            )
            Spacer(Modifier.height(32.dp))

            Text(
                "Método de Pagamento",
                style = MaterialTheme.typography.titleLarge,
                color = EduColors.TextPrimary,
            )
            Spacer(Modifier.height(16.dp))
            PaymentOption(
                icon = Icons.Outlined.CreditCard,
                iconTint = EduColors.Purple,
                title = "Cartão de Crédito",
                subtitle = "Final 4492 • Visa",
                selected = selectedPayment == 0,
                onClick = { selectedPayment = 0 },
            )
            Spacer(Modifier.height(12.dp))
            PaymentOption(
                icon = Icons.Outlined.Payments,
                iconTint = EduColors.GreenDark,
                title = "PIX",
                subtitle = "Aprovação imediata",
                selected = selectedPayment == 1,
                onClick = { selectedPayment = 1 },
            )
            Spacer(Modifier.height(12.dp))
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
}

@Composable
private fun CartItemCard(
    category: String,
    categoryBg: Color,
    categoryFg: Color,
    title: String,
    subtitle: String,
    price: String,
    icon: ImageVector,
    imageBg: Color,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = EduColors.White,
        shadowElevation = 4.dp,
    ) {
        Row(Modifier.padding(16.dp)) {
            Box(
                Modifier
                    .width(96.dp)
                    .height(110.dp)
                    .background(imageBg, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, null, tint = EduColors.White.copy(alpha = 0.85f), modifier = Modifier.size(40.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Surface(
                    color = categoryBg,
                    shape = RoundedCornerShape(20.dp),
                ) {
                    Text(
                        category,
                        color = categoryFg,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, color = EduColors.TextPrimary)
                Spacer(Modifier.height(4.dp))
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = EduColors.TextSecondary)
                Spacer(Modifier.height(10.dp))
                Text(price, style = MaterialTheme.typography.titleMedium, color = EduColors.TextPrimary)
                Spacer(Modifier.height(6.dp))
                Text(
                    "Remover",
                    color = EduColors.Danger,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier.clickable {},
                )
            }
        }
    }
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
