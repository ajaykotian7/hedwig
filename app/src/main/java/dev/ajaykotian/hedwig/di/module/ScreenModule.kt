package dev.ajaykotian.hedwig.di.module

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dev.ajaykotian.hedwig.ui.HelloViewModel
import dev.ajaykotian.hedwig.ui.base.ViewModelProviderFactory


@Module
class ScreenModule(private val owner: ViewModelStoreOwner) {

    @Provides
    fun provideViewModelStoreOwner(): ViewModelStoreOwner = owner

    @Provides
    fun provideHelloViewModel(auth: FirebaseAuth): HelloViewModel =
        ViewModelProvider(owner, ViewModelProviderFactory(HelloViewModel::class) {
            HelloViewModel(auth)
        })[HelloViewModel::class.java]
}