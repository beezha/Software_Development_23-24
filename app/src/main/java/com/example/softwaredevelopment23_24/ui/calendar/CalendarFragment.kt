package com.example.softwaredevelopment23_24.ui.calendar

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.softwaredevelopment23_24.databinding.FragmentCalendarBinding
import com.example.softwaredevelopment23_24.R
import android.widget.GridView
import com.example.softwaredevelopment23_24.MainActivity
import com.example.softwaredevelopment23_24.task_Confirm
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.*

// TODO: dynamically hide and show UI elements (tasks) for when there is only a certain amount completed
// TODO: add in logic for completed tasks giving points and being completed in the database
// TODO: add in task update feature (you drank one cup of water or something like that)

class CalendarFragment : Fragment() {

    private lateinit var binding: FragmentCalendarBinding
    private lateinit var reference: DatabaseReference

    private val tasks = listOf(
        listOf("Drink 6 Cups of Water",0,15, 0),
        listOf("Enjoy nature (30 min)",1,15, 0),
        listOf("Exercise (20 min)",1,15, 0),
        listOf("Brush your teeth (2x)",0,15, 0),
        listOf("Meditate (10 min)",1,15, 0),
        listOf("Read a book (10 min)",1,15, 0),
        listOf("Practice a skill (15 min)",1,15, 0),
        listOf("Eat a full meal (3x)",0,15, 0),
        listOf("Completed Task",1,15, 0)
    )
    private var avaTasks = mutableListOf(0,1,2,3,4,5,6,7,8,8,8,8,8,8,8,8)

    private fun generateTasks(callback: (List<List<Any>>) -> Unit){
        val selectedTasks = mutableListOf<List<Any>>()
        val unSelectedTasks = mutableListOf<List<Any>>()
        (activity as MainActivity).getTaskPreferences(reference, requireContext()) {taskPreferences ->
            for ((counter, preference) in taskPreferences.withIndex()) {
                if (preference as Boolean) {
                    selectedTasks.add(tasks[counter])
                }
                else {
                    unSelectedTasks.add(tasks[counter])
                }
            }
            val combinedList = selectedTasks + unSelectedTasks
            callback(combinedList)
        }
    }

    fun removeTask(taskIndex: Int) {
        avaTasks.removeAt(taskIndex)
    }
    private fun showTaskCon(taskIndex: Int) {
        task_Confirm(this,taskIndex).show(childFragmentManager, "task_Confirm.kt")
    }
    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCalendarBinding.inflate(inflater, container, false)
        val user = FirebaseAuth.getInstance().currentUser!!
        val userID = user.uid
        val view = binding.root
        reference = FirebaseDatabase.getInstance().reference.child("users").child(userID)

        val calendarGridView = view.findViewById<GridView>(R.id.calendarGridView)
        val days = getDaysOfMonth()

        val adapter = CalendarAdapter(requireContext(), days)
        calendarGridView.adapter = adapter

        binding.taskText1.setOnClickListener {
            showTaskCon(0)
        }
        binding.taskText2.setOnClickListener {
            showTaskCon(1)
        }
        binding.taskText3.setOnClickListener {
            showTaskCon(2)
        }
        binding.taskText4.setOnClickListener {
            showTaskCon(3)
        }
        binding.taskText5.setOnClickListener {
            showTaskCon(4)
        }
        binding.taskText6.setOnClickListener {
            showTaskCon(5)
        }
        binding.taskText7.setOnClickListener {
            showTaskCon(6)
        }
        binding.taskText8.setOnClickListener {
            showTaskCon(7)
        }

        refreshTasks()
        return view
    }

    private fun getDaysOfMonth(): List<Date> {
        val calendar = Calendar.getInstance()
        val days = mutableListOf<Date>()

        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val month = calendar.get(Calendar.MONTH)

        while (calendar.get(Calendar.MONTH) == month) {
            days.add(calendar.time)
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        return days
    }

    @SuppressLint("SetTextI18n")
    fun refreshTasks() {
        generateTasks { sortedList ->
            binding.apply {
                taskText1.text = sortedList[0][0].toString()
                taskcoinCount1.text = "+${sortedList[0][2]}"

                taskText2.text = sortedList[1][0].toString()
                taskcoinCount2.text = "+${sortedList[1][2]}"

                taskText3.text = sortedList[2][0].toString()
                taskcoinCount3.text = "+${sortedList[2][2]}"

                taskText4.text = sortedList[3][0].toString()
                taskcoinCount4.text = "+${sortedList[3][2]}"

                taskText5.text = sortedList[4][0].toString()
                taskcoinCount5.text = "+${sortedList[4][2]}"

                taskText6.text = sortedList[5][0].toString()
                taskcoinCount6.text = "+${sortedList[5][2]}"

                taskText7.text = sortedList[6][0].toString()
                taskcoinCount7.text = "+${sortedList[6][2]}"

                taskText8.text = sortedList[7][0].toString()
                taskcoinCount8.text = "+${sortedList[7][2]}"
            }
        }
    }
}

