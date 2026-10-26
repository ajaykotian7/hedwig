package dev.ajaykotian.hedwig

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.ajaykotian.hedwig.di.component.DaggerActivityComponent
import dev.ajaykotian.hedwig.di.module.ActivityModule
import dev.ajaykotian.hedwig.ui.HelloViewModel
import dev.ajaykotian.hedwig.ui.base.LocalApplicationComponent
import dev.ajaykotian.hedwig.ui.base.screenViewModel
import dev.ajaykotian.hedwig.ui.components.Avatar
import dev.ajaykotian.hedwig.ui.theme.HedwigTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        injectDependencies()

        setContent {
            HedwigTheme {
                CompositionLocalProvider(LocalApplicationComponent provides applicationComponent) {
                    DummyChatUI()
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

@Preview
@Composable
fun DummyChatUI(helloViewModel: HelloViewModel = screenViewModel { getHelloViewModel() }) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Design system",
                style = MaterialTheme.typography.titleLarge,
                color = HedwigTheme.colors.accent,
            )
            Text(
                text = helloViewModel.message,
                style = MaterialTheme.typography.bodyLarge,
                color = HedwigTheme.colors.secondaryText,
            )
            Row {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Avatar(name = "Jane Doe", id = "user-1")
                    Avatar(name = "John Smith", id = "user-2")
                    Avatar(name = "", id = "user-3")
                }
            }
        }
    }
}