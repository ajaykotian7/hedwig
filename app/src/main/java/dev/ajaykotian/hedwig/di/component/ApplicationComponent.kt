package dev.ajaykotian.hedwig.di.component

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Component
import dev.ajaykotian.hedwig.HedwigApp
import dev.ajaykotian.hedwig.data.repository.AuthRepository
import dev.ajaykotian.hedwig.data.repository.ChatRepository
import dev.ajaykotian.hedwig.data.repository.UserRepository
import dev.ajaykotian.hedwig.di.ApplicationContext
import dev.ajaykotian.hedwig.di.module.ApplicationModule
import dev.ajaykotian.hedwig.notification.ActiveChatTracker
import dev.ajaykotian.hedwig.notification.NotificationHelper
import javax.inject.Singleton


@Singleton
@Component(modules = [ApplicationModule::class])
interface ApplicationComponent {

    @ApplicationContext
    fun getContext(): Context

    fun getFirebaseAuth(): FirebaseAuth

    fun getFirebaseFirestore(): FirebaseFirestore

    fun getAuthRepository(): AuthRepository

    fun getUserRepository(): UserRepository

    fun getChatRepository(): ChatRepository

    fun getActiveChatTracker(): ActiveChatTracker

    fun getNotificationHelper(): NotificationHelper

    fun inject(application: HedwigApp)
}
