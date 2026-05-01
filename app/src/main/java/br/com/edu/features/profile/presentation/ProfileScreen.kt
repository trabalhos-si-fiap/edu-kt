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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.input.KeyboardType
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
import br.com.edu.features.profile.domain.UserProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onOpenMarketplace: () -> Unit,
    onOpenOrders: () -> Unit,
    onOpenPaymentMethods: () -> Unit,
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
                        Icon(Icons.Outlined.ArrowBack, null, tint = EduColors.TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent),
            )
        },
        bottomBar = {
            MainBottomBar(
                selected = 2,
                onTabSelected = { index ->
                    when (index) {
                        0 -> onOpenMarketplace()
                        1 -> onOpenOrders()
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
                    onOpenOrders = onOpenOrders,
                    onOpenPaymentMethods = onOpenPaymentMethods,
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
    onOpenOrders: () -> Unit,
    onOpenPaymentMethods: () -> Unit,
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
        ShortcutsSection(
            onOpenOrders = onOpenOrders,
            onOpenPaymentMethods = onOpenPaymentMethods,
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
                ReadOnlyField(label = "Telefone", value = profile.phone.ifBlank { "—" })
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
    var phone by rememberSaveable(profile.id) { mutableStateOf(profile.phone) }
    var birthDate by rememberSaveable(profile.id) { mutableStateOf(profile.birthDate.orEmpty()) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        FieldLabel("Nome")
        EduTextField(value = name, onValueChange = { name = it }, placeholder = "Seu nome completo")
        FieldLabel("Telefone")
        EduTextField(
            value = phone,
            onValueChange = { phone = it },
            placeholder = "(11) 99999-9999",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        )
        FieldLabel("Data de nascimento (AAAA-MM-DD)")
        EduTextField(
            value = birthDate,
            onValueChange = { birthDate = it },
            placeholder = "1990-05-12",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )
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
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            "Atalhos",
            style = MaterialTheme.typography.titleMedium,
            color = EduColors.TextPrimary,
            fontWeight = FontWeight.Bold,
        )
        ShortcutRow(
            icon = Icons.Outlined.ReceiptLong,
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
