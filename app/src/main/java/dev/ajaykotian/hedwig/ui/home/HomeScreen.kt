package dev.ajaykotian.hedwig.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.ajaykotian.hedwig.ui.base.screenViewModel

@Composable
fun HomeScreen(homeViewModel: HomeViewModel = screenViewModel { getHomeViewModel() }, onSignOut: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
        Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Home Screen for ${homeViewModel.currentUserPhone ?: "User"}")
            Button(onClick = {
                homeViewModel.signOut()
                onSignOut()
            }) { Text("Sign Out") }
        }
    }
}
