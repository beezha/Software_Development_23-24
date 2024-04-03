package com.example.softwaredevelopment23_24.ui.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.softwaredevelopment23_24.databinding.FragmentCalendarBinding
import com.example.softwaredevelopment23_24.R
import android.widget.GridView
import java.util.*

class CalendarFragment : Fragment() {

    private lateinit var binding: FragmentCalendarBinding


    // This property is only valid between onCreateView and
    // onDestroyView.


//    override fun onCreateView(
//            inflater: LayoutInflater,
//            container: ViewGroup?,
//            savedInstanceState: Bundle?
//    ): View {
//        val calendarViewModel =
//                ViewModelProvider(this).get(CalendarViewModel::class.java)
//
//        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
//        val root: View = binding.root
//
////        val textView: TextView = binding.textNotifications
//        calendarViewModel.text.observe(viewLifecycleOwner) {
////            textView.text = it
//        }
//        return root
//
//
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }

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
}

