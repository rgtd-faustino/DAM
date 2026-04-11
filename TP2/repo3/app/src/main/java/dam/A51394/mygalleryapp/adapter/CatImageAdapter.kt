package dam.A51394.mygalleryapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import dam.A51394.mygalleryapp.R
import dam.A51394.mygalleryapp.model.CatImage

class CatImageAdapter(
    private var catImages: List<CatImage> = emptyList(),
    private val onClick: ((CatImage) -> Unit)? = null
) : RecyclerView.Adapter<CatImageAdapter.CatViewHolder>() {

    inner class CatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageViewCat: ImageView = itemView.findViewById(R.id.imageViewCat)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cat, parent, false)
        return CatViewHolder(view)
    }

    override fun onBindViewHolder(holder: CatViewHolder, position: Int) {
        val catImage = catImages[position]
        
        Glide.with(holder.itemView.context)
            .load(catImage.url)
            // Um erro comum que o Glide previne de forma elegante: Loading states! (Podes adicionar placeholders se quiseres)
            .centerCrop()
            .into(holder.imageViewCat)

        holder.itemView.setOnClickListener {
            onClick?.invoke(catImage)
        }
    }

    override fun getItemCount(): Int = catImages.size

    fun submitList(newList: List<CatImage>) {
        catImages = newList
        notifyDataSetChanged()
    }
}
