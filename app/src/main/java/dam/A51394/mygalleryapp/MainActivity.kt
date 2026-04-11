package dam.A51394.mygalleryapp

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dam.A51394.mygalleryapp.adapter.CatImageAdapter
import dam.A51394.mygalleryapp.viewmodel.MainViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel
    private lateinit var adapter: CatImageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        setupRecyclerView()
        setupObservers()
        setupListeners()
    }

    private fun setupRecyclerView() {
        adapter = CatImageAdapter(onClick = { catImage ->
            val intent = android.content.Intent(this, ImageDetailActivity::class.java).apply {
                putExtra("EXTRA_CAT_ID", catImage.id)
                putExtra("EXTRA_CAT_URL", catImage.url)
            }
            startActivity(intent)
        })
        val recyclerViewCats = findViewById<RecyclerView>(R.id.recyclerViewCats)
        recyclerViewCats.layoutManager = LinearLayoutManager(this)
        recyclerViewCats.adapter = adapter
    }

    private fun setupObservers() {
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        viewModel.catImages.observe(this) { images ->
            adapter.submitList(images)
        }

        viewModel.isLoading.observe(this) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupListeners() {
        val btnRefresh = findViewById<Button>(R.id.btnRefresh)
        btnRefresh.setOnClickListener {
            viewModel.fetchImages()
        }
    }
}