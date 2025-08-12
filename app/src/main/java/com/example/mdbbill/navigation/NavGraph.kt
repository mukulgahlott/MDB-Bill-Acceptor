package com.example.mdbbill.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.mdbbill.ui.screens.AdminPanelScreen
import com.example.mdbbill.ui.screens.AmountSelectionScreen
import com.example.mdbbill.ui.screens.PasswordScreen
import com.example.mdbbill.ui.screens.WelcomeScreen
import com.example.mdbbill.viewmodel.AdminViewModel
import com.example.mdbbill.viewmodel.PaymentViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    adminViewModel: AdminViewModel,
    paymentViewModel: PaymentViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Welcome.route
    ) {
        composable(Screen.Welcome.route) {
            WelcomeScreen(
                onNavigateToPassword = {
                    navController.navigate(Screen.Password.route)
                },
                onNavigateToAmountSelection = {
                    navController.navigate(Screen.AmountSelection.route)
                }
            )
        }
        
        composable(Screen.Password.route) {
            PasswordScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.AdminPanel.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = false }
                    }
                },
                onBack = {
                    navController.popBackStack()
                },
                viewModel = adminViewModel
            )
        }
        
        composable(Screen.AdminPanel.route) {
            AdminPanelScreen(
                viewModel = adminViewModel,
                onBack = {
                    navController.popBackStack()
                },
                onNavigateToWelcome = {
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.AmountSelection.route) {
            AmountSelectionScreen(
                viewModel = paymentViewModel,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

sealed class Screen(val route: String) {
    object Welcome : Screen("welcome")
    object Password : Screen("password")
    object AdminPanel : Screen("admin_panel")
    object AmountSelection : Screen("amount_selection")
} 