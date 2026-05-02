package br.com.edu.features.support.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.SupportAgent
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.edu.core.theme.EduColors
import br.com.edu.core.theme.EduGradients
import br.com.edu.core.ui.EduCard
import br.com.edu.core.ui.EduTextField
import br.com.edu.core.ui.MainBottomBar
import br.com.edu.features.support.domain.Sender
import br.com.edu.features.support.domain.SupportMessage
import br.com.edu.features.support.domain.formatMessageTime
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportScreen(
    onBack: () -> Unit,
    onOpenMarketplace: () -> Unit = {},
    onOpenOrders: () -> Unit = {},
    onOpenProfile: () -> Unit = {},
    viewModel: SupportViewModel = remember { SupportViewModel.get() },
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) { viewModel.load() }

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0),
        modifier = Modifier
            .fillMaxSize()
            .background(EduGradients.Background),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Suporte de Pedidos",
                        style = MaterialTheme.typography.titleLarge,
                        color = EduColors.TextPrimary,
                        fontWeight = FontWeight.ExtraBold,
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
                selected = 2,
                onTabSelected = { index ->
                    when (index) {
                        0 -> onOpenMarketplace()
                        1 -> onOpenOrders()
                        3 -> onOpenProfile()
                    }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .imePadding(),
        ) {
            ChatPanel(
                state = state,
                onSend = viewModel::send,
                onRetry = viewModel::load,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ChatPanel(
    state: SupportUiState,
    onSend: (String) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    EduCard(
        modifier = modifier,
        contentPadding = PaddingValues(0.dp),
        radius = 24.dp,
        color = EduColors.InputFill,
        shadow = 2.dp,
    ) {
        Column(Modifier.fillMaxSize()) {
            when (state) {
                is SupportUiState.Loading -> Box(
                    Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = EduColors.Primary)
                }
                is SupportUiState.Error -> Column(
                    Modifier.weight(1f).fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        "Não foi possível carregar o chat.",
                        style = MaterialTheme.typography.titleMedium,
                        color = EduColors.TextPrimary,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(state.message, color = EduColors.TextSecondary, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(16.dp))
                    TextButton(onClick = onRetry) {
                        Text("Tentar novamente", color = EduColors.Primary)
                    }
                }
                is SupportUiState.Ready -> {
                    MessageList(
                        messages = state.messages,
                        modifier = Modifier.weight(1f),
                    )
                    InputBar(
                        sending = state.sending,
                        onSend = onSend,
                    )
                    Disclaimer()
                }
            }
        }
    }
}

@Composable
private fun MessageList(
    messages: List<SupportMessage>,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }
    if (messages.isEmpty()) {
        Box(modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Column(
                Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    Modifier.size(56.dp).background(EduColors.Primary, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Outlined.SupportAgent,
                        null,
                        tint = EduColors.White,
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    "Olá! Como posso ajudar com seus pedidos hoje?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = EduColors.TextSecondary,
                    textAlign = TextAlign.Center,
                )
            }
        }
        return
    }
    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(messages, key = { it.id }) { msg -> MessageBubble(msg) }
    }
}

@Composable
private fun MessageBubble(message: SupportMessage) {
    val isUser = message.sender == Sender.USER
    val time = formatMessageTime(message.createdAt)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top,
    ) {
        if (!isUser) {
            Box(
                Modifier.size(32.dp).background(EduColors.Primary, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Outlined.SupportAgent,
                    null,
                    tint = EduColors.White,
                    modifier = Modifier.size(18.dp),
                )
            }
            Spacer(Modifier.size(8.dp))
        }
        Surface(
            color = EduColors.White,
            shape = RoundedCornerShape(20.dp),
            shadowElevation = 1.dp,
            modifier = Modifier.widthIn(max = 280.dp),
        ) {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                if (!isUser) {
                    Text(
                        "SUPORTE EDU",
                        style = MaterialTheme.typography.labelSmall,
                        color = EduColors.Purple,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                    )
                    Spacer(Modifier.height(4.dp))
                }
                Text(
                    message.body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = EduColors.TextPrimary,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    time,
                    style = MaterialTheme.typography.labelSmall,
                    color = EduColors.TextSecondary,
                    fontSize = 11.sp,
                    modifier = Modifier.align(Alignment.End),
                )
            }
        }
    }
}

@Composable
private fun InputBar(
    sending: Boolean,
    onSend: (String) -> Unit,
) {
    var text by rememberSaveable { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val canSend = text.trim().isNotEmpty() && !sending

    fun submit() {
        if (!canSend) return
        val toSend = text
        text = ""
        scope.launch { onSend(toSend) }
    }

    Surface(
        color = EduColors.White,
        shape = RoundedCornerShape(28.dp),
        shadowElevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextField(
                value = text,
                onValueChange = { text = it },
                placeholder = {
                    Text(
                        "Escreva sua mensagem aqui...",
                        color = EduColors.TextSecondary,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                },
                enabled = !sending,
                singleLine = false,
                maxLines = 4,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = EduColors.TextPrimary),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    cursorColor = EduColors.Purple,
                ),
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 56.dp),
            )
            Spacer(Modifier.size(8.dp))
            Box(
                Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(if (canSend) EduColors.Primary else EduColors.Primary.copy(alpha = 0.4f))
                    .clickable(enabled = canSend, onClick = ::submit),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.AutoMirrored.Outlined.Send,
                    contentDescription = "Enviar",
                    tint = EduColors.White,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
    }
}

@Composable
private fun Disclaimer() {
    Text(
        "Mentor Edu pode cometer erros, verifique informações importantes.",
        style = MaterialTheme.typography.bodySmall,
        color = EduColors.TextSecondary,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .navigationBarsPadding(),
    )
}

