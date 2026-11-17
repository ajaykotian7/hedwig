package dev.ajaykotian.hedwig.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.ajaykotian.hedwig.ui.chat.ChatScreen
import dev.ajaykotian.hedwig.ui.home.HomeScreen
import dev.ajaykotian.hedwig.ui.login.LoginScreen
import dev.ajaykotian.hedwig.ui.newchat.NewChatScreen
import dev.ajaykotian.hedwig.ui.profile.ProfileSetupScreen
import kotlinx.coroutines.flow.Flow

@Composable
fun AppNavHost(
    startDestination: Any,
    signedOut: Flow<Unit>,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()

    LaunchedEffect(signedOut) {
        signedOut.collect { navController.navigateAndClearBackStack(LoginRoute) }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        composable<LoginRoute> {
            LoginScreen(
                onLoggedIn = {
                    navController.navigateAndClearBackStack(ProfileSetupRoute)
                },
            )
        }

        composable<ProfileSetupRoute> {
            ProfileSetupScreen(
                onProfileSaved = {
                    navController.navigateAndClearBackStack(HomeRoute)
                },
            )
        }

        composable<HomeRoute> {
            HomeScreen(
                onNewChatClick = { navController.navigate(NewChatRoute) },
                onChatClick = { uid -> navController.navigate(ChatRoute(otherUserId = uid)) },
            )
        }

        composable<NewChatRoute> {
            NewChatScreen(
                onBackClick = { navController.popBackStack() },
                onContactClick = { uid ->
                    navController.navigate(ChatRoute(otherUserId = uid)) {
                        popUpTo<NewChatRoute> { inclusive = true }
                    }
                },
            )
        }

        composable<ChatRoute> {
            ChatScreen(onBackClick = { navController.popBackStack() })
        }
    }
}

private fun NavController.navigateAndClearBackStack(route: Any) {
    navigate(route) {
        popUpTo(graph.id) { inclusive = true }
    }
}
