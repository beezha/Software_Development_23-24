package com.example.softwaredevelopment23_24.ui.calendar
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.softwaredevelopment23_24.R
import java.util.*
import android.view.LayoutInflater

class CalendarAdapter(private val mContext: Context, private val days: List<Date>) : BaseAdapter() {

    override fun getCount(): Int {
        return days.size
    }

    override fun getItem(position: Int): Any {
        return days[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var convertView = convertView
        val dayOfMonth = days[position].date.toString()

        if (convertView == null) {
            val inflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = inflater.inflate(R.layout.item_calendar, parent, false)
        }

        val textView = convertView!!.findViewById<TextView>(R.id.calendarDayTextView)
        textView.text = dayOfMonth

        return convertView
    }
}