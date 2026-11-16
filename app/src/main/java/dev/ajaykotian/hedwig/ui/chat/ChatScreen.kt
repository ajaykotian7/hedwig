package dev.ajaykotian.hedwig.ui.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.outlined.WavingHand
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.ajaykotian.hedwig.data.model.Message
import dev.ajaykotian.hedwig.data.model.User
import dev.ajaykotian.hedwig.ui.base.PhoneDarkPreview
import dev.ajaykotian.hedwig.ui.base.PhoneLightPreview
import dev.ajaykotian.hedwig.ui.base.screenViewModel
import dev.ajaykotian.hedwig.ui.components.Avatar
import dev.ajaykotian.hedwig.ui.components.EmptyState
import dev.ajaykotian.hedwig.ui.components.ErrorRow
import dev.ajaykotian.hedwig.ui.components.HedwigTopAppBarDefaults
import dev.ajaykotian.hedwig.ui.components.LightStatusBarIcons
import dev.ajaykotian.hedwig.ui.theme.HedwigTheme

@Composable
fun ChatScreen(
    onBackClick: () -> Unit,
    viewModel: ChatViewModel = screenViewModel { getChatViewModel() },
) {
    LightStatusBarIcons()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ChatContent(
        uiState = uiState,
        messageText = viewModel.messageText,
        onMessageTextChange = viewModel::onMessageTextChange,
        onSendClick = viewModel::sendMessage,
        onBackClick = onBackClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatContent(
    uiState: ChatUIState,
    messageText: String,
    onMessageTextChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Avatar(name = uiState.otherUser.name, id = uiState.otherUser.uid, size = 40.dp)
                        Spacer(Modifier.width(12.dp))
                        Text(uiState.otherUser.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                },
                colors = HedwigTopAppBarDefaults.colors(),
            )
        },
        bottomBar = {
            Column(
                // Lift the bar above the gesture bar, and above the keyboard when it's open.
                modifier = Modifier
                    .navigationBarsPadding()
                    .imePadding(),
            ) {
                uiState.errorMessage?.let {
                    ErrorRow(it, Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
                }
                MessageInput(
                    text = messageText,
                    onTextChange = onMessageTextChange,
                    onSendClick = onSendClick,
                )
            }
        },
        containerColor = HedwigTheme.colors.chatBackground,
        contentColor = MaterialTheme.colorScheme.onBackground,
    ) { innerPadding ->
        when {
            uiState.isLoading -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = HedwigTheme.colors.accent)
            }

            uiState.messages.isEmpty() -> EmptyState(
                icon = Icons.Outlined.WavingHand,
                title = "Say hi",
                message = "Messages you send to ${uiState.otherUser.name.ifBlank { "them" }} appear here.",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )

            else -> MessageList(messages = uiState.messages, contentPadding = innerPadding)
        }
    }
}

@Composable
private fun MessageList(
    messages: List<MessageItem>,
    contentPadding: PaddingValues,
) {
    val listState = rememberLazyListState()

    // Scroll to the latest only if the user is not already on some particular message
    val newest = messages.lastOrNull()
    LaunchedEffect(newest?.message?.id) {
        if (newest != null && (newest.isMine || listState.firstVisibleItemIndex <= 1)) {
            listState.animateScrollToItem(0)
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        reverseLayout = true, //Latest on the bottom
        contentPadding = PaddingValues(
            start = 8.dp,
            end = 8.dp,
            top = contentPadding.calculateTopPadding() + 8.dp,
            bottom = contentPadding.calculateBottomPadding() + 8.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        // Newest first to match the reversed layout. asReversed() is a view, not a copy.
        items(messages.asReversed(), key = { it.message.id }) { item ->
            MessageBubble(item)
        }
    }
}

@Composable
private fun MessageBubble(item: MessageItem) {
    val colors = HedwigTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = if (item.isMine) 48.dp else 0.dp, end = if (item.isMine) 0.dp else 48.dp),
        contentAlignment = if (item.isMine) Alignment.CenterEnd else Alignment.CenterStart,
    ) {
        Surface(
            color = if (item.isMine) colors.outgoingBubble else colors.incomingBubble,
            contentColor = MaterialTheme.colorScheme.onSurface,
            shape = RoundedCornerShape(12.dp),
        ) {
            Text(
                text = item.message.text,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            )
        }
    }
}


@Composable
private fun MessageInput(
    text: String,
    onTextChange: (String) -> Unit,
    onSendClick: () -> Unit,
) {
    val colors = HedwigTheme.colors
    Row(
        modifier = Modifier.padding(8.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        TextField(
            value = text,
            onValueChange = onTextChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Message") },
            maxLines = 5,
            shape = RoundedCornerShape(24.dp),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
        )
        Spacer(Modifier.width(8.dp))
        FilledIconButton(
            onClick = onSendClick,
            enabled = text.isNotBlank(),
            modifier = Modifier.size(56.dp),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = colors.accent,
                contentColor = Color.White,
            ),
        ) {
            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
        }
    }
}

private val PreviewUser = User(uid = "user-2", name = "Riya Sharma")

private val PreviewMessages = listOf(
    MessageItem(Message(id = "1", text = "Hey! Are we still on for tonight?"), isMine = false),
    MessageItem(Message(id = "2", text = "Yes! 8 pm works"), isMine = true),
    MessageItem(Message(id = "3", text = "I'll book a table at that new place near the station. Their pasta is supposed to be really good."), isMine = true),
    MessageItem(Message(id = "4", text = "Perfect 🙌"), isMine = false),
)

@Composable
private fun ChatPreview(uiState: ChatUIState, messageText: String = "") {
    HedwigTheme {
        ChatContent(
            uiState = uiState,
            messageText = messageText,
            onMessageTextChange = {},
            onSendClick = {},
            onBackClick = {},
        )
    }
}

@PhoneLightPreview
@Composable
private fun ChatPreviewLight() = ChatPreview(
    ChatUIState(isLoading = false, otherUser = PreviewUser, messages = PreviewMessages),
    messageText = "See you there",
)

@PhoneDarkPreview
@Composable
private fun ChatPreviewDark() = ChatPreview(
    ChatUIState(isLoading = false, otherUser = PreviewUser, messages = PreviewMessages),
)

@PhoneLightPreview
@Composable
private fun ChatEmptyPreview() = ChatPreview(
    ChatUIState(isLoading = false, otherUser = PreviewUser),
)

@PhoneLightPreview
@Composable
private fun ChatErrorPreview() = ChatPreview(
    ChatUIState(isLoading = false, otherUser = PreviewUser, messages = PreviewMessages, errorMessage = "Message not sent"),
)

