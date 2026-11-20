package dev.ajaykotian.hedwig

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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


    private var pendingNotificationChatUserId by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        injectDependencies()

        // Only on a fresh start
        if (savedInstanceState == null) handleNotificationIntent(intent)

        setContent {
            HedwigTheme {
                CompositionLocalProvider(LocalApplicationComponent provides applicationComponent) {
                    val startDestination by mainViewModel.startDestination.collectAsStateWithLifecycle()
                    val destination = startDestination

                    if (destination == null) {
                        LoadingScreen()
                    } else {
                        AppNavHost(
                            startDestination = destination,
                            signedOut = mainViewModel.signedOut,
                            pendingNotificationChatUserId = pendingNotificationChatUserId,
                            onPendingChatHandled = { pendingNotificationChatUserId = null },
                        )
                    }
                }
            }
        }
    }

    // singleTop: when the Activity already exists, a notification tap arrives here instead of creating a new one.
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNotificationIntent(intent)
    }

    private fun handleNotificationIntent(intent: Intent) {
        intent.getStringExtra(EXTRA_CHAT_USER_ID)?.let { pendingNotificationChatUserId = it }
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

    companion object {
        const val EXTRA_CHAT_USER_ID = "chat_user_id"
    }
}