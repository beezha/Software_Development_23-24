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
import java.util.Calendar

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

        getStreakNum {
            binding.txtStreak.text = it.toString()
        }

        (activity as MainActivity).getCoins(reference, requireContext()) {
            binding.petcoinText.text = it.toString()
        }

        return root
    }
    private fun getStreakNum(callback: (Int) -> Unit) {
        (activity as MainActivity).getStreak(reference, requireContext()) {streakData ->
            var counter = 0
            val currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH) - 1
            for (i in currentDay downTo 0) {
                if (streakData[i]) {
                    counter++
                } else {
                    break
                }
            }
            callback(counter)
        }
    }
}