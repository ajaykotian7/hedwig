package dev.ajaykotian.hedwig.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude

/** One message at `chats/{chatId}/messages/{messageId}`. */
data class Message(
    @DocumentId val id: String = "",
    val senderId: String = "",
    val text: String = "",
    val timestamp: Timestamp? = null,
    /** True while the message only exists in the local cache. Computed, never stored. */
    @get:Exclude val isPending: Boolean = false,
)