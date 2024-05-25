package com.example.firebase

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class NoteAdapter(
    private val context: Context,
    private val noteList: MutableList<Note>,
    private val dataChangeListener: DataChangeListener? = null

) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val outTittle: TextView = itemView.findViewById(R.id.showJudul)
        val delNotes: ImageButton = itemView.findViewById(R.id.deleteNote)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_layout, parent, false)
        return NoteViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val currentNote = noteList[position]
        holder.itemView.setOnClickListener {
            val intent = Intent(context, viewNotes::class.java)
            intent.putExtra("id", currentNote.id)
            intent.putExtra("title", currentNote.title)
            intent.putExtra("description", currentNote.description)
            context.startActivity(intent)
        }

        holder.delNotes.setOnClickListener {
            val itemPosition = holder.adapterPosition
            if (itemPosition != RecyclerView.NO_POSITION) {
                val noteToDelete = noteList[position]
                CoroutineScope(Dispatchers.IO).launch {
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    val userId = currentUser!!.uid
                    val databaseRef = FirebaseDatabase.getInstance("https://prak-firebase-pam-9-default-rtdb.asia-southeast1.firebasedatabase.app/")
                        .getReference("users").child(userId).child("notes")
                    databaseRef.child(noteToDelete.id!!).removeValue().await()
                    withContext(Dispatchers.Main) {
                        dataChangeListener?.onDataChange()
                        notifyItemRangeChanged(position, noteList.size)
                    }
                }
            }
        }
        holder.outTittle.text = currentNote.title
    }

    override fun getItemCount(): Int {
        return noteList.size
    }
}

interface DataChangeListener {
    fun onDataChange()
}

