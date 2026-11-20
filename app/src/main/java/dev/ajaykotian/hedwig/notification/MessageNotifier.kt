package dev.ajaykotian.hedwig.notification

import android.util.Log
import dev.ajaykotian.hedwig.data.repository.AuthRepository
import dev.ajaykotian.hedwig.data.repository.ChatRepository
import dev.ajaykotian.hedwig.data.repository.UserRepository
import dev.ajaykotian.hedwig.ui.base.formatPhoneNumber
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

/**
 * Posts a local notification when a new message arrives. The free-plan alternative to FCM:
 * a Firestore listener stays open while the app's process is alive (foreground, or background
 * until Android reclaims it). Messages that arrive while the process is dead aren't notified.
 */
@Singleton
class MessageNotifier @Inject constructor(
    private val authRepository: AuthRepository,
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository,
    private val activeChatTracker: ActiveChatTracker,
    private val notificationHelper: NotificationHelper,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    fun start() {
        scope.launch {
            // collectLatest cancels the previous user's listener on sign-out or account switch.
            authRepository.observeAuthState().collectLatest { uid ->
                if (uid == null) notificationHelper.cancelAll() else listenForMessages(uid)
            }
        }
    }

    private suspend fun listenForMessages(uid: String) {
        // The latest message time already seen, per chat. The first snapshot only fills this
        // map: it's history, not new messages.
        val lastSeen = mutableMapOf<String, Long>()
        var isFirstSnapshot = true

        chatRepository.observeChats(uid)
            .catch { Log.w(TAG, "Stopped listening for new messages", it) }
            .collect { chats ->
                for (chat in chats) {
                    if (chat.unreadCountFor(uid) == 0) notificationHelper.cancelChatNotification(chat.id)

                    val time = chat.lastMessageTime?.toDate()?.time ?: continue
                    val previous = lastSeen.put(chat.id, time)

                    val isNewMessage = !isFirstSnapshot && (previous == null || time > previous)
                    val shouldNotify = isNewMessage &&
                        chat.lastSenderId != uid &&
                        chat.unreadCountFor(uid) > 0 &&
                        activeChatTracker.activeChatId.value != chat.id

                    if (shouldNotify) {
                        val sender = try {
                            userRepository.getUser(chat.lastSenderId)
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: Exception) {
                            null
                        }
                        notificationHelper.showMessageNotification(
                            chatId = chat.id,
                            senderId = chat.lastSenderId,
                            senderName = sender?.name?.ifBlank { null } ?: sender?.phoneNumber?.let(::formatPhoneNumber) ?: "New message",
                            message = chat.lastMessage,
                            timestamp = time,
                        )
                    }
                }
                isFirstSnapshot = false
            }
    }

    private companion object {
        const val TAG = "MessageNotifier"
    }
}
