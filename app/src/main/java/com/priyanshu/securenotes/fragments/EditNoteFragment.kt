package com.priyanshu.securenotes.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.priyanshu.securenotes.databinding.FragmentEditNoteBinding

class EditNoteFragment : Fragment() {
    public var _binding : FragmentEditNoteBinding? = null
    public val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditNoteBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val noteID = arguments?.getString("noteId") ?: return
        val oldTitle = arguments?.getString("title")?: ""
        val oldDescription = arguments?.getString("description")?: ""
        binding.etEditTitle.setText(oldTitle)
        binding.etEditDescription.setText(oldDescription)

        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.btnUpdateNote.setOnClickListener {
            val newTitle = binding.etEditTitle.text.toString().trim()
            val newDescription = binding.etEditDescription.text.toString().trim()

            when {
                newTitle.isEmpty() -> {
                    binding.etEditTitle.error =" Enter Note Title"
                }
                newDescription.isEmpty()-> {
                    binding.etEditDescription.error = " Enter Description"
                }
                else -> {
                    val userID = FirebaseAuth.getInstance().currentUser?.uid

                    if (userID== null){
                        Toast.makeText(requireContext(),"Please Login First", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    binding.btnUpdateNote.isEnabled=false

                    val updateData = hashMapOf<String, Any>(
                        "title" to newTitle,
                        "description" to newDescription,
                        "updatedAt" to System.currentTimeMillis()
                    )
                    db.collection("users")
                        .document(userID)
                        .collection("notes")
                        .document(noteID)
                        .update(updateData)
                        .addOnCompleteListener { task ->
                            val currentBinding = _binding
                            if (currentBinding == null || !isAdded){
                                return@addOnCompleteListener
                            }
                            currentBinding.btnUpdateNote.isEnabled= true

                            if (task.isSuccessful){
                                val result = Bundle().apply {
                                    putString("title",newTitle)
                                    putString("description", newDescription)
                                }
                                parentFragmentManager.setFragmentResult(
                                    "note_updated",result
                                )
                                Toast.makeText(requireContext(),"Note Update Successfully", Toast.LENGTH_SHORT).show()
                                parentFragmentManager.popBackStack()
                            }
                            else{
                                Toast.makeText(requireContext(),task.exception?.localizedMessage?:"Update Failed",
                                    Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            }
        }
    }
}