package dev.ajaykotian.hedwig.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import dev.ajaykotian.hedwig.data.model.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

interface UserRepository {
    /** Live Profile */
    fun observeUser(uid: String): Flow<User?>

    /** One-time read (used for start routing). Falls back to the local cache when offline. */
    suspend fun getUser(uid: String): User?

    /** Every registered user, sorted by name. Used later by the New Chat screen. */
    fun observeAllUsers(): Flow<List<User>>

    /** Creates or updates users/{uid}; only the given fields are written (merge). */
    suspend fun saveUser(user: User)
}


@Singleton
class UserRepositoryImpl @Inject constructor(
    firestore: FirebaseFirestore,
) : UserRepository {

    private val users = firestore.collection(USERS)

    override fun observeUser(uid: String): Flow<User?> = callbackFlow {
        val registration = users.document(uid).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            trySend(snapshot?.toObject<User>())
        }
        awaitClose { registration.remove() }
    }

    override suspend fun getUser(uid: String): User? =
        users.document(uid).get().await().toObject<User>()

    override fun observeAllUsers(): Flow<List<User>> = callbackFlow {
        val registration = users.orderBy("name").addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            trySend(snapshot?.toObjects<User>().orEmpty())
        }
        awaitClose { registration.remove() }
    }

    override suspend fun saveUser(user: User) {
        // @DocumentId isn't written as a field: the uid lives only in the document path.
        users.document(user.uid).set(user, SetOptions.merge()).await()
    }

    private companion object {
        const val USERS = "users"
    }
}