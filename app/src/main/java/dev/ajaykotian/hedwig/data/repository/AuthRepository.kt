package dev.ajaykotian.hedwig.data.repository

import android.app.Activity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton


sealed interface PhoneAuthEvent {
    data class CodeSent(
        val verificationId: String,
        val resendToken: PhoneAuthProvider.ForceResendingToken,
    ) : PhoneAuthEvent

    data class AutoVerified(val credential: PhoneAuthCredential) : PhoneAuthEvent

    data class Failed(val error: Exception) : PhoneAuthEvent
    data class TimedOut(val message: String) : PhoneAuthEvent
}

interface AuthRepository {

    fun observeAuthState(): Flow<String?>

    val currentUserId: String?
    val currentUserPhone: String?

    fun generateOtp(
        phoneNumber: String,
        activity: Activity,
        resendToken: PhoneAuthProvider.ForceResendingToken? = null,
    ): Flow<PhoneAuthEvent>

    suspend fun signInWithCode(verificationId: String, code: String)

    suspend fun signInWithCredential(credential: PhoneAuthCredential)

    fun signOut()
}


@Singleton
class AuthRepositoryImpl @Inject constructor(private val auth: FirebaseAuth) : AuthRepository {

    override fun observeAuthState(): Flow<String?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser?.uid) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }.distinctUntilChanged()

    override val currentUserId: String? get() = auth.currentUser?.uid
    override val currentUserPhone: String? get() = auth.currentUser?.phoneNumber

    override fun generateOtp(phoneNumber: String, activity: Activity, resendToken: PhoneAuthProvider.ForceResendingToken?): Flow<PhoneAuthEvent> = callbackFlow {
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken,
            ) {
                trySend(PhoneAuthEvent.CodeSent(verificationId, token))
            }

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                trySend(PhoneAuthEvent.AutoVerified(credential))
            }

            override fun onVerificationFailed(e: FirebaseException) {
                trySend(PhoneAuthEvent.Failed(e))
            }

            override fun onCodeAutoRetrievalTimeOut(message: String) {
                trySend(PhoneAuthEvent.TimedOut(message))
            }
        }

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber("+91${phoneNumber}")
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .apply { if (resendToken != null) setForceResendingToken(resendToken) }
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)

        awaitClose()
    }

    override suspend fun signInWithCode(verificationId: String, code: String) {
        signInWithCredential(PhoneAuthProvider.getCredential(verificationId, code))
    }

    override suspend fun signInWithCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential).await()
    }

    override fun signOut() {
        auth.signOut()
    }

}