package com.example.softwaredevelopment23_24

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.DialogFragment
import com.example.softwaredevelopment23_24.databinding.FragmentCalendarBinding
import com.example.softwaredevelopment23_24.ui.calendar.CalendarFragment

class task_Confirm(private val calendarFragment: CalendarFragment, private val task: List<Any>) : DialogFragment() {

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.task_popup, null)

        view.findViewById<Button>(R.id.TaskButton).setOnClickListener {
            Timer_code(calendarFragment, task).show(childFragmentManager, "Timer_code.kt")
        }
        view.findViewById<Button>(R.id.cancelButton).setOnClickListener {
            dismiss()
        }

        builder.setView(view)
        return builder.create()
    }

}