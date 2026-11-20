package dev.ajaykotian.hedwig.di.module

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import dagger.Module
import dagger.Provides
import dev.ajaykotian.hedwig.data.repository.AuthRepository
import dev.ajaykotian.hedwig.data.repository.ChatRepository
import dev.ajaykotian.hedwig.data.repository.UserRepository
import dev.ajaykotian.hedwig.notification.ActiveChatTracker
import dev.ajaykotian.hedwig.notification.NotificationHelper
import dev.ajaykotian.hedwig.ui.base.ViewModelProviderFactory
import dev.ajaykotian.hedwig.ui.chat.ChatViewModel
import dev.ajaykotian.hedwig.ui.chats.ChatsViewModel
import dev.ajaykotian.hedwig.ui.login.LoginViewModel
import dev.ajaykotian.hedwig.ui.newchat.NewChatViewModel
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
    fun provideProfileViewModel(userRepository: UserRepository, authRepository: AuthRepository): ProfileViewModel =
        ViewModelProvider(owner, ViewModelProviderFactory(ProfileViewModel::class) {
            ProfileViewModel(userRepository, authRepository)
        })[ProfileViewModel::class.java]

    @Provides
    fun provideNewChatViewModel(userRepository: UserRepository, authRepository: AuthRepository): NewChatViewModel =
        ViewModelProvider(owner, ViewModelProviderFactory(NewChatViewModel::class) {
            NewChatViewModel(userRepository, authRepository)
        })[NewChatViewModel::class.java]

    @Provides
    fun provideChatViewModel(
        authRepository: AuthRepository,
        userRepository: UserRepository,
        chatRepository: ChatRepository,
        activeChatTracker: ActiveChatTracker,
        notificationHelper: NotificationHelper,
    ): ChatViewModel =
        ViewModelProvider(owner, ViewModelProviderFactory(ChatViewModel::class) { savedStateHandle ->
            ChatViewModel(savedStateHandle, authRepository, userRepository, chatRepository, activeChatTracker, notificationHelper)
        })[ChatViewModel::class.java]

    @Provides
    fun provideChatsViewModel(
        authRepository: AuthRepository,
        userRepository: UserRepository,
        chatRepository: ChatRepository,
    ): ChatsViewModel =
        ViewModelProvider(owner, ViewModelProviderFactory(ChatsViewModel::class) {
            ChatsViewModel(authRepository, userRepository, chatRepository)
        })[ChatsViewModel::class.java]

}