package com.example.softwaredevelopment23_24.ui.calendar

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.softwaredevelopment23_24.databinding.FragmentCalendarBinding
import com.example.softwaredevelopment23_24.R
import android.widget.GridView
import androidx.appcompat.app.AppCompatActivity
import com.example.softwaredevelopment23_24.task_Confirm
import java.util.*

class CalendarFragment : Fragment() {

    private lateinit var binding: FragmentCalendarBinding

    private val tasks = listOf(
        listOf("Drink 5 Cups of Water",0,15),
        listOf("Spend 30 Minutes Time Outside",1,15),
        listOf("Exercise For 20 Minutes",1,15),
        listOf("Maintain Good Hygiene",0,15),
        listOf("Meditate For 10 Minutes",1,15),
        listOf("Task Spot Filler 1",1,15),
        listOf("Task Spot Filler 2",1,15),
        listOf("Task Spot Filler 3",1,15),
        listOf("Completed Task",1,15)
    )
    private var avaTasks = mutableListOf(0,1,2,3,4,5,6,7,8,8,8,8,8,8,8,8)


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
        val view = binding.root

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
        binding.apply {
            taskText1.text = tasks[avaTasks[0]][0].toString()
            taskcoinCount1.text = "+${tasks[avaTasks[0]][2]}"
            taskText2.text = tasks[avaTasks[1]][0].toString()
            taskcoinCount2.text = "+${tasks[avaTasks[1]][2]}"
            taskText3.text = tasks[avaTasks[2]][0].toString()
            taskcoinCount3.text = "+${tasks[avaTasks[2]][2]}"
            taskText4.text = tasks[avaTasks[3]][0].toString()
            taskcoinCount4.text = "+${tasks[avaTasks[3]][2]}"
            taskText5.text = tasks[avaTasks[4]][0].toString()
            taskcoinCount5.text = "+${tasks[avaTasks[4]][2]}"
            taskText6.text = tasks[avaTasks[5]][0].toString()
            taskcoinCount6.text = "+${tasks[avaTasks[5]][2]}"
            taskText7.text = tasks[avaTasks[6]][0].toString()
            taskcoinCount7.text = "+${tasks[avaTasks[6]][2]}"
            taskText8.text = tasks[avaTasks[7]][0].toString()
            taskcoinCount8.text = "+${tasks[avaTasks[7]][2]}"
        }
    }
}

