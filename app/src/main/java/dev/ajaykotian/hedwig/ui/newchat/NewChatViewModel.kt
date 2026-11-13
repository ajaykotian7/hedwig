package dev.ajaykotian.hedwig.ui.newchat

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.ajaykotian.hedwig.data.model.User
import dev.ajaykotian.hedwig.data.repository.AuthRepository
import dev.ajaykotian.hedwig.data.repository.UserRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn


data class NewChatUIState(
    val isLoading: Boolean = true,
    val query: String = "",
    val users: List<User> = emptyList(),
    val errorMessage: String? = null,
)

class NewChatViewModel(
    userRepository: UserRepository,
    authRepository: AuthRepository
) : ViewModel() {

    private val uid = checkNotNull(authRepository.currentUserId) { "NewChatViewModel needs a signed-in user" }
    var query by mutableStateOf("")
        private set

    val uiState: StateFlow<NewChatUIState> =
        combine(userRepository.observeAllUsers(), snapshotFlow { query }) { users, query ->
            // Everyone except you, and only people who finished profile setup.
            val contacts = users
                .filter { it.uid != uid && it.name.isNotBlank() }
                .sortedBy { it.name.lowercase() }
            NewChatUIState(isLoading = false, query = query, users = contacts.matching(query))
        }
            .catch { e -> emit(NewChatUIState(isLoading = false, errorMessage = e.message ?: "Couldn't load contacts")) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NewChatUIState())

    fun onQueryChange(value: String) {
        query = value
    }
}

/**
 * Returns if Name contains query (ignore case) or Phone contains digits from the query
 */
private fun List<User>.matching(query: String): List<User> {
    val q = query.trim()
    if (q.isEmpty()) return this
    val digits = q.filter { it.isDigit() }
    return filter { user ->
        user.name.contains(q, ignoreCase = true) ||
                (digits.isNotEmpty() && user.phoneNumber.contains(digits))
    }
}