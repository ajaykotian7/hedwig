package dev.ajaykotian.hedwig.di.component


import dagger.Component
import dev.ajaykotian.hedwig.di.ScreenScope
import dev.ajaykotian.hedwig.di.module.ScreenModule
import dev.ajaykotian.hedwig.ui.HelloViewModel


@ScreenScope
@Component(dependencies = [ApplicationComponent::class], modules = [ScreenModule::class])
interface ScreenComponent {

    fun getHelloViewModel() : HelloViewModel
}

