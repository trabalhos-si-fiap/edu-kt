package br.com.edu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import br.com.edu.core.theme.EduTheme
import br.com.edu.features.auth.presentation.LoginScreen
import br.com.edu.features.auth.presentation.RegisterScreen
import br.com.edu.features.marketplace.presentation.AddPaymentMethodScreen
import br.com.edu.features.marketplace.presentation.CheckoutScreen
import br.com.edu.features.marketplace.presentation.MarketplaceScreen
import br.com.edu.features.marketplace.presentation.OrdersScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EduTheme { EduApp() }
        }
    }
}

@Composable
private fun EduApp() {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = "login") {
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    nav.navigate("marketplace") { popUpTo("login") { inclusive = true } }
                },
                onNavigateRegister = { nav.navigate("register") },
                onNavigateLogistics = { },
            )
        }
        composable("register") {
            RegisterScreen(onNavigateLogin = {
                if (!nav.popBackStack("login", inclusive = false)) nav.navigate("login")
            })
        }
        composable("marketplace") {
            MarketplaceScreen(
                onOpenCart = { nav.navigate("checkout") },
                onOpenOrders = { nav.navigate("orders") },
                onBack = { nav.popBackStack() },
            )
        }
        composable("checkout") {
            CheckoutScreen(
                onBack = { nav.popBackStack() },
                onAddPaymentMethod = { nav.navigate("add-payment-method") },
            )
        }
        composable("orders") { OrdersScreen(onBack = { nav.popBackStack() }) }
        composable("add-payment-method") {
            AddPaymentMethodScreen(onBack = { nav.popBackStack() })
        }
    }
}
