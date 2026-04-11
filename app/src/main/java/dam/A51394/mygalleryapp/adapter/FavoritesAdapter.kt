package dam.A51394.mygalleryapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import dam.A51394.mygalleryapp.R
import dam.A51394.mygalleryapp.model.CatImage

class FavoritesAdapter(
    private var favoriteCats: List<CatImage> = emptyList(),
    private val onRemoveClick: (CatImage) -> Unit,
    private val onItemClick: (CatImage) -> Unit
) : RecyclerView.Adapter<FavoritesAdapter.FavoriteViewHolder>() {

    inner class FavoriteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageViewFavorite: ImageView = itemView.findViewById(R.id.imageViewFavorite)
        val btnRemove: ImageButton = itemView.findViewById(R.id.btnRemoveFavorite)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_favorite, parent, false)
        return FavoriteViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        val cat = favoriteCats[position]

        Glide.with(holder.itemView.context)
            .load(cat.url)
            .centerCrop()
            .error(android.R.drawable.ic_menu_report_image)
            .into(holder.imageViewFavorite)

        holder.btnRemove.setOnClickListener {
            onRemoveClick(cat)
        }

        holder.itemView.setOnClickListener {
            onItemClick(cat)
        }
    }

    override fun getItemCount(): Int = favoriteCats.size

    fun submitList(newList: List<CatImage>) {
        favoriteCats = newList
        notifyDataSetChanged() // For simplicity in this exercise, but DiffUtil is better for production
    }
}
