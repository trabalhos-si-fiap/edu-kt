package br.com.edu.features.auth.presentation

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.edu.core.theme.EduColors
import br.com.edu.core.theme.EduGradients
import br.com.edu.core.ui.AuthBottomBar
import br.com.edu.core.ui.EduCard
import br.com.edu.core.ui.EduPrimaryButton
import br.com.edu.core.ui.EduTextField
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onNavigateLogin: () -> Unit,
    viewModel: RegisterViewModel = viewModel(),
) {
    val uiState by viewModel.state.collectAsState()
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var birthDate by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirm by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var confirmVisible by rememberSaveable { mutableStateOf(false) }
    var datePickerOpen by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val snackbarHost = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val specialRegex = Regex("[!@#\$%^&*(),.?\":{}|<>]")

    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            snackbarHost.showSnackbar("Cadastro realizado!")
            viewModel.consumed()
            onNavigateLogin()
        }
    }
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHost.showSnackbar(it)
            viewModel.consumed()
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(EduGradients.Background),
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 90.dp),
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 60.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("Edu", color = EduColors.TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Spacer(Modifier.height(32.dp))
                Text(
                    "Crie sua conta!",
                    style = MaterialTheme.typography.displayLarge,
                    color = EduColors.TextPrimary,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    "Preencha os dados para começar a estudar com o Edu",
                    style = MaterialTheme.typography.bodyMedium,
                    color = EduColors.SubtitleGrey,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(24.dp))
            }

            EduCard(
                modifier = Modifier.padding(horizontal = 20.dp),
                contentPadding = PaddingValues(24.dp),
            ) {
                Column {
                    LabeledField("Nome") {
                        EduTextField(
                            value = name,
                            onValueChange = { name = it },
                            placeholder = "Seu nome completo",
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    LabeledField("E-mail") {
                        EduTextField(
                            value = email,
                            onValueChange = { email = it },
                            placeholder = "nome@email.com",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    LabeledField("Telefone") {
                        EduTextField(
                            value = phone,
                            onValueChange = { v -> phone = v.filter { it.isDigit() } },
                            placeholder = "(11) 99999-9999",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    LabeledField("Data de nascimento") {
                        Box(Modifier.fillMaxWidth()) {
                            EduTextField(
                                value = birthDate,
                                onValueChange = { },
                                placeholder = "DD/MM/AAAA",
                                readOnly = true,
                                enabled = false,
                                trailingIcon = {
                                    Icon(
                                        Icons.Outlined.CalendarMonth,
                                        contentDescription = null,
                                        tint = EduColors.TextSecondary,
                                    )
                                },
                            )
                            Box(
                                Modifier
                                    .matchParentSize()
                                    .clickable { datePickerOpen = true },
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    LabeledField("Senha") {
                        EduTextField(
                            value = password,
                            onValueChange = { password = it },
                            placeholder = "Mín. 8 caracteres + especial",
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                        null,
                                        tint = EduColors.TextSecondary,
                                    )
                                }
                            },
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    LabeledField("Confirmar senha") {
                        EduTextField(
                            value = confirm,
                            onValueChange = { confirm = it },
                            placeholder = "Repita a senha",
                            visualTransformation = if (confirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            trailingIcon = {
                                IconButton(onClick = { confirmVisible = !confirmVisible }) {
                                    Icon(
                                        if (confirmVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                        null,
                                        tint = EduColors.TextSecondary,
                                    )
                                }
                            },
                        )
                    }
                    Spacer(Modifier.height(24.dp))
                    EduPrimaryButton(
                        text = if (uiState.loading) "Enviando..." else "Cadastrar",
                        onClick = {
                            val error = when {
                                name.isBlank() -> "Informe o nome"
                                email.isBlank() || !email.contains("@") -> "E-mail inválido"
                                phone.isBlank() -> "Informe o telefone"
                                birthDate.isBlank() -> "Informe a data de nascimento"
                                password.length < 8 -> "Senha deve ter ao menos 8 caracteres"
                                !specialRegex.containsMatchIn(password) -> "Senha precisa de 1 caractere especial"
                                confirm != password -> "Senhas não coincidem"
                                else -> null
                            }
                            if (error != null) {
                                scope.launch { snackbarHost.showSnackbar(error) }
                            } else {
                                viewModel.submit(
                                    email = email,
                                    password = password,
                                    name = name,
                                    phone = phone,
                                    birthDate = birthDate,
                                )
                            }
                        },
                    )
                }
            }

        }

        Box(Modifier.align(Alignment.BottomCenter)) {
            AuthBottomBar(selected = 1, onTabSelected = { if (it == 0) onNavigateLogin() })
        }

        SnackbarHost(snackbarHost, Modifier.align(Alignment.BottomCenter).padding(bottom = 90.dp))
    }

    if (datePickerOpen) {
        DatePickerDialog(
            onDismissRequest = { datePickerOpen = false },
            confirmButton = {
                TextButton(onClick = {
                    val millis = datePickerState.selectedDateMillis
                    if (millis != null) {
                        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
                        sdf.timeZone = TimeZone.getTimeZone("UTC")
                        birthDate = sdf.format(Date(millis))
                    }
                    datePickerOpen = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { datePickerOpen = false }) { Text("Cancelar") }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun LabeledField(label: String, content: @Composable () -> Unit) {
    Text(
        label,
        style = MaterialTheme.typography.labelLarge,
        color = EduColors.TextPrimary,
    )
    Spacer(Modifier.height(8.dp))
    content()
}
