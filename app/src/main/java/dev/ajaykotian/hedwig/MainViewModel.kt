package dev.ajaykotian.hedwig

import androidx.lifecycle.ViewModel
import dev.ajaykotian.hedwig.data.AuthRepository
import dev.ajaykotian.hedwig.ui.navigation.HomeRoute
import dev.ajaykotian.hedwig.ui.navigation.LoginRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _startDestination = MutableStateFlow<Any?>(null)
    val startDestination: StateFlow<Any?> = _startDestination.asStateFlow()

    init {
        _startDestination.value = if (authRepository.currentUserId == null) LoginRoute else HomeRoute
    }
}
