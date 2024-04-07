package com.example.softwaredevelopment23_24

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth

class LogoutDialog : DialogFragment() {
    private lateinit var userAuth: FirebaseAuth
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.logout_dialog_box, null)

        userAuth = FirebaseAuth.getInstance()

        view.findViewById<Button>(R.id.logoutButton).setOnClickListener {
            userAuth.signOut()
            findNavController().navigate(R.id.navigation_login)
        }

        view.findViewById<Button>(R.id.cancelButton).setOnClickListener {
            dismiss()
        }

        builder.setView(view)
        return builder.create()
    }
}