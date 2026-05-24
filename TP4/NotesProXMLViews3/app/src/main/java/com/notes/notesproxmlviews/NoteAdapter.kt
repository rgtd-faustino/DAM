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

        // verificamos se a nota está bloqueada comparando a data de desbloqueio com o momento atual
        val isLocked = note.unlockDate != null && note.unlockDate.toDate().after(java.util.Date())

        if (isLocked) {
            // nota bloqueada: mostramos o cadeado e desativamos o clique
            holder.lockTextView.visibility = View.VISIBLE
            holder.lockTextView.text = "Abre em ${Utility.timestampToString(note.unlockDate)}"
            holder.titleTextView.alpha = 0.4f
            holder.contentTextView.visibility = View.GONE
            holder.imageThumbnail.alpha = 0.4f
            holder.timestampTextView.alpha = 0.4f
            holder.itemView.setOnClickListener {
                Utility.showToast(context, "Esta nota abre em ${Utility.timestampToString(note.unlockDate)}")
            }
        } else {
            // nota normal ou já desbloqueada: comportamento normal
            holder.lockTextView.visibility = View.GONE
            holder.titleTextView.alpha = 1f
            holder.contentTextView.visibility = View.VISIBLE
            holder.contentTextView.alpha = 1f
            holder.imageThumbnail.alpha = 1f
            holder.timestampTextView.alpha = 1f
            holder.itemView.setOnClickListener {
                val intent = Intent(context, NoteDetailsActivity::class.java)
                intent.putExtra("title", note.title)
                intent.putExtra("content", note.content)
                val docId = snapshots.getSnapshot(position).id
                intent.putExtra("docId", docId)
                intent.putExtra("imageUrl", note.imageUrl)
                // passamos a data de desbloqueio como Long para a NoteDetailsActivity poder mostrá-la
                note.unlockDate?.let { intent.putExtra("unlockDate", it.seconds) }
                context.startActivity(intent)
            }
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
        val lockTextView: TextView = itemView.findViewById(R.id.note_lock_text_view)
    }
}