package com.example.softwaredevelopment23_24

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.softwaredevelopment23_24.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var reference: DatabaseReference
    private lateinit var user: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        navView.setupWithNavController((navController))

        user = FirebaseAuth.getInstance()

        if (user.currentUser != null) {
            navController.navigate(R.id.navigation_home)
            val userID = user.currentUser!!.uid
            reference = FirebaseDatabase.getInstance().reference.child("users").child(userID)
        } else {
            navController.navigate(R.id.navigation_login)
        }

    }

    fun generateDatabase(userID: String, username: String, email:String, context: Context) {
        val database: DatabaseReference = FirebaseDatabase.getInstance().reference
        val userData = hashMapOf(
            "username" to username,
            "email" to email,
            "task1" to true,
            "task2" to true,
            "task3" to true,
            "task4" to true,
            "task5" to true,
            "task6" to false,
            "task7" to false,
            "task8" to false,
            "petHunger" to 50,
            "petThirst" to 50,
            "petEnjoyment" to 50,
            "coins" to 20
        )
        database.child("users").child(userID).setValue(userData)
            .addOnFailureListener {
                Toast.makeText(
                    context,
                    "Failed to add user to database. ${it.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
    fun generateStats(
        reference: DatabaseReference, 
        context: Context,
        callback: (petHunger: String, petThirst: String, petEnjoyment: String, userCoins: String) -> Unit) {
        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val petHunger = snapshot.child("petHunger").value.toString()
                val petThirst = snapshot.child("petThirst").value.toString()
                val petEnjoyment = snapshot.child("petEnjoyment").value.toString()
                val coins = snapshot.child("coins").value.toString()
                callback(petHunger, petThirst, petEnjoyment, coins)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    context,
                    "Could not read data from database: ${error.toException()}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
        return
    }
}