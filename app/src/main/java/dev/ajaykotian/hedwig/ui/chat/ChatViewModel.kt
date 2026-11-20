package dev.ajaykotian.hedwig.ui.chat

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dev.ajaykotian.hedwig.data.model.Chat
import dev.ajaykotian.hedwig.data.model.Message
import dev.ajaykotian.hedwig.data.model.User
import dev.ajaykotian.hedwig.data.repository.AuthRepository
import dev.ajaykotian.hedwig.data.repository.ChatRepository
import dev.ajaykotian.hedwig.data.repository.UserRepository
import dev.ajaykotian.hedwig.notification.ActiveChatTracker
import dev.ajaykotian.hedwig.notification.NotificationHelper
import dev.ajaykotian.hedwig.ui.base.toLocalDate
import dev.ajaykotian.hedwig.ui.navigation.ChatRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlin.coroutines.cancellation.CancellationException

enum class MessageStatus { Pending, Sent, Read }


sealed interface ChatEntry {
    /** LazyColumn key. */
    val key: String

    data class DayHeader(val date: LocalDate) : ChatEntry {
        override val key: String get() = "day-$date"
    }

    data class MessageItem(
        val message: Message,
        val isMine: Boolean,
        // First message in a group of consecutive messages by the same sender on the same day
        val isFirstInGroup: Boolean,
        /** Ticks for my messages; null for theirs. */
        val status: MessageStatus?,
    ) : ChatEntry {
        override val key: String get() = message.id
    }
}

data class ChatUIState(
    val isLoading: Boolean = true,
    val otherUser: User = User(),
    val entries: List<ChatEntry> = emptyList(),
    val errorMessage: String? = null,
)

class ChatViewModel(
    savedStateHandle: SavedStateHandle,
    authRepository: AuthRepository,
    userRepository: UserRepository,
    private val chatRepository: ChatRepository,
    private val activeChatTracker: ActiveChatTracker,
    private val notificationHelper: NotificationHelper,
) : ViewModel() {

    private val otherUserId = savedStateHandle.toRoute<ChatRoute>().otherUserId
    private val myUid = checkNotNull(authRepository.currentUserId) { "ChatViewModel needs a signed-in user" }
    private val chatId = Chat.idFor(myUid, otherUserId)

    var messageText by mutableStateOf("")
        private set

    private val sendError = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ChatUIState> = combine(
        userRepository.observeUser(otherUserId).catch { emit(null) },
        chatRepository.observeChat(chatId).catch { emit(null) },
        chatRepository.observeMessages(chatId),
        sendError,
    ) { otherUser, chat, messages, error ->
        ChatUIState(
            isLoading = false,
            otherUser = otherUser ?: User(uid = otherUserId),
            entries = buildEntries(messages, unreadByOther = chat?.unreadCountFor(otherUserId) ?: 0),
            errorMessage = error,
        )
    }
        .catch { e -> emit(ChatUIState(isLoading = false, errorMessage = e.message ?: "Couldn't load messages")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ChatUIState())

    init {
        // While this chat is on screen, incoming messages are read as soon as they arrive.
        viewModelScope.launch {
            combine(chatRepository.observeChat(chatId), activeChatTracker.activeChatId) { chat, activeChatId ->
                activeChatId == chatId && chat != null && chat.unreadCountFor(myUid) > 0
            }
                .filter { it }
                .catch { e -> Log.w(TAG, "Read tracking stopped", e) }
                .collect {
                    try {
                        chatRepository.markAsRead(chatId, myUid)
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        Log.w(TAG, "Mark as read failed", e)
                    }
                }
        }
    }

    fun onScreenVisible() {
        activeChatTracker.onChatVisible(chatId)
        notificationHelper.cancelChatNotification(chatId)
    }

    fun onScreenHidden() = activeChatTracker.onChatHidden(chatId)

    override fun onCleared() = activeChatTracker.onChatHidden(chatId)


    private fun buildEntries(messages: List<Message>, unreadByOther: Int): List<ChatEntry> {
        // The other user's unread count N means my last N messages haven't been read yet.
        val unreadIds = messages.filter { it.senderId == myUid }
            .takeLast(unreadByOther)
            .mapTo(HashSet()) { it.id }

        val entries = mutableListOf<ChatEntry>()
        var currentDay: LocalDate? = null
        var previousSenderId: String? = null
        for (message in messages) {
            val isMine = message.senderId == myUid
            var isFirstInGroup = message.senderId != previousSenderId
            val day = message.timestamp?.toLocalDate()
            if (day != null && day != currentDay) {
                entries += ChatEntry.DayHeader(day)
                currentDay = day
                isFirstInGroup = true // a new day starts a new group
            }
            val status = when {
                !isMine -> null
                message.isPending -> MessageStatus.Pending
                message.id in unreadIds -> MessageStatus.Sent
                else -> MessageStatus.Read
            }
            entries += ChatEntry.MessageItem(message, isMine, isFirstInGroup, status)
            previousSenderId = message.senderId
        }
        return entries
    }

    fun onMessageTextChange(text: String) {
        messageText = text
    }

    fun sendMessage() {
        val text = messageText.trim()
        if (text.isEmpty()) return
        messageText = "" // clear right away. The local cache shows the message now
        send(text, restoreOnFailure = true)
    }

    fun sendWave() = send("👋", restoreOnFailure = false)

    private fun send(text: String, restoreOnFailure: Boolean) {
        sendError.value = null
        viewModelScope.launch {
            try {
                chatRepository.sendMessage(senderId = myUid, receiverId = otherUserId, text = text)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "Send failed", e) // the raw reason is for logcat, not the user
                sendError.value = if (restoreOnFailure) "Message not sent. Tap send to try again." else "Message not sent."
                if (restoreOnFailure && messageText.isEmpty()) messageText = text
            }
        }
    }
}

private const val TAG = "ChatViewModel"