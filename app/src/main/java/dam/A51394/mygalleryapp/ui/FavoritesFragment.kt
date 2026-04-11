package dam.A51394.mygalleryapp.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dam.A51394.mygalleryapp.ImageDetailActivity
import dam.A51394.mygalleryapp.R
import dam.A51394.mygalleryapp.adapter.FavoritesAdapter
import dam.A51394.mygalleryapp.viewmodel.FavoritesViewModel

class FavoritesFragment : Fragment() {

    private lateinit var viewModel: FavoritesViewModel
    private lateinit var adapter: FavoritesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_favorites, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[FavoritesViewModel::class.java]

        setupRecyclerView(view)
        setupObservers()
    }

    override fun onResume() {
        super.onResume()
        // Refresh list every time Fragment comes back to foreground (e.g. after adding from Detail)
        viewModel.loadFavorites()
    }

    private fun setupRecyclerView(view: View) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewFavorites)
        
        adapter = FavoritesAdapter(
            onRemoveClick = { cat ->
                viewModel.removeFavorite(cat)
            },
            onItemClick = { cat ->
                val intent = Intent(requireContext(), ImageDetailActivity::class.java).apply {
                    putExtra("EXTRA_CAT_ID", cat.id)
                    putExtra("EXTRA_CAT_URL", cat.url)
                    putExtra("EXTRA_CAT_WIDTH", cat.width ?: 0)
                    putExtra("EXTRA_CAT_HEIGHT", cat.height ?: 0)
                }
                startActivity(intent)
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.favorites.observe(viewLifecycleOwner) { favoritesList ->
            adapter.submitList(favoritesList)
        }
    }
}
