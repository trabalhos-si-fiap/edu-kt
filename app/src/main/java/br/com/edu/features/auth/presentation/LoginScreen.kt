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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateRegister: () -> Unit,
    onNavigateLogistics: () -> Unit,
    viewModel: LoginViewModel = viewModel(),
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    val snackbarHost = remember { SnackbarHostState() }
    val uiState by viewModel.state.collectAsState()

    LaunchedEffect(uiState.success) {
        if (uiState.success) onLoginSuccess()
    }
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHost.showSnackbar(it)
            viewModel.consumedError()
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
                    .padding(top = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    "Edu",
                    color = EduColors.TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                )
                Spacer(Modifier.height(20.dp))
                Text(
                    "Bem vindo(a)\nde volta!",
                    style = MaterialTheme.typography.displayLarge,
                    color = EduColors.TextPrimary,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    "Entre na sua conta para continuar sua jornada",
                    style = MaterialTheme.typography.bodyMedium,
                    color = EduColors.TextSecondary,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(40.dp))
            }

            EduCard(
                modifier = Modifier
                    .padding(horizontal = 20.dp),
                contentPadding = PaddingValues(24.dp),
            ) {
                Column {
                    Text(
                        "E-mail",
                        style = MaterialTheme.typography.labelLarge,
                        color = EduColors.TextPrimary,
                    )
                    Spacer(Modifier.height(8.dp))
                    EduTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = "nome@email.com",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    )
                    Spacer(Modifier.height(20.dp))

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "Senha",
                            style = MaterialTheme.typography.labelLarge,
                            color = EduColors.TextPrimary,
                        )
                        Text(
                            "Esqueceu sua senha?",
                            color = EduColors.Purple,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            modifier = Modifier.clickable { },
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    EduTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = "••••••••",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                    contentDescription = null,
                                    tint = EduColors.TextSecondary,
                                )
                            }
                        },
                    )
                    Spacer(Modifier.height(24.dp))
                    EduPrimaryButton(
                        text = if (uiState.loading) "Entrando..." else "Entrar",
                        onClick = { viewModel.submit(email, password) },
                    )
                    Spacer(Modifier.height(20.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        HorizontalDivider(Modifier.weight(1f), color = EduColors.InputBorder)
                        Text(
                            "  Ou entre com  ",
                            style = MaterialTheme.typography.bodySmall,
                            color = EduColors.TextSecondary,
                        )
                        HorizontalDivider(Modifier.weight(1f), color = EduColors.InputBorder)
                    }
                    Spacer(Modifier.height(16.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = { },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, EduColors.InputBorder),
                        ) {
                            Text("G", color = EduColors.TextPrimary, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.width(8.dp))
                            Text("Google", color = EduColors.TextPrimary)
                        }
                        OutlinedButton(
                            onClick = { },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, EduColors.InputBorder),
                        ) {
                            Text("Apple", color = EduColors.TextPrimary)
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    "Desenvolvido por",
                    style = MaterialTheme.typography.labelSmall,
                    color = EduColors.TextSecondary,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Elias Moura · Murilo Godoi · Juliana Nascimento · Marcella Esteves",
                    style = MaterialTheme.typography.bodySmall,
                    color = EduColors.TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "© 2026",
                    style = MaterialTheme.typography.labelSmall,
                    color = EduColors.TextSecondary,
                )
            }
        }

        Box(Modifier.align(Alignment.BottomCenter)) {
            AuthBottomBar(selected = 0, onTabSelected = { if (it == 1) onNavigateRegister() })
        }

        SnackbarHost(
            hostState = snackbarHost,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 90.dp),
        )
    }
}
