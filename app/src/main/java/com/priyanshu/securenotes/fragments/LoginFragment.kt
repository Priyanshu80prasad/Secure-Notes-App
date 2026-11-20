
package com.priyanshu.securenotes.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.priyanshu.securenotes.MainActivity
import com.priyanshu.securenotes.R
import com.priyanshu.securenotes.databinding.FragmentLoginBinding
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        // Email Password Login
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()

            if (email.isEmpty()) {
                binding.etEmail.error = "Enter Email"
            } else if (password.isEmpty()) {
                binding.etPassword.error = "Enter Password"
            } else {
                binding.btnLogin.isEnabled = false

                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->

                        val currentBinding = _binding
                        if (currentBinding == null || !isAdded) {
                            return@addOnCompleteListener
                        }

                        currentBinding.btnLogin.isEnabled = true

                        if (task.isSuccessful) {
                            Toast.makeText(
                                requireContext(),
                                "Login Successful",
                                Toast.LENGTH_SHORT
                            ).show()

                            openMainActivity()

                        } else {
                            Toast.makeText(
                                requireContext(),
                                task.exception?.localizedMessage ?: "Login Failed",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }
        }

        // Google Login
        binding.btnGoogleLogin.setOnClickListener {
            googleSignIn()
        }

        // Signup Navigation
        binding.tvGoToSignup.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.authContainer, SignupFragment())
                .addToBackStack(null)
                .commit()
        }

        // Forgot Password Navigation
        binding.tvForgotPassword.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.authContainer, ForgotPasswordFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun googleSignIn() {

        val context = requireContext()
        binding.btnGoogleLogin.isEnabled = false

        val credentialManager = CredentialManager.create(context)

        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(getString(R.string.default_web_client_id))
            .setFilterByAuthorizedAccounts(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        viewLifecycleOwner.lifecycleScope.launch {

            try {
                val result = credentialManager.getCredential(
                    context = context,
                    request = request
                )

                val credential = result.credential

                if (
                    credential is CustomCredential &&
                    credential.type ==
                    GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {

                    val googleIdToken =
                        GoogleIdTokenCredential.createFrom(credential.data)

                    firebaseAuthWithGoogle(googleIdToken.idToken)

                } else {
                    binding.btnGoogleLogin.isEnabled = true

                    Toast.makeText(
                        context,
                        "Invalid Google Credential",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: GetCredentialException) {

                if (_binding != null) {
                    binding.btnGoogleLogin.isEnabled = true

                    Toast.makeText(
                        context,
                        e.localizedMessage ?: "Google Sign-In Failed",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {

        val credential = GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->

                val currentBinding = _binding
                if (currentBinding == null || !isAdded) {
                    return@addOnCompleteListener
                }

                currentBinding.btnGoogleLogin.isEnabled = true

                if (task.isSuccessful) {

                    Toast.makeText(
                        requireContext(),
                        "Google Login Successful",
                        Toast.LENGTH_SHORT
                    ).show()

                    openMainActivity()

                } else {

                    Toast.makeText(
                        requireContext(),
                        task.exception?.localizedMessage ?: "Google Login Failed",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun openMainActivity() {
        val intent = Intent(requireContext(), MainActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
