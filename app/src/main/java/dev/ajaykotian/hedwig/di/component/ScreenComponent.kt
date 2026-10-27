package dev.ajaykotian.hedwig.di.component


import dagger.Component
import dev.ajaykotian.hedwig.di.ScreenScope
import dev.ajaykotian.hedwig.di.module.ScreenModule
import dev.ajaykotian.hedwig.ui.home.HomeViewModel
import dev.ajaykotian.hedwig.ui.login.LoginViewModel


@ScreenScope
@Component(dependencies = [ApplicationComponent::class], modules = [ScreenModule::class])
interface ScreenComponent {

    fun getLoginViewModel(): LoginViewModel

    fun getHomeViewModel(): HomeViewModel
}

