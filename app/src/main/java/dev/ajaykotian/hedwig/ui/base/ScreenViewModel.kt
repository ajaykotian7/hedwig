package dev.ajaykotian.hedwig.ui.base

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import dev.ajaykotian.hedwig.di.component.ApplicationComponent
import dev.ajaykotian.hedwig.di.component.DaggerScreenComponent
import dev.ajaykotian.hedwig.di.component.ScreenComponent
import dev.ajaykotian.hedwig.di.module.ScreenModule


val LocalApplicationComponent = staticCompositionLocalOf<ApplicationComponent> {
    error("No ApplicationComponent provided")
}


@Composable
fun rememberScreenComponent(): ScreenComponent {
    val applicationComponent = LocalApplicationComponent.current
    val owner = checkNotNull(LocalViewModelStoreOwner.current) { "No ViewModelStoreOwner" }
    return remember(applicationComponent, owner) {
        DaggerScreenComponent.builder()
            .applicationComponent(applicationComponent)
            .screenModule(ScreenModule(owner))
            .build()
    }
}

@Composable
inline fun <VM : ViewModel> screenViewModel(crossinline get: ScreenComponent.() -> VM): VM {
    val component = rememberScreenComponent()
    return remember(component) { component.get() }
}
