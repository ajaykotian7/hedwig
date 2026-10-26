package dev.ajaykotian.hedwig.di.component


import dagger.Component
import dev.ajaykotian.hedwig.MainActivity
import dev.ajaykotian.hedwig.di.ActivityScope
import dev.ajaykotian.hedwig.di.module.ActivityModule


@ActivityScope
@Component(dependencies = [ApplicationComponent::class], modules = [ActivityModule::class])
interface ActivityComponent {

    fun inject(activity: MainActivity)
}
