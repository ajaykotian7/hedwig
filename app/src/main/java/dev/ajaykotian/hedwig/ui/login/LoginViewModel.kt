package dev.ajaykotian.hedwig.ui.login

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthProvider
import dev.ajaykotian.hedwig.data.AuthRepository
import dev.ajaykotian.hedwig.data.PhoneAuthEvent
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.milliseconds

data class LoginUIState(
    val mobileNumber: String = "",
    val otp: String = "",
    val isOtpGenerated: Boolean = false,
    val isOtpValidated: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val resendCountdown: Int = 0
) {
    val canSubmitMobile: Boolean get() = mobileNumber.length == MOBILE_LENGTH && !isLoading
    val canSubmitOtp: Boolean get() = otp.length == OTP_LENGTH && !isLoading
    val canResend: Boolean get() = resendCountdown == 0 && !isLoading

    companion object {
        const val MOBILE_LENGTH = 10
        const val OTP_LENGTH = 6
    }
}

class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUIState())

    val uiState: StateFlow<LoginUIState> = _uiState.asStateFlow()


    private var verificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null
    private var codeSentToNumber: String? = null


    private var generateOtpJob: Job? = null

    fun onMobileNumberChanged(value: String) {
        val digits = value.filter(Char::isDigit).take(LoginUIState.MOBILE_LENGTH)
        _uiState.update { it.copy(mobileNumber = digits, errorMessage = null) }
    }

    fun onOtpChanged(value: String) {
        val current = _uiState.value
        val typedOverError = current.errorMessage != null && value.length > current.otp.length
        val otp = (if (typedOverError) value.takeLast(1) else value)
            .filter(Char::isDigit)
            .take(LoginUIState.OTP_LENGTH)
        _uiState.update { it.copy(otp = otp, errorMessage = null) }
        if (otp.length == LoginUIState.OTP_LENGTH) validateOtp()
    }

    private var countdownJob: Job? = null

    private fun startResendCountdown() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            for (seconds in RESEND_DELAY_SECONDS downTo 0) {
                _uiState.update { it.copy(resendCountdown = seconds) }
                if (seconds > 0) delay(1_000.milliseconds)
            }
        }
    }

    fun generateOtp(activity: Activity) {
        if (!_uiState.value.canSubmitMobile) return

        val number = _uiState.value.mobileNumber
        /* If user clicks on edit and tries to submit the same number within timeout window
        * Firebase auth assumes same otp can be reused and doesn't give callback as well so infinite loader
        * So we are passing the resendToken if the previous number and new number is same */
        val token = if (number == codeSentToNumber) resendToken else null
        requestOtp(activity, token)
    }

    fun resendOtp(activity: Activity) {
        if (!_uiState.value.canResend) return
        requestOtp(activity, resendToken)
    }

    private fun requestOtp(activity: Activity, resendTokenToSend: PhoneAuthProvider.ForceResendingToken? = null) {
        if (_uiState.value.isLoading) return
        generateOtpJob?.cancel()
        generateOtpJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            authRepository.generateOtp(_uiState.value.mobileNumber, activity, resendTokenToSend).collect { event ->
                when (event) {
                    is PhoneAuthEvent.CodeSent -> {
                        verificationId = event.verificationId
                        resendToken = event.resendToken
                        codeSentToNumber = _uiState.value.mobileNumber
                        _uiState.update { it.copy(isOtpGenerated = true, isLoading = false, isOtpValidated = false, otp = "") }
                        startResendCountdown()
                    }

                    is PhoneAuthEvent.AutoVerified -> {
                        signIn { authRepository.signInWithCredential(event.credential) }
                    }

                    is PhoneAuthEvent.Failed -> {
                        _uiState.update { it.copy(isLoading = false, errorMessage = event.error.toUserMessage()) }
                    }

                    is PhoneAuthEvent.TimedOut -> {
                        if (!_uiState.value.isOtpGenerated) {
                            _uiState.update { it.copy(isLoading = false, errorMessage = "Couldn't send the code. Please try again.") }
                        }
                    }
                }
            }
        }
    }

    fun validateOtp() {
        if (_uiState.value.isLoading || _uiState.value.otp.length != LoginUIState.OTP_LENGTH) return
        val id = verificationId ?: return
        val code = _uiState.value.otp
        signIn { authRepository.signInWithCode(id, code) }
    }

    private fun signIn(block: suspend () -> Unit) {
        if (_uiState.value.isOtpValidated) return
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                block()
                _uiState.update { it.copy(isLoading = false, isOtpValidated = true) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.toUserMessage()) }
            }
        }
    }

    fun editMobileNumber() {
        generateOtpJob?.cancel()
        countdownJob?.cancel()
        verificationId = null
        _uiState.update {
            it.copy(isOtpGenerated = false, otp = "", resendCountdown = 0, isLoading = false, errorMessage = null)
        }
    }


    private fun Exception.toUserMessage(): String = when (this) {
        is FirebaseAuthInvalidCredentialsException ->
            if (_uiState.value.isOtpGenerated) "The code you entered is incorrect." else "Invalid phone number."

        is FirebaseTooManyRequestsException -> "Too many attempts. Please try again later."
        else -> localizedMessage ?: "Something went wrong. Please try again."
    }


    private companion object {
        const val RESEND_DELAY_SECONDS = 60
    }
}