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

        val imageUrl = intent.getStringExtra("EXTRA_CAT_URL")
        val imageId = intent.getStringExtra("EXTRA_CAT_ID")

        Glide.with(this)
            .load(imageUrl)
            .into(imageViewDetail)

        btnFavorite.setOnClickListener {
            // Lógica dos favoritos será implementada no Passo 11
            Toast.makeText(this, "A implementar no Passo 11!", Toast.LENGTH_SHORT).show()
        }
    }
}
