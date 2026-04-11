package dam.A51394.mygalleryapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import dam.A51394.mygalleryapp.ui.FavoritesFragment
import dam.A51394.mygalleryapp.ui.GalleryFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0) // Bottom padding will be handled by BottomNav
            insets
        }

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        // Set default fragment
        if (savedInstanceState == null) {
            replaceFragment(GalleryFragment())
        }

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_gallery -> {
                    replaceFragment(GalleryFragment())
                    true
                }
                R.id.nav_favorites -> {
                    replaceFragment(FavoritesFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}