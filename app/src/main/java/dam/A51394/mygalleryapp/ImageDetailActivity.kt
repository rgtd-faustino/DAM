package dam.A51394.mygalleryapp

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide

class ImageDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detail)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainDetail)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val imageViewDetail: ImageView = findViewById(R.id.imageViewDetail)
        val btnFavorite: Button = findViewById(R.id.btnFavorite)
        val tvId: android.widget.TextView = findViewById(R.id.tvImageId)
        val tvDimensions: android.widget.TextView = findViewById(R.id.tvDimensions)

        val toolbarDetail = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarDetail)
        setSupportActionBar(toolbarDetail)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbarDetail.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val imageUrl = intent.getStringExtra("EXTRA_CAT_URL")
        val imageId = intent.getStringExtra("EXTRA_CAT_ID")
        val imageWidth = intent.getIntExtra("EXTRA_CAT_WIDTH", 0)
        val imageHeight = intent.getIntExtra("EXTRA_CAT_HEIGHT", 0)

        tvId.text = "Ref ID: ${imageId ?: "N/A"}"
        tvDimensions.text = "Tamanho: ${imageWidth} x ${imageHeight} px"

        Glide.with(this)
            .load(imageUrl)
            .error(android.R.drawable.ic_menu_report_image)
            .into(imageViewDetail)

        val favManager = dam.A51394.mygalleryapp.data.FavoritesManager(this)

        var isFavorite = if (imageId != null) favManager.isFavorite(imageId) else false
        updateFavoriteButton(btnFavorite, isFavorite)

        btnFavorite.setOnClickListener {
            if (imageId != null && imageUrl != null) {
                if (isFavorite) {
                    favManager.removeFavorite(imageId, this)
                    isFavorite = false
                } else {
                    val cat = dam.A51394.mygalleryapp.model.CatImage(
                        id = imageId, 
                        url = imageUrl, 
                        width = imageWidth, 
                        height = imageHeight, 
                        isFavourite = true
                    )
                    favManager.addFavorite(cat, this)
                    // Check if it was actually added (might fail due to FIFO if we wanted to be strict, 
                    // but addFavorite handles its own messaging)
                    isFavorite = favManager.isFavorite(imageId)
                }
                updateFavoriteButton(btnFavorite, isFavorite)
            } else {
                Toast.makeText(this, "Erro: Falta informação desta imagem.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateFavoriteButton(button: Button, isFavorite: Boolean) {
        if (isFavorite) {
            button.text = "Remover dos Favoritos"
            button.setBackgroundColor(androidx.core.content.ContextCompat.getColor(this, R.color.error_red))
        } else {
            button.text = "Adicionar aos Favoritos"
            button.setBackgroundColor(androidx.core.content.ContextCompat.getColor(this, R.color.success_green))
        }
    }
}
