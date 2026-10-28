package dev.ajaykotian.hedwig.di.module

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import dagger.Module
import dagger.Provides
import dev.ajaykotian.hedwig.data.repository.AuthRepository
import dev.ajaykotian.hedwig.data.repository.UserRepository
import dev.ajaykotian.hedwig.ui.base.ViewModelProviderFactory
import dev.ajaykotian.hedwig.ui.home.HomeViewModel
import dev.ajaykotian.hedwig.ui.login.LoginViewModel
import dev.ajaykotian.hedwig.ui.profile.ProfileViewModel


@Module
class ScreenModule(private val owner: ViewModelStoreOwner) {

    @Provides
    fun provideViewModelStoreOwner(): ViewModelStoreOwner = owner

    @Provides
    fun provideLoginViewModel(authRepository: AuthRepository): LoginViewModel =
        ViewModelProvider(owner, ViewModelProviderFactory(LoginViewModel::class) {
            LoginViewModel(authRepository)
        })[LoginViewModel::class.java]


    @Provides
    fun provideHomeViewModel(authRepository: AuthRepository): HomeViewModel =
        ViewModelProvider(owner, ViewModelProviderFactory(HomeViewModel::class) {
            HomeViewModel(authRepository)
        })[HomeViewModel::class.java]

    @Provides
    fun provideProfileViewModel(userRepository: UserRepository, authRepository: AuthRepository): ProfileViewModel =
        ViewModelProvider(owner, ViewModelProviderFactory(ProfileViewModel::class) {
            ProfileViewModel(userRepository, authRepository)
        })[ProfileViewModel::class.java]
}