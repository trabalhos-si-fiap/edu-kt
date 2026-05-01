package br.com.edu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import br.com.edu.core.theme.EduTheme
import br.com.edu.features.auth.presentation.LoginScreen
import br.com.edu.features.auth.presentation.RegisterScreen
import br.com.edu.features.marketplace.presentation.AddPaymentMethodScreen
import br.com.edu.features.marketplace.presentation.CheckoutScreen
import br.com.edu.features.marketplace.presentation.MarketplaceScreen
import br.com.edu.features.marketplace.presentation.MarketplaceViewModel
import br.com.edu.features.marketplace.presentation.OrdersScreen
import br.com.edu.features.marketplace.presentation.ProductDetailScreen
import br.com.edu.features.profile.presentation.ProfileScreen
import br.com.edu.features.support.presentation.SupportScreen

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
    val marketplaceVm: MarketplaceViewModel = viewModel()
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
                onOpenProfile = { nav.navigate("profile") },
                onOpenSupport = { nav.navigate("support") },
                onBack = { nav.popBackStack() },
                onOpenProductDetail = { id -> nav.navigate("product-detail/$id") },
                viewModel = marketplaceVm,
            )
        }
        composable("profile") {
            ProfileScreen(
                onBack = { nav.popBackStack() },
                onOpenMarketplace = {
                    nav.navigate("marketplace") {
                        popUpTo("marketplace") { inclusive = true }
                    }
                },
                onOpenOrders = { nav.navigate("orders") },
                onOpenPaymentMethods = { nav.navigate("checkout") },
                onOpenSupport = { nav.navigate("support") },
                onLogout = {
                    nav.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                },
            )
        }
        composable("support") {
            SupportScreen(
                onBack = { nav.popBackStack() },
                onOpenMarketplace = {
                    nav.navigate("marketplace") {
                        popUpTo("marketplace") { inclusive = true }
                    }
                },
                onOpenOrders = { nav.navigate("orders") },
                onOpenProfile = { nav.navigate("profile") },
            )
        }
        composable("checkout") {
            CheckoutScreen(
                onBack = { nav.popBackStack() },
                onAddPaymentMethod = { nav.navigate("add-payment-method") },
                onEditPaymentMethod = { id -> nav.navigate("edit-payment-method/$id") },
            )
        }
        composable("orders") {
            OrdersScreen(
                onBack = { nav.popBackStack() },
                onOpenCheckout = { nav.navigate("checkout") },
                onOpenMarketplace = {
                    nav.navigate("marketplace") {
                        popUpTo("marketplace") { inclusive = true }
                    }
                },
                onOpenSupport = { nav.navigate("support") },
                onOpenProfile = { nav.navigate("profile") },
            )
        }
        composable(
            route = "product-detail/{id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType }),
        ) { entry ->
            ProductDetailScreen(
                productId = entry.arguments?.getInt("id") ?: 0,
                onBack = { nav.popBackStack() },
                onOpenProfile = { nav.navigate("profile") },
                onOpenCart = { nav.navigate("checkout") },
                onSearchInteract = { nav.popBackStack("marketplace", inclusive = false) },
                marketplaceViewModel = marketplaceVm,
            )
        }
        composable("add-payment-method") {
            AddPaymentMethodScreen(onBack = { nav.popBackStack() })
        }
        composable(
            route = "edit-payment-method/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType }),
        ) { entry ->
            AddPaymentMethodScreen(
                onBack = { nav.popBackStack() },
                editingId = entry.arguments?.getString("id"),
            )
        }
    }
}
