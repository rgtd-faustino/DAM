package dam.A51394.mygalleryapp.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dam.A51394.mygalleryapp.ImageDetailActivity
import dam.A51394.mygalleryapp.R
import dam.A51394.mygalleryapp.adapter.CatImageAdapter
import dam.A51394.mygalleryapp.viewmodel.MainViewModel

class GalleryFragment : Fragment() {

    private lateinit var viewModel: MainViewModel
    private lateinit var adapter: CatImageAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_gallery, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Partilhar o ViewModel com a Activity garante que os dados sobrevivem à navegação
        viewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]

        setupRecyclerView(view)
        setupObservers(view)
        setupListeners(view)
    }

    private fun setupRecyclerView(view: View) {
        adapter = CatImageAdapter(onClick = { catImage ->
            val intent = Intent(requireContext(), ImageDetailActivity::class.java).apply {
                putExtra("EXTRA_CAT_ID", catImage.id)
                putExtra("EXTRA_CAT_URL", catImage.url)
            }
            startActivity(intent)
        })
        val recyclerViewCats = view.findViewById<RecyclerView>(R.id.recyclerViewCats)
        recyclerViewCats.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewCats.adapter = adapter
    }

    private fun setupObservers(view: View) {
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)

        viewModel.catImages.observe(viewLifecycleOwner) { images ->
            adapter.submitList(images)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupListeners(view: View) {
        val btnRefresh = view.findViewById<Button>(R.id.btnRefresh)
        btnRefresh.setOnClickListener {
            viewModel.fetchImages()
        }
    }
}
