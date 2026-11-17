package dev.ajaykotian.hedwig.ui.chats

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import dev.ajaykotian.hedwig.data.model.User
import dev.ajaykotian.hedwig.data.repository.AuthRepository
import dev.ajaykotian.hedwig.data.repository.ChatRepository
import dev.ajaykotian.hedwig.data.repository.UserRepository
import dev.ajaykotian.hedwig.ui.chat.MessageStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn


/** A row of the chats list: the chat summary joined with the other user's profile. */
data class ChatListItem(
    val chatId: String,
    val otherUser: User,
    val lastMessage: String,
    val lastMessageTime: Timestamp?,
    /** The tick for my last message: Sent or Read. Null when the last message is theirs. */
    val lastMessageStatus: MessageStatus?,
    val unreadCount: Int,
)

data class ChatsUIState(
    val isLoading: Boolean = true,
    val chats: List<ChatListItem> = emptyList(),
    val errorMessage: String? = null,
)

class ChatsViewModel(
    authRepository: AuthRepository,
    userRepository: UserRepository,
    chatRepository: ChatRepository,
) : ViewModel() {

    private val myUid = checkNotNull(authRepository.currentUserId) { "ChatsViewModel needs a signed-in user" }

    val uiState: StateFlow<ChatsUIState> = combine(
        chatRepository.observeChats(myUid),
        userRepository.observeAllUsers(),
    ) { chats, users ->
        val usersById = users.associateBy { it.uid }
        ChatsUIState(
            isLoading = false,
            chats = chats.map { chat ->
                val otherUid = chat.otherParticipant(myUid)
                ChatListItem(
                    chatId = chat.id,
                    otherUser = usersById[otherUid] ?: User(uid = otherUid),
                    lastMessage = chat.lastMessage,
                    lastMessageTime = chat.lastMessageTime,
                    lastMessageStatus = when {
                        chat.lastSenderId != myUid -> null
                        chat.unreadCountFor(otherUid) == 0 -> MessageStatus.Read
                        else -> MessageStatus.Sent
                    },
                    unreadCount = chat.unreadCountFor(myUid),
                )
            },
        )
    }
        .catch { e ->
            Log.w(TAG, "Chats failed to load", e)
            emit(ChatsUIState(isLoading = false, errorMessage = "Couldn't load chats"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ChatsUIState())
}

private const val TAG = "ChatsViewModel"