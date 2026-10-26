package dev.ajaykotian.hedwig.ui.base

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import kotlin.reflect.KClass


class ViewModelProviderFactory<T : ViewModel>(
    private val kClass: KClass<T>,
    private val creator: (SavedStateHandle) -> T,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <VM : ViewModel> create(modelClass: Class<VM>, extras: CreationExtras): VM {
        require(modelClass.isAssignableFrom(kClass.java)) { "Unknown ViewModel class: ${modelClass.name}" }
        return creator(extras.createSavedStateHandle()) as VM
    }
}
