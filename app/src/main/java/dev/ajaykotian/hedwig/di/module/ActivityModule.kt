package dev.ajaykotian.hedwig.di.module

import android.content.Context
import androidx.activity.ComponentActivity
import dagger.Module
import dagger.Provides
import dev.ajaykotian.hedwig.di.ActivityContext

@Module
class ActivityModule(private val activity: ComponentActivity) {

    @ActivityContext
    @Provides
    fun provideContext(): Context = activity
}
