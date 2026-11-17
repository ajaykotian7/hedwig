package dev.ajaykotian.hedwig.ui.chats

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.firebase.Timestamp
import dev.ajaykotian.hedwig.R
import dev.ajaykotian.hedwig.data.model.User
import dev.ajaykotian.hedwig.ui.base.PhoneDarkPreview
import dev.ajaykotian.hedwig.ui.base.PhoneLightPreview
import dev.ajaykotian.hedwig.ui.base.formatPhoneNumber
import dev.ajaykotian.hedwig.ui.base.rememberChatListTime
import dev.ajaykotian.hedwig.ui.base.screenViewModel
import dev.ajaykotian.hedwig.ui.chat.MessageStatus
import dev.ajaykotian.hedwig.ui.components.Avatar
import dev.ajaykotian.hedwig.ui.components.EmptyState
import dev.ajaykotian.hedwig.ui.components.Shimmer
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
        uiState.isLoading -> ChatsShimmer()

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

        else -> ChatList(uiState.chats, onChatClick)
    }
}

@Composable
private fun ChatList(chats: List<ChatListItem>, onChatClick: (otherUserId: String) -> Unit) {
    val listState = rememberLazyListState()

    // When a chat moves to the top, LazyColumn keeps the old first row in view, so the moved chat
    // ends up just above the screen. If the user was at the top, scroll up to show it.
    val topChatId = chats.firstOrNull()?.chatId
    LaunchedEffect(topChatId) {
        if (listState.firstVisibleItemIndex <= 1) listState.scrollToItem(0)
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 4.dp, bottom = 88.dp), // the last row can scroll clear of the FAB
    ) {
        items(chats, key = { it.chatId }) { chat ->
            ChatRow(
                chat = chat,
                onClick = { onChatClick(chat.otherUser.uid) },
                modifier = Modifier.animateItem(),
            )
        }
    }
}


@Composable
private fun ChatRow(chat: ChatListItem, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = HedwigTheme.colors
    val hasUnread = chat.unreadCount > 0
    val name = chat.otherUser.name.ifBlank { formatPhoneNumber(chat.otherUser.phoneNumber) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .heightIn(min = 72.dp) // grows with large font sizes instead of clipping
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Avatar(name = chat.otherUser.name, id = chat.otherUser.uid)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = name,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp, lineHeight = 22.sp, fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = rememberChatListTime(chat.lastMessageTime),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        fontWeight = if (hasUnread) FontWeight.Bold else FontWeight.Normal,
                        fontFeatureSettings = "tnum",
                    ),
                    color = if (hasUnread) colors.accentText else colors.secondaryText,
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                chat.lastMessageStatus?.let {
                    StatusIcon(it, dimColor = colors.secondaryText)
                    Spacer(Modifier.width(3.dp))
                }
                Text(
                    text = chat.lastMessage,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        fontWeight = if (hasUnread) FontWeight.SemiBold else FontWeight.Normal,
                    ),
                    color = if (hasUnread) MaterialTheme.colorScheme.onSurface else colors.secondaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (hasUnread) {
                    Spacer(Modifier.width(8.dp))
                    UnreadBadge(chat.unreadCount)
                }
            }
        }
    }
}

@Composable
private fun UnreadBadge(count: Int) {
    val description = pluralStringResource(R.plurals.unread_messages, count, count)
    Box(
        modifier = Modifier
            .defaultMinSize(minWidth = 20.dp, minHeight = 20.dp)
            .background(HedwigTheme.colors.accent, CircleShape)
            .padding(horizontal = 6.dp)
            .clearAndSetSemantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = if (count > 99) "99+" else count.toString(),
            color = Color.White,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 12.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFeatureSettings = "tnum",
            ),
        )
    }
}


private val ShimmerRowWidths = listOf(
    120.dp to 210.dp, 96.dp to 160.dp, 140.dp to 230.dp, 110.dp to 180.dp,
    128.dp to 150.dp, 88.dp to 200.dp, 132.dp to 170.dp, 104.dp to 220.dp,
)

@Composable
private fun ChatsShimmer() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .clearAndSetSemantics { contentDescription = "Loading chats" },
        contentPadding = PaddingValues(top = 4.dp),
        userScrollEnabled = false,
    ) {
        items(ShimmerRowWidths) { (nameWidth, messageWidth) ->
            ShimmerChatRow(nameWidth, messageWidth)
        }
    }
}

/** A ChatRow stand-in: the same padding, avatar and line positions, so nothing shifts when data arrives. */
@Composable
private fun ShimmerChatRow(nameWidth: Dp, messageWidth: Dp) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Shimmer(Modifier.size(48.dp), shape = CircleShape)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Shimmer(Modifier.size(width = nameWidth, height = 14.dp))
                Spacer(Modifier.weight(1f))
                Shimmer(Modifier.size(width = 40.dp, height = 10.dp))
            }
            Shimmer(Modifier.size(width = messageWidth, height = 12.dp))
        }
    }
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
