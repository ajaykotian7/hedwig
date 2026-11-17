package dev.ajaykotian.hedwig.notification

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Used to mark messages as read
 */
@Singleton
class ActiveChatTracker @Inject constructor() {

    private val _activeChatId = MutableStateFlow<String?>(null)
    val activeChatId: StateFlow<String?> = _activeChatId.asStateFlow()

    fun onChatVisible(chatId: String) {
        _activeChatId.value = chatId
    }

    fun onChatHidden(chatId: String) {
        // Only clear it if it's still this chat: another chat may already have become visible.
        _activeChatId.compareAndSet(chatId, null)
    }
}
