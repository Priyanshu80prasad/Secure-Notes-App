package com.priyanshu.securenotes.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.R
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.priyanshu.securenotes.databinding.FragmentNoteDetailsBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NoteDetailsFragment : Fragment() {
    private var _binding: FragmentNoteDetailsBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNoteDetailsBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val noteID = arguments?.getString("noteId")
        val title = arguments?.getString("title") ?: ""
        val description = arguments?.getString("description") ?: ""
        val createdAt  = arguments?.getLong("createdAt") ?: 0L

        binding.tvDetailTitle.text = title
        binding.tvDetailDescription.text = description

        val dateFormat = SimpleDateFormat(
            "dd MM yyyy, hh:mm a",
            Locale.getDefault()
        )
        binding.tvDetailDate.text = dateFormat.format(Date(createdAt))
        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        binding.btnEditNote.setOnClickListener {
            val bundle = Bundle().apply {
                putString("noteId",noteID)
                putString("title",title)
                putString("description",description)
            }
            val fragment = EditNoteFragment()
            fragment.arguments = bundle
            parentFragmentManager.beginTransaction()
                .replace(com.priyanshu.securenotes.R.id.mainContainer,fragment)
                .addToBackStack(null)
                .commit()
        }
        parentFragmentManager.setFragmentResultListener(
            "note_updated",
            viewLifecycleOwner
        ){_, result->
            binding.tvDetailTitle.text = result.getString("title")
            binding.tvDetailDescription.text= result.getString("description")
        }
        binding.btnDeleteNote.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Delete Note")
                .setMessage("Are you want to delete this note?")
                .setPositiveButton("Delete"){ _,_->
                    val userId = FirebaseAuth.getInstance().currentUser?.uid
                    if (userId == null){
                        Toast.makeText(requireContext(),"Please Login First", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }
                    if (noteID.isNullOrBlank()){
                        Toast.makeText(requireContext(),"Note Id not found", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }
                    db.collection("users")
                        .document(userId)
                        .collection("notes")
                        .document(noteID)
                        .delete()
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(),"Note Delete Succusfully", Toast.LENGTH_SHORT).show()
                        parentFragmentManager.popBackStack()
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(requireContext(),"Delete Failed", Toast.LENGTH_SHORT).show()
                        }

                }
                .setNegativeButton("cancel",null)
                .show()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
