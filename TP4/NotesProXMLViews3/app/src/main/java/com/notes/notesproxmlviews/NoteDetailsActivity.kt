package com.notes.notesproxmlviews

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp.Companion.now
import com.google.firebase.firestore.DocumentReference

class NoteDetailsActivity : AppCompatActivity() {
    var titleEditText: EditText? = null
    var contentEditText: EditText? = null
    var saveNoteBtn: ImageButton? = null
    var pageTitleTextView: TextView? = null
    var title: String? = null
    var content: String? = null
    var docId: String? = null
    var isEditMode: Boolean = false
    var deleteNoteTextViewBtn: TextView? = null

    // mostra a imagem escolhida ou já guardada na nota
    var noteImageView: android.widget.ImageView? = null
    var addImageBtn: TextView? = null // abre a galeria
    // guarda o URI local da imagem que o utilizador escolheu da galeria, antes de fazer upload
    // o URI é basicamente o endereço do ficheiro no telemóvel (ex: content://media/external/images/...)
    var selectedImageUri: android.net.Uri? = null
    // guarda o URL da imagem que já estava guardada no firebase storage caso estejamos em modo de edição
    // assim se o utilizador não escolher uma nova imagem conseguimos manter a que já existia
    var existingImageUrl: String? = null

    // o registerForActivityResult é a forma moderna de abrir outra app (a galeria) e receber o resultado de volta
    // funciona como um contrato: dizemos que queremos obter conteúdo (GetContent) e quando o utilizador
    // escolhe uma imagem o resultado (o URI) volta para aqui através da lambda { uri -> ... }
    val pickImageLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri ->
        // só fazemos alguma coisa se o utilizador realmente escolheu uma imagem e não cancelou
        if (uri != null) {
            selectedImageUri = uri
            // mostramos a imagem escolhida diretamente do telemóvel antes de fazer upload
            noteImageView!!.setImageURI(uri)
            noteImageView!!.visibility = android.view.View.VISIBLE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_details)

        titleEditText = findViewById<EditText?>(R.id.notes_title_text)
        contentEditText = findViewById<EditText?>(R.id.notes_content_text)
        saveNoteBtn = findViewById<ImageButton?>(R.id.save_note_btn)
        pageTitleTextView = findViewById<TextView?>(R.id.page_title)
        deleteNoteTextViewBtn = findViewById<TextView?>(R.id.delete_note_text_view_btn)

        //receive data
        title = intent.getStringExtra("title")
        content = intent.getStringExtra("content")
        docId = intent.getStringExtra("docId")

        if (docId != null && !docId!!.isEmpty()) {
            isEditMode = true
        }

        titleEditText!!.setText(title)
        contentEditText!!.setText(content)
        if (isEditMode) {
            pageTitleTextView!!.text = getString(R.string.edit_your_note)
            deleteNoteTextViewBtn!!.visibility = View.VISIBLE
        }

        noteImageView = findViewById(R.id.note_image_view)
        addImageBtn = findViewById(R.id.add_image_btn)

        // se estivermos em modo de edição e a nota já tiver uma imagem guardada
        // carregamos essa imagem com o glide a partir do URL que está no firestore
        existingImageUrl = intent.getStringExtra("imageUrl")
        if (!existingImageUrl.isNullOrEmpty()) {
            noteImageView!!.visibility = android.view.View.VISIBLE
            // o glide descarrega a imagem do URL em background e coloca-a na view automaticamente
            // sem bloquear o ecrã enquanto carrega
            com.bumptech.glide.Glide.with(this).load(existingImageUrl).into(noteImageView!!)
        }

        // quando o utilizador clica no botão de adicionar imagem abrimos a galeria
        // o "image/*" diz ao android para mostrar apenas ficheiros de imagem
        addImageBtn!!.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        saveNoteBtn!!.setOnClickListener(View.OnClickListener { v: View? -> saveNote() })

        deleteNoteTextViewBtn!!.setOnClickListener(View.OnClickListener { v: View? -> deleteNoteFromFirebase() })
    }

    fun saveNote() {
        val noteTitle = titleEditText!!.getText().toString()
        val noteContent = contentEditText!!.getText().toString()
        if (noteTitle.isEmpty()) {
            titleEditText!!.error = "Title is required"
            return
        }

        val note = Note()
        note.setTitle(noteTitle)
        note.setContent(noteContent)
        note.setTimestamp(now())

        saveNoteToFirebase(note)
    }

    fun saveNoteToFirebase(note: Note) {
        val documentReference: DocumentReference
        if (isEditMode) {
            documentReference = Utility.getCollectionReferenceForNotes().document(docId.toString())
        } else {
            documentReference = Utility.getCollectionReferenceForNotes().document()
        }

        // se o utilizador escolheu uma nova imagem da galeria temos de a fazer upload primeiro
        // só depois de ter o URL do firebase storage é que guardamos a nota no firestore
        // porque precisamos do URL para o meter dentro do objeto note antes de o guardar
        if (selectedImageUri != null) {
            // criamos uma referência única no storage usando o timestamp atual como nome do ficheiro
            // assim evitamos colisões de nomes caso dois utilizadores façam upload ao mesmo tempo
            // usamos getInstance() sem argumentos para o firebase usar o bucket default
            // definido no google-services.json, evitando erros por ter o nome do bucket errado hardcoded
            val storageRef = com.google.firebase.storage.FirebaseStorage
                .getInstance()
                .reference.child("notes_images/${System.currentTimeMillis()}.jpg")

            storageRef.putFile(selectedImageUri!!).addOnCompleteListener { uploadTask ->
                if (uploadTask.isSuccessful) {
                    // após o upload com sucesso pedimos ao storage o URL público do ficheiro
                    // este URL é o que vai ficar guardado no firestore e que o glide vai usar para carregar a imagem
                    storageRef.downloadUrl.addOnCompleteListener { urlTask ->
                        note.setImageUrl(urlTask.result.toString())
                        // agora que já temos o URL podemos finalmente guardar a nota completa no firestore
                        documentReference.set(note).addOnCompleteListener {
                            if (it.isSuccessful) {
                                Utility.showToast(this@NoteDetailsActivity, "Note added successfully")
                                finish()
                            } else {
                                Utility.showToast(this@NoteDetailsActivity, "Failed while adding note")
                            }
                        }
                    }
                } else {
                    // mostramos o erro real do firebase em vez de uma mensagem genérica
                    // assim conseguimos perceber onde é o problema quando tentamos meter a imagem
                    val errorMsg = uploadTask.exception?.localizedMessage ?: "Unknown error"
                    Utility.showToast(this@NoteDetailsActivity, "Upload failed: $errorMsg")
                }
            }
        } else {
            // se não foi escolhida nenhuma imagem nova mas a nota já tinha uma imagem guardada
            // mantemos o URL existente para não perder a imagem quando se edita só o texto
            if (!existingImageUrl.isNullOrEmpty()) {
                note.setImageUrl(existingImageUrl)
            }
            documentReference.set(note).addOnCompleteListener(object : OnCompleteListener<Void?> {
                override fun onComplete(task: Task<Void?>) {
                    if (task.isSuccessful) {
                        Utility.showToast(this@NoteDetailsActivity, "Note added successfully")
                        finish()
                    } else {
                        Utility.showToast(this@NoteDetailsActivity, "Failed while adding note")
                    }
                }
            })
        }
    }

    fun deleteNoteFromFirebase() {
        val documentReference: DocumentReference = Utility.getCollectionReferenceForNotes().document(
            docId.toString()
        )
        documentReference.delete().addOnCompleteListener(object : OnCompleteListener<Void?> {
            override fun onComplete(task: Task<Void?>) {
                if (task.isSuccessful) {
                    //note is deleted
                    Utility.showToast(this@NoteDetailsActivity, "Note deleted successfully")
                    finish()
                } else {
                    Utility.showToast(this@NoteDetailsActivity, "Failed while deleting note")
                }
            }
        })
    }
}