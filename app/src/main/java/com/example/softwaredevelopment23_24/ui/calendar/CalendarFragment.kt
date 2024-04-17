package com.example.softwaredevelopment23_24.ui.calendar

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import androidx.fragment.app.Fragment
import com.example.softwaredevelopment23_24.databinding.FragmentCalendarBinding
import com.example.softwaredevelopment23_24.R
import android.widget.GridView
import android.widget.TextView
import android.widget.Toast
import com.example.softwaredevelopment23_24.MainActivity
import com.example.softwaredevelopment23_24.TaskUpdate
import com.example.softwaredevelopment23_24.task_Confirm
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.*
import kotlin.math.abs

// TODO: reset tasks after a new day (see MainActivity)
// TODO: make buttons gray

class CalendarFragment : Fragment() {

    private lateinit var binding: FragmentCalendarBinding
    private lateinit var reference: DatabaseReference

    private val tasks = listOf(
        listOf("Drink Water (6x)",0,15, 6),
        listOf("Brush teeth (2x)",0,15, 2),
        listOf("Eat a full meal (3x)",0,15, 3),
        listOf("Enjoy nature (30 min)",1,15, 30),
        listOf("Exercise (20 min)",1,15, 20),
        listOf("Meditate (10 min)",1,15, 10),
        listOf("Read a book (10 min)",1,15, 10),
        listOf("Practice a skill (15 min)",1,15, 15),
    )

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

    fun completeTask(task: List<Any>) {
        val coinsEarned = task[2] as Int
       generateTasks { taskList ->
           val taskIndex = tasks.indexOf(task)
           (activity as MainActivity).getCoins(reference, requireContext()) {coins ->
               val totalCoins = coins + coinsEarned
               val newValues = hashMapOf(
                   "coins" to totalCoins,
                   "taskStatus${taskIndex+1}" to true
               )
               if (task[1] == 0) {
                   newValues["task${taskIndex+1}Progress"] = 0
               }
               reference.updateChildren(newValues as Map<String, Any>)
                   .addOnCompleteListener {
                       taskList.remove(task)
                       taskList.add(task)
                       if (it.isSuccessful) {
                           refreshTasks(taskList)
                       }
                       else {
                           Toast.makeText(
                               requireContext(),
                               "Could not complete task. Please try again.",
                               Toast.LENGTH_SHORT
                           ).show()
                       }
                   }
           }
        }

    }


    private fun showTaskCon(taskIndex: Int) {
        generateTasks { taskList ->
            if (taskList[taskIndex][1] == 0) {
                TaskUpdate(this, taskList[taskIndex], tasks).show(childFragmentManager, "TaskUpdate.kt")
            }
            else {
                task_Confirm(this,taskIndex).show(childFragmentManager, "task_Confirm.kt")
            }
        }
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

        val collapsingToolbar = view.findViewById<CollapsingToolbarLayout>(R.id.collapsingToolbar)
        val toolbarText = view.findViewById<TextView>(R.id.toolbarText)
        val startSize = resources.getDimensionPixelSize(R.dimen.start_size)
        val endSize = resources.getDimensionPixelSize(R.dimen.end_size)
        val textSizeAnimator = ValueAnimator.ofInt(startSize, endSize).apply {
            addUpdateListener { valueAnimator ->
                val animatedValue = valueAnimator.animatedValue as Int
                toolbarText.setTextSize(TypedValue.COMPLEX_UNIT_PX, animatedValue.toFloat())
            }
        }

        val appBarLayout = view.findViewById<AppBarLayout>(R.id.appBarLayout)
        appBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            val percentage = abs(verticalOffset).toFloat() / appBarLayout.totalScrollRange
            textSizeAnimator.currentPlayTime = (percentage * textSizeAnimator.duration).toLong()
        })

        val calendarGridView = view.findViewById<GridView>(R.id.calendarGridView)
        val days = getDaysOfMonth()

        val adapter = CalendarAdapter(requireContext(), days)
        calendarGridView.adapter = adapter

        binding.coinButton1.setOnClickListener {
            showTaskCon(0)
        }
        binding.coinButton2.setOnClickListener {
            showTaskCon(1)
        }
        binding.coinButton3.setOnClickListener {
            showTaskCon(2)
        }
        binding.coinButton4.setOnClickListener {
            showTaskCon(3)
        }
        binding.coinButton5.setOnClickListener {
            showTaskCon(4)
        }
        binding.coinButton6.setOnClickListener {
            showTaskCon(5)
        }
        binding.coinButton7.setOnClickListener {
            showTaskCon(6)
        }
        binding.coinButton8.setOnClickListener {
            showTaskCon(7)
        }

        generateTasks { selectedTasks ->
            refreshTasks(selectedTasks)
        }
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
    fun refreshTasks(selectedTasks: MutableList<List<Any>>) {
        binding.apply {
            taskText1.text = selectedTasks[0][0].toString()
            taskcoinCount1.text = "+${selectedTasks[0][2]}"

            taskText2.text = selectedTasks[1][0].toString()
            taskcoinCount2.text = "+${selectedTasks[1][2]}"

            taskText3.text = selectedTasks[2][0].toString()
            taskcoinCount3.text = "+${selectedTasks[2][2]}"

            taskText4.text = selectedTasks[3][0].toString()
            taskcoinCount4.text = "+${selectedTasks[3][2]}"

            taskText5.text = selectedTasks[4][0].toString()
            taskcoinCount5.text = "+${selectedTasks[4][2]}"

            taskText6.text = selectedTasks[5][0].toString()
            taskcoinCount6.text = "+${selectedTasks[5][2]}"

            taskText7.text = selectedTasks[6][0].toString()
            taskcoinCount7.text = "+${selectedTasks[6][2]}"

            taskText8.text = selectedTasks[7][0].toString()
            taskcoinCount8.text = "+${selectedTasks[7][2]}"
            }
        (activity as MainActivity).getTaskPreferences(reference, requireContext()) {_, taskComplete ->
            val buttons = listOf(
                binding.coinButton8,
                binding.coinButton7,
                binding.coinButton6,
                binding.coinButton5,
                binding.coinButton4,
                binding.coinButton3,
                binding.coinButton2,
                binding.coinButton1
            )
            val numberOfCompletedTasks = taskComplete.count { it }
            buttons.take(numberOfCompletedTasks).forEach {it.isEnabled = false}
        }
    }
}

