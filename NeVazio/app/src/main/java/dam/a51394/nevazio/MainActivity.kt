package dam.a51394.nevazio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dam.a51394.nevazio.ui.home.HomeScreen
import dam.a51394.nevazio.ui.home.HomeViewModel
import dam.a51394.nevazio.ui.login.LoginScreen
import dam.a51394.nevazio.ui.login.LoginViewModel
import dam.a51394.nevazio.ui.recipe.RecipeDetailScreen
import dam.a51394.nevazio.ui.recipe.RecipeViewModel
import dam.a51394.nevazio.ui.recipes.RecipesScreen
import dam.a51394.nevazio.ui.recipes.RecipesViewModel
import dam.a51394.nevazio.ui.register.RegisterScreen
import dam.a51394.nevazio.ui.register.RegisterViewModel
import dam.a51394.nevazio.ui.shopping.ShoppingListScreen
import dam.a51394.nevazio.ui.shopping.ShoppingViewModel
import dam.a51394.nevazio.ui.theme.NeVazioTheme
import dam.a51394.nevazio.ui.theme.SuccessGreen

// ── Routes ──────────────────────────────────────────────────────────────────
object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val RECIPES = "recipes"
    const val RECIPE_DETAIL = "recipe_detail/{recipeId}"
    const val SHOPPING = "shopping"

    fun recipeDetail(recipeId: String) = "recipe_detail/$recipeId"
}

// ── Bottom nav items ─────────────────────────────────────────────────────────
data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(Routes.HOME, "Frigorífico", Icons.Default.Home),
    BottomNavItem(Routes.RECIPES, "Receitas", Icons.Default.MenuBook),
    BottomNavItem(Routes.SHOPPING, "Compras", Icons.Default.ShoppingCart),
)

// ── Routes that show the bottom bar ─────────────────────────────────────────
val bottomBarRoutes = setOf(Routes.HOME, Routes.RECIPES, Routes.SHOPPING)

// ── Activity ─────────────────────────────────────────────────────────────────
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NeVazioTheme {
                NeVazioApp()
            }
        }
    }
}

// ── Root composable ───────────────────────────────────────────────────────────
@Composable
fun NeVazioApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in bottomBarRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = androidx.compose.ui.unit.Dp(4f)
                ) {
                    val currentDestination = navBackStackEntry?.destination
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = SuccessGreen,
                                selectedTextColor = SuccessGreen,
                                indicatorColor = SuccessGreen.copy(alpha = 0.12f)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.LOGIN,
            modifier = Modifier.padding(innerPadding)
        ) {
            // ── Auth ──────────────────────────────────────────────────────
            composable(Routes.LOGIN) {
                val vm: LoginViewModel = viewModel()
                LoginScreen(
                    viewModel = vm,
                    onNavigateToRegister = { navController.navigate(Routes.REGISTER) },
                    onLoginSuccess = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    }
                )
            }
            composable(Routes.REGISTER) {
                val vm: RegisterViewModel = viewModel()
                RegisterScreen(
                    viewModel = vm,
                    onNavigateToLogin = { navController.popBackStack() },
                    onRegisterSuccess = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    }
                )
            }

            // ── Main tabs ─────────────────────────────────────────────────
            composable(Routes.HOME) {
                val vm: HomeViewModel = viewModel()
                HomeScreen(
                    viewModel = vm,
                    onNavigateToScan = { /* TODO: scan */ }
                )
            }
            composable(Routes.RECIPES) {
                val vm: RecipesViewModel = viewModel()
                RecipesScreen(
                    viewModel = vm,
                    onNavigateToRecipeDetail = { recipeId ->
                        navController.navigate(Routes.recipeDetail(recipeId))
                    }
                )
            }
            composable(Routes.SHOPPING) {
                val vm: ShoppingViewModel = viewModel()
                ShoppingListScreen(viewModel = vm)
            }

            // ── Recipe detail ─────────────────────────────────────────────
            composable(Routes.RECIPE_DETAIL) {
                val vm: RecipeViewModel = viewModel()
                RecipeDetailScreen(
                    viewModel = vm,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
