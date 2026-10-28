package dev.ajaykotian.hedwig

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.ajaykotian.hedwig.data.repository.AuthRepository
import dev.ajaykotian.hedwig.data.repository.UserRepository
import dev.ajaykotian.hedwig.ui.navigation.HomeRoute
import dev.ajaykotian.hedwig.ui.navigation.LoginRoute
import dev.ajaykotian.hedwig.ui.navigation.ProfileSetupRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class MainViewModel(private val authRepository: AuthRepository, private val userRepository: UserRepository) : ViewModel() {

    private val _startDestination = MutableStateFlow<Any?>(null)
    val startDestination: StateFlow<Any?> = _startDestination.asStateFlow()

    init {
        viewModelScope.launch {
            _startDestination.value = resolveStartDestination()
        }
    }

    private suspend fun resolveStartDestination(): Any {
        val uid = authRepository.currentUserId ?: return LoginRoute
        val user = try {
            userRepository.getUser(uid)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Offline with nothing cached, so we can't tell. Let a signed-in user in rather than block them.
            return HomeRoute
        }
        return if (user?.name.isNullOrBlank()) ProfileSetupRoute else HomeRoute
    }
}
