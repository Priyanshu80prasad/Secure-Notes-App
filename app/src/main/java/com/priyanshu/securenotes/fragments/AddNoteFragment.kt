package com.priyanshu.securenotes.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.priyanshu.securenotes.databinding.FragmentAddNoteBinding
import com.priyanshu.securenotes.models.Note
import com.priyanshu.securenotes.repository.NoteRepository

class AddNoteFragment : Fragment() {
    private var _binding: FragmentAddNoteBinding? = null
    private val binding get() = _binding!!

    private lateinit var noteRepository: NoteRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddNoteBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        noteRepository = NoteRepository()
        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        binding.btnSaveNote.setOnClickListener {
            val title = binding.etNoteTitle.text.toString().trim()
            val description = binding.etNoteDescription.text.toString().trim()
            when{
                title.isEmpty() -> {
                    binding.etNoteTitle.error = "Enter Note Titlee"
                }
                description.isEmpty() ->
                {
                    binding.etNoteDescription.error = "Enter Description"
                }
                else -> {
                    val user = FirebaseAuth.getInstance().currentUser
                    if (user == null){
                        Toast.makeText(requireContext(),"Please Login First", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    binding.btnSaveNote.isEnabled = false
                    val currentTime = System.currentTimeMillis()
                    val note = Note(
                        noteID = "",
                        title = title,
                        description = description,
                        createdAt = currentTime,
                        updatedAt = currentTime
                    )
                    noteRepository.saveNote(user.uid, note)
                        .addOnCompleteListener { task ->
                            val currentBinding = _binding
                            if (currentBinding == null || !isAdded){
                                return@addOnCompleteListener
                            }
                            currentBinding.btnSaveNote.isEnabled = true
                            if (task.isSuccessful){
                                Toast.makeText(requireContext(),"Note Save Successfully", Toast.LENGTH_SHORT).show()
                            parentFragmentManager.popBackStack()
                            }else{
                                Toast.makeText(requireContext(),task.exception?.localizedMessage?: "Failed to save note",
                                    Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding= null
    }
}