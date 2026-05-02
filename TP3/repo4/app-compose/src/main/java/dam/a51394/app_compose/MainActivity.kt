package dam.a51394.app_compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dam.a51394.app_compose.ui.screens.MainScreen
import dam.a51394.app_compose.ui.theme.MyGalleryAppTheme

class MainActivity : ComponentActivity() {
    private val galleryViewModel by viewModels<GalleryViewModel>()
    private val favoritesViewModel by viewModels<FavoritesViewModel>()
    private val detailViewModel by viewModels<DetailViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val systemTheme = isSystemInDarkTheme()
            var isDarkTheme by remember { mutableStateOf(systemTheme) }

            MyGalleryAppTheme(darkTheme = isDarkTheme) {
                MainScreen(
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = { isDarkTheme = !isDarkTheme },
                    galleryViewModel = galleryViewModel,
                    favoritesViewModel = favoritesViewModel,
                    detailViewModel = detailViewModel
                )
            }
        }
    }
}