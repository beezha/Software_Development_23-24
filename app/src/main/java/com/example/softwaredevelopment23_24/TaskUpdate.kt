package com.example.softwaredevelopment23_24

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.softwaredevelopment23_24.ui.calendar.CalendarFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class TaskUpdate(private val calendarFragment: CalendarFragment, private val task: List<Any>, private val taskList: List<List<Any>>): DialogFragment() {
   private lateinit var reference: DatabaseReference
   private lateinit var user: FirebaseUser

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.task_update_dialog, null)
        val taskProgressBar = view.findViewById<ProgressBar>(R.id.prgUpdateStatus)
        taskProgressBar.max = task.last() as Int
        user = FirebaseAuth.getInstance().currentUser!!
        val userID = user.uid
        reference = FirebaseDatabase.getInstance().reference.child("users").child(userID)
        val taskID = taskList.indexOf(task) + 1

        (activity as MainActivity).getTaskProgress(reference, requireContext(),taskID) {taskProgress ->
            taskProgressBar.progress = taskProgress
        }

        view.findViewById<ImageButton>(R.id.btnAddProgress).setOnClickListener {
            taskProgressBar.progress += 1
        }
        view.findViewById<ImageButton>(R.id.btnMinusProgress).setOnClickListener {
            taskProgressBar.progress -= 1
        }
        view.findViewById<Button>(R.id.btnTaskUpCancel).setOnClickListener {
            dismiss()
        }

        view.findViewById<Button>(R.id.btnTaskUpdate).setOnClickListener {

            lateinit var newValues: HashMap<String, Any>
            val progress = taskProgressBar.progress

            when (taskID) {
                1 -> {
                    newValues = hashMapOf( "task1Progress" to progress )
                }
                2 -> {
                    newValues = hashMapOf( "task2Progress" to progress )
                }
                3 -> {
                    newValues = hashMapOf( "task3Progress" to progress )
                }
            }
            reference.updateChildren(newValues)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        if (progress >= taskProgressBar.max) {
                            calendarFragment.completeTask(task)
                        }
                        dismiss()
                    }
                    else {
                        Toast.makeText(
                            requireContext(),
                            "Unable to complete task. Please try again.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }



        builder.setView(view)
        return builder.create()
    }
}