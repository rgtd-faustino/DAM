package com.notes.notesproxmlviews

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions

class NoteAdapter(options: FirestoreRecyclerOptions<Note>, val context: Context) :
    FirestoreRecyclerAdapter<Note, NoteAdapter.NoteViewHolder>(options) {

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int, note: Note) {
        holder.titleTextView.text = note.title
        holder.contentTextView.text = note.content
        holder.timestampTextView.text = Utility.timestampToString(note.timestamp)

        // verificamos se a nota tem imagem antes de tentar carregá-la,
        // se não tiver escondemos a view para não ficar um espaço em branco na lista
        // se tiver, mostramos a view e usamos o glide para carregar a imagem a partir do URL
        if (!note.imageUrl.isNullOrEmpty()) {
            holder.imageThumbnail.visibility = android.view.View.VISIBLE
            // o glide trata de descarregar a imagem em background e de a colocar na view
            // sem bloquear o scroll da lista enquanto carrega
            com.bumptech.glide.Glide.with(context).load(note.imageUrl).into(holder.imageThumbnail)
        } else {
            holder.imageThumbnail.visibility = android.view.View.GONE
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, NoteDetailsActivity::class.java)
            intent.putExtra("title", note.title)
            intent.putExtra("content", note.content)
            val docId = snapshots.getSnapshot(position).id
            intent.putExtra("docId", docId)
            // passamos o URL da imagem para que a NoteDetailsActivity consiga carregar
            // a imagem que já estava guardada no firebase storage quando abrimos a nota para editar
            intent.putExtra("imageUrl", note.imageUrl)
            context.startActivity(intent)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_note_item, parent, false)
        return NoteViewHolder(view)
    }

    inner class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.note_title_text_view)
        val contentTextView: TextView = itemView.findViewById(R.id.note_content_text_view)
        val timestampTextView: TextView = itemView.findViewById(R.id.note_timestamp_text_view)
        // referência à imageview do thumbnail que adicionámos ao layout do item da lista
        val imageThumbnail: android.widget.ImageView = itemView.findViewById(R.id.note_image_thumbnail)
    }
}