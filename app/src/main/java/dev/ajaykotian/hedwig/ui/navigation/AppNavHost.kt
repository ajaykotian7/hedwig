package dev.ajaykotian.hedwig.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import dev.ajaykotian.hedwig.ui.home.HomeScreen
import dev.ajaykotian.hedwig.ui.login.LoginScreen

@Composable
fun AppNavHost(
    startDestination: Any,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        composable<LoginRoute> {
            LoginScreen(
                onLoggedIn = {
                    navController.navigateAndClearBackStack(HomeRoute)
                },
            )
        }

        composable<ProfileSetupRoute> {
            PlaceholderScreen(
                "Profile setup",
                "Save profile" to { navController.navigateAndClearBackStack(HomeRoute) },
                "Sign Out" to {
                    navController.navigateAndClearBackStack(LoginRoute)
                },
            )
        }

        composable<HomeRoute> {
            HomeScreen(
                onSignOut = {
                    navController.navigateAndClearBackStack(LoginRoute)
                }
            )
        }

        composable<NewChatRoute> {
            PlaceholderScreen(
                "New chat",
                "Pick a contact" to {
                    navController.navigate(ChatRoute(otherUserId = "demo-user")) {
                        popUpTo<NewChatRoute> { inclusive = true }
                    }
                },
                "Sign Out" to {
                    navController.navigateAndClearBackStack(LoginRoute)
                },
            )
        }

        composable<ChatRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<ChatRoute>()
            PlaceholderScreen(
                "Chat with ${route.otherUserId}",
                "Back" to { navController.popBackStack() },
                "Sign Out" to {
                    navController.navigateAndClearBackStack(LoginRoute)
                },
            )
        }
    }
}

private fun NavController.navigateAndClearBackStack(route: Any) {
    navigate(route) {
        popUpTo(graph.id) { inclusive = true }
    }
}
