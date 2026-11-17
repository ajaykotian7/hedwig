package dev.ajaykotian.hedwig.di.component


import dagger.Component
import dev.ajaykotian.hedwig.di.ScreenScope
import dev.ajaykotian.hedwig.di.module.ScreenModule
import dev.ajaykotian.hedwig.ui.chat.ChatViewModel
import dev.ajaykotian.hedwig.ui.chats.ChatsViewModel
import dev.ajaykotian.hedwig.ui.login.LoginViewModel
import dev.ajaykotian.hedwig.ui.newchat.NewChatViewModel
import dev.ajaykotian.hedwig.ui.profile.ProfileViewModel


@ScreenScope
@Component(dependencies = [ApplicationComponent::class], modules = [ScreenModule::class])
interface ScreenComponent {

    fun getLoginViewModel(): LoginViewModel

    fun getProfileViewModel(): ProfileViewModel

    fun getNewChatViewModel(): NewChatViewModel

    fun getChatViewModel(): ChatViewModel

    fun getChatsViewModel(): ChatsViewModel
}

