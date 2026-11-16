package dev.ajaykotian.hedwig.data.repository

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.toObject
import dev.ajaykotian.hedwig.data.model.Chat
import dev.ajaykotian.hedwig.data.model.Message
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton


interface ChatRepository {
    /** Messages of a chat, oldest first. Empty until the first message is sent. */
    fun observeMessages(chatId: String): Flow<List<Message>>

    /** Writes the message and updates the chat summary in one atomic batch. */
    suspend fun sendMessage(senderId: String, receiverId: String, text: String)
}

@Singleton
class ChatRepositoryImpl @Inject constructor(private val firestore: FirebaseFirestore) : ChatRepository {

    private val chats = firestore.collection(CHATS)
    override fun observeMessages(chatId: String): Flow<List<Message>> = callbackFlow {
        val registration = chats.document(chatId)
            .collection(MESSAGES)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener(MetadataChanges.INCLUDE) { snapshots, exception ->
                if (exception != null) {
                    close(exception)
                    return@addSnapshotListener
                }
                val messages = snapshots?.documents.orEmpty().mapNotNull { documentSnapshot ->
                    documentSnapshot.toObject<Message>(DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)?.copy(isPending = documentSnapshot.metadata.hasPendingWrites())
                }
                trySend(messages)
            }
        awaitClose { registration.remove() }
    }

    override suspend fun sendMessage(senderId: String, receiverId: String, text: String) {
        val chatRef = chats.document(Chat.idFor(senderId, receiverId))
        val messageRef = chatRef.collection(MESSAGES).document()
        firestore.batch()
            .set(
                messageRef,
                mapOf(
                    "senderId" to senderId,
                    "text" to text,
                    "timestamp" to FieldValue.serverTimestamp()
                )
            )
            .set(
                chatRef,
                mapOf(
                    "participants" to listOf(senderId, receiverId).sorted(),
                    "lastMessage" to text,
                    "lastMessageTime" to FieldValue.serverTimestamp(),
                    "lastSenderId" to senderId,
                    "unread" to mapOf(receiverId to FieldValue.increment(1)),
                ),
                SetOptions.merge()
            )
            .commit()
            .await()
    }

    private companion object {
        const val CHATS = "chats"
        const val MESSAGES = "messages"
    }

}