package com.example.softwaredevelopment23_24

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.softwaredevelopment23_24.ui.calendar.CalendarFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlin.time.times

class Timer_code(private val calendarFragment: CalendarFragment, private val task: List<Any>) : DialogFragment() {
    lateinit var countDownTimer: CountDownTimer
    @SuppressLint("MissingInflatedId")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.timer_popup, null)

        val minius = task[3] as Int

        val timer = view.findViewById<TextView>(R.id.timerView)
        val bottomNavigationView = requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)
        bottomNavigationView.visibility = View.GONE

        val initialTimeMillis: Long = minius * 60L * 1000L // 15 minutes in milliseconds

        countDownTimer = object : CountDownTimer(initialTimeMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                // Update the UI with the remaining time
                val minutes = millisUntilFinished / (1000 * 60)
                val seconds = (millisUntilFinished / 1000) % 60
                val timeLeftFormatted = String.format("%02d:%02d", minutes, seconds)
                timer.text = timeLeftFormatted
            }

            @SuppressLint("SetTextI18n")
            override fun onFinish() {
                timer.text = "00:00"
                //continue here funture me
            }
        }



        countDownTimer.start()

        view.findViewById<Button>(R.id.cancelButtonTimer).setOnClickListener {
            bottomNavigationView.visibility = View.VISIBLE
            dismiss()
        }


        builder.setView(view)
        return builder.create()
    }

}