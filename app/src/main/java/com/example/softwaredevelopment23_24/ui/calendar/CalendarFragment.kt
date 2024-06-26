package com.example.softwaredevelopment23_24.ui.calendar

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.media.MediaPlayer
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
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.example.softwaredevelopment23_24.MainActivity
import com.example.softwaredevelopment23_24.TaskUpdate
import com.example.softwaredevelopment23_24.task_Confirm
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs


class CalendarFragment : Fragment() {

    private lateinit var binding: FragmentCalendarBinding
    private lateinit var reference: DatabaseReference
    private lateinit var calendarAdapter: CalendarAdapter
    private var highlights: Map<Int, Int> = emptyMap()

    private val tasks = listOf(
        listOf(
            "Drink Water (6x)",
            0,
            15,
            "Be sure to drink at least six cups of water a day to stay healthy!",
            6
        ),
        listOf(
            "Brush Teeth (2x)",
            0,
            15,
            "Brushing your teeth is very important to keeping good hygiene!",
            2
        ),
        listOf(
            "Eat a Full Meal (3x)",
            0,
            15,
            "Nourish your body with healthy well-balanced meals three times a day.",
            3
        ),
        listOf(
            "Explore (30 min)",
            1,
            15,
            "Connecting with nature can reduce stress levels and improve mood.",
            30
        ),
        listOf(
            "Exercise (20 min)",
            1,
            15,
            "Physical activity keeps your body healthy and you mind stress free!",
            20
        ),
        listOf(
            "Meditate (10 min)",
            1,
            15,
            "Meditation helps the mind reduce the effects of anxiety, increase self-awareness, and promotes balance!",
            10
        ),
        listOf(
            "Literature (10 min)",
            1,
            15,
            "Reading stimulates the mind and lets you escape from day to day worries!",
            10
        ),
        listOf(
            "Hone Skills (15 min)",
            1,
            15,
            "Practicing your favorite hobby makes you feel accomplished and helps boost your self-esteem!",
            15
        ),
    )

    private fun generateTasks(callback: (MutableList<List<Any>>) -> Unit) {
        val selectedTasks = mutableListOf<List<Any>>()
        val completedTasks = mutableListOf<List<Any>>()
        (activity as MainActivity).getTaskPreferences(
            reference,
            requireContext()
        ) { taskPreferences, taskCompleteList ->
            for ((counter, preference) in taskPreferences.withIndex()) {
                if (preference != null && preference as? Boolean == true) {
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
        val currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        generateTasks { taskList ->
            val taskIndex = tasks.indexOf(task)
            (activity as MainActivity).getCoins(reference, requireContext()) { coins ->
                val totalCoins = coins + coinsEarned
                val newValues = hashMapOf(
                    "coins" to totalCoins,
                    "taskStatus${taskIndex + 1}" to true,
                    "dayStreak$currentDay" to true
                )
                if (task[1] == 0) {
                    newValues["task${taskIndex + 1}Progress"] = 0
                }
                reference.updateChildren(newValues as Map<String, Any>)
                    .addOnCompleteListener {
                        taskList.remove(task)
                        taskList.add(task)
                        if (it.isSuccessful) {
                            val mediaPlayer = MediaPlayer.create(requireContext(), R.raw.coin_sound)
                            mediaPlayer.start()
                            mediaPlayer.setOnCompletionListener {
                                mediaPlayer.release()
                            }
                            refreshTasks(taskList)
                        } else {
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
                TaskUpdate(this, taskList[taskIndex], tasks).show(
                    childFragmentManager,
                    "TaskUpdate.kt"
                )
            } else {
                task_Confirm(this, taskList[taskIndex]).show(
                    childFragmentManager,
                    "task_Confirm.kt"
                )
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCalendarBinding.inflate(inflater, container, false)
        val user = FirebaseAuth.getInstance().currentUser!!
        val userID = user.uid
        val views = inflater.inflate(R.layout.avatar_chooser_box, container, false)
        val view = binding.root
        reference = FirebaseDatabase.getInstance().reference.child("users").child(userID)

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

            val green = ContextCompat.getColor(requireContext(), R.color.green)
            val calendarToolbar = view.findViewById<androidx.appcompat.widget.Toolbar>(R.id.calendarToolbar)

            if (percentage > 0.65f) {
                calendarToolbar.setBackgroundColor(green) // Change to desired color
            } else {
                calendarToolbar.setBackgroundColor(Color.TRANSPARENT) // Reset to transparent or another color
                appBarLayout.setBackgroundColor(Color.TRANSPARENT) // Reset to transparent or another color
            }
        })

        val calendarGridView = view.findViewById<GridView>(R.id.calendarGridView)
        val days = getDaysOfMonth()

        getStreakDays(highlights) { selectedDays ->
            getHighlightsFromFirebase { highlights ->
                calendarAdapter = CalendarAdapter(requireContext(), days, selectedDays, highlights)
                calendarGridView.adapter = calendarAdapter
            }
        }
        val currentMonth = getMonth()
        val currentYear = getYear()
        binding.monthText.text = currentMonth
        binding.yearText.text = currentYear

        (activity as MainActivity).getAvatar(reference, requireContext(), views) {
            binding.avatarCalendarImage.background = it
        }

        binding.avatarCalendarImage.setOnClickListener{
            findNavController().navigate(R.id.action_Fragment_to_settingsFragment)
        }

        binding.fantasticBtn.setOnClickListener {
            highlightCurrentDay(R.drawable.fantastic_highlight)
        }
        binding.neutralBtn.setOnClickListener {
            highlightCurrentDay(R.drawable.neutral_highlight)
        }
        binding.terribleBtn.setOnClickListener {
            highlightCurrentDay(R.drawable.terrible_highlight)
        }

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

    private fun saveHighlightToFirebase(day: Int, drawable: Int) {
        val user = FirebaseAuth.getInstance().currentUser!!
        val userID = user.uid
        val reference = FirebaseDatabase.getInstance().reference.child("users").child(userID)
        val highlightsRef = reference.child("highlights")

        highlightsRef.child(day.toString()).setValue(drawable)
    }

    private fun highlightCurrentDay(drawable: Int) {
        val currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        calendarAdapter.setHighlightedDay(currentDay, drawable)
        saveHighlightToFirebase(currentDay, drawable)
    }

    private fun getHighlightsFromFirebase(callback: (MutableMap<Int, Int>) -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        val userID = user!!.uid
        val reference = FirebaseDatabase.getInstance().reference.child("users").child(userID)
        val highlightsRef = reference.child("highlights")

        highlightsRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val result = task.result
                val highlights = mutableMapOf<Int, Int>()
                result?.children?.forEach { snapshot ->
                    val day = snapshot.key?.toIntOrNull()
                    val drawable = snapshot.getValue(Int::class.java)
                    if (day != null && drawable != null) {
                        highlights[day] = drawable
                    }
                }
                callback(highlights)
            } else {
                callback(mutableMapOf()) // Return an empty mutable map in case of failure
            }
        }
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
            if (selectedTasks.isNotEmpty()) {
                taskText1.text = selectedTasks[0][0].toString()
                taskcoinCount1.text = "+${selectedTasks[0][2]}"
                taskDescription1.text = selectedTasks[0][3].toString()
            } else {
                taskText1.text = "No tasks available"
                taskText2.text = "No tasks available"
                taskText3.text = "No tasks available"
                taskText4.text = "No tasks available"
                taskText5.text = "No tasks available"
                // Set other task text views to "No tasks available" as needed
                return@apply
            }

            if (selectedTasks.size > 1) {
                taskText2.text = selectedTasks[1][0].toString()
                taskcoinCount2.text = "+${selectedTasks[1][2]}"
                taskDescription2.text = selectedTasks[1][3].toString()
            } else {
                taskText2.text = "No tasks available"
                taskText3.text = "No tasks available"
                taskText4.text = "No tasks available"
                taskText5.text = "No tasks available"
                // Set other task text views to "No tasks available" as needed
                return@apply
            }

            if (selectedTasks.size > 2) {
                taskText3.text = selectedTasks[2][0].toString()
                taskcoinCount3.text = "+${selectedTasks[2][2]}"
                taskDescription3.text = selectedTasks[2][3].toString()
            } else {
                taskText3.text = "No tasks available"
                taskText4.text = "No tasks available"
                taskText5.text = "No tasks available"
                // Set other task text views to "No tasks available" as needed
                return@apply
            }

            if (selectedTasks.size > 3) {
                taskText4.text = selectedTasks[3][0].toString()
                taskcoinCount4.text = "+${selectedTasks[3][2]}"
                taskDescription4.text = selectedTasks[3][3].toString()
            } else {
                taskText4.text = "No tasks available"
                taskText5.text = "No tasks available"
                // Set other task text views to "No tasks available" as needed
                return@apply
            }

            if (selectedTasks.size > 4) {
                taskText5.text = selectedTasks[4][0].toString()
                taskcoinCount5.text = "+${selectedTasks[4][2]}"
                taskDescription5.text = selectedTasks[4][3].toString()
            } else {
                taskText5.text = "No tasks available"
                // Set other task text views to "No tasks available" as needed
                return@apply
            }


//            taskText6.text = selectedTasks[5][0].toString()
//            taskcoinCount6.text = "+${selectedTasks[5][2]}"
//            taskDescription6.text = selectedTasks[5][3].toString()

//            taskText7.text = selectedTasks[6][0].toString()
//            taskcoinCount7.text = "+${selectedTasks[6][2]}"
//            taskDescription7.text = selectedTasks[6][3].toString()
//
//            taskText8.text = selectedTasks[7][0].toString()
//            taskcoinCount8.text = "+${selectedTasks[7][2]}"
//            taskDescription8.text = selectedTasks[7][3].toString()

            (activity as MainActivity).getTaskPreferences(
                reference,
                requireContext()
            ) { _, taskComplete ->
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
                buttons.take(numberOfCompletedTasks).forEach { it.isEnabled = false }
                buttons.take(numberOfCompletedTasks).forEach {
                    it.backgroundTintList = ColorStateList.valueOf(Color.DKGRAY)
                }
            }
            (activity as MainActivity).getCoins(reference, requireContext()) {
                binding.petcoinText.text = it.toString()

            taskText6.text = selectedTasks[5][0].toString()
            taskcoinCount6.text = "+${selectedTasks[5][2]}"
            taskDescription6.text = selectedTasks[5][3].toString()

            taskText7.text = selectedTasks[6][0].toString()
            taskcoinCount7.text = "+${selectedTasks[6][2]}"
            taskDescription7.text = selectedTasks[6][3].toString()

            taskText8.text = selectedTasks[7][0].toString()
            taskcoinCount8.text = "+${selectedTasks[7][2]}"
            taskDescription8.text = selectedTasks[7][3].toString()

            }
        }
    }

        private fun getMonth(): String {
            val calendar = Calendar.getInstance()
            val monthFormat = SimpleDateFormat("MMMM", Locale.getDefault())
            return monthFormat.format(calendar.time)
        }

        private fun getYear(): String {
            val calendar = Calendar.getInstance()
            val yearFormat = SimpleDateFormat("yyyy", Locale.getDefault())
            return yearFormat.format(calendar.time)
        }

    private fun getStreakDays(highlights: Map<Int, Int>, callback: (MutableList<Int>) -> Unit) {
        (activity as MainActivity).getStreak(
            reference,
            requireContext()
        ) { streakData ->
            val days: MutableList<Int> = mutableListOf()
            for (i in streakData.indices) {
                if (streakData[i]) {
                    days.add(i + 1)
                }
            }
            callback(days)
        }
    }


    }

