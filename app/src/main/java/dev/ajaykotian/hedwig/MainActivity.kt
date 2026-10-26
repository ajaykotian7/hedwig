package dev.ajaykotian.hedwig

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.ajaykotian.hedwig.di.component.DaggerActivityComponent
import dev.ajaykotian.hedwig.di.module.ActivityModule
import dev.ajaykotian.hedwig.ui.base.LocalApplicationComponent
import dev.ajaykotian.hedwig.ui.components.LoadingScreen
import dev.ajaykotian.hedwig.ui.navigation.AppNavHost
import dev.ajaykotian.hedwig.ui.theme.HedwigTheme
import javax.inject.Inject

class MainActivity : ComponentActivity() {

    @Inject
    lateinit var mainViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        injectDependencies()

        setContent {
            HedwigTheme {
                CompositionLocalProvider(LocalApplicationComponent provides applicationComponent) {
                    val startDestination by mainViewModel.startDestination.collectAsStateWithLifecycle()
                    val destination = startDestination

                    if (destination == null) {
                        LoadingScreen()
                    } else {
                        AppNavHost(startDestination = destination)
                    }
                }
            }
        }
    }

    private val applicationComponent
        get() = (application as HedwigApp).applicationComponent

    private fun injectDependencies() {
        DaggerActivityComponent.builder()
            .applicationComponent(applicationComponent)
            .activityModule(ActivityModule(this))
            .build()
            .inject(this)
    }
}