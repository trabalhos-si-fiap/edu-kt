package br.com.edu.features.profile.presentation

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.SupportAgent
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.edu.core.theme.EduColors
import br.com.edu.core.theme.EduGradients
import br.com.edu.core.ui.EduCard
import br.com.edu.core.ui.EduPurpleButton
import br.com.edu.core.ui.EduSoftButton
import br.com.edu.core.ui.EduTextField
import br.com.edu.core.ui.MainBottomBar
import br.com.edu.features.profile.data.remote.AddressInDto
import br.com.edu.features.profile.data.remote.AddressPatchDto
import br.com.edu.features.profile.domain.Address
import br.com.edu.features.profile.domain.UserProfile
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onOpenMarketplace: () -> Unit,
    onOpenOrders: () -> Unit,
    onOpenPaymentMethods: () -> Unit,
    onOpenSupport: () -> Unit,
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        if (state is ProfileUiState.Loading) viewModel.load()
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
                        "Perfil",
                        color = EduColors.TextPrimary,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, null, tint = EduColors.TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent),
            )
        },
        bottomBar = {
            MainBottomBar(
                selected = 3,
                onTabSelected = { index ->
                    when (index) {
                        0 -> onOpenMarketplace()
                        1 -> onOpenOrders()
                        2 -> onOpenSupport()
                    }
                },
            )
        },
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (val s = state) {
                is ProfileUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = EduColors.Primary)
                }
                is ProfileUiState.Error -> Column(
                    Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        "Não foi possível carregar o perfil.",
                        style = MaterialTheme.typography.titleMedium,
                        color = EduColors.TextPrimary,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(s.message, color = EduColors.TextSecondary)
                    Spacer(Modifier.height(16.dp))
                    TextButton(onClick = { viewModel.load() }) {
                        Text("Tentar novamente", color = EduColors.Primary)
                    }
                }
                is ProfileUiState.Ready -> ProfileContent(
                    state = s,
                    onStartEdit = viewModel::startEdit,
                    onCancelEdit = viewModel::cancelEdit,
                    onSave = viewModel::save,
                    onCreateAddress = viewModel::createAddress,
                    onUpdateAddress = viewModel::updateAddress,
                    onDeleteAddress = viewModel::deleteAddress,
                    onSetFavoriteAddress = viewModel::setFavorite,
                    onOpenOrders = onOpenOrders,
                    onOpenPaymentMethods = onOpenPaymentMethods,
                    onOpenSupport = onOpenSupport,
                    onLogout = { viewModel.logout(onLogout) },
                )
            }
        }
    }
}

@Composable
private fun ProfileContent(
    state: ProfileUiState.Ready,
    onStartEdit: () -> Unit,
    onCancelEdit: () -> Unit,
    onSave: (name: String, phone: String, birthDate: String) -> Unit,
    onCreateAddress: (AddressInDto) -> Unit,
    onUpdateAddress: (Int, AddressPatchDto) -> Unit,
    onDeleteAddress: (Int) -> Unit,
    onSetFavoriteAddress: (Int) -> Unit,
    onOpenOrders: () -> Unit,
    onOpenPaymentMethods: () -> Unit,
    onOpenSupport: () -> Unit,
    onLogout: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        ProfileHeader(state.profile)
        ProfileDataCard(
            profile = state.profile,
            isEditing = state.isEditing,
            saving = state.saving,
            saveError = state.saveError,
            onStartEdit = onStartEdit,
            onCancelEdit = onCancelEdit,
            onSave = onSave,
        )
        AddressesSection(
            addresses = state.addresses,
            busy = state.addressBusy,
            error = state.addressError,
            onCreate = onCreateAddress,
            onUpdate = onUpdateAddress,
            onDelete = onDeleteAddress,
            onSetFavorite = onSetFavoriteAddress,
        )
        ShortcutsSection(
            onOpenOrders = onOpenOrders,
            onOpenPaymentMethods = onOpenPaymentMethods,
            onOpenSupport = onOpenSupport,
        )
        EduSoftButton(
            text = "Sair da conta",
            onClick = onLogout,
            container = EduColors.Danger.copy(alpha = 0.1f),
            content = EduColors.Danger,
        )
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun ProfileHeader(profile: UserProfile) {
    val initial = profile.name.trim().firstOrNull()?.uppercase()
        ?: profile.email.trim().firstOrNull()?.uppercase()
        ?: "?"
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box(
            Modifier
                .size(72.dp)
                .background(EduColors.Purple, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                initial,
                color = EduColors.White,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
            )
        }
        Column {
            Text(
                profile.name.ifBlank { "Sem nome" },
                style = MaterialTheme.typography.titleLarge,
                color = EduColors.TextPrimary,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                profile.email,
                style = MaterialTheme.typography.bodyMedium,
                color = EduColors.TextSecondary,
            )
        }
    }
}

@Composable
private fun ProfileDataCard(
    profile: UserProfile,
    isEditing: Boolean,
    saving: Boolean,
    saveError: String?,
    onStartEdit: () -> Unit,
    onCancelEdit: () -> Unit,
    onSave: (String, String, String) -> Unit,
) {
    EduCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(20.dp),
        radius = 16.dp,
        shadow = 2.dp,
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    "Dados pessoais",
                    style = MaterialTheme.typography.titleMedium,
                    color = EduColors.TextPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                if (!isEditing) {
                    IconButton(onClick = onStartEdit) {
                        Icon(Icons.Outlined.Edit, contentDescription = "Editar", tint = EduColors.Purple)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            if (isEditing) {
                EditForm(profile, saving, saveError, onCancel = onCancelEdit, onSave = onSave)
            } else {
                ReadOnlyField(label = "Nome", value = profile.name.ifBlank { "—" })
                Spacer(Modifier.height(12.dp))
                ReadOnlyField(
                    label = "Telefone",
                    value = profile.phone.ifBlank { "—" }.let {
                        if (it == "—") it else formatPhoneBR(it)
                    },
                )
                Spacer(Modifier.height(12.dp))
                ReadOnlyField(
                    label = "Data de nascimento",
                    value = profile.birthDate?.let(::formatBirthDate) ?: "—",
                )
            }
        }
    }
}

@Composable
private fun ReadOnlyField(label: String, value: String) {
    Column {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = EduColors.TextSecondary,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.bodyLarge, color = EduColors.TextPrimary)
    }
}

@Composable
private fun EditForm(
    profile: UserProfile,
    saving: Boolean,
    saveError: String?,
    onCancel: () -> Unit,
    onSave: (String, String, String) -> Unit,
) {
    var name by rememberSaveable(profile.id) { mutableStateOf(profile.name) }
    var phone by rememberSaveable(profile.id) { mutableStateOf(profile.phone.filter(Char::isDigit).take(11)) }
    var birthDate by rememberSaveable(profile.id) { mutableStateOf(profile.birthDate.orEmpty()) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        FieldLabel("Nome")
        EduTextField(value = name, onValueChange = { name = it }, placeholder = "Seu nome completo")
        FieldLabel("Telefone")
        EduTextField(
            value = phone,
            onValueChange = { phone = it.filter(Char::isDigit).take(11) },
            placeholder = "(11) 99999-9999",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            visualTransformation = PhoneVisualTransformation,
        )
        FieldLabel("Data de nascimento")
        BirthDateField(value = birthDate, onValueChange = { birthDate = it })
        if (saveError != null) {
            Text(saveError, color = EduColors.Danger, style = MaterialTheme.typography.bodySmall)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            EduSoftButton(
                text = "Cancelar",
                onClick = onCancel,
                modifier = Modifier.weight(1f),
            )
            EduPurpleButton(
                text = if (saving) "Salvando..." else "Salvar",
                onClick = { onSave(name, phone, birthDate) },
                enabled = !saving,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelSmall,
        color = EduColors.TextSecondary,
        fontWeight = FontWeight.SemiBold,
    )
}

@Composable
private fun ShortcutsSection(
    onOpenOrders: () -> Unit,
    onOpenPaymentMethods: () -> Unit,
    onOpenSupport: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            "Atalhos",
            style = MaterialTheme.typography.titleMedium,
            color = EduColors.TextPrimary,
            fontWeight = FontWeight.Bold,
        )
        ShortcutRow(
            icon = Icons.AutoMirrored.Outlined.ReceiptLong,
            title = "Meus pedidos",
            subtitle = "Acompanhe suas compras",
            onClick = onOpenOrders,
        )
        ShortcutRow(
            icon = Icons.Outlined.CreditCard,
            title = "Métodos de pagamento",
            subtitle = "Gerencie seus cartões",
            onClick = onOpenPaymentMethods,
        )
        ShortcutRow(
            icon = Icons.Outlined.SupportAgent,
            title = "Falar com Suporte",
            subtitle = "Tire dúvidas com o Mentor Edu",
            onClick = onOpenSupport,
        )
    }
}

@Composable
private fun ShortcutRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    EduCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        contentPadding = PaddingValues(16.dp),
        radius = 14.dp,
        shadow = 1.dp,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(40.dp)
                    .background(EduColors.PurpleSoft, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, null, tint = EduColors.Purple)
            }
            Spacer(Modifier.size(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    color = EduColors.TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = EduColors.TextSecondary,
                )
            }
            Icon(
                Icons.AutoMirrored.Outlined.ArrowForward,
                null,
                tint = EduColors.TextSecondary,
            )
        }
    }
}

private fun formatBirthDate(iso: String): String {
    val parts = iso.split("-")
    if (parts.size != 3) return iso
    val (y, m, d) = parts
    return "$d/$m/$y"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BirthDateField(value: String, onValueChange: (String) -> Unit) {
    var showPicker by rememberSaveable { mutableStateOf(false) }
    val display = if (value.isBlank()) "" else formatBirthDate(value)

    Box {
        EduTextField(
            value = display,
            onValueChange = {},
            placeholder = "Selecione a data",
            readOnly = true,
            trailingIcon = {
                Icon(Icons.Outlined.CalendarMonth, contentDescription = null, tint = EduColors.Purple)
            },
        )
        Box(
            Modifier
                .matchParentSize()
                .clickable { showPicker = true },
        )
    }

    if (showPicker) {
        val initialMillis = remember(value) {
            runCatching { LocalDate.parse(value).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli() }
                .getOrNull()
        }
        val todayUtcMillis = remember {
            LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        }
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialMillis,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis <= todayUtcMillis
                override fun isSelectableYear(year: Int) = year <= LocalDate.now().year
            },
        )
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let {
                        val date = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
                        onValueChange(date.toString())
                    }
                    showPicker = false
                }) { Text("OK", color = EduColors.Purple) }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text("Cancelar", color = EduColors.TextSecondary)
                }
            },
            colors = DatePickerDefaults.colors(containerColor = EduColors.White),
        ) {
            DatePicker(state = pickerState, showModeToggle = false)
        }
    }
}

@Composable
private fun AddressesSection(
    addresses: List<Address>,
    busy: Boolean,
    error: String?,
    onCreate: (AddressInDto) -> Unit,
    onUpdate: (Int, AddressPatchDto) -> Unit,
    onDelete: (Int) -> Unit,
    onSetFavorite: (Int) -> Unit,
) {
    var dialogState by rememberSaveable(stateSaver = AddressDialogStateSaver) {
        mutableStateOf<AddressDialogState>(AddressDialogState.Hidden)
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            "Endereços de entrega",
            style = MaterialTheme.typography.titleMedium,
            color = EduColors.TextPrimary,
            fontWeight = FontWeight.Bold,
        )

        if (addresses.isEmpty()) {
            EduCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(20.dp),
                radius = 16.dp,
                shadow = 1.dp,
            ) {
                Text(
                    "Você ainda não cadastrou nenhum endereço.",
                    color = EduColors.TextSecondary,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        } else {
            addresses.forEach { address ->
                AddressCard(
                    address = address,
                    onSetFavorite = { onSetFavorite(address.id) },
                    onEdit = { dialogState = AddressDialogState.Edit(address.id) },
                    onDelete = { onDelete(address.id) },
                )
            }
        }

        if (error != null) {
            Text(error, color = EduColors.Danger, style = MaterialTheme.typography.bodySmall)
        }

        EduSoftButton(
            text = if (busy) "Salvando..." else "Adicionar endereço",
            onClick = { dialogState = AddressDialogState.Create },
            container = EduColors.PurpleSoft,
            content = EduColors.Purple,
        )
    }

    when (val s = dialogState) {
        AddressDialogState.Hidden -> Unit
        AddressDialogState.Create -> AddressFormDialog(
            initial = null,
            onDismiss = { dialogState = AddressDialogState.Hidden },
            onSubmit = {
                onCreate(it)
                dialogState = AddressDialogState.Hidden
            },
        )
        is AddressDialogState.Edit -> {
            val target = addresses.firstOrNull { it.id == s.id }
            if (target == null) {
                dialogState = AddressDialogState.Hidden
            } else {
                AddressFormDialog(
                    initial = target,
                    onDismiss = { dialogState = AddressDialogState.Hidden },
                    onSubmit = { input ->
                        onUpdate(
                            target.id,
                            AddressPatchDto(
                                label = input.label,
                                zipCode = input.zipCode,
                                street = input.street,
                                number = input.number,
                                complement = input.complement,
                                neighborhood = input.neighborhood,
                                city = input.city,
                                state = input.state,
                                isFavorite = input.isFavorite,
                            ),
                        )
                        dialogState = AddressDialogState.Hidden
                    },
                )
            }
        }
    }
}

private sealed interface AddressDialogState {
    data object Hidden : AddressDialogState
    data object Create : AddressDialogState
    data class Edit(val id: Int) : AddressDialogState
}

private val AddressDialogStateSaver = androidx.compose.runtime.saveable.Saver<AddressDialogState, Any>(
    save = { state ->
        when (state) {
            AddressDialogState.Hidden -> "h"
            AddressDialogState.Create -> "c"
            is AddressDialogState.Edit -> "e:${state.id}"
        }
    },
    restore = { value ->
        val s = value as String
        when {
            s == "h" -> AddressDialogState.Hidden
            s == "c" -> AddressDialogState.Create
            s.startsWith("e:") -> AddressDialogState.Edit(s.substringAfter("e:").toInt())
            else -> AddressDialogState.Hidden
        }
    },
)

@Composable
private fun AddressCard(
    address: Address,
    onSetFavorite: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    EduCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        radius = 14.dp,
        shadow = 1.dp,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(36.dp)
                        .background(EduColors.PurpleSoft, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Outlined.LocationOn, null, tint = EduColors.Purple)
                }
                Spacer(Modifier.size(12.dp))
                Text(
                    address.label.ifBlank { "Endereço" },
                    style = MaterialTheme.typography.titleSmall,
                    color = EduColors.TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                if (address.isFavorite) {
                    Box(
                        Modifier
                            .background(EduColors.PurpleSoft, RoundedCornerShape(999.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                    ) {
                        Text(
                            "Favorito",
                            color = EduColors.Purple,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
            Text(
                buildString {
                    append(address.street)
                    if (address.number.isNotBlank()) append(", ").append(address.number)
                    if (address.complement.isNotBlank()) append(" — ").append(address.complement)
                },
                color = EduColors.TextPrimary,
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                "${address.neighborhood} · ${address.city}/${address.state} · CEP ${address.zipCode}",
                color = EduColors.TextSecondary,
                style = MaterialTheme.typography.bodySmall,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onSetFavorite, enabled = !address.isFavorite) {
                    Icon(
                        if (address.isFavorite) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favoritar",
                        tint = if (address.isFavorite) EduColors.Purple else EduColors.TextSecondary,
                    )
                }
                IconButton(onClick = onEdit) {
                    Icon(Icons.Outlined.Edit, contentDescription = "Editar", tint = EduColors.Purple)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Outlined.Delete, contentDescription = "Excluir", tint = EduColors.Danger)
                }
            }
        }
    }
}

@Composable
private fun AddressFormDialog(
    initial: Address?,
    onDismiss: () -> Unit,
    onSubmit: (AddressInDto) -> Unit,
) {
    var label by rememberSaveable { mutableStateOf(initial?.label.orEmpty()) }
    var zipCode by rememberSaveable { mutableStateOf(initial?.zipCode.orEmpty()) }
    var street by rememberSaveable { mutableStateOf(initial?.street.orEmpty()) }
    var number by rememberSaveable { mutableStateOf(initial?.number.orEmpty()) }
    var complement by rememberSaveable { mutableStateOf(initial?.complement.orEmpty()) }
    var neighborhood by rememberSaveable { mutableStateOf(initial?.neighborhood.orEmpty()) }
    var city by rememberSaveable { mutableStateOf(initial?.city.orEmpty()) }
    var state by rememberSaveable { mutableStateOf(initial?.state.orEmpty()) }
    var isFavorite by rememberSaveable { mutableStateOf(initial?.isFavorite ?: false) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
    ) {
        EduCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            contentPadding = PaddingValues(20.dp),
            radius = 20.dp,
            shadow = 8.dp,
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .imePadding(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    if (initial == null) "Novo endereço" else "Editar endereço",
                    style = MaterialTheme.typography.titleMedium,
                    color = EduColors.TextPrimary,
                    fontWeight = FontWeight.Bold,
                )
                FieldLabel("Identificação (Casa, Trabalho)")
                EduTextField(value = label, onValueChange = { label = it }, placeholder = "Casa")
                FieldLabel("CEP")
                EduTextField(
                    value = zipCode,
                    onValueChange = { zipCode = it },
                    placeholder = "00000-000",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
                FieldLabel("Rua")
                EduTextField(value = street, onValueChange = { street = it }, placeholder = "Av. Paulista")
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Column(Modifier.weight(1f)) {
                        FieldLabel("Número")
                        EduTextField(value = number, onValueChange = { number = it }, placeholder = "1000")
                    }
                    Column(Modifier.weight(1f)) {
                        FieldLabel("Complemento")
                        EduTextField(value = complement, onValueChange = { complement = it }, placeholder = "Apto 12")
                    }
                }
                FieldLabel("Bairro")
                EduTextField(value = neighborhood, onValueChange = { neighborhood = it }, placeholder = "Centro")
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Column(Modifier.weight(2f)) {
                        FieldLabel("Cidade")
                        EduTextField(value = city, onValueChange = { city = it }, placeholder = "São Paulo")
                    }
                    Column(Modifier.weight(1f)) {
                        FieldLabel("UF")
                        EduTextField(
                            value = state,
                            onValueChange = { if (it.length <= 2) state = it.uppercase() },
                            placeholder = "SP",
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(checked = isFavorite, onCheckedChange = { isFavorite = it })
                    Spacer(Modifier.size(12.dp))
                    Text("Definir como favorito", color = EduColors.TextPrimary)
                }
                if (error != null) {
                    Text(error!!, color = EduColors.Danger, style = MaterialTheme.typography.bodySmall)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    EduSoftButton(
                        text = "Cancelar",
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                    )
                    EduPurpleButton(
                        text = if (initial == null) "Adicionar" else "Salvar",
                        onClick = {
                            val required = listOf(
                                "CEP" to zipCode,
                                "Rua" to street,
                                "Número" to number,
                                "Bairro" to neighborhood,
                                "Cidade" to city,
                                "UF" to state,
                            )
                            val missing = required.firstOrNull { it.second.isBlank() }
                            if (missing != null) {
                                error = "${missing.first} é obrigatório."
                                return@EduPurpleButton
                            }
                            error = null
                            onSubmit(
                                AddressInDto(
                                    label = label.trim(),
                                    zipCode = zipCode.trim(),
                                    street = street.trim(),
                                    number = number.trim(),
                                    complement = complement.trim(),
                                    neighborhood = neighborhood.trim(),
                                    city = city.trim(),
                                    state = state.trim().uppercase(),
                                    isFavorite = isFavorite,
                                ),
                            )
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

private fun formatPhoneBR(raw: String): String {
    val d = raw.filter(Char::isDigit).take(11)
    return when (d.length) {
        0 -> ""
        1, 2 -> "(${d}"
        in 3..6 -> "(${d.substring(0, 2)}) ${d.substring(2)}"
        in 7..10 -> "(${d.substring(0, 2)}) ${d.substring(2, 6)}-${d.substring(6)}"
        else -> "(${d.substring(0, 2)}) ${d.substring(2, 7)}-${d.substring(7)}"
    }
}

private object PhoneVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text.filter(Char::isDigit).take(11)
        val formatted = formatPhoneBR(digits)
        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val o = offset.coerceIn(0, digits.length)
                val t = when {
                    o == 0 -> 0
                    o <= 2 -> o + 1
                    o <= 7 -> o + 3
                    else -> o + 4
                }
                return t.coerceAtMost(formatted.length)
            }
            override fun transformedToOriginal(offset: Int): Int {
                val o = offset.coerceIn(0, formatted.length)
                return when {
                    o <= 1 -> 0
                    o <= 3 -> o - 1
                    o <= 5 -> 2
                    o <= 10 -> o - 3
                    o <= 11 -> 7
                    else -> (o - 4).coerceAtMost(digits.length)
                }
            }
        }
        return TransformedText(AnnotatedString(formatted), mapping)
    }
}
