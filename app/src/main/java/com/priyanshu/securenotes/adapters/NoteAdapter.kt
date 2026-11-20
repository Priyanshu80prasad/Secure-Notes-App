package com.priyanshu.securenotes.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.priyanshu.securenotes.databinding.ItemNoteBinding
import com.priyanshu.securenotes.models.Note
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NoteAdapter(
    private val noteList: ArrayList<Note>,
    private val onNoteClick: (Note) -> Unit
): RecyclerView.Adapter<NoteAdapter.NoteViewHolder>(){
    inner class NoteViewHolder(
        val binding: ItemNoteBinding
    ): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
     val binding = ItemNoteBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return NoteViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: NoteViewHolder,
        position: Int
    ) {
        val note = noteList[position]
        holder.binding.tvNoteTitle.text= note.title
        holder.binding.tvNoteDescription.text = note.description

        val date = Date(note.createdAt)
        val dateFormat = SimpleDateFormat(
            "dd MMM yyyy, hh:mm a",
            Locale.getDefault()
        )
        holder.binding.tvNoteDate.text = dateFormat.format(date)
        holder.itemView.setOnClickListener {
            onNoteClick(note)
        }
    }

    override fun getItemCount(): Int {
       return noteList.size
    }
    fun updateNotes(newNotes : List<Note>){
        noteList.clear()
        noteList.addAll(newNotes)
        notifyDataSetChanged()
    }
}