package com.example.softwaredevelopment23_24

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import com.google.firebase.auth.FirebaseAuth

class AvatarDialog : DialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.avatar_chooser_box, container, false)

        val avatarSettingsImage = requireActivity().findViewById<ImageButton>(R.id.avatarsettingsImage)
        val avatarImage = requireActivity().findViewById<ImageView>(R.id.avatarImage)

        val avatar1 = view.findViewById<ImageButton>(R.id.avatar1)
        val avatar2 = view.findViewById<ImageButton>(R.id.avatar2)
        val avatar3 = view.findViewById<ImageButton>(R.id.avatar3)
        val avatar4 = view.findViewById<ImageButton>(R.id.avatar4)
        val avatar5 = view.findViewById<ImageButton>(R.id.avatar5)

        val avatarsaveButton = view.findViewById<Button>(R.id.avatarsaveButton)

        avatarsaveButton.setOnClickListener {
            // Get the background drawable of the selected avatar
            val selectedAvatarDrawable = when {
                avatar1.isSelected -> avatar1.background
                avatar2.isSelected -> avatar2.background
                avatar3.isSelected -> avatar3.background
                avatar4.isSelected -> avatar4.background
                avatar5.isSelected -> avatar5.background
                else -> null
            }

            // Set the background of avatarsettingsImage
            selectedAvatarDrawable?.let {
                avatarSettingsImage.background = it
            }

            // Dismiss the dialog
            dismiss()
        }

        // Set click listeners for avatar1 to avatar5 to toggle their selection
        avatar1.setOnClickListener { toggleAvatarSelection(avatar1) }
        avatar2.setOnClickListener { toggleAvatarSelection(avatar2) }
        avatar3.setOnClickListener { toggleAvatarSelection(avatar3) }
        avatar4.setOnClickListener { toggleAvatarSelection(avatar4) }
        avatar5.setOnClickListener { toggleAvatarSelection(avatar5) }

        val cancelButton = view.findViewById<Button>(R.id.cancelButton)

        cancelButton.setOnClickListener {
            dismiss()
        }
        return view
    }

    private fun toggleAvatarSelection(avatar: ImageButton) {
        // Toggle the selected state of the avatar
        avatar.isSelected = !avatar.isSelected
    }
}