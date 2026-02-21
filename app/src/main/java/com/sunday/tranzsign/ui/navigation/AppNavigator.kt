package com.sunday.tranzsign.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sunday.tranzsign.ui.feature.main.MainScreen
import com.sunday.tranzsign.ui.feature.withdrawal.WithdrawalScreen

@Composable
fun AppNavigator() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppDestinations.MAIN_ROUTE
    ) {
        composable(AppDestinations.MAIN_ROUTE) {
            MainScreen(
                onNavigateToFeature = { destination ->
                    navController.navigate(destination)
                }
            )
        }

        composable(AppDestinations.WITHDRAWAL_ROUTE) {
            WithdrawalScreen(
                onNavigateToMain = {
                    navController.navigate(AppDestinations.MAIN_ROUTE) {
                        popUpTo(AppDestinations.MAIN_ROUTE) {
                            inclusive = false // Do not save the state of the popped screen
                        }
                        launchSingleTop = true // Do not recreate if main screen already exists
                    }
                }
            )
        }

        // Add other screens like Transfer, Swap etc. here in the future
    }
}