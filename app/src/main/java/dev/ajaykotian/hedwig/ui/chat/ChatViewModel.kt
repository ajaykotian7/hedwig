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
import dev.ajaykotian.hedwig.ui.navigation.ChatRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

data class MessageItem(
    val message: Message,
    val isMine: Boolean,
    //First message in group of consecutive messages by the same sender
    val isFirstInGroup: Boolean,
)

data class ChatUIState(
    val isLoading: Boolean = true,
    val otherUser: User = User(),
    val messages: List<MessageItem> = emptyList(),
    val errorMessage: String? = null,
)

class ChatViewModel(
    savedStateHandle: SavedStateHandle,
    authRepository: AuthRepository,
    userRepository: UserRepository,
    private val chatRepository: ChatRepository,
) : ViewModel() {

    private val otherUserId = savedStateHandle.toRoute<ChatRoute>().otherUserId
    private val myUid = checkNotNull(authRepository.currentUserId) { "ChatViewModel needs a signed-in user" }
    private val chatId = Chat.idFor(myUid, otherUserId)

    var messageText by mutableStateOf("")
        private set

    private val sendError = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ChatUIState> = combine(
        userRepository.observeUser(otherUserId).catch { emit(null) },
        chatRepository.observeMessages(chatId),
        sendError,
    ) { otherUser, messages, error ->
        ChatUIState(
            isLoading = false,
            otherUser = otherUser ?: User(uid = otherUserId),
            messages = messages.mapIndexed { index, message ->
                MessageItem(
                    message = message,
                    isMine = message.senderId == myUid,
                    isFirstInGroup = index == 0 || messages[index - 1].senderId != message.senderId,
                )
            },
            errorMessage = error,
        )
    }
        .catch { e -> emit(ChatUIState(isLoading = false, errorMessage = e.message ?: "Couldn't load messages")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ChatUIState())

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