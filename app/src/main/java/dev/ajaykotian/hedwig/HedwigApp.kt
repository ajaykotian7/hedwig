package dev.ajaykotian.hedwig

import android.app.Application
import dev.ajaykotian.hedwig.di.component.ApplicationComponent
import dev.ajaykotian.hedwig.di.component.DaggerApplicationComponent
import dev.ajaykotian.hedwig.di.module.ApplicationModule
import dev.ajaykotian.hedwig.notification.MessageNotifier
import dev.ajaykotian.hedwig.notification.NotificationHelper
import javax.inject.Inject

class HedwigApp : Application() {

    lateinit var applicationComponent: ApplicationComponent
        private set

    @Inject
    lateinit var notificationHelper: NotificationHelper

    @Inject
    lateinit var messageNotifier: MessageNotifier

    override fun onCreate() {
        super.onCreate()
        injectDependencies()
        notificationHelper.createChannels()
        messageNotifier.start()
    }

    private fun injectDependencies() {
        applicationComponent = DaggerApplicationComponent.builder()
            .applicationModule(ApplicationModule(this))
            .build()
        applicationComponent.inject(this)
    }
}