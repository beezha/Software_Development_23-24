package com.example.softwaredevelopment23_24

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.softwaredevelopment23_24.databinding.FragmentSettingsBinding
import com.google.firebase.auth.FirebaseAuth

class Settings : Fragment() {

    private lateinit var userAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentSettingsBinding.inflate(inflater, container, false)

        userAuth = FirebaseAuth.getInstance()

        val logoutButton = binding.logoutButton
        logoutButton.setOnClickListener {
            LogoutDialog().show(childFragmentManager, "LogoutDialog.kt")
        }

        return binding.root
    }
}