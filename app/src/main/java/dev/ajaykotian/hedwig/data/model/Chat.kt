package dev.ajaykotian.hedwig.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

/**
 * A one-to-one conversation summary at `chats/{chatId}`, used by the chats list.
 * The messages themselves live in the `chats/{chatId}/messages` sub-collection.
 */
data class Chat(
    @DocumentId val id: String = "",
    val participants: List<String> = emptyList(),
    val lastMessage: String = "",
    val lastMessageTime: Timestamp? = null,
    val lastSenderId: String = "",
    /** Unread message count, keyed by user id. */
    val unread: Map<String, Long> = emptyMap(),
) {
    companion object {
        /** Both users derive the same id without a lookup: the two uids, sorted, joined by "_". */
        fun idFor(uid1: String, uid2: String): String = listOf(uid1, uid2).sorted().joinToString("_")
    }
}