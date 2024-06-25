package com.example.softwaredevelopment23_24

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.softwaredevelopment23_24.databinding.FragmentSettingsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class Settings : Fragment() {
    private lateinit var binding: FragmentSettingsBinding
    private lateinit var userAuth: FirebaseAuth
    private lateinit var user: FirebaseUser
    private lateinit var reference: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.avatar_chooser_box, container, false)
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        userAuth = FirebaseAuth.getInstance()
        user = userAuth.currentUser!!
        val userID = user.uid

        reference = FirebaseDatabase.getInstance().reference.child("users").child(userID)

        (activity as MainActivity).getAvatar(reference, requireContext(), view) {
            binding.avatarsettingsImage.background = it
        }

        binding.logoutButton.setOnClickListener {
            LogoutDialog().show(childFragmentManager, "LogoutDialog.kt")
        }
        binding.saveButton.setOnClickListener {
            checkUpdates()
        }
        binding.avatarsettingsImage.setOnClickListener {
            AvatarDialog().show(childFragmentManager, "AvatarBox.kt")
        }
        return binding.root
    }

    private fun checkUpdates() {
        val updates = mutableMapOf<String, Any>()
        val username = binding.etUsername.text.toString()
        val email = binding.etEmailChange.text.toString()
        val password = binding.etNewPass.text.toString()
        if (username.isNotEmpty()) {
            updates["username"] = username
        }
        if (email.isNotEmpty()) {
            updates["email"] = email
        }
       if (password.isNotEmpty()) {
            if (checkPassword(password)) {
                updates["password"] = password
            }
            else {
                Toast.makeText(
                    requireContext(),
                    "Could not update password",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        updateUser(updates)
        clearUI()
    }

    private fun clearUI() {
        binding.etUsername.text.clear()
        binding.etEmailChange.text.clear()
        binding.etOldPass.text.clear()
        binding.etNewPass.text.clear()
        binding.etPassConf.text.clear()
    }

    private fun updateUser(updates: Map<String, Any>) {
        val user = FirebaseAuth.getInstance().currentUser

        val profileUpdates = UserProfileChangeRequest.Builder()

        val email = updates["email"] as? String
        val password = updates["password"] as? String

        email?.let {
            user?.verifyBeforeUpdateEmail(it)
        }

        password?.let {
            user?.updatePassword(it)
        }

        updates["username"]?.let {
            profileUpdates.setDisplayName(it as String)
        }

        user?.updateProfile(profileUpdates.build())
            ?.addOnCompleteListener {
                if (it.isSuccessful) {
                    updateDatabase(updates)
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Could not update user. Please try again",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun updateDatabase(updates: Map<String, Any>) {
        val newValues = updates.filter { it.key != "password" }
        reference.updateChildren(newValues)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(
                        requireContext(),
                        "Update successful",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else {
                    Toast.makeText(
                        requireContext(),
                        "Could not update database",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
    private fun checkPassword(newPassword: String): Boolean {
        val confirmPassword = binding.etPassConf.text.toString()
        return newPassword == confirmPassword && newPassword.length >= 8
    }
}