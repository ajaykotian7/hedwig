package dev.ajaykotian.hedwig.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.ajaykotian.hedwig.data.model.User
import dev.ajaykotian.hedwig.data.repository.AuthRepository
import dev.ajaykotian.hedwig.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

data class ProfileUIState(
    val isLoading: Boolean = true,
    val user: User = User(),
    val nameDraft: String = "",
    val aboutDraft: String = User.DEFAULT_ABOUT,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null,
) {
    val canSave: Boolean get() = nameDraft.isNotBlank() && !isSaving

    companion object {
        const val MAX_NAME_LENGTH = 25
        const val MAX_ABOUT_LENGTH = 139
    }
}

class ProfileViewModel(private val userRepository: UserRepository, private val authRepository: AuthRepository) : ViewModel() {

    private val uid = checkNotNull(authRepository.currentUserId) { "ProfileViewModel needs a signed-in user" }
    private val _uiState = MutableStateFlow(
        ProfileUIState(
            user = User(
                uid = uid,
                phoneNumber = authRepository.currentUserPhone.orEmpty()
            )
        )
    )

    val uiState: StateFlow<ProfileUIState> = _uiState.asStateFlow()

    private var draftsInitialized = false

    init {
        viewModelScope.launch {
            userRepository.observeUser(uid)
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Couldn't fetch your profile") }
                }
                .collect { user ->
                    _uiState.update { state ->
                        val current = user ?: state.user
                        if (draftsInitialized) {
                            state.copy(isLoading = false, user = current)
                        } else {
                            state.copy(isLoading = false, user = current, nameDraft = current.name, aboutDraft = current.about)
                        }
                    }
                    draftsInitialized = true
                }
        }
    }

    fun onNameDraftChanged(value: String) {
        _uiState.update { it.copy(nameDraft = value.take(ProfileUIState.MAX_NAME_LENGTH), errorMessage = null) }
    }

    fun onAboutDraftChanged(value: String) {
        val about = value.replace('\n', ' ').take(ProfileUIState.MAX_ABOUT_LENGTH)
        _uiState.update { it.copy(aboutDraft = about, errorMessage = null) }
    }

    fun saveProfile() {
        val state = _uiState.value
        if (!state.canSave) return
        save(
            state.user.copy(
                name = state.nameDraft.trim(),
                about = state.aboutDraft.trim().ifBlank { User.DEFAULT_ABOUT },
            )
        )
    }

    /** Profile tab: saves just the name. */
    fun updateName(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty() || _uiState.value.isLoading) return
        save(_uiState.value.user.copy(name = trimmed.take(ProfileUIState.MAX_NAME_LENGTH)))
    }

    /** Profile tab: saves just the 'about'; blank falls back to the default. */
    fun updateAbout(about: String) {
        if (_uiState.value.isLoading) return
        val cleaned = about.replace('\n', ' ').trim().take(ProfileUIState.MAX_ABOUT_LENGTH)
        save(_uiState.value.user.copy(about = cleaned.ifBlank { User.DEFAULT_ABOUT }))
    }

    fun signOut() {
        authRepository.signOut()
    }

    private fun save(user: User) {
        _uiState.update { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                userRepository.saveUser(user)
                _uiState.update { it.copy(isSaving = false, isSaved = true) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, errorMessage = e.message ?: "Couldn't save your profile.") }
            }
        }
    }

}