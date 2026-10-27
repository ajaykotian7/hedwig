package dev.ajaykotian.hedwig.di.module

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModelProvider
import dagger.Module
import dagger.Provides
import dev.ajaykotian.hedwig.MainViewModel
import dev.ajaykotian.hedwig.data.AuthRepository
import dev.ajaykotian.hedwig.di.ActivityContext
import dev.ajaykotian.hedwig.ui.base.ViewModelProviderFactory

@Module
class ActivityModule(private val activity: ComponentActivity) {

    @ActivityContext
    @Provides
    fun provideContext(): Context = activity

    @Provides
    fun provideMainViewModel(authRepository: AuthRepository): MainViewModel =
        ViewModelProvider(activity, ViewModelProviderFactory(MainViewModel::class) {
            MainViewModel(authRepository)
        })[MainViewModel::class.java]
}
