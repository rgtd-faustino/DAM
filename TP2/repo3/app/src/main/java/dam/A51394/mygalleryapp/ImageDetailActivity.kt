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
import androidx.lifecycle.ViewModelProvider
import dam.A51394.mygalleryapp.viewmodel.DetailViewModel

class ImageDetailActivity : AppCompatActivity() {

    private lateinit var viewModel: DetailViewModel

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
        val tvImageUrl: android.widget.TextView = findViewById(R.id.tvImageUrl)

        val tvBreedName: android.widget.TextView = findViewById(R.id.tvBreedName)
        val tvBreedOrigin: android.widget.TextView = findViewById(R.id.tvBreedOrigin)
        val tvBreedLifeSpan: android.widget.TextView = findViewById(R.id.tvBreedLifeSpan)
        val tvBreedTemperament: android.widget.TextView = findViewById(R.id.tvBreedTemperament)
        val tvBreedDescription: android.widget.TextView = findViewById(R.id.tvBreedDescription)
        val tvBreedTitle: android.widget.TextView = findViewById(R.id.tvBreedTitle)

        val toolbarDetail = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarDetail)
        setSupportActionBar(toolbarDetail)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbarDetail.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        viewModel = ViewModelProvider(this)[DetailViewModel::class.java]

        val imageUrl = intent.getStringExtra("EXTRA_CAT_URL")
        val imageId = intent.getStringExtra("EXTRA_CAT_ID")

        // Inicializar com o que já temos
        tvId.text = "Ref ID: ${imageId ?: "..."}"
        tvDimensions.text = "Tamanho: a carregar..."
        tvImageUrl.text = "URL: ${imageUrl ?: "N/A"}"

        // Carregar detalhes reais da API para obter dimensões e raça
        imageId?.let {
            viewModel.fetchImageDetails(it)
        }

        setupObservers(tvId, tvDimensions, tvImageUrl, tvBreedName, tvBreedOrigin, tvBreedLifeSpan, tvBreedTemperament, tvBreedDescription, tvBreedTitle)

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
                    // Usamos os detalhes da API se já existirem no ViewModel
                    val currentDetail = viewModel.imageDetail.value
                    val cat = dam.A51394.mygalleryapp.model.CatImage(
                        id = imageId, 
                        url = imageUrl, 
                        width = currentDetail?.width ?: 0, 
                        height = currentDetail?.height ?: 0, 
                        isFavourite = true
                    )
                    favManager.addFavorite(cat, this)
                    isFavorite = favManager.isFavorite(imageId)
                }
                updateFavoriteButton(btnFavorite, isFavorite)
            } else {
                Toast.makeText(this, "Erro: Falta informação desta imagem.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupObservers(
        tvId: android.widget.TextView, 
        tvDimensions: android.widget.TextView,
        tvImageUrl: android.widget.TextView,
        tvBreedName: android.widget.TextView,
        tvBreedOrigin: android.widget.TextView,
        tvBreedLifeSpan: android.widget.TextView,
        tvBreedTemperament: android.widget.TextView,
        tvBreedDescription: android.widget.TextView,
        tvBreedTitle: android.widget.TextView
    ) {
        viewModel.imageDetail.observe(this) { detail ->
            detail?.let {
                tvId.text = "Ref ID: ${it.id}"
                tvDimensions.text = "Tamanho: ${it.width} x ${it.height} px"
                tvImageUrl.text = "URL: ${it.url}"

                val breed = it.breeds?.firstOrNull()
                if (breed != null) {
                    tvBreedTitle.text = "Informação da Raça"
                    tvBreedName.text = "Raça: ${breed.name}"
                    tvBreedOrigin.text = "Origem: ${breed.origin ?: "Desconhecida"}"
                    tvBreedLifeSpan.text = "Esperança de Vida: ${breed.lifeSpan ?: "--"} anos"
                    tvBreedTemperament.text = "Temperamento: ${breed.temperament ?: "--"}"
                    tvBreedDescription.text = "Descrição: ${breed.description ?: "Sem descrição disponível."}"
                } else {
                    tvBreedTitle.text = "Raça desconhecida"
                    tvBreedName.text = "Informação não disponível"
                    tvBreedOrigin.text = "Origem: --"
                    tvBreedLifeSpan.text = "Esperança de Vida: --"
                    tvBreedTemperament.text = "Temperamento: --"
                    tvBreedDescription.text = "Não foram encontrados detalhes sobre a raça deste gato na base de dados."
                }
            }
        }

        viewModel.errorMessage.observe(this) { error ->
            error?.let {
                Toast.makeText(this, "Erro detalhes: $it", Toast.LENGTH_SHORT).show()
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
