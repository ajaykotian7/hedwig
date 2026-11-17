package dev.ajaykotian.hedwig.ui.chats

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material3.Badge
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.firebase.Timestamp
import dev.ajaykotian.hedwig.data.model.User
import dev.ajaykotian.hedwig.ui.base.PhoneDarkPreview
import dev.ajaykotian.hedwig.ui.base.PhoneLightPreview
import dev.ajaykotian.hedwig.ui.base.rememberChatListTime
import dev.ajaykotian.hedwig.ui.base.screenViewModel
import dev.ajaykotian.hedwig.ui.chat.MessageStatus
import dev.ajaykotian.hedwig.ui.components.Avatar
import dev.ajaykotian.hedwig.ui.components.EmptyState
import dev.ajaykotian.hedwig.ui.components.StatusIcon
import dev.ajaykotian.hedwig.ui.theme.HedwigTheme
import java.util.Date

@Composable
fun ChatsTab(
    onChatClick: (otherUserId: String) -> Unit,
    viewModel: ChatsViewModel = screenViewModel { getChatsViewModel() },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ChatsTabContent(uiState = uiState, onChatClick = onChatClick)
}

@Composable
internal fun ChatsTabContent(
    uiState: ChatsUIState,
    onChatClick: (otherUserId: String) -> Unit,
) {
    when {
        uiState.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = HedwigTheme.colors.accent)
        }

        uiState.errorMessage != null -> EmptyState(
            icon = Icons.Outlined.ErrorOutline,
            title = uiState.errorMessage,
            message = "Check your connection and try again.",
            modifier = Modifier.fillMaxSize(),
        )

        uiState.chats.isEmpty() -> EmptyState(
            icon = Icons.Outlined.Forum,
            title = "No chats yet",
            message = "Tap the new chat button to message anyone on hedwig.",
            modifier = Modifier.fillMaxSize(),
        )

        else -> LazyColumn(Modifier.fillMaxSize()) {
            items(uiState.chats, key = { it.chatId }) { chat ->
                ChatRow(chat, onClick = { onChatClick(chat.otherUser.uid) })
            }
        }
    }
}

@Composable
private fun ChatRow(chat: ChatListItem, onClick: () -> Unit) {
    val colors = HedwigTheme.colors
    val hasUnread = chat.unreadCount > 0
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        leadingContent = { Avatar(name = chat.otherUser.name, id = chat.otherUser.uid) },
        headlineContent = {
            Text(
                text = chat.otherUser.name.ifBlank { chat.otherUser.phoneNumber },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        supportingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                chat.lastMessageStatus?.let {
                    StatusIcon(it, dimColor = colors.secondaryText)
                    Spacer(Modifier.width(4.dp))
                }
                Text(
                    text = chat.lastMessage,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
            }
        },
        trailingContent = {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = rememberChatListTime(chat.lastMessageTime),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (hasUnread) colors.accentText else colors.secondaryText,
                )
                if (hasUnread) {
                    Spacer(Modifier.height(4.dp))
                    Badge(containerColor = colors.accent, contentColor = Color.White) {
                        Text(if (chat.unreadCount > 99) "99+" else chat.unreadCount.toString())
                    }
                }
            }
        },
    )
}


private val PreviewChats = listOf(
    ChatListItem(
        chatId = "1",
        otherUser = User(uid = "user-2", name = "Riya Sharma"),
        lastMessage = "8 works? There's a new pasta place near the station 🍝",
        lastMessageTime = Timestamp.now(),
        lastMessageStatus = null,
        unreadCount = 3,
    ),
    ChatListItem(
        chatId = "2",
        otherUser = User(uid = "user-3", name = "Arjun Mehta"),
        lastMessage = "See you at the station",
        lastMessageTime = Timestamp.now(),
        lastMessageStatus = MessageStatus.Read,
        unreadCount = 0,
    ),
    ChatListItem(
        chatId = "3",
        otherUser = User(uid = "user-4", phoneNumber = "+919876543210"),
        lastMessage = "Booked ✅",
        lastMessageTime = Timestamp(Date(System.currentTimeMillis() - 86_400_000)),
        lastMessageStatus = MessageStatus.Sent,
        unreadCount = 0,
    ),
)

@Composable
private fun ChatsPreview(uiState: ChatsUIState) {
    HedwigTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            ChatsTabContent(uiState = uiState, onChatClick = {})
        }
    }
}

@PhoneLightPreview
@Composable
private fun ChatsListPreview() = ChatsPreview(ChatsUIState(isLoading = false, chats = PreviewChats))

@PhoneDarkPreview
@Composable
private fun ChatsListDarkPreview() = ChatsPreview(ChatsUIState(isLoading = false, chats = PreviewChats))

@PhoneLightPreview
@Composable
private fun ChatsErrorPreview() = ChatsPreview(ChatsUIState(isLoading = false, errorMessage = "Couldn't load chats"))
