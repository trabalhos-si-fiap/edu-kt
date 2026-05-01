package br.com.edu.features.marketplace.presentation

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Pix
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.LaunchedEffect
import br.com.edu.core.theme.EduColors
import br.com.edu.core.theme.EduGradients
import br.com.edu.core.ui.EduPurpleButton
import br.com.edu.core.ui.EduTextField
import br.com.edu.features.payment.domain.CardNumberTransformation
import br.com.edu.features.payment.domain.ExpiryTransformation
import br.com.edu.features.payment.domain.PaymentMethod
import br.com.edu.features.payment.domain.PaymentMethodType
import br.com.edu.features.payment.domain.brandFromNumber
import br.com.edu.features.payment.domain.message
import br.com.edu.features.payment.domain.sanitizeCardNumber
import br.com.edu.features.payment.domain.sanitizeCvv
import br.com.edu.features.payment.domain.sanitizeExpiry
import br.com.edu.features.payment.domain.validateCreditCardForm
import br.com.edu.features.payment.presentation.PaymentMethodViewModel
import kotlinx.coroutines.launch

private enum class PaymentType { CreditCard, Pix, Boleto }

private fun PaymentMethodType.toUi(): PaymentType = when (this) {
    PaymentMethodType.CREDIT_CARD -> PaymentType.CreditCard
    PaymentMethodType.PIX -> PaymentType.Pix
    PaymentMethodType.BOLETO -> PaymentType.Boleto
}

private fun PaymentType.toDomain(): PaymentMethodType = when (this) {
    PaymentType.CreditCard -> PaymentMethodType.CREDIT_CARD
    PaymentType.Pix -> PaymentMethodType.PIX
    PaymentType.Boleto -> PaymentMethodType.BOLETO
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPaymentMethodScreen(
    onBack: () -> Unit,
    editingId: String? = null,
    viewModel: PaymentMethodViewModel = PaymentMethodViewModel.get(),
) {
    val isEditing = editingId != null
    var selected by rememberSaveable { mutableStateOf(PaymentType.CreditCard) }
    var cardNumber by rememberSaveable { mutableStateOf("") }
    var cardName by rememberSaveable { mutableStateOf("") }
    var expiry by rememberSaveable { mutableStateOf("") }
    var cvv by rememberSaveable { mutableStateOf("") }
    var pixKey by rememberSaveable { mutableStateOf("") }
    var saveAsDefault by rememberSaveable { mutableStateOf(false) }
    var existingLast4 by rememberSaveable { mutableStateOf<String?>(null) }
    val snackbarHost = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(editingId) {
        if (editingId != null) {
            val existing = viewModel.getById(editingId)
            if (existing != null) {
                selected = existing.type.toUi()
                cardName = existing.cardholderName.orEmpty()
                expiry = existing.cardExpiry.orEmpty()
                pixKey = existing.pixKey.orEmpty()
                saveAsDefault = existing.isDefault
                existingLast4 = existing.cardLast4
                cardNumber = ""
            }
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
                        if (isEditing) "Editar Método" else "Adicionar Método",
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
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
        ) {
            Text(
                "Tipo de pagamento",
                style = MaterialTheme.typography.titleMedium,
                color = EduColors.TextPrimary,
            )
            Spacer(Modifier.height(12.dp))
            TypeSelector(selected = selected, onChange = { selected = it })
            Spacer(Modifier.height(28.dp))

            when (selected) {
                PaymentType.CreditCard -> {
                    if (isEditing && existingLast4 != null && cardNumber.isBlank()) {
                        InfoBox(
                            background = EduColors.InputFill,
                            contentColor = EduColors.TextSecondary,
                            icon = Icons.Outlined.CreditCard,
                            message = "Cartão atual final ${existingLast4}. Informe um novo número apenas se quiser substituir.",
                        )
                        Spacer(Modifier.height(12.dp))
                    }
                    CardFields(
                        number = cardNumber,
                        onNumberChange = { cardNumber = sanitizeCardNumber(it) },
                        name = cardName,
                        onNameChange = { cardName = it.uppercase() },
                        expiry = expiry,
                        onExpiryChange = { expiry = sanitizeExpiry(it) },
                        cvv = cvv,
                        onCvvChange = { cvv = sanitizeCvv(it) },
                    )
                }
                PaymentType.Pix -> PixFields(pixKey = pixKey, onPixChange = { pixKey = it })
                PaymentType.Boleto -> BoletoInfo()
            }

            Spacer(Modifier.height(12.dp))
            DefaultCheckbox(value = saveAsDefault, onChange = { saveAsDefault = it })
            Spacer(Modifier.height(24.dp))

            EduPurpleButton(
                text = if (isEditing) "Salvar alterações" else "Salvar método",
                onClick = {
                    val error = when (selected) {
                        PaymentType.CreditCard -> validateCreditCardForm(
                            cardNumber = cardNumber,
                            cardName = cardName,
                            expiry = expiry,
                            cvv = cvv,
                            isEditing = isEditing,
                        )?.message()
                        PaymentType.Pix -> if (pixKey.isBlank()) "Informe a chave PIX" else null
                        PaymentType.Boleto -> null
                    }
                    if (error != null) {
                        scope.launch { snackbarHost.showSnackbar(error) }
                        return@EduPurpleButton
                    }

                    val method = when (selected) {
                        PaymentType.CreditCard -> {
                            val last4 = if (cardNumber.isNotBlank()) cardNumber.takeLast(4) else existingLast4
                            val brand = if (cardNumber.isNotBlank()) brandFromNumber(cardNumber) else null
                            PaymentMethod(
                                id = editingId.orEmpty(),
                                type = PaymentMethodType.CREDIT_CARD,
                                cardLast4 = last4,
                                cardBrand = brand ?: viewModel.getById(editingId.orEmpty())?.cardBrand,
                                cardholderName = cardName,
                                cardExpiry = expiry,
                            )
                        }
                        PaymentType.Pix -> PaymentMethod(
                            id = editingId.orEmpty(),
                            type = PaymentMethodType.PIX,
                            pixKey = pixKey,
                        )
                        PaymentType.Boleto -> PaymentMethod(
                            id = editingId.orEmpty(),
                            type = PaymentMethodType.BOLETO,
                        )
                    }

                    if (isEditing) {
                        viewModel.update(method, makeDefault = saveAsDefault)
                    } else {
                        viewModel.add(method, makeDefault = saveAsDefault)
                    }
                    scope.launch {
                        snackbarHost.showSnackbar(
                            if (isEditing) "Método atualizado" else "Método de pagamento adicionado",
                        )
                    }
                    onBack()
                },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Outlined.Lock,
                    null,
                    tint = EduColors.TextSecondary,
                    modifier = Modifier.size(14.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    "Pagamentos protegidos com criptografia",
                    fontSize = 12.sp,
                    color = EduColors.TextSecondary,
                )
            }
        }
    }
}

@Composable
private fun TypeSelector(selected: PaymentType, onChange: (PaymentType) -> Unit) {
    val options = listOf(
        Triple(PaymentType.CreditCard, Icons.Outlined.CreditCard, "Cartão"),
        Triple(PaymentType.Pix, Icons.Outlined.Pix, "PIX"),
        Triple(PaymentType.Boleto, Icons.Outlined.ReceiptLong, "Boleto"),
    )
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        options.forEach { (type, icon, label) ->
            TypeOption(
                icon = icon,
                label = label,
                selected = selected == type,
                onTap = { onChange(type) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun TypeOption(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .clickable { onTap() }
            .border(
                width = 2.dp,
                color = if (selected) EduColors.Purple else Color.Transparent,
                shape = RoundedCornerShape(14.dp),
            ),
        shape = RoundedCornerShape(14.dp),
        color = if (selected) EduColors.White else EduColors.InputFill,
        shadowElevation = if (selected) 2.dp else 0.dp,
    ) {
        Column(
            Modifier.padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                icon,
                null,
                tint = if (selected) EduColors.Purple else EduColors.TextSecondary,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                label,
                color = if (selected) EduColors.TextPrimary else EduColors.TextSecondary,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
            )
        }
    }
}

@Composable
private fun CardFields(
    number: String,
    onNumberChange: (String) -> Unit,
    name: String,
    onNameChange: (String) -> Unit,
    expiry: String,
    onExpiryChange: (String) -> Unit,
    cvv: String,
    onCvvChange: (String) -> Unit,
) {
    LabeledField("Número do cartão") {
        EduTextField(
            value = number,
            onValueChange = onNumberChange,
            placeholder = "0000 0000 0000 0000",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            visualTransformation = CardNumberTransformation,
        )
    }
    LabeledField("Nome impresso no cartão") {
        EduTextField(
            value = name,
            onValueChange = onNameChange,
            placeholder = "NOME COMPLETO",
        )
    }
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(Modifier.weight(1f)) {
            LabeledField("Validade") {
                EduTextField(
                    value = expiry,
                    onValueChange = onExpiryChange,
                    placeholder = "MM/AA",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    visualTransformation = ExpiryTransformation,
                )
            }
        }
        Box(Modifier.weight(1f)) {
            LabeledField("CVV") {
                EduTextField(
                    value = cvv,
                    onValueChange = onCvvChange,
                    placeholder = "•••",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation(),
                )
            }
        }
    }
}

@Composable
private fun PixFields(pixKey: String, onPixChange: (String) -> Unit) {
    LabeledField("Chave PIX") {
        EduTextField(
            value = pixKey,
            onValueChange = onPixChange,
            placeholder = "CPF, e-mail, telefone ou chave aleatória",
        )
    }
    InfoBox(
        background = EduColors.GreenSoft.copy(alpha = 0.5f),
        contentColor = EduColors.GreenDark,
        icon = Icons.Outlined.Info,
        message = "Aprovação imediata após o pagamento.",
    )
}

@Composable
private fun BoletoInfo() {
    InfoBox(
        background = EduColors.InputFill,
        contentColor = EduColors.TextSecondary,
        icon = Icons.Outlined.Schedule,
        message = "O boleto será gerado na finalização do pedido. Compensação em até 2 dias úteis.",
    )
}

@Composable
private fun InfoBox(
    background: Color,
    contentColor: Color,
    icon: ImageVector,
    message: String,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = background,
    ) {
        Row(
            Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, null, tint = contentColor, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(10.dp))
            Text(
                message,
                color = contentColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun LabeledField(label: String, content: @Composable () -> Unit) {
    Column(Modifier.padding(bottom = 16.dp)) {
        Text(
            label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = EduColors.TextPrimary,
        )
        Spacer(Modifier.height(8.dp))
        content()
    }
}

@Composable
private fun DefaultCheckbox(value: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier.clickable { onChange(!value) },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = value,
            onCheckedChange = { onChange(it) },
            colors = CheckboxDefaults.colors(
                checkedColor = EduColors.Purple,
                uncheckedColor = EduColors.TextSecondary,
            ),
        )
        Spacer(Modifier.width(4.dp))
        Text(
            "Definir como método padrão",
            fontSize = 14.sp,
            color = EduColors.TextPrimary,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

