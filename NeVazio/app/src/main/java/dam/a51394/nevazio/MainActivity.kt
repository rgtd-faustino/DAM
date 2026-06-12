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
import org.koin.androidx.compose.KoinAndroidContext
import org.koin.androidx.compose.koinViewModel
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
import dam.a51394.nevazio.ui.scan.ScanScreen
import dam.a51394.nevazio.ui.profile.ProfileScreen
import dam.a51394.nevazio.ui.profile.ProfileViewModel

import kotlinx.serialization.Serializable
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.toRoute

// ── Type-Safe Routes ────────────────────────────────────────────────────────
@Serializable
object LoginRoute

@Serializable
object RegisterRoute

@Serializable
object HomeRoute

@Serializable
object RecipesRoute

@Serializable
data class RecipeDetailRoute(val recipeId: String)

@Serializable
object ShoppingRoute

@Serializable
object ScanRoute

@Serializable
object ProfileRoute

// ── Bottom nav items ─────────────────────────────────────────────────────────
data class BottomNavItem(
    val route: Any,
    val label: String,
    val icon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(HomeRoute, "Inventário", Icons.Default.Home),
    BottomNavItem(RecipesRoute, "Receitas", Icons.Default.MenuBook),
    BottomNavItem(ShoppingRoute, "Compras", Icons.Default.ShoppingCart),
)

// ── Activity ─────────────────────────────────────────────────────────────────
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NeVazioTheme {
                // KoinAndroidContext associa o contexto Koin à árvore de Compose.
                // Sem este wrapper, cada koinViewModel() emite um aviso [Warning] por não
                // encontrar um contexto Compose explícito, mesmo que o Koin global esteja ativo.
                KoinAndroidContext {
                    NeVazioApp()
                }
            }
        }
    }
}

// ── Root composable ───────────────────────────────────────────────────────────
@Composable
fun NeVazioApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = currentDestination?.let { dest ->
        dest.hasRoute<HomeRoute>() || dest.hasRoute<RecipesRoute>() || dest.hasRoute<ShoppingRoute>()
    } ?: false

    val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
    val startDest: Any = if (auth.currentUser != null) HomeRoute else LoginRoute

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = androidx.compose.ui.unit.Dp(4f)
                ) {
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any { 
                            when (item.route) {
                                is HomeRoute -> it.hasRoute<HomeRoute>()
                                is RecipesRoute -> it.hasRoute<RecipesRoute>()
                                is ShoppingRoute -> it.hasRoute<ShoppingRoute>()
                                else -> false
                            }
                        } == true
                        
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
            startDestination = startDest,
            modifier = Modifier.padding(innerPadding)
        ) {
            // ── Auth ──────────────────────────────────────────────────────
            composable<LoginRoute> {
                val vm: LoginViewModel = koinViewModel()
                LoginScreen(
                    viewModel = vm,
                    onNavigateToRegister = { navController.navigate(RegisterRoute) },
                    onLoginSuccess = {
                        navController.navigate(HomeRoute) {
                            popUpTo<LoginRoute> { inclusive = true }
                        }
                    }
                )
            }
            composable<RegisterRoute> {
                val vm: RegisterViewModel = koinViewModel()
                RegisterScreen(
                    viewModel = vm,
                    onNavigateToLogin = { navController.popBackStack() },
                    onRegisterSuccess = {
                        navController.navigate(HomeRoute) {
                            popUpTo<LoginRoute> { inclusive = true }
                        }
                    }
                )
            }

            // ── Main tabs ─────────────────────────────────────────────────
            composable<HomeRoute> { backStackEntry ->
                val vm: HomeViewModel = koinViewModel()
                val detectedItem = backStackEntry.savedStateHandle.get<String>("detected_item")
                if (detectedItem != null) {
                    backStackEntry.savedStateHandle.remove<String>("detected_item")
                    vm.showAddSheet(detectedItem)
                }

                HomeScreen(
                    viewModel = vm,
                    onNavigateToScan = { navController.navigate(ScanRoute) },
                    onNavigateToProfile = { navController.navigate(ProfileRoute) }
                )
            }
            composable<ScanRoute> {
                ScanScreen(
                    onNavigateBack = { detectedItem -> 
                        if (detectedItem != null) {
                            navController.previousBackStackEntry?.savedStateHandle?.set("detected_item", detectedItem)
                        }
                        navController.popBackStack() 
                    }
                )
            }
            composable<RecipesRoute> {
                val vm: RecipesViewModel = koinViewModel()
                RecipesScreen(
                    viewModel = vm,
                    onNavigateToRecipeDetail = { recipeId ->
                        navController.navigate(RecipeDetailRoute(recipeId))
                    }
                )
            }
            composable<ShoppingRoute> {
                val vm: ShoppingViewModel = koinViewModel()
                ShoppingListScreen(viewModel = vm)
            }

            // ── Recipe detail ─────────────────────────────────────────────
            composable<RecipeDetailRoute> { backStackEntry ->
                val routeInfo = backStackEntry.toRoute<RecipeDetailRoute>()
                val vm: RecipeViewModel = koinViewModel { org.koin.core.parameter.parametersOf(routeInfo.recipeId) }
                RecipeDetailScreen(
                    viewModel = vm,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            
            // ── Profile ───────────────────────────────────────────────────
            composable<ProfileRoute> {
                val vm: ProfileViewModel = koinViewModel()
                ProfileScreen(
                    viewModel = vm,
                    onNavigateBack = { navController.popBackStack() },
                    onLogoutSuccess = {
                        navController.navigate(LoginRoute) {
                            popUpTo<HomeRoute> { inclusive = true }
                        }
                    },
                    // Quando o familyCode muda, todos os ViewModels já instanciados (Home, Shopping)
                    // têm o fridge ID antigo em memória. O reset completo da navegação garante que
                    // são recriados via Koin com o ID correto na próxima composição.
                    onFamilyChanged = {
                        navController.navigate(HomeRoute) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
