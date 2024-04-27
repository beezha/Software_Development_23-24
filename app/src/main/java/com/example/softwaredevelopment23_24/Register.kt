package com.example.softwaredevelopment23_24

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.softwaredevelopment23_24.databinding.FragmentRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class Register : Fragment() {
    private lateinit var userAuth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentRegisterBinding.inflate(inflater, container, false)
        val view = binding.root
        userAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        binding.txtLogin.setOnClickListener {
            findNavController().navigate(R.id.navigation_login)
        }

        binding.btnRegister.setOnClickListener {
            val email = binding.etRegEmail.text.toString()
            val password = binding.etRegPassword.text.toString()
            val username = binding.etRegUsername.text.toString()

            if (email.isEmpty() || password.isEmpty() || username.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Please fill in all fields",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            userAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        val user = userAuth.currentUser
                        val profileUpdate = UserProfileChangeRequest.Builder()
                            .setDisplayName(username)
                            .build()
                        user?.updateProfile(profileUpdate)
                            ?.addOnCompleteListener { profileUpdateTask ->
                                if (profileUpdateTask.isSuccessful) {
                                    Toast.makeText(
                                        requireContext(),
                                        "Registration successful",
                                        Toast.LENGTH_SHORT
                                    ).show() //HERE LATER ME
                                    MainActivity().generateDatabase(database, user.uid, username, email, requireContext()) {
                                        findNavController().navigate(R.id.navigation_login)
                                    }
                                } else {
                                    Toast.makeText(
                                        requireContext(),
                                        "Failed to add username. ${profileUpdateTask.exception?.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Registration failed. ${task.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
        return view
    }
}