package dam.a51394.app_compose.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dam.a51394.app_compose.DetailViewModel
import dam.a51394.app_compose.FavoritesViewModel
import dam.a51394.app_compose.GalleryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    galleryViewModel: GalleryViewModel,
    favoritesViewModel: FavoritesViewModel,
    detailViewModel: DetailViewModel
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        topBar = {
            if (currentRoute == "gallery" || currentRoute == "favorites") {
                TopAppBar(
                    title = { 
                        Text(if (currentRoute == "gallery") "Galeria" else "Favoritos") 
                    },
                    actions = {
                        IconButton(onClick = onToggleTheme) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Default.Brightness7 else Icons.Default.Brightness4,
                                contentDescription = "Alternar Tema"
                            )
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (currentRoute == "gallery" || currentRoute == "favorites") {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = "Galeria") },
                        label = { Text("Galeria") },
                        selected = currentRoute == "gallery",
                        onClick = {
                            navController.navigate("gallery") {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Favorite, contentDescription = "Favoritos") },
                        label = { Text("Favoritos") },
                        selected = currentRoute == "favorites",
                        onClick = {
                            navController.navigate("favorites") {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            NavHost(navController = navController, startDestination = "gallery") {
                composable("gallery") {
                    GalleryScreen(
                        viewModel = galleryViewModel,
                        onImageClick = { imageId ->
                            navController.navigate("detail/$imageId")
                        }
                    )
                }
                composable("favorites") {
                    FavoritesScreen(
                        viewModel = favoritesViewModel,
                        onImageClick = { imageId ->
                            navController.navigate("detail/$imageId")
                        }
                    )
                }
                composable(
                    route = "detail/{imageId}",
                    arguments = listOf(navArgument("imageId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val imageId = backStackEntry.arguments?.getString("imageId") ?: ""
                    DetailScreen(
                        imageId = imageId,
                        viewModel = detailViewModel,
                        favoritesViewModel = favoritesViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
