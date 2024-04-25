package com.example.softwaredevelopment23_24.ui.home

import android.util.Log
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation.findNavController
import com.example.softwaredevelopment23_24.MainActivity
import com.example.softwaredevelopment23_24.R
import com.example.softwaredevelopment23_24.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.Calendar

class HomeFragment : Fragment() {
    private lateinit var reference: DatabaseReference
    private lateinit var binding: FragmentHomeBinding
    private val tasks = listOf(
        listOf("Drink Water (6x)",0,15,"Be sure to drink at least six cups of water a day to stay healthy!", 6),
        listOf("Brush teeth (2x)",0,15,"Brushing your teeth is very important to keeping good hygiene!", 2),
        listOf("Eat a full meal (3x)",0,15,"Nourish your body with healthy well-balanced meals three times a day.", 3),
        listOf("Enjoy nature (30 min)",1,15,"Connecting with the outdoors can reduce stress levels and improve mood.", 30),
        listOf("Exercise (20 min)",1,15,"Physical activity keeps your body healthy and you mind stress free!", 20),
        listOf("Meditate (10 min)",1,15,"Meditation helps the mind reduce the effects of anxiety, increase self-awareness, and promotes emotional balance!", 10),
        listOf("Read a book (10 min)",1,15,"Reading stimulates the mind and lets you escape from day to day worries!", 10),
        listOf("Practice a skill (15 min)",1,15,"Practicing your favorite hobby makes you feel accomplished and helps boost your self-esteem!", 15),
    )

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        try {
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
        } catch (e: Exception) {
            Log.d("HomeFragment.kt", e.toString())
        }

        getStreakNum {
            binding.txtStreak.text = it.toString()
        }

        (activity as MainActivity).getCoins(reference, requireContext()) {
            binding.petcoinText.text = it.toString()
        }

        generateTasks {
            binding.hometaskText1.text = it[0][0].toString()
            binding.hometaskText2.text = it[1][0].toString()
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
    private fun generateTasks(callback: (MutableList<List<Any>>) -> Unit) {
        val selectedTasks = mutableListOf<List<Any>>()
        val completedTasks = mutableListOf<List<Any>>()
        (activity as MainActivity).getTaskPreferences(reference, requireContext()) {taskPreferences, taskCompleteList ->
            for ((counter, preference) in taskPreferences.withIndex()) {
                if (preference as Boolean) {
                    if (taskCompleteList[counter]) {
                        completedTasks.add(tasks[counter])
                    } else {
                        selectedTasks.add(tasks[counter])
                    }
                }
            }
            val taskList = selectedTasks + completedTasks
            callback(taskList.toMutableList())
        }
    }
}