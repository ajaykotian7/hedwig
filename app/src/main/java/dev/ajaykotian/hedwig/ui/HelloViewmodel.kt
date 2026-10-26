package dev.ajaykotian.hedwig.ui

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class HelloViewModel(auth: FirebaseAuth) : ViewModel() {
    val message: String = auth.currentUser?.uid ?: "signed out"
}