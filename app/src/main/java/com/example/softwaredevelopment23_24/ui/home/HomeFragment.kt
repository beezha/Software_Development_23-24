package com.example.softwaredevelopment23_24.ui.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.softwaredevelopment23_24.MainActivity
import com.example.softwaredevelopment23_24.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class HomeFragment : Fragment() {
    private lateinit var reference: DatabaseReference

    private lateinit var binding: FragmentHomeBinding

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val user = FirebaseAuth.getInstance()
        val username = user.currentUser!!.displayName?.uppercase()
        binding.usernameText.text = username
        val userID = user.currentUser!!.uid
        reference = FirebaseDatabase.getInstance().reference.child("users").child(userID)

        (activity as MainActivity).generateStats(
            reference,
            requireContext()
        ) { petHunger, petThirst, petEnjoyment, _ ->
            binding.apply {
                hungerwidgetProgress.progress = petHunger
                thirstwidgetProgress.progress = petThirst
                funwidgetProgress.progress = petEnjoyment
            }
        }
        (activity as MainActivity).getPetName(reference) { petName ->
            binding.petnamewidgetText.text = petName
        }
        (activity as MainActivity).getTaskPreferences(
            reference,
            requireContext()
        ) { _, taskCompleted ->
            val completedTasks = taskCompleted.count { it }
            binding.progressText.text = "${completedTasks}/8"
            binding.taskprogressBar.progress = completedTasks
        }

        return root
    }
}