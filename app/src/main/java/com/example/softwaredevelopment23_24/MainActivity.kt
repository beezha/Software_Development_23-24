package com.example.softwaredevelopment23_24

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.softwaredevelopment23_24.databinding.ActivityMainBinding
import com.google.firebase.FirebaseApp

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        navView.setupWithNavController((navController))

        val isAuthenticated = FirebaseAuth.getInstance().currentUser != null

        if (isAuthenticated) {
            navController.navigate(R.id.navigation_home)
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
            "task8" to false
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
}