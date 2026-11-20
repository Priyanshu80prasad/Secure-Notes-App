package com.priyanshu.securenotes.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.priyanshu.securenotes.R
import com.priyanshu.securenotes.activities.AuthActivity
import com.priyanshu.securenotes.adapters.NoteAdapter
import com.priyanshu.securenotes.databinding.FragmentHomeBinding
import com.priyanshu.securenotes.models.Note
import com.priyanshu.securenotes.repository.NoteRepository

class HomeFragment: Fragment()  {
    private var _binding : FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var noteRepository: NoteRepository
    private lateinit var noteAdapter: NoteAdapter
    private val noteList = ArrayList<Note>()
    private val db = FirebaseFirestore.getInstance()
    private var notesListner: ListenerRegistration? = null

    private val allNotes = ArrayList<Note>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val user = com.google.firebase.auth.FirebaseAuth
            .getInstance()
            .currentUser
        val userName = user?.displayName
            ?: user?.email?.substringBefore("@")
            ?: "User"
        binding.tvUserName.text = userName

        noteRepository = NoteRepository()
        noteAdapter = NoteAdapter(noteList){ note ->
            val bundle = Bundle().apply {
                putString("noteId", note.noteID)
                putString("title",note.title)
                putString("description",note.description)
                putLong("createAt",note.createdAt)
            }
            val fragment = NoteDetailsFragment()
            fragment.arguments = bundle

            parentFragmentManager.beginTransaction()
                .replace(R.id.mainContainer, fragment)
                .addToBackStack(null)
                .commit()
        }

        binding.rvNotes.layoutManager = LinearLayoutManager(requireContext())
        binding.rvNotes.adapter = noteAdapter

        if (user != null){
            loadNotes(user.uid)
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchText = s.toString().trim()
                val filteredNotes = allNotes.filter { note ->
                    note.title.contains(searchText, ignoreCase = true) ||
                            note.description.contains(searchText, ignoreCase = true)
                }
                noteAdapter.updateNotes(filteredNotes)
            }
        })

        binding.btnAddNote.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(com.priyanshu.securenotes.R.id.mainContainer,
            AddNoteFragment()
                )
                .addToBackStack(null)
                .commit()
        }
        binding.btnLogout.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure want to logout")
                .setPositiveButton("Yes, Logout"){_,_->
                    FirebaseAuth.getInstance().signOut()
                    val intent = Intent(requireContext(), AuthActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
                .setNegativeButton("cancel", null)
                .show()

        }
    }
    private fun loadNotes(userId: String){
        noteRepository.getNotes(userId){ notes ->
            val currentBinding = _binding ?: return@getNotes
            allNotes.clear()
            allNotes.addAll(notes)
            noteAdapter.updateNotes(notes)
            currentBinding.tvNotesCount.text = "${notes.size} Notes"
            if (notes.isEmpty()){
                currentBinding.tvEmptyNotes.visibility = View.VISIBLE
                currentBinding.rvNotes.visibility = View.VISIBLE
            }else {
                currentBinding.tvEmptyNotes.visibility = View.GONE
                currentBinding.rvNotes.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}