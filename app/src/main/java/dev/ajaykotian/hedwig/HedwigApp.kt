package dev.ajaykotian.hedwig

import android.app.Application
import dev.ajaykotian.hedwig.di.component.ApplicationComponent
import dev.ajaykotian.hedwig.di.component.DaggerApplicationComponent
import dev.ajaykotian.hedwig.di.module.ApplicationModule

class HedwigApp : Application() {

    lateinit var applicationComponent: ApplicationComponent
        private set

    override fun onCreate() {
        super.onCreate()
        injectDependencies()
    }

    private fun injectDependencies() {
        applicationComponent = DaggerApplicationComponent.builder()
            .applicationModule(ApplicationModule(this))
            .build()
    }
}