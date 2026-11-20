
package com.priyanshu.securenotes.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.priyanshu.securenotes.R
import com.priyanshu.securenotes.databinding.FragmentSignupBinding

class SignupFragment : Fragment() {

    private var _binding: FragmentSignupBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        binding.btnSignup.setOnClickListener {

            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()
            val confirmPassword = binding.etConfirmPassword.text.toString()

            when {
                name.isEmpty() -> {
                    binding.etName.error = "Enter Your Name"
                }

                email.isEmpty() -> {
                    binding.etEmail.error = "Enter Email"
                }

                password.isEmpty() -> {
                    binding.etPassword.error = "Enter Password"
                }

                password.length < 8 -> {
                    binding.etPassword.error = "Minimum 8 characters required"
                }

                confirmPassword.isEmpty() -> {
                    binding.etConfirmPassword.error = "Confirm Password"
                }

                password != confirmPassword -> {
                    binding.etConfirmPassword.error = "Passwords do not Match"
                }

                else -> {
                    binding.btnSignup.isEnabled = false

                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->

                            if (task.isSuccessful) {

                                val currentUser = auth.currentUser

                                if (currentUser == null) {
                                    _binding?.btnSignup?.isEnabled = true
                                    return@addOnCompleteListener
                                }

                                val profileUpdates =
                                    UserProfileChangeRequest.Builder()
                                        .setDisplayName(name)
                                        .build()

                                currentUser.updateProfile(profileUpdates)
                                    .addOnCompleteListener profileTask@{ result ->

                                        val currentBinding = _binding

                                        if (currentBinding == null || !isAdded) {
                                            return@profileTask
                                        }

                                        currentBinding.btnSignup.isEnabled = true

                                        val message = if (result.isSuccessful) {
                                            "Account Created Successfully"
                                        } else {
                                            "Account created, but profile update failed"
                                        }

                                        Toast.makeText(
                                            requireContext(),
                                            message,
                                            Toast.LENGTH_LONG
                                        ).show()

                                        parentFragmentManager.popBackStack()
                                    }

                            } else {

                                _binding?.btnSignup?.isEnabled = true

                                val errorMessage =
                                    task.exception?.localizedMessage
                                        ?: "Signup Failed"

                                context?.let {
                                    Toast.makeText(
                                        it,
                                        errorMessage,
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }
                }
            }
        }

        binding.tvGoToLogin.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
