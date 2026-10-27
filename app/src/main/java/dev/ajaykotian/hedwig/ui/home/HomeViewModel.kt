package dev.ajaykotian.hedwig.ui.home

import androidx.lifecycle.ViewModel
import dev.ajaykotian.hedwig.data.AuthRepository

class HomeViewModel(private val authRepository: AuthRepository) : ViewModel() {

    val currentUserId: String? get() = authRepository.currentUserId
    val currentUserPhone: String? get() = authRepository.currentUserPhone

    fun signOut() {
        authRepository.signOut()
    }
}