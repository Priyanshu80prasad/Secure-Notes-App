package com.priyanshu.securenotes.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.priyanshu.securenotes.models.Note

class NoteRepository {
    private val db = FirebaseFirestore.getInstance()
    fun saveNote(userId : String , note: Note): Task<Void> {
        val notesRef = db.collection("users")
            .document(userId)
            .collection("notes")
        val documentID = if (note.noteID.isBlank()) {
            notesRef.document().id
        } else {
            note.noteID
        }
        val updateNote = note.copy(
            noteID = documentID
        )
        return notesRef.document(documentID).set(updateNote)
    }

        fun getNotes(userId: String, callback: (List<Note>) -> Unit)
        {
            db.collection("users")
                .document(userId)
                .collection("notes")
                .get()
                .addOnSuccessListener { result ->
                    val notes = result.documents.mapNotNull { document ->
                        document.toObject(Note::class.java)
                    }
                    callback(notes)

                }
    }
}